package com.axisruler.keybind;

import com.axisruler.util.AxisRulerText;
import com.axisruler.util.ModConstants;
import java.util.List;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeybindRegistrar {
    private final KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ModConstants.MOD_ID, "controls"));
    private List<KeyBinding> keyBindings = List.of();
    private KeyBinding setPointA;
    private KeyBinding setPointB;
    private KeyBinding clearPoints;
    private KeyBinding swapPoints;
    private KeyBinding cycleMode;
    private KeyBinding toggleHud;
    private KeyBinding toggleGuides;
    private KeyBinding toggleLabels;
    private KeyBinding toggleLine;
    private KeyBinding copyMeasurement;
    private boolean registered;

    public void register() {
        if (registered) {
            return;
        }

        setPointA = registerKey(AxisRulerText.KEY_SET_POINT_A, GLFW.GLFW_KEY_Z);
        setPointB = registerKey(AxisRulerText.KEY_SET_POINT_B, GLFW.GLFW_KEY_X);
        clearPoints = registerKey(AxisRulerText.KEY_CLEAR_POINTS, GLFW.GLFW_KEY_C);
        swapPoints = registerKey(AxisRulerText.KEY_SWAP_POINTS, GLFW.GLFW_KEY_V);
        cycleMode = registerKey(AxisRulerText.KEY_CYCLE_MODE, GLFW.GLFW_KEY_R);
        toggleHud = registerKey(AxisRulerText.KEY_TOGGLE_HUD, GLFW.GLFW_KEY_H);
        toggleGuides = registerKey(AxisRulerText.KEY_TOGGLE_GUIDES, GLFW.GLFW_KEY_G);
        toggleLabels = registerKey(AxisRulerText.KEY_TOGGLE_LABELS, GLFW.GLFW_KEY_L);
        toggleLine = registerKey(AxisRulerText.KEY_TOGGLE_LINE, GLFW.GLFW_KEY_J);
        copyMeasurement = registerKey(AxisRulerText.KEY_COPY_MEASUREMENT, GLFW.GLFW_KEY_M);
        keyBindings = List.of(
                setPointA,
                setPointB,
                clearPoints,
                swapPoints,
                cycleMode,
                toggleHud,
                toggleGuides,
                toggleLabels,
                toggleLine,
                copyMeasurement
        );
        registered = true;
    }

    public List<KeyBinding> keyBindings() {
        ensureRegistered();
        return keyBindings;
    }

    public KeyBinding setPointA() {
        ensureRegistered();
        return setPointA;
    }

    public KeyBinding setPointB() {
        ensureRegistered();
        return setPointB;
    }

    public KeyBinding clearPoints() {
        ensureRegistered();
        return clearPoints;
    }

    public KeyBinding swapPoints() {
        ensureRegistered();
        return swapPoints;
    }

    public KeyBinding cycleMode() {
        ensureRegistered();
        return cycleMode;
    }

    public KeyBinding toggleHud() {
        ensureRegistered();
        return toggleHud;
    }

    public KeyBinding toggleGuides() {
        ensureRegistered();
        return toggleGuides;
    }

    public KeyBinding toggleLabels() {
        ensureRegistered();
        return toggleLabels;
    }

    public KeyBinding toggleLine() {
        ensureRegistered();
        return toggleLine;
    }

    public KeyBinding copyMeasurement() {
        ensureRegistered();
        return copyMeasurement;
    }

    private KeyBinding registerKey(String translationKey, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.KEYSYM,
                defaultKey,
                category
        ));
    }

    private void ensureRegistered() {
        if (!registered) {
            throw new IllegalStateException("AxisRuler key bindings are not registered yet");
        }
    }
}
