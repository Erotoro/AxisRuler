package dev.erotoro.axisruler.mc261x.util;

import dev.erotoro.axisruler.core.measure.MeasurePoint;
import dev.erotoro.axisruler.core.measure.SelectionMode;
import dev.erotoro.axisruler.core.text.AxisRulerKeys;
import net.minecraft.network.chat.Component;

public final class AxisRulerText {
    private AxisRulerText() {
    }

    public static Component pointASet(MeasurePoint point) {
        return Component.translatable(AxisRulerKeys.MESSAGE_POINT_A_SET, point.formatBlockPosition());
    }

    public static Component pointBSet(MeasurePoint point) {
        return Component.translatable(AxisRulerKeys.MESSAGE_POINT_B_SET, point.formatBlockPosition());
    }

    public static Component selectionCleared() {
        return Component.translatable(AxisRulerKeys.MESSAGE_SELECTION_CLEARED);
    }

    public static Component pointsSwapped() {
        return Component.translatable(AxisRulerKeys.MESSAGE_POINTS_SWAPPED);
    }

    public static Component bothPointsRequired() {
        return Component.translatable(AxisRulerKeys.MESSAGE_BOTH_POINTS_REQUIRED);
    }

    public static Component mode(SelectionMode mode) {
        return Component.translatable(AxisRulerKeys.MESSAGE_MODE, Component.translatable(mode.translationKey()));
    }

    public static Component hud(boolean enabled) {
        return Component.translatable(AxisRulerKeys.MESSAGE_HUD, enabledState(enabled));
    }

    public static Component guides(boolean enabled) {
        return Component.translatable(AxisRulerKeys.MESSAGE_GUIDES, enabledState(enabled));
    }

    public static Component labels(boolean enabled) {
        return Component.translatable(AxisRulerKeys.MESSAGE_LABELS, enabledState(enabled));
    }

    public static Component line(boolean enabled) {
        return Component.translatable(AxisRulerKeys.MESSAGE_LINE, enabledState(enabled));
    }

    public static Component noBlockTarget() {
        return Component.translatable(AxisRulerKeys.MESSAGE_NO_BLOCK_TARGET);
    }

    public static Component measurementCopied() {
        return Component.translatable(AxisRulerKeys.MESSAGE_MEASUREMENT_COPIED);
    }

    public static Component nothingToCopy() {
        return Component.translatable(AxisRulerKeys.MESSAGE_NOTHING_TO_COPY);
    }

    public static Component copyDifferentWorlds() {
        return Component.translatable(AxisRulerKeys.MESSAGE_COPY_DIFFERENT_WORLDS);
    }

    public static Component pinned(int count) {
        return Component.translatable(AxisRulerKeys.MESSAGE_PINNED, Integer.toString(count));
    }

    public static Component pinsCleared(int count) {
        return Component.translatable(AxisRulerKeys.MESSAGE_PINS_CLEARED, Integer.toString(count));
    }

    public static Component nothingToPin() {
        return Component.translatable(AxisRulerKeys.MESSAGE_NOTHING_TO_PIN);
    }

    public static Component pinsFull() {
        return Component.translatable(AxisRulerKeys.MESSAGE_PINS_FULL);
    }

    private static Component enabledState(boolean enabled) {
        return Component.translatable(enabled ? AxisRulerKeys.MESSAGE_ENABLED : AxisRulerKeys.MESSAGE_DISABLED);
    }
}
