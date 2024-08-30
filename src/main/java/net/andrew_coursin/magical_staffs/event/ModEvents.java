package net.andrew_coursin.magical_staffs.event;

import net.andrew_coursin.magical_staffs.MagicalStaffs;
import net.andrew_coursin.magical_staffs.capability.attack_effects.AttackEffectsCapabilityProvider;
import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.effect.AttackMobEffect;
import net.andrew_coursin.magical_staffs.inventory.StaffItemListener;
import net.andrew_coursin.magical_staffs.inventory.TimedEnchantmentsListener;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MagicalStaffs.MOD_ID)
@ParametersAreNonnullByDefault
public class ModEvents {
    public static final List<ItemStack> TIMED_ITEM_STACKS = new ArrayList<>();

    @SubscribeEvent
    public static void addAttackEffect(final MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect().get() instanceof AttackMobEffect attackMobEffect) {
            event.getEntity().getCapability(AttackEffectsCapabilityProvider.ATTACK_EFFECTS).ifPresent(
                attackEffects -> {
                    int duration = event.getEffectInstance().isInfiniteDuration() ? -1 : event.getEffectInstance().getDuration() / 20;
                    attackEffects.addEffect(new MobEffectInstance(attackMobEffect.getAppliedEffect(), duration, event.getEffectInstance().getAmplifier()));
                }
            );
        }
    }

    // Client Side Event
    @SubscribeEvent
    public static void addTimedEnchantmentsTooltips(final ItemTooltipEvent event) {
        TimedEnchantments timedEnchantments = event.getItemStack().get(ModComponents.TIMED_ENCHANTMENTS.get());

        if (timedEnchantments == null || event.getEntity() == null) return;

        Item.TooltipContext tooltipContext = Item.TooltipContext.of(event.getEntity().level());

        timedEnchantments.forEach(timedEnchantment -> {
            Holder<Enchantment> enchantment = timedEnchantment.getEnchantment();
            int index = event.getToolTip().lastIndexOf(Enchantment.getFullname(enchantment, EnchantmentHelper.getItemEnchantmentLevel(enchantment, event.getItemStack())));
            event.getToolTip().add(index + 1, (((MutableComponent) Enchantment.getFullname(enchantment, timedEnchantment.getLevel())).append(Component.translatable("tooltip.magical_staffs.duration", StringUtil.formatTickDuration(timedEnchantment.getDuration(), tooltipContext.tickRate()))).withStyle(ChatFormatting.DARK_PURPLE)));
        });
    }

    @SubscribeEvent
    public static void applyAttackEffects(final LivingAttackEvent event) {
        if (event.getSource().getEntity() == null) return;

        event.getSource().getEntity().getCapability(AttackEffectsCapabilityProvider.ATTACK_EFFECTS).ifPresent(
            attackEffects -> attackEffects.getEffects().forEach(
                attackEffect -> event.getEntity().addEffect(new MobEffectInstance(attackEffect))
            )
        );
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            AttackEffectsCapabilityProvider attackEffectsCapabilityProvider = new AttackEffectsCapabilityProvider();
            event.addCapability(AttackEffectsCapabilityProvider.KEY, attackEffectsCapabilityProvider);
            event.addListener(attackEffectsCapabilityProvider::invalidateCaps);
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(final PlayerContainerEvent.Open event) {
        event.getContainer().addSlotListener(new TimedEnchantmentsListener());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().inventoryMenu.addSlotListener(new StaffItemListener());
        event.getEntity().inventoryMenu.addSlotListener(new TimedEnchantmentsListener());
    }

    @SubscribeEvent
    public static void removeAttackEffect(final MobEffectEvent event) {
        if (!(event instanceof MobEffectEvent.Expired || event instanceof MobEffectEvent.Remove)) return;

        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().get() instanceof AttackMobEffect attackMobEffect) {
            event.getEntity().getCapability(AttackEffectsCapabilityProvider.ATTACK_EFFECTS).ifPresent(
                attackEffects -> attackEffects.removeEffect(attackMobEffect.getAppliedEffect().get())
            );
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        for (ItemStack itemStack : TIMED_ITEM_STACKS) {
            TimedEnchantments timedEnchantments = itemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);

            timedEnchantments.forEach(timedEnchantment -> {
                if (timedEnchantment.tick()) {
                    int newLevel = EnchantmentHelper.getItemEnchantmentLevel(timedEnchantment.getEnchantment(), itemStack) - timedEnchantment.getLevel();
                    EnchantmentHelper.updateEnchantments(itemStack, itemEnchantments -> itemEnchantments.set(timedEnchantment.getEnchantment(), newLevel));
                }
            });

            TimedEnchantments newTimedEnchantments = timedEnchantments.remove();
            if (newTimedEnchantments.isEmpty()) itemStack.remove(ModComponents.TIMED_ENCHANTMENTS.get());
            else itemStack.set(ModComponents.TIMED_ENCHANTMENTS.get(), newTimedEnchantments);
        }

        TIMED_ITEM_STACKS.removeIf(itemStack -> itemStack.get(ModComponents.TIMED_ENCHANTMENTS.get()) == null);
    }
}
