package net.andrew_coursin.magical_staffs.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.item.custom.StaffItem;
import net.andrew_coursin.magical_staffs.item.forge_material.ForgeMaterial;
import net.andrew_coursin.magical_staffs.item.forge_material.ForgeMaterials;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.stream.Stream;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmithingForgeRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;

    public SmithingForgeRecipe(Ingredient pTemplate, Ingredient pBase, Ingredient pAddition) {
        this.template = pTemplate;
        this.base = pBase;
        this.addition = pAddition;
    }

    public static void register() {
        ForgeRegistries.RECIPE_SERIALIZERS.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_forge"), new SmithingForgeRecipe.Serializer());
    }

    @Override
    public boolean isAdditionIngredient(ItemStack pStack) {
        return this.addition.test(pStack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        return this.base.test(pStack);
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(net.minecraftforge.common.ForgeHooks::hasNoElements);
    }

    @Override
    public boolean isTemplateIngredient(ItemStack pStack) {
        return this.template.test(pStack);
    }

    @Override
    public boolean matches(SmithingRecipeInput smithingRecipeInput, Level pLevel) {
        return this.template.test(smithingRecipeInput.template()) && this.base.test(smithingRecipeInput.base()) && this.addition.test(smithingRecipeInput.addition());
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
        ItemStack baseItemStack = smithingRecipeInput.base();

        if (this.base.test(baseItemStack)) {
            ItemStack materialItemStack = smithingRecipeInput.addition();

            if (this.addition.test(materialItemStack)) {
                ItemStack resultItemStack = baseItemStack.copy();

                if (resultItemStack.getItem() instanceof StaffItem staffItem && staffItem.canForge(resultItemStack)) {
                    ForgeMaterial forgeMaterial = ForgeMaterials.getForgeMaterialFromIngredient(materialItemStack);
                    resultItemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("forge_material.magical_staffs.hover_name", Component.translatable(Util.makeDescriptionId("forge_material", ResourceLocation.fromNamespaceAndPath(MOD_ID, forgeMaterial.name()))), Component.translatable(resultItemStack.getDescriptionId())).withStyle(forgeMaterial.style()));
                    resultItemStack.set(ModComponents.FORGE_MATERIAL.get(), forgeMaterial);
                    return resultItemStack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(Items.NETHERITE_SWORD);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_forge")));
    }

    public static class Serializer implements RecipeSerializer<SmithingForgeRecipe> {
//        public SmithingForgeRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
//            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "template"));
//            Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "base"));
//            Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "addition"));
//            return new SmithingForgeRecipe(resourceLocation, ingredient, ingredient1, ingredient2);
//        }
//
        private static SmithingForgeRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
            Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
            Ingredient addition = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
            return new SmithingForgeRecipe(template, base, addition);
        }

        private static void toNetwork(RegistryFriendlyByteBuf pBuffer, SmithingForgeRecipe pRecipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.addition);
        }

        private static final MapCodec<SmithingForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("template").forGetter(smithingForgeRecipe -> smithingForgeRecipe.template),
                Ingredient.CODEC.fieldOf("base").forGetter(smithingForgeRecipe -> smithingForgeRecipe.base),
                Ingredient.CODEC.fieldOf("addition").forGetter(smithingForgeRecipe -> smithingForgeRecipe.addition)
            ).apply(instance, SmithingForgeRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingForgeRecipe> STREAM_CODEC = StreamCodec.of(
                SmithingForgeRecipe.Serializer::toNetwork, SmithingForgeRecipe.Serializer::fromNetwork
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
