package dev.erotoro.axisruler.mc1218.util;

import dev.erotoro.axisruler.core.measure.MeasurePoint;
import dev.erotoro.axisruler.core.measure.SelectionMode;
import dev.erotoro.axisruler.core.text.AxisRulerKeys;
import net.minecraft.text.Text;

public final class AxisRulerText {
    private AxisRulerText() {
    }

    public static Text pointASet(MeasurePoint point) {
        return Text.translatable(AxisRulerKeys.MESSAGE_POINT_A_SET, point.formatBlockPosition());
    }

    public static Text pointBSet(MeasurePoint point) {
        return Text.translatable(AxisRulerKeys.MESSAGE_POINT_B_SET, point.formatBlockPosition());
    }

    public static Text selectionCleared() {
        return Text.translatable(AxisRulerKeys.MESSAGE_SELECTION_CLEARED);
    }

    public static Text pointsSwapped() {
        return Text.translatable(AxisRulerKeys.MESSAGE_POINTS_SWAPPED);
    }

    public static Text bothPointsRequired() {
        return Text.translatable(AxisRulerKeys.MESSAGE_BOTH_POINTS_REQUIRED);
    }

    public static Text mode(SelectionMode mode) {
        return Text.translatable(AxisRulerKeys.MESSAGE_MODE, Text.translatable(mode.translationKey()));
    }

    public static Text hud(boolean enabled) {
        return Text.translatable(AxisRulerKeys.MESSAGE_HUD, enabledState(enabled));
    }

    public static Text guides(boolean enabled) {
        return Text.translatable(AxisRulerKeys.MESSAGE_GUIDES, enabledState(enabled));
    }

    public static Text labels(boolean enabled) {
        return Text.translatable(AxisRulerKeys.MESSAGE_LABELS, enabledState(enabled));
    }

    public static Text line(boolean enabled) {
        return Text.translatable(AxisRulerKeys.MESSAGE_LINE, enabledState(enabled));
    }

    public static Text noBlockTarget() {
        return Text.translatable(AxisRulerKeys.MESSAGE_NO_BLOCK_TARGET);
    }

    public static Text measurementCopied() {
        return Text.translatable(AxisRulerKeys.MESSAGE_MEASUREMENT_COPIED);
    }

    public static Text nothingToCopy() {
        return Text.translatable(AxisRulerKeys.MESSAGE_NOTHING_TO_COPY);
    }

    public static Text copyDifferentWorlds() {
        return Text.translatable(AxisRulerKeys.MESSAGE_COPY_DIFFERENT_WORLDS);
    }

    public static Text pinned(int count) {
        return Text.translatable(AxisRulerKeys.MESSAGE_PINNED, Integer.toString(count));
    }

    public static Text pinsCleared(int count) {
        return Text.translatable(AxisRulerKeys.MESSAGE_PINS_CLEARED, Integer.toString(count));
    }

    public static Text nothingToPin() {
        return Text.translatable(AxisRulerKeys.MESSAGE_NOTHING_TO_PIN);
    }

    public static Text pinsFull() {
        return Text.translatable(AxisRulerKeys.MESSAGE_PINS_FULL);
    }

    private static Text enabledState(boolean enabled) {
        return Text.translatable(enabled ? AxisRulerKeys.MESSAGE_ENABLED : AxisRulerKeys.MESSAGE_DISABLED);
    }
}
