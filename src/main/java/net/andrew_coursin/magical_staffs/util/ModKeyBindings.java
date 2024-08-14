package net.andrew_coursin.magical_staffs.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final String KEY_CATEGORY_MOD = "key.category.magical_staffs";
    public static final KeyMapping CYCLE_EFFECTS_FORWARD = new KeyMapping("key.magical_staffs.cycle_effects_forward", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, KEY_CATEGORY_MOD);
    public static final KeyMapping CYCLE_EFFECTS_BACKWARD = new KeyMapping("key.magical_staffs.cycle_effects_backward", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, KEY_CATEGORY_MOD);
    public static final KeyMapping CYCLE_EFFECTS_INCREASE = new KeyMapping("key.magical_staffs.cycle_effects_increase", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, KEY_CATEGORY_MOD);
    public static final KeyMapping CYCLE_EFFECTS_DECREASE = new KeyMapping("key.magical_staffs.cycle_effects_decrease", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, KEY_CATEGORY_MOD);
}
