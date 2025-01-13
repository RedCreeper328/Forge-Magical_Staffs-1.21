package net.red_creeper.magical_staffs.item.custom;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.InteractionResult;
import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.components.staff_modes.StaffModes;
import net.red_creeper.magical_staffs.components.stored_staff_effects.StoredStaffEffects;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.red_creeper.magical_staffs.effect.AttackMobEffectInstance;
import net.red_creeper.magical_staffs.effect.ModEffects;
import net.red_creeper.magical_staffs.components.forge_material.ForgeMaterial;
import net.red_creeper.magical_staffs.components.forge_material.ForgeMaterials;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.red_creeper.magical_staffs.level.TimerSavedData;
import net.red_creeper.magical_staffs.networking.ModPacketHandler;
import net.red_creeper.magical_staffs.networking.packet.StaffItemKeyBindC2SPacket;
import net.red_creeper.magical_staffs.util.ModKeyBindings;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StaffItem extends Item {
    // Private final variables
    private final int activeDuration;
    private final int coolDownFactor;
    private final int maxEnchantmentSlots;
    private final int maxPotionSlots;

    // Constructor method
    public StaffItem(int activeDuration, int coolDownFactor, int maxEnchantmentSlots, int maxPotionSlots, Properties properties) {
        super(properties);
        this.activeDuration = activeDuration;
        this.coolDownFactor = coolDownFactor;
        this.maxEnchantmentSlots = maxEnchantmentSlots;
        this.maxPotionSlots = maxPotionSlots;
    }

    // Private methods
    private boolean canEnchant(ItemStack otherItemStack, StaffModes staffModes) {
        Set<Holder<Enchantment>> otherItemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack).keySet();

        // Need to check if the item is an enchanted, or regular, book because enchanted books aren't defined as a supported item for enchantments
        boolean canEnchantItem =  staffModes.getEnchantment().get().canEnchant(otherItemStack) || otherItemStack.is(Items.ENCHANTED_BOOK) || otherItemStack.is(Items.BOOK);

        // Need to check if the item has the enchantment because every enchantment is considered incompatible with itself, but the staff should be able to upgrade the enchantment
        boolean enchantmentIsCompatible = EnchantmentHelper.isEnchantmentCompatible(otherItemEnchantments, staffModes.getEnchantment()) || otherItemEnchantments.contains(staffModes.getEnchantment());

        // If either condition fails then the staff cannot enchant the item
        return canEnchantItem && enchantmentIsCompatible;
    }

    private boolean completeAbsorb(ItemStack otherItemStack, ItemStack staffItemStack, Player player, StaffModes staffModes) {
        // Cannot absorb with no selected enchantment or potion
        if (staffModes.getEnchantment() == null && staffModes.getPotion() == null) {
            message(false, player, Component.translatable("message.magical_staffs.no_choices").getString());
            return false;
        }

        // Cannot absorb if player experience is less than requirement
        if (player.experienceLevel < staffModes.getNewStaffLevel() && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", staffModes.getNewStaffLevel()).getString());
            return false;
        }

        // Initialize local variables based on isEnchantment
        boolean isEnchantment = staffModes.getEnchantment() != null;
        StoredStaffEffects.Mutable storedStaffEffects = new StoredStaffEffects.Mutable(getStoredEffects(staffItemStack));

        // Update the level, points, and slots of the stored effect
        if (isEnchantment) storedStaffEffects.setEnchantmentValues(staffModes.getEnchantment(), List.of(staffModes.getNewStaffLevel(), staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots()));
        else storedStaffEffects.setPotionValues(staffModes.getPotion(), List.of(staffModes.getNewStaffLevel(), staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots()));

        // Apply the updated values to the item stack
        staffItemStack.set(ModDataComponents.STORED_STAFF_EFFECTS.get(), storedStaffEffects.toImmutable());

        // Update the other item tag or player effect to the new level
        if (isEnchantment) absorbEnchantment(otherItemStack, player, staffModes);
        else absorbPotion(player, staffModes);

        // Reduce experience levels, message the player, and play the enchanting table sound
        message(false, player, Component.translatable("message.magical_staffs.absorb.complete", isEnchantment ? "Enchantment" : "Potion").getString());
        player.giveExperiencePoints(-1 * xpCostToPoints(staffModes.getNewStaffLevel()));
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        staffModes.reset(false);
        return true;
    }

    private boolean completeInfuse(ItemStack otherItemStack, ItemStack staffItemStack, Player player, StaffModes staffModes) {
        // Cannot infuse with no selected enchantment or potion
        if (staffModes.getEnchantment() == null && staffModes.getPotion() == null) {
            message(false, player, Component.translatable("message.magical_staffs.no_choices").getString());
            staffModes.reset(false);
            return false;
        }

        // Cannot infuse if player experience is less than required
        if (player.experienceLevel < staffModes.getNewOtherLevel() && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", staffModes.getNewOtherLevel()).getString());
            staffModes.reset(false);
            return false;
        }

        // Initialize local variables
        boolean isEnchantment = !otherItemStack.is(Items.POTION);
        StoredStaffEffects.Mutable storedStaffEffects = new StoredStaffEffects.Mutable(getStoredEffects(staffItemStack));

        // Update the level, points, and slots of the stored effect
        if (isEnchantment) storedStaffEffects.setEnchantmentValues(staffModes.getEnchantment(), List.of(staffModes.getNewStaffLevel(), staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots()));
        else storedStaffEffects.setPotionValues(staffModes.getPotion(), List.of(staffModes.getNewStaffLevel(), staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots()));

        // Apply the updated values to the item stack
        staffItemStack.set(ModDataComponents.STORED_STAFF_EFFECTS.get(), storedStaffEffects.toImmutable());

        // Increase the effect level
        if (isEnchantment) infuseEnchantment(otherItemStack, staffModes, player);
        else infusePotion(otherItemStack, staffItemStack, staffModes);

        // Reduce experience levels, message the player, and play the enchanting table sound
        message(false, player, Component.translatable("message.magical_staffs.infuse.complete", isEnchantment ? "Enchantment" : "Potion").getString());
        player.giveExperiencePoints(-1 * xpCostToPoints(staffModes.getNewOtherLevel()));
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        staffModes.reset(false);
        return true;
    }

    private boolean imbue(ItemStack staffItemStack, Player player, ServerLevel serverLevel) {
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        // Cannot imbue with no enchantments and no potions
        if (storedStaffEffects.isEmpty(true) && storedStaffEffects.isEmpty(false)) {
            message(false, player, Component.translatable("message.magical_staffs.imbue.no_effects").getString());
            return false;
        }

        int experienceCost = getStoredEffects(staffItemStack).getUsedSlots(true) + storedStaffEffects.getUsedSlots(false);

        // Cannot imbue without the required experience
        if (player.experienceLevel < experienceCost && !player.isCreative()) {
            message(false, player, Component.translatable("message.magical_staffs.no_experience", experienceCost).getString());
            return false;
        }

        // Apply the imbue effects to the player
        imbueEnchantments(staffItemStack, player);
        imbuePotions(staffItemStack, player);
        player.giveExperiencePoints(-1 * xpCostToPoints(experienceCost));
        serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY(), player.getZ(), experienceCost * 10, 0.0, 0.0, 0.0, 1.0);

        // Add a cooldown timer to the staff
        int staffTimerId = TimerSavedData.addStaffTimer(getCooldownDuration(staffItemStack));
        staffItemStack.set(ModDataComponents.STAFF_TIMER.get(), staffTimerId);
        return true;
    }

    private boolean useMode(ItemStack otherItemStack, ItemStack staffItemStack, Player player, ServerLevel serverLevel, StaffModes staffModes) {
        switch(staffModes.getMode()) {
            case ABSORB -> { return completeAbsorb(otherItemStack, staffItemStack, player, staffModes); }
            case INFUSE -> { return completeInfuse(otherItemStack, staffItemStack, player, staffModes); }
            case IMBUE -> { return imbue(staffItemStack, player, serverLevel); }
        }
        return false;
    }

    private double staffPointsInverse(boolean isEnchantment, int points) {
        return isEnchantment ? Math.log1p(points) / Math.log(2) : (-1 + Math.sqrt(1 + 8 * points)) / 2;
    }

    private int getActiveDuration(ItemStack staffItemStack) {
        ForgeMaterial forgeMaterial = getForgeMaterial(staffItemStack);
        return (this.activeDuration + forgeMaterial.activeDuration()) / 2;
    }

    private int getCooldownDuration(ItemStack staffItemStack) {
        ForgeMaterial forgeMaterial = getForgeMaterial(staffItemStack);
        return getActiveDuration(staffItemStack) + getActiveDuration(staffItemStack) * 2 / (this.coolDownFactor + forgeMaterial.cooldownFactor());
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

    private int xpCostToPoints(int cost) {
        if (cost >= 30) return 1395 + 112 * (cost - 30) + 9 * (cost - 31) * (cost - 30) / 2;
        if (cost >= 15) return 315 + 37 * (cost - 15) + 5 * (cost - 16) * (cost - 15) / 2;
        return 7 * cost + 2 * (cost - 1) * cost / 2;
    }

    private void absorbEnchantment(ItemStack otherItemStack, Player player, StaffModes staffModes) {
        // Don't remove the effect if the player is in creative
        if (player.isCreative()) return;

        // Set the new level of the enchantment
        ItemEnchantments otherEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack);
        ItemEnchantments.Mutable mutableOtherEnchantments = new ItemEnchantments.Mutable(otherEnchantments);
        mutableOtherEnchantments.set(staffModes.getEnchantment(), staffModes.getNewOtherLevel());
        EnchantmentHelper.setEnchantments(otherItemStack, mutableOtherEnchantments.toImmutable());

        // Replace an empty enchanted book with a book item
        if (otherItemStack.is(Items.ENCHANTED_BOOK) && mutableOtherEnchantments.toImmutable().isEmpty()) {
            InteractionHand interactionHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            player.setItemInHand(interactionHand, Items.BOOK.getDefaultInstance());
        }
    }

    private void absorbPotion(Player player, StaffModes staffModes) {
        // Don't remove the effect if the player is in creative
        if (player.isCreative()) return;

        // Get the instance of the effect before removing it from the player
        MobEffectInstance absorbPotionInstance = player.getEffect(staffModes.getPotion());
        player.removeEffect(staffModes.getPotion());

        // Set the new level if it is not zero
        if (staffModes.getNewOtherLevel() > 0 && absorbPotionInstance != null)
            player.addEffect(new MobEffectInstance(staffModes.getPotion(), absorbPotionInstance.getDuration(), staffModes.getNewOtherLevel() - 1));
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

    private void cycleMode(Player player, StaffModes staffModes) {
        switch (staffModes.getMode()) {
            case ABSORB -> staffModes.setMode(StaffModes.Modes.INFUSE);
            case INFUSE -> staffModes.setMode(StaffModes.Modes.IMBUE);
            case IMBUE -> staffModes.setMode(StaffModes.Modes.ABSORB);
        }

        message(false, player, Component.translatable("message.magical_staffs.mode_selected", Component.translatable("message.magical_staffs." + staffModes.getMode().toString().toLowerCase(Locale.ROOT))).getString());
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    private void imbueEnchantments(ItemStack staffItemStack, Player player) {
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        for (int i = 0; i < storedStaffEffects.size(true); i++) {
            Holder<Enchantment> enchantment = storedStaffEffects.getEnchantment(i);
            int addLevel = storedStaffEffects.getValue(Either.left(enchantment), StoredStaffEffects.Indices.LEVEL);
            TimedEnchantment timedEnchantment = new TimedEnchantment(enchantment, getActiveDuration(staffItemStack), addLevel);
            int timedEnchantmentId = TimerSavedData.addTimedEnchantment(timedEnchantment);

            for (ItemStack itemStack : player.containerMenu.getItems()) {
                if (!enchantment.value().canEnchant(itemStack)) continue;

                // Increase the enchantment level
                int newLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack) + addLevel;
                int overflow = 0;

                if (newLevel > 2 * enchantment.get().getMaxLevel()) {
                    overflow = newLevel - 2 * enchantment.get().getMaxLevel();
                    newLevel = 2 * enchantment.get().getMaxLevel();
                }

                itemStack.enchant(enchantment, newLevel);

                // Add the timed enchantment
                TimedEnchantments timedEnchantments = itemStack.getOrDefault(ModDataComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);
                itemStack.set(ModDataComponents.TIMED_ENCHANTMENTS.get(), timedEnchantments.add(overflow, timedEnchantmentId, timedEnchantment));
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
            } else if (attackMobEffect != null) player.addEffect(new AttackMobEffectInstance(attackMobEffect, amplifier, getActiveDuration(staffItemStack)));
        }
    }

    private void infuseEnchantment(ItemStack otherItemStack, StaffModes staffModes, Player player) {
        // Set book items to enchanted book items
        if (otherItemStack.is(Items.BOOK)) {
            ItemStack newItemStack = Items.ENCHANTED_BOOK.getDefaultInstance();
            newItemStack.enchant(staffModes.getEnchantment(), staffModes.getNewOtherLevel());
            InteractionHand interactionHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(otherItemStack, player, newItemStack));
        } else {
            otherItemStack.enchant(staffModes.getEnchantment(), staffModes.getNewOtherLevel());
        }
    }

    private void infusePotion(ItemStack otherItemStack, ItemStack staffItemStack, StaffModes staffModes) {
        // Get current potions
        Iterable<MobEffectInstance> otherPotions = otherItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
        PotionContents newOtherPotions = new PotionContents(Potions.THICK);
        int currentDuration = 0;

        // Make sure all the potions are in custom effects
        for (MobEffectInstance mobEffectInstance : otherPotions) {
            if (mobEffectInstance.getEffect() == staffModes.getPotion()) {
                currentDuration = mobEffectInstance.getDuration();
            } else {
                newOtherPotions = newOtherPotions.withEffectAdded(mobEffectInstance);
            }
        }

        // Add the new potion with the higher level
        MobEffectInstance newPotion = new MobEffectInstance(staffModes.getPotion(), (currentDuration + getActiveDuration(staffItemStack)) / 2, staffModes.getNewOtherLevel() - 1);
        otherItemStack.set(DataComponents.POTION_CONTENTS, newOtherPotions.withEffectAdded(newPotion));
    }

    private void prepareAbsorb(int indexIncrement, ItemStack otherItemStack, ItemStack staffItemStack, Player player, StaffModes staffModes) {
        // Initialize local variables based on isEnchantment
        boolean isEnchantment = !otherItemStack.isEmpty();
        ItemEnchantments otherEnchantments = isEnchantment ? EnchantmentHelper.getEnchantmentsForCrafting(otherItemStack) : null;
        Collection<MobEffectInstance> otherPotions = !isEnchantment ? player.getActiveEffects() : null;
        String type = isEnchantment ? "enchantment" : "potion";

        // Cannot absorb if there are no enchantments or potions
        if ((isEnchantment && otherEnchantments.isEmpty()) || (!isEnchantment && otherPotions.isEmpty())) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.no_effect", type).getString());
            staffModes.reset(false);
            return;
        }

        // Increment the index and set the effect to absorb
        int size = isEnchantment ? otherEnchantments.size() : otherPotions.size();
        staffModes.setIndex((staffModes.getIndex() + indexIncrement + size) % size);
        if (isEnchantment) staffModes.setEnchantment(otherEnchantments.keySet().stream().toList().get(staffModes.getIndex()));
        else staffModes.setPotion(otherPotions.stream().toList().get(staffModes.getIndex()).getEffect());

        // Get the current staff level, points, and slots
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);
        int currentStaffLevel = storedStaffEffects.getValue(isEnchantment ? Either.left(staffModes.getEnchantment()) : Either.right(staffModes.getPotion()), StoredStaffEffects.Indices.LEVEL);
        int currentStaffPoints = storedStaffEffects.getValue(isEnchantment ? Either.left(staffModes.getEnchantment()) : Either.right(staffModes.getPotion()), StoredStaffEffects.Indices.POINTS);
        int currentStaffSlots = storedStaffEffects.getValue(isEnchantment ? Either.left(staffModes.getEnchantment()) : Either.right(staffModes.getPotion()), StoredStaffEffects.Indices.SLOTS);
        int usedStaffSlots = storedStaffEffects.getUsedSlots(isEnchantment);

        // Create the name of the effect to be used in messages
        Component nameComponent = Component.translatable(isEnchantment ? staffModes.getEnchantment().get().description().getString() : staffModes.getPotion().get().getDescriptionId());

        // Limit the absorbed enchantments to the maximum enchantment level
        if (isEnchantment && currentStaffLevel >= staffModes.getEnchantment().get().getMaxLevel()) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.max_level", nameComponent, Component.translatable("enchantment.level." + staffModes.getEnchantment().get().getMaxLevel())).getString());
            staffModes.reset(false);
            return;
        }

        // Calculate new other level and points
        int currentOtherLevel = isEnchantment ? getEnchantmentLevel(staffModes.getEnchantment(), otherItemStack) : getPotionLevel(staffModes.getPotion(), player);
        int currentOtherPoints = otherLevelToPoints(isEnchantment, currentOtherLevel);
        staffModes.setLevel(Math.clamp(staffModes.getLevel(), 1, currentOtherLevel));
        staffModes.setNewOtherLevel(currentOtherLevel - staffModes.getLevel());
        int newOtherPoints = otherLevelToPoints(isEnchantment, staffModes.getNewOtherLevel());

        // The points absorbed into the staff equals the points the item lost
        int deltaPoints = currentOtherPoints - newOtherPoints;

        int slotLimit = Math.min(getMaxSlots(isEnchantment, staffItemStack) - usedStaffSlots + currentStaffSlots, isEnchantment ? staffModes.getEnchantment().get().getMaxLevel() : Integer.MAX_VALUE);
        int pointLimit = staffSlotsToPoints(isEnchantment, slotLimit);

        // Limit the absorbed effects to the maximum slots
        if (currentStaffPoints >= pointLimit) {
            message(false, player, Component.translatable("message.magical_staffs.absorb.no_slots", type).getString());
            staffModes.reset(false);
            return;
        }

        // Calculate new staff points, level, and slots
        staffModes.setNewStaffPoints(Math.min(currentStaffPoints + deltaPoints, pointLimit));
        double inverse = staffPointsInverse(isEnchantment, staffModes.getNewStaffPoints());
        staffModes.setNewStaffLevel((int) Math.floor(inverse));
        staffModes.setNewStaffSlots((int) Math.ceil(inverse));

        // Add the updated staff modes to the staff item stack
        staffItemStack.set(ModDataComponents.STAFF_MODES.get(), staffModes);

        // Create translatable components and message the player
        String pKey = isEnchantment ? "enchantment.level." : "potion.potency.";
        int adjustment = isEnchantment ? 0 : 1;
        Component newOtherLevelComponent = Component.translatable(pKey + (staffModes.getNewOtherLevel() - adjustment));
        Component newStaffLevelComponent = Component.translatable(pKey + (staffModes.getNewStaffLevel() - adjustment));
        message(false, player, Component.translatable("message.magical_staffs.absorb.prepare", nameComponent, newStaffLevelComponent, staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots(), newOtherLevelComponent).getString());
    }

    private void prepareInfuse(int indexIncrement, ItemStack otherItemStack, ItemStack staffItemStack, Player player, StaffModes staffModes) {
        // Initialize local variables
        boolean isEnchantment = !otherItemStack.is(Items.POTION);
        StoredStaffEffects storedStaffEffects = getStoredEffects(staffItemStack);

        // Stop if there is no offhand item
        if (otherItemStack.isEmpty()) {
            message(false, player, Component.translatable("message.magical_staffs.infuse.no_item").getString());
            staffModes.reset(false);
            return;
        }

        // Stop if there is no effect to infuse
        if (storedStaffEffects.isEmpty(isEnchantment)) {
            message(false, player, Component.translatable("message.magical_staffs.infuse.no_effect", isEnchantment ? "enchantment" : "potion").getString());
            staffModes.reset(false);
            return;
        }

        // Change the index and set the effect to infuse
        staffModes.setIndex((staffModes.getIndex() + indexIncrement + storedStaffEffects.size(isEnchantment)) % storedStaffEffects.size(isEnchantment));
        if (isEnchantment) staffModes.setEnchantment(storedStaffEffects.getEnchantment(staffModes.getIndex()));
        else staffModes.setPotion(storedStaffEffects.getPotion(staffModes.getIndex()));

        // Get the current level, and points
        int currentOtherLevel = isEnchantment ? getEnchantmentLevel(staffModes.getEnchantment(), otherItemStack) : getPotionLevel(staffModes.getPotion(), otherItemStack);
        int currentOtherPoints = otherLevelToPoints(isEnchantment, currentOtherLevel);
        int currentStaffPoints = storedStaffEffects.getValue(isEnchantment ? Either.left(staffModes.getEnchantment()) : Either.right(staffModes.getPotion()), StoredStaffEffects.Indices.POINTS);

        Component nameComponent = Component.translatable(isEnchantment ? staffModes.getEnchantment().get().description().getString() : staffModes.getPotion().get().getDescriptionId());

        // Stop if item can not be enchanted with enchantment
        if (isEnchantment) {
            if (!canEnchant(otherItemStack, staffModes)) {
                message(false, player, Component.translatable("message.magical_staffs.infuse.can_not_enchant", nameComponent).getString());
                staffModes.reset(false);
                return;
            } else if (currentOtherLevel >= staffModes.getEnchantment().get().getMaxLevel()) {
                message(false, player, Component.translatable("message.magical_staffs.infuse.max_level", nameComponent, Component.translatable("enchantment.level." + staffModes.getEnchantment().get().getMaxLevel())).getString());
                staffModes.reset(false);
                return;
            }
        }

        // Clamp the infuse level between 1 and the maximum determined by points stored in the staff
        int maxInfuseLevel = Math.min(otherPointsToLevel(isEnchantment, currentOtherPoints + currentStaffPoints), isEnchantment ? staffModes.getEnchantment().get().getMaxLevel() : Integer.MAX_VALUE);

        if (maxInfuseLevel - currentOtherLevel < 1) {
            message(false, player, Component.translatable("message.magical_staffs.infuse.no_points", nameComponent, otherLevelToPoints(isEnchantment, currentOtherLevel + 1) - currentOtherPoints).getString());
            staffModes.reset(false);
            return;
        }

        staffModes.setLevel(Math.clamp(staffModes.getLevel(), 1, maxInfuseLevel - currentOtherLevel));

        // Calculate new other level, and points
        staffModes.setNewOtherLevel(currentOtherLevel + staffModes.getLevel());
        int newOtherPoints = otherLevelToPoints(isEnchantment, staffModes.getNewOtherLevel());

        // Calculate new staff level, points, and slots
        staffModes.setNewStaffPoints(currentStaffPoints - newOtherPoints + currentOtherPoints);
        double inverse = staffPointsInverse(isEnchantment, staffModes.getNewStaffPoints());
        staffModes.setNewStaffLevel((int) Math.floor(inverse));
        staffModes.setNewStaffSlots((int) Math.ceil(inverse));

        // Add the updated staff modes to the staff item stack
        staffItemStack.set(ModDataComponents.STAFF_MODES.get(), staffModes);

        // Create translatable components and message the player
        String pKey = isEnchantment ? "enchantment.level." : "potion.potency.";
        int adjustment = isEnchantment ? 0 : 1;
        Component newOtherLevelComponent = Component.translatable(pKey + (staffModes.getNewOtherLevel() - adjustment));
        Component newStaffLevelComponent = Component.translatable(pKey + (staffModes.getNewStaffLevel() - adjustment));
        message(false, player, Component.translatable("message.magical_staffs.infuse.prepare", nameComponent, newStaffLevelComponent, staffModes.getNewStaffPoints(), staffModes.getNewStaffSlots(), newOtherLevelComponent).getString());
    }

    // Private static methods
    private static ForgeMaterial getForgeMaterial(ItemStack staffItemStack) {
        return staffItemStack.getOrDefault(ModDataComponents.FORGE_MATERIAL.get(), ForgeMaterials.NONE);
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
        return staffItemStack.getOrDefault(ModDataComponents.STORED_STAFF_EFFECTS.get(), StoredStaffEffects.EMPTY);
    }

    private static void message(boolean debug, Player player, String string) {
        player.displayClientMessage(Component.literal(string), !debug);
    }

    // Public methods
    public boolean canForge(ItemStack staffItemStack) {
        return getStoredEffects(staffItemStack).getUsedSlots(true) <= getMaxSlots(true, staffItemStack) && getStoredEffects(staffItemStack).getUsedSlots(false) <= getMaxSlots(false, staffItemStack);
    }

    public void useKeyBind(ItemStack otherItemStack, ItemStack staffItemStack, Player player, StaffItemKeyBindC2SPacket.KEY_BINDS keyBind) {
        StaffModes staffModes = staffItemStack.getOrDefault(ModDataComponents.STAFF_MODES.get(), new StaffModes());
        switch(keyBind) {
            case CYCLE_FORWARD:
                switch(staffModes.getMode()) {
                    case ABSORB:
                        staffModes.setLevel(1);
                        this.prepareAbsorb(1, otherItemStack, staffItemStack, player, staffModes);
                        break;
                    case INFUSE:
                        staffModes.setLevel(1);
                        this.prepareInfuse(1, otherItemStack, staffItemStack, player, staffModes);
                        break;
                }
                break;
            case CYCLE_BACKWARD:
                switch(staffModes.getMode()) {
                    case ABSORB:
                        staffModes.setLevel(1);
                        this.prepareAbsorb(-1, otherItemStack, staffItemStack, player, staffModes);
                        break;
                    case INFUSE:
                        staffModes.setLevel(1);
                        this.prepareInfuse(-1, otherItemStack, staffItemStack, player, staffModes);
                        break;
                }
                break;
            case CYCLE_INCREASE:
                switch(staffModes.getMode()) {
                    case ABSORB:
                        staffModes.setLevel(staffModes.getLevel() + 1);
                        this.prepareAbsorb(0, otherItemStack, staffItemStack, player, staffModes);
                        break;
                    case INFUSE:
                        staffModes.setLevel(staffModes.getLevel() + 1);
                        this.prepareInfuse(0, otherItemStack, staffItemStack, player, staffModes);
                        break;
                }
                break;
            case CYCLE_DECREASE:
                switch(staffModes.getMode()) {
                    case ABSORB:
                        staffModes.setLevel(staffModes.getLevel() - 1);
                        this.prepareAbsorb(0, otherItemStack, staffItemStack, player, staffModes);
                        break;
                    case INFUSE:
                        staffModes.setLevel(staffModes.getLevel() - 1);
                        this.prepareInfuse(0, otherItemStack, staffItemStack, player, staffModes);
                        break;
                }
                break;
        }
    }

    // Override public methods
    @Override
    public boolean isFoil(ItemStack pStack) {
        return !pStack.has(ModDataComponents.STAFF_TIMER.get());
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 72000;
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack staffItemStack = pPlayer.getItemInHand(pUsedHand);
        ItemStack otherItemStack = pUsedHand == InteractionHand.MAIN_HAND ? pPlayer.getOffhandItem() : pPlayer.getMainHandItem();

        if (pLevel.isClientSide()) return InteractionResult.CONSUME;
        pPlayer.startUsingItem(pUsedHand);

        if (!this.isFoil(staffItemStack)) {
            Integer staffTimer = staffItemStack.get(ModDataComponents.STAFF_TIMER.get());
            if (staffTimer == null) return InteractionResult.CONSUME;
            int time = TimerSavedData.getStaffTimer(staffTimer);
            message(false, pPlayer, Component.translatable("message.magical_staffs.on_cool_down", StringUtil.formatTickDuration(time, pLevel.tickRateManager().tickrate())).getString());
            return InteractionResult.CONSUME;
        }

        StaffModes staffModes = staffItemStack.getOrDefault(ModDataComponents.STAFF_MODES.get(), new StaffModes());
        if (!staffItemStack.has(ModDataComponents.STAFF_MODES.get())) staffItemStack.set(ModDataComponents.STAFF_MODES.get(), staffModes);
        if (pPlayer.isSecondaryUseActive()) cycleMode(pPlayer, staffModes);
        else if (useMode(otherItemStack, staffItemStack, pPlayer, (ServerLevel) pLevel, staffModes)) pPlayer.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.SPYGLASS;
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