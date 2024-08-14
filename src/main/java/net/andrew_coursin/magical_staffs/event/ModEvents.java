package net.andrew_coursin.magical_staffs.event;

import net.andrew_coursin.magical_staffs.MagicalStaffs;
import net.andrew_coursin.magical_staffs.capability.attack_effects.AttackEffectsCapabilityProvider;
import net.andrew_coursin.magical_staffs.effect.AttackMobEffect;
import net.andrew_coursin.magical_staffs.inventory.StaffItemListener;
import net.andrew_coursin.magical_staffs.TimedEnchantment;
import net.andrew_coursin.magical_staffs.level.TimedEnchantmentSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
//        event.getItemStack().getCapability(TimedEnchantmentsCapabilityProvider.TIMED_ENCHANTMENTS).ifPresent(timedEnchantments -> {
//            Player player = event.getEntity();
//            if (player == null || timedEnchantments.getTimedEnchantments().isEmpty()) return;
//
//            // On the server side player.containerMenu is set to inventoryMenu instead of CreativeModeInventoryScreen.ItemPickerMenu
//            AbstractContainerMenu abstractContainerMenu = player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu ? player.inventoryMenu : player.containerMenu;
//            ModPacketHandler.sendToServer(new AddTimedEnchantmentsTooltipsC2SPacket(abstractContainerMenu.getItems().indexOf(event.getItemStack())));
//
//            for (TimedEnchantment timedEnchantment : timedEnchantments.getTimedEnchantments()) {
//                if (timedEnchantment == null) continue;
//                Enchantment enchantment = timedEnchantment.getEnchantment();
//                int index = event.getToolTip().lastIndexOf(enchantment.getFullname(EnchantmentHelper.getTagEnchantmentLevel(enchantment, event.getItemStack())));
//                if (index != -1) event.getToolTip().add(index, (((MutableComponent) enchantment.getFullname(timedEnchantment.getLevel())).append(Component.translatable("tooltip.magical_staffs.duration", StringUtil.formatTickDuration(timedEnchantments.getDisplayDuration(timedEnchantment.getId())))).withStyle(ChatFormatting.DARK_PURPLE)));
//            }
//        });
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

        if (event.getEffectInstance().getEffect().get() instanceof AttackMobEffect attackMobEffect) {
            event.getEntity().getCapability(AttackEffectsCapabilityProvider.ATTACK_EFFECTS).ifPresent(
                attackEffects -> attackEffects.removeEffect(attackMobEffect.getAppliedEffect().get())
            );
        }
    }

    @SubscribeEvent
    public static void timedEnchantmentTickDownDuration(final TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<Integer> removedIds = TimedEnchantment.TIMED_ENCHANTMENT_SAVED_DATA.updateDurations();

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
