package net.andrew_coursin.magical_staffs.event;

import net.andrew_coursin.magical_staffs.MagicalStaffs;
import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.effect.AttackMobEffectInstance;
import net.andrew_coursin.magical_staffs.inventory.StaffItemListener;
import net.andrew_coursin.magical_staffs.inventory.TimedEnchantmentsListener;
import net.andrew_coursin.magical_staffs.level.TimedEnchantmentSavedData;
import net.andrew_coursin.magical_staffs.networking.ModPacketHandler;
import net.andrew_coursin.magical_staffs.networking.packet.AddTimedEnchantmentsTooltipsC2SPacket;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = MagicalStaffs.MOD_ID)
@ParametersAreNonnullByDefault
public class ModEvents {
    public static final Set<ItemStack> TIMED_ITEM_STACKS = new HashSet<>();

    public static void addIfTimedItemStack(ItemStack itemStack) {
        itemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY).forEach((id, timedEnchantment) -> {
            if (!TimedEnchantmentSavedData.has(id)) removeTimedEnchantment(itemStack, id, timedEnchantment);
        });
        if (!itemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY).isEmpty()) TIMED_ITEM_STACKS.add(itemStack);
    }

    public static boolean removeTimedEnchantment(ItemStack itemStack, int id, TimedEnchantment timedEnchantment) {
        TimedEnchantments timedEnchantments = itemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);
        if (!timedEnchantments.has(id)) return false;

        // Remove enchantment
        int newLevel = EnchantmentHelper.getItemEnchantmentLevel(timedEnchantment.getEnchantment(), itemStack) - timedEnchantment.getLevel();
        EnchantmentHelper.updateEnchantments(itemStack, itemEnchantments -> itemEnchantments.set(timedEnchantment.getEnchantment(), newLevel));

        // Remove timed enchantment
        TimedEnchantments newTimedEnchantments = timedEnchantments.remove(id);
        if (newTimedEnchantments.isEmpty()) itemStack.remove(ModComponents.TIMED_ENCHANTMENTS.get());
        else itemStack.set(ModComponents.TIMED_ENCHANTMENTS.get(), newTimedEnchantments);
        return newTimedEnchantments.isEmpty();
    }

    // Client Side Event
    @SubscribeEvent
    public static void addTimedEnchantmentsTooltips(final ItemTooltipEvent event) {
        TimedEnchantments timedEnchantments = event.getItemStack().get(ModComponents.TIMED_ENCHANTMENTS.get());
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
            serverLevel.getDataStorage().computeIfAbsent(TimedEnchantmentSavedData.factory(serverLevel), TimedEnchantmentSavedData.FILE_NAME);
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(final PlayerContainerEvent.Open event) {
        event.getContainer().addSlotListener(new TimedEnchantmentsListener());
        event.getContainer().getItems().forEach(ModEvents::addIfTimedItemStack);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().inventoryMenu.addSlotListener(new StaffItemListener());
        event.getEntity().inventoryMenu.addSlotListener(new TimedEnchantmentsListener());
        event.getEntity().inventoryMenu.getItems().forEach(ModEvents::addIfTimedItemStack);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        TimedEnchantmentSavedData.tick().forEach((id, timedEnchantment) -> TIMED_ITEM_STACKS.removeIf(itemStack -> removeTimedEnchantment(itemStack, id, timedEnchantment)));
    }
}
