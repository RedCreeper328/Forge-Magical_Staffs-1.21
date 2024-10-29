package net.red_creeper.magical_staffs.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.item.custom.StaffItem;
import net.red_creeper.magical_staffs.components.forge_material.ForgeMaterial;
import net.red_creeper.magical_staffs.components.forge_material.ForgeMaterials;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmithingForgeRecipe implements SmithingRecipe {
    final Optional<Ingredient> addition;
    final Optional<Ingredient> base;
    final Optional<Ingredient> template;
    private PlacementInfo placementInfo;

    public SmithingForgeRecipe(Optional<Ingredient> pTemplate, Optional<Ingredient> pBase, Optional<Ingredient> pAddition) {
        this.template = pTemplate;
        this.base = pBase;
        this.addition = pAddition;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
        ItemStack baseItemStack = smithingRecipeInput.base();
        ItemStack materialItemStack = smithingRecipeInput.addition();
        ItemStack resultItemStack = baseItemStack.copy();

        if (!(resultItemStack.getItem() instanceof StaffItem staffItem)) {
            return ItemStack.EMPTY;
        }

        ForgeMaterial forgeMaterial = ForgeMaterials.getForgeMaterialFromIngredient(materialItemStack);
        resultItemStack.set(DataComponents.CUSTOM_NAME, Component.translatable(Util.makeDescriptionId("forge_material", ResourceLocation.fromNamespaceAndPath(MOD_ID, "hover_name")), Component.translatable(Util.makeDescriptionId("forge_material", ResourceLocation.fromNamespaceAndPath(MOD_ID, forgeMaterial.name()))), resultItemStack.getItemName().getString()).withStyle(forgeMaterial.style()));
        resultItemStack.set(ModDataComponents.FORGE_MATERIAL.get(), forgeMaterial);
        return staffItem.canForge(resultItemStack) ? resultItemStack : ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
        }

        return this.placementInfo;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public Optional<Ingredient> baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return this.template;
    }

    @Override
    public RecipeSerializer<SmithingForgeRecipe> getSerializer() {
        return ModRecipeSerializers.SMITHING_FORGE.get();
    }

    public static class Serializer implements RecipeSerializer<SmithingForgeRecipe> {
        private static final MapCodec<SmithingForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("template").forGetter(smithingForgeRecipe -> smithingForgeRecipe.template),
                Ingredient.CODEC.optionalFieldOf("base").forGetter(smithingForgeRecipe -> smithingForgeRecipe.base),
                Ingredient.CODEC.optionalFieldOf("addition").forGetter(smithingForgeRecipe -> smithingForgeRecipe.addition)
        ).apply(instance, SmithingForgeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingForgeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
                smithingForgeRecipe -> smithingForgeRecipe.template,
                Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
                smithingForgeRecipe -> smithingForgeRecipe.base,
                Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC,
                smithingForgeRecipe -> smithingForgeRecipe.addition,
                SmithingForgeRecipe::new
        );

        @Override
        public MapCodec<SmithingForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
