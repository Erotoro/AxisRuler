package com.axisruler.util;

import com.axisruler.measure.MeasurePoint;
import com.axisruler.measure.SelectionMode;
import net.minecraft.text.Text;

public final class AxisRulerText {
    public static final String KEY_CATEGORY = "key.categories.axisruler.controls";
    public static final String KEY_SET_POINT_A = "key.axisruler.set_point_a";
    public static final String KEY_SET_POINT_B = "key.axisruler.set_point_b";
    public static final String KEY_CLEAR_POINTS = "key.axisruler.clear_points";
    public static final String KEY_SWAP_POINTS = "key.axisruler.swap_points";
    public static final String KEY_CYCLE_MODE = "key.axisruler.cycle_mode";
    public static final String KEY_TOGGLE_HUD = "key.axisruler.toggle_hud";
    public static final String KEY_TOGGLE_GUIDES = "key.axisruler.toggle_guides";
    public static final String KEY_TOGGLE_LABELS = "key.axisruler.toggle_labels";
    public static final String KEY_TOGGLE_LINE = "key.axisruler.toggle_line";
    public static final String KEY_COPY_MEASUREMENT = "key.axisruler.copy_measurement";

    public static final String MESSAGE_POINT_A_SET = "axisruler.message.point_a_set";
    public static final String MESSAGE_POINT_B_SET = "axisruler.message.point_b_set";
    public static final String MESSAGE_SELECTION_CLEARED = "axisruler.message.selection_cleared";
    public static final String MESSAGE_POINTS_SWAPPED = "axisruler.message.points_swapped";
    public static final String MESSAGE_BOTH_POINTS_REQUIRED = "axisruler.message.both_points_required";
    public static final String MESSAGE_MODE = "axisruler.message.mode";
    public static final String MESSAGE_HUD = "axisruler.message.hud";
    public static final String MESSAGE_GUIDES = "axisruler.message.guides";
    public static final String MESSAGE_LABELS = "axisruler.message.labels";
    public static final String MESSAGE_LINE = "axisruler.message.line";
    public static final String MESSAGE_NO_BLOCK_TARGET = "axisruler.message.no_block_target";
    public static final String MESSAGE_MEASUREMENT_COPIED = "axisruler.message.measurement_copied";
    public static final String MESSAGE_NOTHING_TO_COPY = "axisruler.message.nothing_to_copy";
    public static final String MESSAGE_COPY_DIFFERENT_WORLDS = "axisruler.message.copy_different_worlds";
    public static final String MESSAGE_ENABLED = "axisruler.message.enabled";
    public static final String MESSAGE_DISABLED = "axisruler.message.disabled";

    public static final String HUD_TITLE = "axisruler.hud.title";
    public static final String HUD_LABEL_A = "axisruler.hud.label.a";
    public static final String HUD_LABEL_B = "axisruler.hud.label.b";
    public static final String HUD_LABEL_WARN = "axisruler.hud.label.warn";
    public static final String HUD_LABEL_SIZE = "axisruler.hud.label.size";
    public static final String HUD_LABEL_DXYZ = "axisruler.hud.label.dxyz";
    public static final String HUD_LABEL_DIST = "axisruler.hud.label.dist";
    public static final String HUD_LABEL_TAXI = "axisruler.hud.label.taxi";
    public static final String HUD_LABEL_FLOOR = "axisruler.hud.label.floor";
    public static final String HUD_LABEL_VOL = "axisruler.hud.label.vol";
    public static final String HUD_LABEL_MODE = "axisruler.hud.label.mode";
    public static final String HUD_WARNING_DIFFERENT_WORLDS = "axisruler.hud.warning.different_worlds";

    private AxisRulerText() {
    }

    public static Text pointASet(MeasurePoint point) {
        return Text.translatable(MESSAGE_POINT_A_SET, point.formatBlockPosition());
    }

    public static Text pointBSet(MeasurePoint point) {
        return Text.translatable(MESSAGE_POINT_B_SET, point.formatBlockPosition());
    }

    public static Text selectionCleared() {
        return Text.translatable(MESSAGE_SELECTION_CLEARED);
    }

    public static Text pointsSwapped() {
        return Text.translatable(MESSAGE_POINTS_SWAPPED);
    }

    public static Text bothPointsRequired() {
        return Text.translatable(MESSAGE_BOTH_POINTS_REQUIRED);
    }

    public static Text mode(SelectionMode mode) {
        return Text.translatable(MESSAGE_MODE, Text.translatable(mode.translationKey()));
    }

    public static Text hud(boolean enabled) {
        return Text.translatable(MESSAGE_HUD, enabledState(enabled));
    }

    public static Text guides(boolean enabled) {
        return Text.translatable(MESSAGE_GUIDES, enabledState(enabled));
    }

    public static Text labels(boolean enabled) {
        return Text.translatable(MESSAGE_LABELS, enabledState(enabled));
    }

    public static Text line(boolean enabled) {
        return Text.translatable(MESSAGE_LINE, enabledState(enabled));
    }

    public static Text noBlockTarget() {
        return Text.translatable(MESSAGE_NO_BLOCK_TARGET);
    }

    public static Text measurementCopied() {
        return Text.translatable(MESSAGE_MEASUREMENT_COPIED);
    }

    public static Text nothingToCopy() {
        return Text.translatable(MESSAGE_NOTHING_TO_COPY);
    }

    public static Text copyDifferentWorlds() {
        return Text.translatable(MESSAGE_COPY_DIFFERENT_WORLDS);
    }

    private static Text enabledState(boolean enabled) {
        return Text.translatable(enabled ? MESSAGE_ENABLED : MESSAGE_DISABLED);
    }
}
