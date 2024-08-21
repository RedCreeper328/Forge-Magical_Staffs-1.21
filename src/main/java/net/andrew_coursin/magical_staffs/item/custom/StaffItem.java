package net.andrew_coursin.magical_staffs.item.custom;

import com.mojang.datafixers.util.Either;
import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.stored_staff_effects.StoredStaffEffects;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.effect.ModEffects;
import net.andrew_coursin.magical_staffs.components.forge_material.ForgeMaterial;
import net.andrew_coursin.magical_staffs.components.forge_material.ForgeMaterials;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.andrew_coursin.magical_staffs.networking.ModPacketHandler;
import net.andrew_coursin.magical_staffs.networking.packet.StaffItemKeyBindC2SPacket;
import net.andrew_coursin.magical_staffs.util.ModKeyBindings;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StaffItem extends Item {
    // Private enum
    private enum Modes {ABSORB, INFUSE, IMBUE}

    // Private variables
    private Holder<Enchantment> absorbEnchantment;
    private Holder<Enchantment> infuseEnchantment;
    private Holder<MobEffect> absorbPotion;
    private Holder<MobEffect> infusePotion;
    private int absorbLevel;
    private int index;
    private int infuseLevel;
    private int newOtherLevel;
    private int newStaffLevel;
    private int newStaffPoints;
    private int newStaffSlots;
    private Modes mode = Modes.ABSORB;

    // Private final variables
    private final int activeDuration;
    private final int coolDownFactor;
    private final int maxEnchantmentSlots;
    private final int maxPotionSlots;

    // Constructor method
    public StaffItem(int pActiveDuration, int pCoolDownFactor, int pMaxEnchantmentSlots, int pMaxPotionSlots, Properties properties) {
        super(properties);
        this.index = -1;
        this.activeDuration = pActiveDuration;
        this.coolDownFactor = pCoolDownFactor;
        this.maxEnchantmentSlots = pMaxEnchantmentSlots;
        this.maxPotionSlots = pMaxPotionSlots;
    }

    // Private methods
    private double staffPointsInverse(boolean isEnchantment, int points) {
        return isEnchantment ? Math.log1p(points) / Math.log(2) : (-1 + Math.sqrt(1 + 8 * points)) / 2;
    }

    private int getActiveDuration(ItemStack staffItemStack) {
        ForgeMaterial forgeMaterial = getForgeMaterial(staffItemStack);

        if (forgeMaterial.activeDuration() == Integer.MIN_VALUE) {
            return this.activeDuration;
        } else {
            return (this.activeDuration + forgeMaterial.activeDuration()) / 2;
        }
    }

    private int getCooldownDuration(ItemStack staffItemStack) {
        ForgeMaterial forgeMaterial = getForgeMaterial(staffItemStack);

        if (forgeMaterial.cooldownFactor() == Integer.MIN_VALUE) {
            return getActiveDuration(staffItemStack) / (1 + 1 / this.coolDownFactor);
        } else {
            return getActiveDuration(staffItemStack) * (1 + 2 / (this.coolDownFactor + forgeMaterial.cooldownFactor()));
        }
    }

    private int getMaxSlots(boolean isEnchantment, ItemStack staffItemStack) {
        ForgeMaterial forgeMaterial = getForgeMaterial(staffItemStack);

        if (isEnchantment) {
            return this.maxEnchantmentSlots - Math.floorDiv(forgeMaterial.enchantmentSlots(), -2); // Gets around casting int to double to use Math.ceil()
        } else {
            return this.maxPotionSlots - Math.floorDiv(forgeMaterial.potionSlots(), -2); // Gets around casting int to double to use Math.ceil()
        }
    }

    private int otherLevelToPoints(boolean isEnchantment, int level) {
        return isEnchantment ? (int) Math.pow(2, level - 1) : level;
    }

    private int otherPointsToLevel(boolean isEnchantment, int points) {
        return isEnchantment ? (int) (Math.log(points) / Math.log(2)) + 1 : points;
    }

    private int staffSlotsToPoints(boolean isEnchantment, int slots) {
        return isEnchantment ? (int) (Math.pow(2, slots) - 1) : slots * (slots + 1) / 2;
    }

    private void absorbEnchantment(Player player) {
        // Don't remove the effect if the player is in creative
        if (player.isCreative()) return;

        // Set the new level of the enchantment
        ItemStack otherItemStack = player.getOffhandItem();
        ItemEnchantments otherEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack);
        ItemEnchantments.Mutable mutableOtherEnchantments = new ItemEnchantments.Mutable(otherEnchantments);
        mutableOtherEnchantments.set(this.absorbEnchantment, this.newOtherLevel);
        EnchantmentHelper.setEnchantments(otherItemStack, mutableOtherEnchantments.toImmutable());

        // Replace an empty enchanted book with a book item
        if (otherItemStack.is(Items.ENCHANTED_BOOK) && mutableOtherEnchantments.toImmutable().isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, Items.BOOK.getDefaultInstance());
        }
    }

    private void absorbPotion(Player player) {
        // Don't remove the effect if the player is in creative
        if (player.isCreative()) return;

        // Get the instance of the effect before removing it from the player
        MobEffectInstance absorbPotionInstance = player.getEffect(this.absorbPotion);
        player.removeEffect(this.absorbPotion);

        // Set the new level if it is not zero
        if (this.newOtherLevel > 0 && absorbPotionInstance != null)
            player.addEffect(new MobEffectInstance(this.absorbPotion, absorbPotionInstance.getDuration(), this.newOtherLevel - 1));
    }

    private void appendEnchantments(ItemStack staffItemStack, List<Component> tooltipComponents) {
        ArrayList<Component> enchantmentComponents = new ArrayList<>();
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        for (int i = 0; i < storedStaffEffects.size(true); i++) {
            Holder<Enchantment> enchantment = storedStaffEffects.getEnchantment(i);

            int level = storedStaffEffects.getValue(Either.left(enchantment), StoredStaffEffects.Indices.LEVEL);
            int slots = storedStaffEffects.getValue(Either.left(enchantment), StoredStaffEffects.Indices.SLOTS);
            enchantmentComponents.add(Component.translatable("tooltip.magical_staffs.effect", Enchantment.getFullname(enchantment, level), slots).withStyle(ChatFormatting.BLUE));

            if (Screen.hasShiftDown()) {
                int currentPoints = storedStaffEffects.getValue(Either.left(enchantment), StoredStaffEffects.Indices.POINTS);
                int nextPoints = staffSlotsToPoints(true, slots);
                enchantmentComponents.add(Component.translatable("tooltip.magical_staffs.effect_extra", currentPoints, nextPoints, Enchantment.getFullname(enchantment, slots)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltipComponents.add(Component.translatable("tooltip.magical_staffs.enchantment_slots", getStoredEffects(staffItemStack).getUsedSlots(true), getMaxSlots(true, staffItemStack)).withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.addAll(enchantmentComponents);
    }

    private void appendPotions(ItemStack staffItemStack, List<Component> tooltipComponents) {
        ArrayList<Component> potionComponents = new ArrayList<>();
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        for (int i = 0; i < storedStaffEffects.size(false); i++) {
            Holder<MobEffect> potion = storedStaffEffects.getPotion(i);

            MutableComponent id = Component.translatable(potion.get().getDescriptionId());
            MutableComponent lvl = Component.translatable("potion.potency." + (storedStaffEffects.getValue(Either.right(potion), StoredStaffEffects.Indices.LEVEL) - 1));
            int slots = storedStaffEffects.getValue(Either.right(potion), StoredStaffEffects.Indices.SLOTS);
            potionComponents.add(Component.translatable("tooltip.magical_staffs.effect", Component.translatable("potion.withAmplifier", id, lvl).withStyle(potion.get().getCategory().getTooltipFormatting()), slots).withStyle(ChatFormatting.BLUE));

            if (Screen.hasShiftDown()) {
                int currentPoints = storedStaffEffects.getValue(Either.right(potion), StoredStaffEffects.Indices.POINTS);
                int nextPoints = staffSlotsToPoints(false, slots);
                MutableComponent nextLvl = Component.translatable("potion.potency." + (slots - 1));
                potionComponents.add(Component.translatable("tooltip.magical_staffs.effect_extra", currentPoints, nextPoints, Component.translatable("potion.withAmplifier", id, nextLvl)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltipComponents.add(Component.translatable("tooltip.magical_staffs.potion_slots", getStoredEffects(staffItemStack).getUsedSlots(false), getMaxSlots(false, staffItemStack)).withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.addAll(potionComponents);
    }

    private void completeAbsorb(Player player) {
        // Cannot absorb with no selected enchantment or potion
        if (this.absorbEnchantment == null && this.absorbPotion == null) {
            message(false, player, Component.translatable("message.magical_staffs.no_choices").getString());
            return;
        }

        // Cannot absorb if player experience is less than requirement
        if (player.experienceLevel < this.newStaffLevel && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", this.newStaffLevel).getString());
            return;
        }

        // Initialize local variables based on isEnchantment
        ItemStack staffItemStack = player.getMainHandItem();
        boolean isEnchantment = this.absorbEnchantment != null;
        StoredStaffEffects.Mutable storedStaffEffects = new StoredStaffEffects.Mutable(getStoredEffects(staffItemStack));

        // Update the level, points, and slots of the stored effect
        if (isEnchantment) storedStaffEffects.setEnchantmentValues(this.absorbEnchantment, List.of(this.newStaffLevel, this.newStaffPoints, this.newStaffSlots));
        else storedStaffEffects.setPotionValues(this.absorbPotion, List.of(this.newStaffLevel, this.newStaffPoints, this.newStaffSlots));

        // Apply the updated values to the item stack
        staffItemStack.set(ModComponents.STORED_STAFF_EFFECTS.get(), storedStaffEffects.toImmutable());

        // Update the other item tag or player effect to the new level
        if (isEnchantment) absorbEnchantment(player);
        else absorbPotion(player);

        // Reduce experience levels, message the player, and play the enchanting table sound
        message(false, player, Component.translatable("message.magical_staffs.absorb.complete", isEnchantment ? "Enchantment" : "Potion").getString());
        player.giveExperienceLevels(-1 * this.newStaffLevel);
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        reset(false);
    }

    private void completeInfuse(Player player) {
        // Cannot infuse with no selected enchantment or potion
        if (this.infuseEnchantment == null && this.infusePotion == null) {
            message(false, player, Component.translatable("message.magical_staffs.no_choices").getString());
            reset(false);
            return;
        }

        // Cannot infuse if player experience is less than required
        if (player.experienceLevel < this.newOtherLevel && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", this.newOtherLevel).getString());
            reset(false);
            return;
        }

        // Initialize local variables
        ItemStack staffItemStack = player.getMainHandItem();
        ItemStack otherItemStack = player.getOffhandItem();
        boolean isEnchantment = !otherItemStack.is(Items.POTION);
        StoredStaffEffects.Mutable storedStaffEffects = new StoredStaffEffects.Mutable(getStoredEffects(staffItemStack));

        // Update the level, points, and slots of the stored effect
        if (isEnchantment) storedStaffEffects.setEnchantmentValues(this.infuseEnchantment, List.of(this.newStaffLevel, this.newStaffPoints, this.newStaffSlots));
        else storedStaffEffects.setPotionValues(this.infusePotion, List.of(this.newStaffLevel, this.newStaffPoints, this.newStaffSlots));

        // Apply the updated values to the item stack
        staffItemStack.set(ModComponents.STORED_STAFF_EFFECTS.get(), storedStaffEffects.toImmutable());

        // Increase the effect level
        if (isEnchantment) infuseEnchantment(otherItemStack, player);
        else infusePotion(otherItemStack, staffItemStack);

        // Reduce experience levels, message the player, and play the enchanting table sound
        message(false, player, Component.translatable("message.magical_staffs.infuse.complete", isEnchantment ? "Enchantment" : "Potion").getString());
        player.giveExperienceLevels(-1 * this.newOtherLevel);
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        reset(false);
    }

    private void cycleMode(Player player) {
        switch (mode) {
            case ABSORB -> mode = Modes.INFUSE;
            case INFUSE -> mode = Modes.IMBUE;
            case IMBUE -> mode = Modes.ABSORB;
        }

        message(false, player, Component.translatable("message.magical_staffs.mode_selected", Component.translatable("message.magical_staffs." + mode.toString().toLowerCase(Locale.ROOT))).getString());
        reset(true);
    }

    private void imbue(ItemStack staffItemStack, Player player) {
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);
//        ListTag staffEnchantments = staffItemStack.getOrCreateTag().getList(ENCHANTMENT_KEY, 10);
//        ListTag staffPotions = staffItemStack.getOrCreateTag().getList(POTION_KEY, 10);

        // Cannot imbue with no enchantments and no potions
        if (storedStaffEffects.isEmpty(true) && storedStaffEffects.isEmpty(false)) {
            message(false, player, Component.translatable("message.magical_staffs.imbue.no_effects").getString());
            return;
        }

        int experienceCost = getStoredEffects(staffItemStack).getUsedSlots(true) + storedStaffEffects.getUsedSlots(false);

        // Cannot imbue without the required experience
        if (player.experienceLevel < experienceCost && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", experienceCost).getString());
            return;
        }

        // Apply the imbue effects to the player
        imbueEnchantments(staffItemStack, player);
        imbuePotions(staffItemStack, player);
        player.giveExperienceLevels(-1 * experienceCost);
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(this, getCooldownDuration(staffItemStack));
//        staffItemStack.getCapability(TimerCapabilityProvider.TIMER).ifPresent(timer -> timer.setTime(getActiveDuration(staffItemStack) + getCoolDownDuration(staffItemStack)));

        // Sent a packet to the player so that the foil appears properly
//        if (player instanceof ServerPlayer serverPlayer) {
//            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
//                if (!player.getInventory().getItem(i).isEmpty() && ItemStack.isSameItemSameTags(staffItemStack, player.getInventory().getItem(i))) {
//                    ModPacketHandler.sendToPlayer(new SetTimerS2CPacket(i, getActiveDuration(staffItemStack) + getCoolDownDuration(staffItemStack)), serverPlayer);
//                    break;
//                }
//            }
//        }
    }

    private void imbueEnchantments(ItemStack staffItemStack, Player player) {
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        for (int i = 0; i < storedStaffEffects.size(true); i++) {
            Holder<Enchantment> enchantment = storedStaffEffects.getEnchantment(i);
            int addLevel = storedStaffEffects.getValue(Either.left(enchantment), StoredStaffEffects.Indices.LEVEL);
            TimedEnchantment timedEnchantment = new TimedEnchantment(enchantment, getActiveDuration(staffItemStack), addLevel);

            for (ItemStack otherItemStack : player.containerMenu.getItems()) {
                if (!enchantment.get().canEnchant(otherItemStack)) continue;
                int newLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, otherItemStack) + addLevel;
                otherItemStack.enchant(enchantment, newLevel);
                TimedEnchantments timedEnchantments = otherItemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);
                timedEnchantments.add(timedEnchantment);
                otherItemStack.set(ModComponents.TIMED_ENCHANTMENTS.get(), timedEnchantments);
            }
        }
    }

    private void imbuePotions(ItemStack staffItemStack, Player player) {
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        for (int i = 0; i < storedStaffEffects.size(false); i++) {
            Holder<MobEffect> potion = storedStaffEffects.getPotion(i);
            Holder<MobEffect> attackMobEffect = ModEffects.getAttackEffect(potion);
            int amplifier = storedStaffEffects.getValue(Either.right(potion), StoredStaffEffects.Indices.LEVEL) - 1;

            if (potion.get().isBeneficial()) {
                MobEffectInstance mobEffectInstance = player.getEffect(potion);
                player.addEffect(new MobEffectInstance(potion, getActiveDuration(staffItemStack), amplifier + (mobEffectInstance != null ? mobEffectInstance.getAmplifier() + 1 : 0)));
            } else if (attackMobEffect != null) {
                player.addEffect(new MobEffectInstance(attackMobEffect, getActiveDuration(staffItemStack), amplifier));
            }
        }
    }

    private void infuseEnchantment(ItemStack otherItemStack, Player player) {
        // Set book items to enchanted book items
        if (otherItemStack.is(Items.BOOK)) {
            ItemStack newItemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            newItemStack.enchant(this.infuseEnchantment, this.newOtherLevel);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemUtils.createFilledResult(otherItemStack, player, newItemStack));
        } else {
            otherItemStack.enchant(this.infuseEnchantment, this.newOtherLevel);
        }
    }

    private void infusePotion(ItemStack otherItemStack, ItemStack staffItemStack) {
        // Get current potions
        Iterable<MobEffectInstance> otherPotions = otherItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
        PotionContents newOtherPotions = new PotionContents(Potions.THICK);
        int currentDuration = 0;

        // Make sure all the potions are in custom effects
        for (MobEffectInstance mobEffectInstance : otherPotions) {
            if (mobEffectInstance.getEffect() == this.infusePotion) {
                currentDuration = mobEffectInstance.getDuration();
            } else {
                newOtherPotions = newOtherPotions.withEffectAdded(mobEffectInstance);
            }
        }

        // Add the new potion with the higher level
        MobEffectInstance newPotion = new MobEffectInstance(this.infusePotion, (currentDuration + getActiveDuration(staffItemStack)) / 2, this.newOtherLevel - 1);
        otherItemStack.set(DataComponents.POTION_CONTENTS, newOtherPotions.withEffectAdded(newPotion));
    }

    private void prepareAbsorb(boolean isEnchantment, int indexIncrement, ItemStack staffItemStack, Player player) {
        // Initialize local variables based on isEnchantment
        ItemStack otherItemStack = player.getOffhandItem();
        ItemEnchantments otherEnchantments = isEnchantment ? EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack) : null;
        Collection<MobEffectInstance> otherPotions = !isEnchantment ? player.getActiveEffects() : null;
        String type = isEnchantment ? "enchantment" : "potion";

        // Cannot absorb if there are no enchantments or potions
        if ((isEnchantment && otherEnchantments.isEmpty()) || (!isEnchantment && otherPotions.isEmpty())) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.no_effect", type).getString());
            reset(false);
            return;
        }

        // Increment the index by one and set the effect to absorb
        int size = isEnchantment ? otherEnchantments.size() : otherPotions.size();
        this.index = (this.index + indexIncrement + size) % size;
        this.absorbEnchantment = isEnchantment ? otherEnchantments.keySet().stream().toList().get(this.index) : null;
        this.absorbPotion = !isEnchantment ? otherPotions.stream().toList().get(this.index).getEffect() : null;

        // Get the current staff level, points, and slots
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);
        int currentStaffLevel = storedStaffEffects.getValue(isEnchantment ? Either.left(this.absorbEnchantment) : Either.right(this.absorbPotion), StoredStaffEffects.Indices.LEVEL);
        int currentStaffPoints = storedStaffEffects.getValue(isEnchantment ? Either.left(this.absorbEnchantment) : Either.right(this.absorbPotion), StoredStaffEffects.Indices.POINTS);
        int currentStaffSlots = storedStaffEffects.getValue(isEnchantment ? Either.left(this.absorbEnchantment) : Either.right(this.absorbPotion), StoredStaffEffects.Indices.SLOTS);
        int usedStaffSlots = storedStaffEffects.getUsedSlots(isEnchantment);

        // Create the name of the effect to be used in messages
        Component nameComponent = Component.translatable(isEnchantment ? absorbEnchantment.get().description().getString() : absorbPotion.get().getDescriptionId());

        // Limit the absorbed enchantments to the maximum enchantment level
        if (isEnchantment && currentStaffLevel >= this.absorbEnchantment.get().getMaxLevel()) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.max_level", nameComponent, Component.translatable("enchantment.level." + this.absorbEnchantment.get().getMaxLevel())).getString());
            reset(false);
            return;
        }

        // Calculate new other level and points
        int currentOtherLevel = isEnchantment ? getEnchantmentLevel(this.absorbEnchantment, otherItemStack) : getPotionLevel(this.absorbPotion, player);
        int currentOtherPoints = otherLevelToPoints(isEnchantment, currentOtherLevel);
        this.absorbLevel = Math.clamp(this.absorbLevel, 1, currentOtherLevel);
        this.newOtherLevel = currentOtherLevel - this.absorbLevel;
        int newOtherPoints = otherLevelToPoints(isEnchantment, newOtherLevel);

        // The points absorbed into the staff equals the points the item lost
        int deltaPoints = currentOtherPoints - newOtherPoints;

        int slotLimit = Math.min(getMaxSlots(isEnchantment, staffItemStack) - usedStaffSlots + currentStaffSlots, isEnchantment ? this.absorbEnchantment.get().getMaxLevel() : Integer.MAX_VALUE);
        int pointLimit = staffSlotsToPoints(isEnchantment, slotLimit);

        // Limit the absorbed effects to the maximum slots
        if (currentStaffPoints >= pointLimit) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.no_slots", type).getString());
            reset(false);
            return;
        }

        // Calculate new staff points, level, and slots
        this.newStaffPoints = Math.min(currentStaffPoints + deltaPoints, pointLimit);
        double inverse = staffPointsInverse(isEnchantment, this.newStaffPoints);
        this.newStaffLevel = (int) Math.floor(inverse);
        this.newStaffSlots = (int) Math.ceil(inverse);

        // Create translatable components and message the player
        String pKey = isEnchantment ? "enchantment.level." : "potion.potency.";
        int adjustment = isEnchantment ? 0 : 1;
        Component newOtherLevelComponent = this.newOtherLevel - adjustment > 0 ? Component.translatable(pKey + (this.newOtherLevel - adjustment)) : Component.literal("");
        Component newStaffLevelComponent = this.newStaffLevel - adjustment > 0 ? Component.translatable(pKey + (this.newStaffLevel - adjustment)) : Component.literal("");
        message(false, player, Component.translatable("message.magical_staffs.absorb.prepare", nameComponent, newStaffLevelComponent, this.newStaffPoints, this.newStaffSlots, newOtherLevelComponent).getString());
    }

    private void prepareInfuse(boolean isEnchantment, int indexIncrement, ItemStack staffItemStack, Player player) {
        // Initialize local variables
        ItemStack otherItemStack = player.getOffhandItem();
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        // Stop if there is no offhand item
        if (otherItemStack.isEmpty()) {
            message(false, player, Component.translatable("message.magical_staffs.infuse.no_item").getString());
            reset(false);
            return;
        }

        // Stop if there is no effect to infuse
        if (storedStaffEffects.isEmpty(isEnchantment)) {
            message(false, player, Component.translatable("message.magical_staffs.infuse.no_effect", isEnchantment ? "enchantment" : "potion").getString());
            reset(false);
            return;
        }

        // Change the index and set the effect to infuse
        this.index = (this.index + indexIncrement + storedStaffEffects.size(isEnchantment)) % storedStaffEffects.size(isEnchantment);
        this.infuseEnchantment = isEnchantment ? storedStaffEffects.getEnchantment(this.index) : null;
        this.infusePotion = !isEnchantment ? storedStaffEffects.getPotion(this.index) : null;

        // Get the current level, and points
        int currentOtherLevel = isEnchantment ? getEnchantmentLevel(this.infuseEnchantment, otherItemStack) : getPotionLevel(this.infusePotion, otherItemStack);
        int currentOtherPoints = otherLevelToPoints(isEnchantment, currentOtherLevel);
        int currentStaffPoints = storedStaffEffects.getValue(isEnchantment ? Either.left(this.infuseEnchantment) : Either.right(this.infusePotion), StoredStaffEffects.Indices.POINTS);

        Component nameComponent = Component.translatable(isEnchantment ? this.infuseEnchantment.get().description().getString() : this.infusePotion.get().getDescriptionId());

        // Stop if item can not be enchanted with enchantment
        if (isEnchantment) {
            if (!this.infuseEnchantment.get().canEnchant(otherItemStack) && !otherItemStack.is(Items.BOOK) && !otherItemStack.is(Items.ENCHANTED_BOOK)) {
                message(false, player, Component.translatable("message.magical_staffs.infuse.can_not_enchant", nameComponent).getString());
                reset(false);
                return;
            } else if (currentOtherLevel >= this.infuseEnchantment.get().getMaxLevel()) {
                message(false, player, Component.translatable("message.magical_staffs.infuse.max_level", nameComponent, Component.translatable("enchantment.level." + this.infuseEnchantment.get().getMaxLevel())).getString());
                reset(false);
                return;
            }
        }

        // Clamp the infuse level between 1 and the maximum determined by points stored in the staff
        int maxInfuseLevel = Math.min(otherPointsToLevel(isEnchantment, currentOtherPoints + currentStaffPoints), isEnchantment ? this.infuseEnchantment.get().getMaxLevel() : Integer.MAX_VALUE);
        this.infuseLevel = Math.clamp(this.infuseLevel, 1, maxInfuseLevel - currentOtherLevel);

        // Calculate new other level, and points
        this.newOtherLevel = currentOtherLevel + infuseLevel;
        int newOtherPoints = otherLevelToPoints(isEnchantment, this.newOtherLevel);

        // Calculate new staff level, points, and slots
        this.newStaffPoints = currentStaffPoints - newOtherPoints + currentOtherPoints;
        double inverse = staffPointsInverse(isEnchantment, this.newStaffPoints);
        this.newStaffLevel = (int) Math.floor(inverse);
        this.newStaffSlots = (int) Math.ceil(inverse);

        // Create translatable components and message the player
        String pKey = isEnchantment ? "enchantment.level." : "potion.potency.";
        int adjustment = isEnchantment ? 0 : 1;
        Component newOtherLevelComponent = this.newOtherLevel - adjustment > 0 ? Component.translatable(pKey + (this.newOtherLevel - adjustment)) : Component.literal("");
        Component newStaffLevelComponent = this.newStaffLevel - adjustment > 0 ? Component.translatable(pKey + (this.newStaffLevel - adjustment)) : Component.literal("");
        message(false, player, Component.translatable("message.magical_staffs.infuse.prepare", nameComponent, newStaffLevelComponent, this.newStaffPoints, this.newStaffSlots, newOtherLevelComponent).getString());
    }

    // Private static methods
    private static ForgeMaterial getForgeMaterial(ItemStack staffItemStack) {
        return staffItemStack.getOrDefault(ModComponents.FORGE_MATERIAL.get(), ForgeMaterials.NONE);
    }

    private static int getEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack otherItemStack) {
        return EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack).getLevel(enchantment);
    }

    private static int getPotionLevel(Holder<MobEffect> potion, ItemStack otherItemStack) {
        Iterable<MobEffectInstance> potions = otherItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();

        // Return 0 if the potion doesn't have a resource key
        if (potion.unwrapKey().isEmpty()) {
            return 0;
        }

        // Check each effect if it is the same as the potion
        for (MobEffectInstance effect : potions) {
            if (effect.getEffect().is(potion.unwrapKey().get())) {
                return effect.getAmplifier() + 1;
            }
        }

        // If no effect matches return 0
        return 0;
    }

    private static int getPotionLevel(Holder<MobEffect> potion, Player player) {
        MobEffectInstance effect = player.getEffect(potion);

        if (effect == null) {
            message(true, player, Component.translatable("message.magical_staffs.null", "potion").getString());
            return 0;
        }

        return effect.getAmplifier() + 1;
    }

    private static StoredStaffEffects getStoredEffects(ItemStack staffItemStack) {
        return staffItemStack.getOrDefault(ModComponents.STORED_STAFF_EFFECTS.get(), StoredStaffEffects.EMPTY);
    }

    private static void message(boolean debug, Player player, String string) {
        if (!debug) player.displayClientMessage(Component.literal(string), true);
        else player.sendSystemMessage(Component.literal(string));
    }

    // Public methods
    public boolean canForge(ItemStack staffItemStack) {
        return getStoredEffects(staffItemStack).getUsedSlots(true) <= getMaxSlots(true, staffItemStack) && getStoredEffects(staffItemStack).getUsedSlots(false) <= getMaxSlots(false, staffItemStack);
    }

    public void reset(boolean resetIndex) {
        this.absorbEnchantment = null;
        this.absorbPotion = null;
        this.infuseEnchantment = null;
        this.infusePotion = null;
        this.newStaffLevel = 0;
        this.newStaffPoints = 0;
        this.newStaffSlots = 0;
        if (resetIndex) this.index = 0;
    }

    public void useKeyBind(Player player, StaffItemKeyBindC2SPacket.KEY_BINDS keyBind) {
        switch(keyBind) {
            case CYCLE_FORWARD -> {
                switch(mode) {
                    case ABSORB -> {
                        this.absorbLevel = 1;
                        this.prepareAbsorb(!player.getOffhandItem().isEmpty(), 1, player.getMainHandItem(), player);
                    }
                    case INFUSE -> {
                        this.infuseLevel = 1;
                        this.prepareInfuse(!player.getOffhandItem().is(Items.POTION), 1, player.getMainHandItem(), player);
                    }
                }
            }
            case CYCLE_BACKWARD -> {
                switch(mode) {
                    case ABSORB -> {
                        this.absorbLevel = 1;
                        this.prepareAbsorb(!player.getOffhandItem().isEmpty(), -1, player.getMainHandItem(), player);
                    }
                    case INFUSE -> {
                        this.infuseLevel = 1;
                        this.prepareInfuse(!player.getOffhandItem().is(Items.POTION), -1, player.getMainHandItem(), player);
                    }
                }
            }
            case CYCLE_INCREASE -> {
                switch(mode) {
                    case ABSORB -> {
                        this.absorbLevel++;
                        this.prepareAbsorb(!player.getOffhandItem().isEmpty(), 0, player.getMainHandItem(), player);
                    }
                    case INFUSE -> {
                        this.infuseLevel++;
                        this.prepareInfuse(!player.getOffhandItem().is(Items.POTION), 0, player.getMainHandItem(), player);
                    }
                }
            }
            case CYCLE_DECREASE -> {
                switch(mode) {
                    case ABSORB -> {
                        this.absorbLevel--;
                        this.prepareAbsorb(!player.getOffhandItem().isEmpty(), 0, player.getMainHandItem(), player);
                    }
                    case INFUSE -> {
                        this.infuseLevel--;
                        this.prepareInfuse(!player.getOffhandItem().is(Items.POTION), 0, player.getMainHandItem(), player);
                    }
                }
            }
        }
    }

    // Override public methods
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
//        if (pStack.getCapability(TimerCapabilityProvider.TIMER).resolve().isEmpty()) {
//            return false;
//        } else {
//            return pStack.getCapability(TimerCapabilityProvider.TIMER).resolve().get().getTime() == 0;
//        }
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return switch (mode) {
            case ABSORB, INFUSE -> false;
            case IMBUE -> true;
        };
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

