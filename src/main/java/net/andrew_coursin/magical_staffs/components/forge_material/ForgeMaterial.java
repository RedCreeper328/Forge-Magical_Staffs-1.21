package net.andrew_coursin.magical_staffs.components.forge_material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record ForgeMaterial(String name, Optional<Item> ingredient, Optional<TagKey<Item>> tag, int activeDuration, int cooldownFactor, int enchantmentSlots, int potionSlots, Style style) {
    public static final Codec<ForgeMaterial> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.string(0, Integer.MAX_VALUE).fieldOf("name").forGetter(forgeMaterial -> forgeMaterial.name)
            ).apply(instance, ForgeMaterials::getForgeMaterialFromName)
        );
    }
}
