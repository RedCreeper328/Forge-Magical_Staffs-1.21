package net.andrew_coursin.magical_staffs.item;

import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.event.TimedEnchantmentEndEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TimedItemStack extends ItemStack {
    public TimedItemStack(ItemStack itemStack) {
        super(itemStack.getItemHolder(), itemStack.getCount(), itemStack.getComponentsPatch());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void timedEnchantmentEnd(TimedEnchantmentEndEvent event) {
        TimedEnchantments timedEnchantments = this.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY);
        TimedEnchantment timedEnchantment = timedEnchantments.remove(event.getId());
        if (timedEnchantments.isEmpty()) MinecraftForge.EVENT_BUS.unregister(this);
        if (timedEnchantment == null) return;
        int newLevel = EnchantmentHelper.getItemEnchantmentLevel(timedEnchantment.getEnchantment(), this) - timedEnchantment.getLevel();
        EnchantmentHelper.updateEnchantments(this, itemEnchantments -> itemEnchantments.set(timedEnchantment.getEnchantment(), newLevel));
    }
}
