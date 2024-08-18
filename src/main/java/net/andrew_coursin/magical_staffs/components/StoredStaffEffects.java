package net.andrew_coursin.magical_staffs.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class StoredStaffEffects implements TooltipProvider {
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

    private final HashMap<Holder<Enchantment>, List<Integer>> enchantments;
    private final HashMap<Holder<MobEffect>, List<Integer>> potions;
    private int enchantmentSlots;
    private int potionSlots;

    StoredStaffEffects(HashMap<Holder<Enchantment>, List<Integer>> enchantments, HashMap<Holder<MobEffect>, List<Integer>> potions, int enchantmentSlots, int potionSlots) {
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

    public int getValue(boolean isEnchantment, Holder<?> effect, Indices index) {
        if (isEnchantment && effect.get() instanceof Enchantment && enchantments.containsKey(effect)){
            return enchantments.get(effect).get(index.ordinal());
        }

        if (!isEnchantment && effect.get() instanceof MobEffect && potions.containsKey(effect)) {
            return potions.get(effect).get(index.ordinal());
        }

        return 0;
    }

    public int getUsedSlots(boolean isEnchantment) {
        return isEnchantment ? this.enchantmentSlots : this.potionSlots;
    }

    public int size(boolean isEnchantment) {
       return isEnchantment ? this.enchantments.size() : this.potions.size();
    }

    public void setEnchantmentValues(Holder<Enchantment> enchantment, List<Integer> values) {
        if (values.equals(List.of(0, 0, 0))) this.enchantments.remove(enchantment);
        else this.enchantments.put(enchantment, values);
    }

    public void setPotionValues(Holder<MobEffect> potion, List<Integer> values) {
        if (values.equals(List.of(0, 0, 0))) this.potions.remove(potion);
        else this.potions.put(potion, values);
    }

    public void setUsedSlots(boolean isEnchantment, int slots) {
        if (isEnchantment) this.enchantmentSlots = slots;
        else this.potionSlots = slots;
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
    public void addToTooltip(Item.@NotNull TooltipContext tooltipContext, @NotNull Consumer<Component> consumer, @NotNull TooltipFlag tooltipFlag) {

    }
}
