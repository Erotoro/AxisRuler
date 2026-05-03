package com.axisruler.keybind;

import com.axisruler.util.AxisRulerText;
import com.axisruler.util.ModConstants;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeybindRegistrar {
    private final KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "controls"));
    private List<KeyMapping> keyBindings = List.of();
    private KeyMapping setPointA;
    private KeyMapping setPointB;
    private KeyMapping clearPoints;
    private KeyMapping swapPoints;
    private KeyMapping cycleMode;
    private KeyMapping toggleHud;
    private KeyMapping toggleGuides;
    private KeyMapping toggleLabels;
    private KeyMapping toggleLine;
    private KeyMapping copyMeasurement;
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

    public List<KeyMapping> keyBindings() {
        ensureRegistered();
        return keyBindings;
    }

    public KeyMapping setPointA() {
        ensureRegistered();
        return setPointA;
    }

    public KeyMapping setPointB() {
        ensureRegistered();
        return setPointB;
    }

    public KeyMapping clearPoints() {
        ensureRegistered();
        return clearPoints;
    }

    public KeyMapping swapPoints() {
        ensureRegistered();
        return swapPoints;
    }

    public KeyMapping cycleMode() {
        ensureRegistered();
        return cycleMode;
    }

    public KeyMapping toggleHud() {
        ensureRegistered();
        return toggleHud;
    }

    public KeyMapping toggleGuides() {
        ensureRegistered();
        return toggleGuides;
    }

    public KeyMapping toggleLabels() {
        ensureRegistered();
        return toggleLabels;
    }

    public KeyMapping toggleLine() {
        ensureRegistered();
        return toggleLine;
    }

    public KeyMapping copyMeasurement() {
        ensureRegistered();
        return copyMeasurement;
    }

    private KeyMapping registerKey(String translationKey, int defaultKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
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
