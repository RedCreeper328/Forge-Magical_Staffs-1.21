package net.andrew_coursin.magical_staffs.event;

import net.minecraftforge.eventbus.api.Event;

public class TimedEnchantmentEndEvent extends Event {
    private final int id;

    public TimedEnchantmentEndEvent(int pId) {
        this.id = pId;
    }

    public int getId() {
        return this.id;
    }
}
