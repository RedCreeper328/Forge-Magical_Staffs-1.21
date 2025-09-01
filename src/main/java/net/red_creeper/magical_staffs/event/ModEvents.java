package net.red_creeper.magical_staffs.event;

import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.red_creeper.magical_staffs.effect.AttackMobEffectInstance;
import net.red_creeper.magical_staffs.inventory.StaffItemListener;
import net.red_creeper.magical_staffs.inventory.TimerListener;
import net.red_creeper.magical_staffs.item.custom.StaffItem;
import net.red_creeper.magical_staffs.level.TimerSavedData;
import net.red_creeper.magical_staffs.networking.ModPacketHandler;
import net.red_creeper.magical_staffs.networking.packet.AddTimedEnchantmentsTooltipsC2SPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
@ParametersAreNonnullByDefault
public class ModEvents {
    public static final Map<AbstractContainerMenu, Set<Integer>> TIMED_ITEM_STACKS = new HashMap<>();
    public static final Map<AbstractContainerMenu, Set<Integer>> TIMED_STAFFS = new HashMap<>();

    public static boolean removeStaffTimer(int id, ItemStack itemStack) {
        Integer staffTimer = itemStack.get(ModDataComponents.STAFF_TIMER.get());
        if (staffTimer != null && staffTimer == id) itemStack.remove(ModDataComponents.STAFF_TIMER.get());
        return !itemStack.has(ModDataComponents.STAFF_TIMER.get());
    }

    public static boolean removeTimedEnchantment(int id, ItemStack itemStack, TimedEnchantment timedEnchantment) {
        TimedEnchantments timedEnchantments = itemStack.getOrDefault(ModDataComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);
        if (!timedEnchantments.has(id)) return false;

        // Only reduces the enchantment level once overflow has been reduced to zero
        int reduction = timedEnchantments.getOverflow(timedEnchantment.getEnchantment()) - timedEnchantment.getLevel();
        if (reduction < 0) {
            // Enchantment level cannot be below zero, this check is necessary if an enchantment was absorbed from an item with timed enchantments
            int newLevel = Math.max(EnchantmentHelper.getItemEnchantmentLevel(timedEnchantment.getEnchantment(), itemStack) + reduction, 0);
            EnchantmentHelper.updateEnchantments(itemStack, itemEnchantments -> itemEnchantments.set(timedEnchantment.getEnchantment(), newLevel));
        }

        // Remove timed enchantment
        TimedEnchantments newTimedEnchantments = timedEnchantments.remove(id);
        if (newTimedEnchantments.isEmpty()) itemStack.remove(ModDataComponents.TIMED_ENCHANTMENTS.get());
        else itemStack.set(ModDataComponents.TIMED_ENCHANTMENTS.get(), newTimedEnchantments);
        return !itemStack.has(ModDataComponents.TIMED_ENCHANTMENTS.get());
    }

    public static void addIfTimedItemStack(AbstractContainerMenu containerMenu, int index) {
        ItemStack itemStack = containerMenu.getItems().get(index);
        itemStack.getOrDefault(ModDataComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY).forEach((id, timedEnchantment) -> {
            if (!TimerSavedData.hasTimedEnchantmentId(id)) removeTimedEnchantment(id, itemStack, timedEnchantment);
        });
        if (itemStack.has(ModDataComponents.TIMED_ENCHANTMENTS.get())) {
            Set<Integer> indices = TIMED_ITEM_STACKS.getOrDefault(containerMenu, new HashSet<>());
            indices.add(index);
            TIMED_ITEM_STACKS.put(containerMenu, indices);
        }
    }

    public static void addIfTimedStaff(AbstractContainerMenu containerMenu, int index) {
        ItemStack itemStack = containerMenu.getItems().get(index);
        Integer id = itemStack.get(ModDataComponents.STAFF_TIMER.get());
        if (id != null && !TimerSavedData.hasStaffTimerId(id)) removeStaffTimer(id, itemStack);
        else if (itemStack.has(ModDataComponents.STAFF_TIMER.get())) {
            Set<Integer> indices = TIMED_STAFFS.getOrDefault(containerMenu, new HashSet<>());
            indices.add(index);
            TIMED_STAFFS.put(containerMenu, indices);
        }
    }

