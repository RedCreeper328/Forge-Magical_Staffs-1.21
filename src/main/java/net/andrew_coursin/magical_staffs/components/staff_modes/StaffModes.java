package net.andrew_coursin.magical_staffs.components.staff_modes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Objects;

public class StaffModes {
    // Private Variables
    private Holder<Enchantment> enchantment = null;
    private Holder<MobEffect> potion = null;
    private int index = 0;
    private int level = 0;
    private int newOtherLevel = 0;
    private int newStaffLevel = 0;
    private int newStaffPoints = 0;
    private int newStaffSlots = 0;
    private Modes mode = Modes.ABSORB;

    // Public enum
    public enum Modes {ABSORB, INFUSE, IMBUE}

    // Public variables
    public static final Codec<StaffModes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(Modes::valueOf, Enum::toString).fieldOf("mode").forGetter(staffModes -> staffModes.mode)
    ).apply(instance, StaffModes::new));

    public StaffModes() {

    }

    private StaffModes(Modes mode) {
        this.mode = mode;
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public Holder<MobEffect> getPotion() {
        return this.potion;
    }

    public int getIndex() {
        return this.index;
    }

    public int getLevel() {
        return this.level;
    }

    public int getNewOtherLevel() {
        return this.newOtherLevel;
    }

    public int getNewStaffLevel() {
        return this.newStaffLevel;
    }

    public int getNewStaffPoints() {
        return this.newStaffPoints;
    }

    public int getNewStaffSlots() {
        return this.newStaffSlots;
    }

    public Modes getMode() {
        return this.mode;
    }

    public void reset(boolean resetIndex) {
        this.enchantment = null;
        this.potion = null;
        if (resetIndex) this.index = 0;
        this.level = 0;
        this.newOtherLevel = 0;
        this.newStaffLevel = 0;
        this.newStaffPoints = 0;
        this.newStaffSlots = 0;
    }

    public void setEnchantment(Holder<Enchantment> enchantment) {
        this.enchantment = enchantment;
    }

    public void setPotion(Holder<MobEffect> potion) {
        this.potion = potion;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setNewOtherLevel(int newOtherLevel) {
        this.newOtherLevel = newOtherLevel;
    }

    public void setNewStaffLevel(int newStaffLevel) {
        this.newStaffLevel = newStaffLevel;
    }

    public void setNewStaffPoints(int newStaffPoints) {
        this.newStaffPoints = newStaffPoints;
    }

    public void setNewStaffSlots(int newStaffSlots) {
        this.newStaffSlots = newStaffSlots;
    }

    public void setMode(Modes mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof StaffModes staffModes
                    && this.enchantment == staffModes.enchantment
                    && this.potion == staffModes.potion
                    && this.index == staffModes.index
                    && this.level == staffModes.level
                    && this.newOtherLevel == staffModes.newOtherLevel
                    && this.newStaffLevel == staffModes.newStaffLevel
                    && this.newStaffPoints == staffModes.newStaffPoints
                    && this.newStaffSlots == staffModes.newStaffSlots
                    && this.mode == staffModes.mode;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(enchantment, potion, index, level, newOtherLevel, newStaffLevel, newStaffPoints, newStaffSlots, mode);
    }
}
