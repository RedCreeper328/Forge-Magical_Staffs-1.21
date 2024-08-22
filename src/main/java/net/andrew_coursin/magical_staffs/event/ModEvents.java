package net.andrew_coursin.magical_staffs.event;

import net.andrew_coursin.magical_staffs.MagicalStaffs;
import net.andrew_coursin.magical_staffs.capability.attack_effects.AttackEffectsCapabilityProvider;
import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.effect.AttackMobEffect;
import net.andrew_coursin.magical_staffs.inventory.StaffItemListener;
import net.andrew_coursin.magical_staffs.level.TimedEnchantmentSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Mod.EventBusSubscriber(modid = MagicalStaffs.MOD_ID)
@ParametersAreNonnullByDefault
public class ModEvents {
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

    @SubscribeEvent
    public static void addTimedEnchantmentsTooltips(final ItemTooltipEvent event) {
        TimedEnchantments timedEnchantments = event.getItemStack().get(ModComponents.TIMED_ENCHANTMENTS.get());

        if (timedEnchantments != null && event.getEntity() != null) {
            Item.TooltipContext tooltipContext = Item.TooltipContext.of(event.getEntity().level());

            timedEnchantments.forEach(timedEnchantment -> {
                Holder<Enchantment> enchantment = timedEnchantment.getEnchantment();
                int index = event.getToolTip().lastIndexOf(Enchantment.getFullname(enchantment, EnchantmentHelper.getItemEnchantmentLevel(enchantment, event.getItemStack())));
                event.getToolTip().add(index + 1, (((MutableComponent) Enchantment.getFullname(enchantment, timedEnchantment.getLevel())).append(Component.translatable("tooltip.magical_staffs.duration", StringUtil.formatTickDuration(timedEnchantment.getDuration(), tooltipContext.tickRate()))).withStyle(ChatFormatting.DARK_PURPLE)));
            });
        }
    }

    @SubscribeEvent
    public static void applyAttackEffects(final LivingAttackEvent event) {
        if (event.getSource().getEntity() == null) {
            return;
        }

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
    public static void onAttachSavedData(final LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == ServerLevel.OVERWORLD) {
            serverLevel.getDataStorage().computeIfAbsent(TimedEnchantmentSavedData.factory(), TimedEnchantmentSavedData.ID);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().inventoryMenu.addSlotListener(new StaffItemListener());
    }

    @SubscribeEvent
    public static void removeAttackEffect(final MobEffectEvent event) {
        if (!(event instanceof MobEffectEvent.Expired || event instanceof MobEffectEvent.Remove)) {
            return;
        }

        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().get() instanceof AttackMobEffect attackMobEffect) {
            event.getEntity().getCapability(AttackEffectsCapabilityProvider.ATTACK_EFFECTS).ifPresent(
                attackEffects -> attackEffects.removeEffect(attackMobEffect.getAppliedEffect().get())
            );
        }
    }

    @SubscribeEvent
    public static void timedEnchantmentTickDownDuration(final TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<Integer> removedIds = TimedEnchantmentSavedData.get(event.getServer().overworld()).updateDurations();

            if (removedIds.isEmpty()) return;

            for (int id : removedIds) {
                MinecraftForge.EVENT_BUS.post(new TimedEnchantmentEndEvent(id));
            }

            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                player.containerMenu.broadcastFullState();
            }
        }
    }
}
