package net.andrew_coursin.magical_staffs.components.timer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Timer {
    public static final Timer DEFAULT = new Timer(0);
    public static final Codec<Timer> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("time").forGetter(timer -> timer.time)).apply(instance, Timer::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Timer> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, timer -> timer.time, Timer::new);
    private int time;

    public Timer(int time) {
        this.time = time;
        if (this.time > 0) MinecraftForge.EVENT_BUS.register(this);
    }

    public int getTime() {
        return this.time;
    }

    @SubscribeEvent
    public void tickTime(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (this.time > 0) this.time--;
        else MinecraftForge.EVENT_BUS.unregister(this);
    }

//    @Override
//    public boolean equals(Object pOther) {
//        if (this == pOther) return true;
//        else return pOther instanceof Timer timer && this.time == timer.time;
//    }
//
//    @Override
//    public int hashCode() {
//        return this.time;
//    }
}