    // Client Side Event
    @SubscribeEvent
    public static void addStaffTooltips(final ItemTooltipEvent event) {
        Player player = event.getEntity();

        if (player == null || !(event.getItemStack().getItem() instanceof StaffItem staffItem)) return;

        staffItem.appendHoverText(event.getItemStack(), Item.TooltipContext.of(player.level()), event.getToolTip(), event.getFlags());
    }

    // Client Side Event
    @SubscribeEvent
    public static void addTimedEnchantmentsTooltips(final ItemTooltipEvent event) {
        TimedEnchantments timedEnchantments = event.getItemStack().get(ModDataComponents.TIMED_ENCHANTMENTS.get());
        Player player = event.getEntity();

        if (timedEnchantments == null || player == null) return;

        // On the server side player.containerMenu is set to inventoryMenu instead of CreativeModeInventoryScreen.ItemPickerMenu
        AbstractContainerMenu containerMenu = player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu ? player.inventoryMenu : player.containerMenu;
        ModPacketHandler.sendToServer(new AddTimedEnchantmentsTooltipsC2SPacket(containerMenu.getItems().indexOf(event.getItemStack())));

        Item.TooltipContext tooltipContext = Item.TooltipContext.of(event.getEntity().level());

        timedEnchantments.forEach((id, timedEnchantment) -> {
            Holder<Enchantment> enchantment = timedEnchantment.getEnchantment();
            int index = event.getToolTip().lastIndexOf(Enchantment.getFullname(enchantment, EnchantmentHelper.getItemEnchantmentLevel(enchantment, event.getItemStack())));
            event.getToolTip().add(index + 1, (((MutableComponent) Enchantment.getFullname(enchantment, timedEnchantment.getLevel())).append(Component.translatable("tooltip.magical_staffs.duration", StringUtil.formatTickDuration(timedEnchantment.getDuration(), tooltipContext.tickRate()))).withStyle(ChatFormatting.DARK_PURPLE)));
        });
    }

    @SubscribeEvent
    public static void applyAttackEffects(final LivingAttackEvent event) {
        if (event.getSource().getEntity() == null || !(event.getSource().getEntity() instanceof LivingEntity livingEntity)) return;

        livingEntity.getActiveEffects().forEach(mobEffectInstance -> {
            if (mobEffectInstance instanceof AttackMobEffectInstance attackMobEffectInstance) {
                event.getEntity().addEffect(attackMobEffectInstance.getAppliedMobEffectInstance());
            }
        });
    }

    @SubscribeEvent
    public static void onAttachSavedData(final LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == ServerLevel.OVERWORLD) {
            TimerSavedData timerSavedData = serverLevel.getDataStorage().computeIfAbsent(TimerSavedData.TYPE);
            timerSavedData.setServerLevel(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().inventoryMenu.addSlotListener(new StaffItemListener());
        event.getEntity().inventoryMenu.addSlotListener(new TimerListener());
        event.getEntity().inventoryMenu.getItems().forEach(itemStack -> {
            ModEvents.addIfTimedItemStack(event.getEntity().inventoryMenu, event.getEntity().inventoryMenu.getItems().indexOf(itemStack));
            ModEvents.addIfTimedStaff(event.getEntity().inventoryMenu, event.getEntity().inventoryMenu.getItems().indexOf(itemStack));
        });
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(final PlayerContainerEvent.Open event) {
        event.getContainer().addSlotListener(new TimerListener());
        event.getContainer().getItems().forEach(itemStack -> {
            ModEvents.addIfTimedItemStack(event.getContainer(), event.getContainer().getItems().indexOf(itemStack));
            ModEvents.addIfTimedStaff(event.getContainer(), event.getContainer().getItems().indexOf(itemStack));
        });
    }

    @SubscribeEvent
    public static void tick(final TickEvent.ServerTickEvent.Post event) {
        if (event.getServer().tickRateManager().isFrozen() && event.getServer().tickRateManager().frozenTicksToRun() <= 0) return;
        TimerSavedData.tick();
    }
}
