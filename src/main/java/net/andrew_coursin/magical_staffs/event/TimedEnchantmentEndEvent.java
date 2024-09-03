package net.andrew_coursin.magical_staffs.event;

import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.minecraftforge.eventbus.api.Event;

public class TimedEnchantmentEndEvent extends Event {
    private final int id;
    private final TimedEnchantment timedEnchantment;

    public TimedEnchantmentEndEvent(int id, TimedEnchantment timedEnchantment) {
        this.id = id;
        this.timedEnchantment = timedEnchantment;
    }

    public int getId() {
        return this.id;
    }

    public TimedEnchantment getTimedEnchantment() {
        return this.timedEnchantment;
    }
}
