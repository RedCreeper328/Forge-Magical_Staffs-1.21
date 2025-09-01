package net.red_creeper.magical_staffs.components.forge_material;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class ForgeMaterials {
    public static final ForgeMaterial NONE = new ForgeMaterial("none", Optional.of(Items.AIR), Optional.empty(), 0, 9, 0, 0, Style.EMPTY);
    public static final ForgeMaterial WOOD = new ForgeMaterial("wood", Optional.empty(), Optional.of(ItemTags.PLANKS), 1200, 8, 4, 0, Style.EMPTY.withColor(6697728));
    public static final ForgeMaterial GOLD = new ForgeMaterial("gold", Optional.of(Items.GOLD_INGOT), Optional.empty(), 2400, 7, 5, 0, Style.EMPTY.withColor(ChatFormatting.GOLD));
    public static final ForgeMaterial AMETHYST = new ForgeMaterial("amethyst", Optional.of(Items.AMETHYST_SHARD), Optional.empty(), 3600, 6, 3, 3, Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
    public static final ForgeMaterial IRON = new ForgeMaterial("iron", Optional.of(Items.IRON_INGOT), Optional.empty(), 4800, 5, 2, 5, Style.EMPTY.withColor(ChatFormatting.GRAY));
    public static final ForgeMaterial DIAMOND = new ForgeMaterial("diamond", Optional.of(Items.DIAMOND), Optional.empty(), 6000, 4, 1, 7, Style.EMPTY.withColor(ChatFormatting.AQUA));
    public static final ForgeMaterial BLAZE = new ForgeMaterial("blaze", Optional.of(Items.BLAZE_ROD), Optional.empty(), 7200, 3, 0, 9, Style.EMPTY.withColor(ChatFormatting.YELLOW));
    public static final ForgeMaterial NETHERITE = new ForgeMaterial("netherite", Optional.of(Items.NETHERITE_INGOT), Optional.empty(), 8400, 2, 4, 6, Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
    public static final ForgeMaterial WITHER = new ForgeMaterial("wither", Optional.of(Items.NETHER_STAR), Optional.empty(), 9600, 1, 0, 12, Style.EMPTY.withColor(ChatFormatting.WHITE));
    public static final ForgeMaterial DRAGON = new ForgeMaterial("dragon", Optional.of(Items.DRAGON_BREATH), Optional.empty(), 9600, 1, 12, 0, Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE));
    public static final List<ForgeMaterial> FORGE_MATERIALS = List.of(NONE, WOOD, GOLD, AMETHYST, IRON, DIAMOND, BLAZE, NETHERITE, WITHER, DRAGON);

    public static ForgeMaterial getForgeMaterialFromIngredient(ItemStack pIngredient) {
        for (ForgeMaterial forgeMaterial : FORGE_MATERIALS) {
            if ((forgeMaterial.ingredient().isPresent() && pIngredient.is(forgeMaterial.ingredient().get())) || (forgeMaterial.tag().isPresent() && pIngredient.is(forgeMaterial.tag().get()))) {
                return forgeMaterial;
            }
        }

        return NONE;
    }

    public static ForgeMaterial getForgeMaterialFromName(String pName) {
        for (ForgeMaterial forgeMaterial : FORGE_MATERIALS) {
            if (pName.equals(forgeMaterial.name())) {
                return forgeMaterial;
            }
        }

        return NONE;
    }
}