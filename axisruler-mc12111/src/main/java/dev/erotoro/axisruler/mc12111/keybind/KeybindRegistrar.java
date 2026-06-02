package dev.erotoro.axisruler.mc12111.keybind;

import dev.erotoro.axisruler.core.text.AxisRulerKeys;
import dev.erotoro.axisruler.core.util.ModConstants;
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
    private KeyBinding pinMeasurement;
    private KeyBinding clearPinned;
    private boolean registered;

    public void register() {
        if (registered) {
            return;
        }

        setPointA = registerKey(AxisRulerKeys.KEY_SET_POINT_A, GLFW.GLFW_KEY_Z);
        setPointB = registerKey(AxisRulerKeys.KEY_SET_POINT_B, GLFW.GLFW_KEY_X);
        clearPoints = registerKey(AxisRulerKeys.KEY_CLEAR_POINTS, GLFW.GLFW_KEY_C);
        swapPoints = registerKey(AxisRulerKeys.KEY_SWAP_POINTS, GLFW.GLFW_KEY_V);
        cycleMode = registerKey(AxisRulerKeys.KEY_CYCLE_MODE, GLFW.GLFW_KEY_R);
        toggleHud = registerKey(AxisRulerKeys.KEY_TOGGLE_HUD, GLFW.GLFW_KEY_H);
        toggleGuides = registerKey(AxisRulerKeys.KEY_TOGGLE_GUIDES, GLFW.GLFW_KEY_G);
        toggleLabels = registerKey(AxisRulerKeys.KEY_TOGGLE_LABELS, GLFW.GLFW_KEY_L);
        toggleLine = registerKey(AxisRulerKeys.KEY_TOGGLE_LINE, GLFW.GLFW_KEY_J);
        copyMeasurement = registerKey(AxisRulerKeys.KEY_COPY_MEASUREMENT, GLFW.GLFW_KEY_M);
        pinMeasurement = registerKey(AxisRulerKeys.KEY_PIN, GLFW.GLFW_KEY_P);
        clearPinned = registerKey(AxisRulerKeys.KEY_CLEAR_PINNED, GLFW.GLFW_KEY_U);
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
                copyMeasurement,
                pinMeasurement,
                clearPinned
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

    public KeyBinding pinMeasurement() {
        ensureRegistered();
        return pinMeasurement;
    }

    public KeyBinding clearPinned() {
        ensureRegistered();
        return clearPinned;
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
