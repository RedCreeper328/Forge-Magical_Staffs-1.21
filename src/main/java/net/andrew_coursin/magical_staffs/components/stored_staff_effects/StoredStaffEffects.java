package net.andrew_coursin.magical_staffs.components.stored_staff_effects;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class StoredStaffEffects {
    public enum Indices {LEVEL, POINTS, SLOTS}

    private static final Codec<List<Integer>> ENCHANTMENT_VALUES_CODEC;
    private static final Codec<List<Integer>> POTION_VALUES_CODEC;
    private static final Codec<HashMap<Holder<Enchantment>, List<Integer>>> ENCHANTMENT_CODEC;
    private static final Codec<HashMap<Holder<MobEffect>, List<Integer>>> POTION_CODEC;
    private static final Codec<Integer> ENCHANTMENT_SLOTS_CODEC;
    private static final Codec<Integer> POTION_SLOTS_CODEC;
    public static final Codec<StoredStaffEffects> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, StoredStaffEffects> STREAM_CODEC;
    public static final StoredStaffEffects EMPTY = new StoredStaffEffects(new HashMap<>(), new HashMap<>(), 0, 0);

    protected HashMap<Holder<Enchantment>, List<Integer>> enchantments;
    protected HashMap<Holder<MobEffect>, List<Integer>> potions;
    protected int enchantmentSlots;
    protected int potionSlots;

    private StoredStaffEffects(HashMap<Holder<Enchantment>, List<Integer>> enchantments, HashMap<Holder<MobEffect>, List<Integer>> potions, int enchantmentSlots, int potionSlots) {
        this.enchantments = enchantments;
        this.potions = potions;
        this.enchantmentSlots = enchantmentSlots;
        this.potionSlots = potionSlots;
    }

    public boolean isEmpty(boolean isEnchantment) {
        return isEnchantment ? this.enchantments.isEmpty() : this.potions.isEmpty();
    }

    public Holder<Enchantment> getEnchantment(int index) {
        return this.enchantments.keySet().stream().toList().get(index);
    }

    public Holder<MobEffect> getPotion(int index) {
        return this.potions.keySet().stream().toList().get(index);
    }

    public int getValue(Either<Holder<Enchantment>, Holder<MobEffect>> effect, Indices index) {
        if (effect.left().isPresent()){
            return enchantments.getOrDefault(effect.left().get(), List.of(0, 0, 0)).get(index.ordinal());
        }

        if (effect.right().isPresent()) {
            return potions.getOrDefault(effect.right().get(), List.of(0, 0, 0)).get(index.ordinal());
        }

        // Default value
        return 0;
    }

    public int getUsedSlots(boolean isEnchantment) {
        return isEnchantment ? this.enchantmentSlots : this.potionSlots;
    }

    public int size(boolean isEnchantment) {
       return isEnchantment ? this.enchantments.size() : this.potions.size();
    }


    static {
        ENCHANTMENT_VALUES_CODEC = Codec.intRange(0, 255).listOf();
        POTION_VALUES_CODEC = Codec.intRange(0, 255).listOf();
        ENCHANTMENT_CODEC = Codec.unboundedMap(Enchantment.CODEC, ENCHANTMENT_VALUES_CODEC).xmap(HashMap::new, Function.identity());
        POTION_CODEC = Codec.unboundedMap(MobEffect.CODEC, POTION_VALUES_CODEC).xmap(HashMap::new, Function.identity());
        ENCHANTMENT_SLOTS_CODEC = Codec.INT;
        POTION_SLOTS_CODEC = Codec.INT;

        CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                ENCHANTMENT_CODEC.fieldOf("enchantments").forGetter(storedStaffEffects -> storedStaffEffects.enchantments),
                POTION_CODEC.fieldOf("potions").forGetter(storedStaffEffects -> storedStaffEffects.potions),
                ENCHANTMENT_SLOTS_CODEC.fieldOf("enchantment_slots").forGetter(storedStaffEffects -> storedStaffEffects.enchantmentSlots),
                POTION_SLOTS_CODEC.fieldOf("potion_slots").forGetter(storedStaffEffects -> storedStaffEffects.potionSlots)
            ).apply(instance, StoredStaffEffects::new)
        );

        STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(HashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.fromCodec(Codec.intRange(0, 255).listOf())),
                storedStaffEffects -> storedStaffEffects.enchantments,
                ByteBufCodecs.map(HashMap::new, MobEffect.STREAM_CODEC, ByteBufCodecs.fromCodec(Codec.intRange(0, 255).listOf())),
                storedStaffEffects -> storedStaffEffects.potions,
                ByteBufCodecs.VAR_INT,
                storedStaffEffects -> storedStaffEffects.enchantmentSlots,
                ByteBufCodecs.VAR_INT,
                storedStaffEffects -> storedStaffEffects.potionSlots,
                StoredStaffEffects::new
        );
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof StoredStaffEffects storedStaffEffects
                    && this.enchantments.equals(storedStaffEffects.enchantments)
                    && this.enchantmentSlots == storedStaffEffects.enchantmentSlots
                    && this.potions.equals(storedStaffEffects.potions)
                    && this.potionSlots == storedStaffEffects.potionSlots;
        }
    }

    @Override
    public int hashCode() {
        int i = this.enchantments.hashCode();
        i = 31 * i + this.potions.hashCode();
        return i;
    }

    public static class Mutable extends StoredStaffEffects{
        public Mutable(StoredStaffEffects storedStaffEffects) {
            super(new HashMap<>(), new HashMap<>(), storedStaffEffects.enchantmentSlots, storedStaffEffects.potionSlots);
            this.enchantments.putAll(storedStaffEffects.enchantments);
            this.potions.putAll(storedStaffEffects.potions);
        }

        public void setEnchantmentValues(Holder<Enchantment> enchantment, List<Integer> values) {
            this.enchantmentSlots += values.get(Indices.SLOTS.ordinal()) - getValue(Either.left(enchantment), Indices.SLOTS);
            if (values.equals(List.of(0, 0, 0))) this.enchantments.remove(enchantment);
            else this.enchantments.put(enchantment, values);
        }

        public void setPotionValues(Holder<MobEffect> potion, List<Integer> values) {
            this.potionSlots += values.get(Indices.SLOTS.ordinal()) - getValue(Either.right(potion), Indices.SLOTS);
            if (values.equals(List.of(0, 0, 0))) this.potions.remove(potion);
            else this.potions.put(potion, values);
        }

        public StoredStaffEffects toImmutable() {
            return new StoredStaffEffects(this.enchantments, this.potions, this.enchantmentSlots, this.potionSlots);
        }
    }
}