//        if (!this.isFoil(itemStack)) {
//            itemStack.getCapability(TimerCapabilityProvider.TIMER).ifPresent(
//                timer -> message(false, pPlayer, Component.translatable("message.magical_staffs.on_cool_down", StringUtil.formatTickDuration(timer.getTime())).getString())
//            );
//            return InteractionResultHolder.fail(itemStack);
//        }

        if (pLevel.isClientSide()) {
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(itemStack);
        }

        if (pPlayer.isSecondaryUseActive()) {
            cycleMode(pPlayer);
            pPlayer.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(itemStack);
        }

        switch (mode) {
            case ABSORB -> {
                if (pUsedHand == InteractionHand.MAIN_HAND) completeAbsorb(pPlayer);
            }
            case INFUSE -> {
                if (pUsedHand == InteractionHand.MAIN_HAND) completeInfuse(pPlayer);
            }
            case IMBUE -> imbue(itemStack, pPlayer);
        }

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPYGLASS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("tooltip.magical_staffs.when_used").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.magical_staffs.active_duration", StringUtil.formatTickDuration(getActiveDuration(pStack), pContext.tickRate())).withStyle(ChatFormatting.DARK_GREEN));
        pTooltipComponents.add(Component.translatable("tooltip.magical_staffs.cool_down_duration", StringUtil.formatTickDuration(getCooldownDuration(pStack), pContext.tickRate())).withStyle(ChatFormatting.DARK_GREEN));
        pTooltipComponents.add(CommonComponents.EMPTY);
        appendEnchantments(pStack, pTooltipComponents);
        pTooltipComponents.add(CommonComponents.EMPTY);
        appendPotions(pStack, pTooltipComponents);

        if (!Screen.hasShiftDown()) {
            pTooltipComponents.add(Component.translatable("tooltip.magical_staffs.extra_info").withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class StaffItemEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (ModKeyBindings.CYCLE_EFFECTS_FORWARD.consumeClick()) {
                    ModPacketHandler.sendToServer(new StaffItemKeyBindC2SPacket(StaffItemKeyBindC2SPacket.KEY_BINDS.CYCLE_FORWARD));
                }

                while (ModKeyBindings.CYCLE_EFFECTS_BACKWARD.consumeClick()) {
                    ModPacketHandler.sendToServer(new StaffItemKeyBindC2SPacket(StaffItemKeyBindC2SPacket.KEY_BINDS.CYCLE_BACKWARD));
                }

                while (ModKeyBindings.CYCLE_EFFECTS_INCREASE.consumeClick()) {
                    ModPacketHandler.sendToServer(new StaffItemKeyBindC2SPacket(StaffItemKeyBindC2SPacket.KEY_BINDS.CYCLE_INCREASE));
                }

                while (ModKeyBindings.CYCLE_EFFECTS_DECREASE.consumeClick()) {
                    ModPacketHandler.sendToServer(new StaffItemKeyBindC2SPacket(StaffItemKeyBindC2SPacket.KEY_BINDS.CYCLE_DECREASE));
                }
            }
        }
    }
}