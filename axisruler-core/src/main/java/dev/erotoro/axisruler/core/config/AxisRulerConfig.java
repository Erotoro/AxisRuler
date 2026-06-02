package dev.erotoro.axisruler.core.config;

import java.util.Locale;
import java.util.Objects;

public record AxisRulerConfig(
        boolean enabled,
        boolean hudEnabledDefault,
        boolean hudCompactDefault,
        boolean guidesEnabledDefault,
        boolean renderEnabledDefault,
        boolean labelsEnabledDefault,
        boolean fillEnabledDefault,
        boolean outlineEnabledDefault,
        boolean lineEnabledDefault,
        boolean showCenterMarkerDefault,
        boolean showOnlyWithTwoPointsDefault,
        String pointAColor,
        String pointBColor,
        String boxColor,
        String connectionLineColor,
        String xGuideColor,
        String yGuideColor,
        String zGuideColor,
        String labelColor,
        String labelBackgroundColor,
        String labelBackgroundMode,
        boolean labelBillboard,
        boolean labelShowUnit,
        String hudAnchor,
        int hudOffsetX,
        int hudOffsetY,
        float hudScale,
        String hudTitleColor,
        String hudPrimaryTextColor,
        String hudSecondaryTextColor,
        String hudAccentColor,
        String hudBackgroundColor,
        String hudBorderColor,
        String hudWarningColor,
        int hudBackgroundAlpha,
        int hudBorderAlpha,
        boolean hudTextShadow,
        float labelScale,
        float lineThickness,
        float calloutOffset,
        float tickSize,
        int boxFillAlpha,
        int guideAlpha,
        int lineAlpha,
        String customPresetName,
        String customPointAColor,
        String customPointBColor,
        String customBoxColor,
        String customConnectionLineColor,
        String customXGuideColor,
        String customYGuideColor,
        String customZGuideColor,
        String customLabelColor,
        String customLabelBackgroundColor,
        String customLabelBackgroundMode,
        String customHudTitleColor,
        String customHudPrimaryTextColor,
        String customHudSecondaryTextColor,
        String customHudAccentColor,
        String customHudBackgroundColor,
        String customHudBorderColor,
        String customHudWarningColor,
        int customHudBackgroundAlpha,
        int customHudBorderAlpha,
        boolean customHudTextShadow,
        float customLabelScale,
        float customLineThickness,
        float customCalloutOffset,
        float customTickSize,
        int customBoxFillAlpha,
        int customGuideAlpha,
        int customLineAlpha
) {
    public static final String DEFAULT_POINT_A_COLOR = "#FF43D98C";
    public static final String DEFAULT_POINT_B_COLOR = "#FFFF8A5B";
    public static final String DEFAULT_BOX_COLOR = "#FF8FD8F7";
    public static final String DEFAULT_CONNECTION_LINE_COLOR = "#FFDCE3EB";
    public static final String DEFAULT_X_GUIDE_COLOR = "#FFE6EEF7";
    public static final String DEFAULT_Y_GUIDE_COLOR = "#FFE6EEF7";
    public static final String DEFAULT_Z_GUIDE_COLOR = "#FFE6EEF7";
    public static final String DEFAULT_LABEL_COLOR = "#FFF7FAFC";
    public static final String DEFAULT_LABEL_BACKGROUND_COLOR = "#8A101317";
    public static final String DEFAULT_HUD_TITLE_COLOR = "#FF7BD8FF";
    public static final String DEFAULT_HUD_PRIMARY_TEXT_COLOR = "#FFE5E9EC";
    public static final String DEFAULT_HUD_SECONDARY_TEXT_COLOR = "#FF9FA7AE";
    public static final String DEFAULT_HUD_ACCENT_COLOR = "#CC7BD8FF";
    public static final String DEFAULT_HUD_BACKGROUND_COLOR = "#FF14171B";
    public static final String DEFAULT_HUD_BORDER_COLOR = "#FF272D35";
    public static final String DEFAULT_HUD_WARNING_COLOR = "#FFFFB86C";
    public static final String DEFAULT_CUSTOM_PRESET_NAME = "Custom";
    private static final float MIN_HUD_SCALE = 0.75F;
    private static final float MAX_HUD_SCALE = 2.0F;
    private static final float MIN_LABEL_SCALE = 0.01F;
    private static final float MAX_LABEL_SCALE = 0.08F;
    private static final float MIN_LINE_THICKNESS = 1.0F;
    private static final float MAX_LINE_THICKNESS = 3.5F;
    private static final float MIN_CALLOUT_OFFSET = 0.20F;
    private static final float MAX_CALLOUT_OFFSET = 1.25F;
    private static final float MIN_TICK_SIZE = 0.03F;
    private static final float MAX_TICK_SIZE = 0.22F;

    public AxisRulerConfig {
        pointAColor = sanitizeColor(pointAColor, DEFAULT_POINT_A_COLOR);
        pointBColor = sanitizeColor(pointBColor, DEFAULT_POINT_B_COLOR);
        boxColor = sanitizeColor(boxColor, DEFAULT_BOX_COLOR);
        connectionLineColor = sanitizeColor(connectionLineColor, DEFAULT_CONNECTION_LINE_COLOR);
        xGuideColor = sanitizeColor(xGuideColor, DEFAULT_X_GUIDE_COLOR);
        yGuideColor = sanitizeColor(yGuideColor, DEFAULT_Y_GUIDE_COLOR);
        zGuideColor = sanitizeColor(zGuideColor, DEFAULT_Z_GUIDE_COLOR);
        labelColor = sanitizeColor(labelColor, DEFAULT_LABEL_COLOR);
        labelBackgroundColor = sanitizeColor(labelBackgroundColor, DEFAULT_LABEL_BACKGROUND_COLOR);
        labelBackgroundMode = LabelBackgroundMode.fromName(labelBackgroundMode).configValue();
        hudAnchor = HudAnchor.fromName(hudAnchor).configValue();
        hudOffsetX = clamp(hudOffsetX, -10_000, 10_000);
        hudOffsetY = clamp(hudOffsetY, -10_000, 10_000);
        hudScale = clamp(hudScale, MIN_HUD_SCALE, MAX_HUD_SCALE);
        hudTitleColor = sanitizeColor(hudTitleColor, DEFAULT_HUD_TITLE_COLOR);
        hudPrimaryTextColor = sanitizeColor(hudPrimaryTextColor, DEFAULT_HUD_PRIMARY_TEXT_COLOR);
        hudSecondaryTextColor = sanitizeColor(hudSecondaryTextColor, DEFAULT_HUD_SECONDARY_TEXT_COLOR);
        hudAccentColor = sanitizeColor(hudAccentColor, DEFAULT_HUD_ACCENT_COLOR);
        hudBackgroundColor = sanitizeColor(hudBackgroundColor, DEFAULT_HUD_BACKGROUND_COLOR);
        hudBorderColor = sanitizeColor(hudBorderColor, DEFAULT_HUD_BORDER_COLOR);
        hudWarningColor = sanitizeColor(hudWarningColor, DEFAULT_HUD_WARNING_COLOR);
        hudBackgroundAlpha = clamp(hudBackgroundAlpha, 0, 255);
        hudBorderAlpha = clamp(hudBorderAlpha, 0, 255);
        labelScale = clamp(labelScale, MIN_LABEL_SCALE, MAX_LABEL_SCALE);
        lineThickness = clamp(lineThickness, MIN_LINE_THICKNESS, MAX_LINE_THICKNESS);
        calloutOffset = clamp(calloutOffset, MIN_CALLOUT_OFFSET, MAX_CALLOUT_OFFSET);
        tickSize = clamp(tickSize, MIN_TICK_SIZE, MAX_TICK_SIZE);
        boxFillAlpha = clamp(boxFillAlpha, 0, 255);
        guideAlpha = clamp(guideAlpha, 0, 255);
        lineAlpha = clamp(lineAlpha, 0, 255);
        customPresetName = sanitizePresetName(customPresetName);
        customPointAColor = sanitizeColor(customPointAColor, pointAColor);
        customPointBColor = sanitizeColor(customPointBColor, pointBColor);
        customBoxColor = sanitizeColor(customBoxColor, boxColor);
        customConnectionLineColor = sanitizeColor(customConnectionLineColor, connectionLineColor);
        customXGuideColor = sanitizeColor(customXGuideColor, xGuideColor);
        customYGuideColor = sanitizeColor(customYGuideColor, yGuideColor);
        customZGuideColor = sanitizeColor(customZGuideColor, zGuideColor);
        customLabelColor = sanitizeColor(customLabelColor, labelColor);
        customLabelBackgroundColor = sanitizeColor(customLabelBackgroundColor, labelBackgroundColor);
        customLabelBackgroundMode = LabelBackgroundMode.fromName(customLabelBackgroundMode).configValue();
        customHudTitleColor = sanitizeColor(customHudTitleColor, hudTitleColor);
        customHudPrimaryTextColor = sanitizeColor(customHudPrimaryTextColor, hudPrimaryTextColor);
        customHudSecondaryTextColor = sanitizeColor(customHudSecondaryTextColor, hudSecondaryTextColor);
        customHudAccentColor = sanitizeColor(customHudAccentColor, hudAccentColor);
        customHudBackgroundColor = sanitizeColor(customHudBackgroundColor, hudBackgroundColor);
        customHudBorderColor = sanitizeColor(customHudBorderColor, hudBorderColor);
        customHudWarningColor = sanitizeColor(customHudWarningColor, hudWarningColor);
        customHudBackgroundAlpha = clamp(customHudBackgroundAlpha, 0, 255);
        customHudBorderAlpha = clamp(customHudBorderAlpha, 0, 255);
        customLabelScale = clamp(customLabelScale, MIN_LABEL_SCALE, MAX_LABEL_SCALE);
        customLineThickness = clamp(customLineThickness, MIN_LINE_THICKNESS, MAX_LINE_THICKNESS);
        customCalloutOffset = clamp(customCalloutOffset, MIN_CALLOUT_OFFSET, MAX_CALLOUT_OFFSET);
        customTickSize = clamp(customTickSize, MIN_TICK_SIZE, MAX_TICK_SIZE);
        customBoxFillAlpha = clamp(customBoxFillAlpha, 0, 255);
        customGuideAlpha = clamp(customGuideAlpha, 0, 255);
        customLineAlpha = clamp(customLineAlpha, 0, 255);
    }

    public static AxisRulerConfig defaults() {
        return new AxisRulerConfig(
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                DEFAULT_POINT_A_COLOR,
                DEFAULT_POINT_B_COLOR,
                DEFAULT_BOX_COLOR,
                DEFAULT_CONNECTION_LINE_COLOR,
                DEFAULT_X_GUIDE_COLOR,
                DEFAULT_Y_GUIDE_COLOR,
                DEFAULT_Z_GUIDE_COLOR,
                DEFAULT_LABEL_COLOR,
                DEFAULT_LABEL_BACKGROUND_COLOR,
                LabelBackgroundMode.SUBTLE.configValue(),
                true,
                false,
                HudAnchor.TOP_LEFT.configValue(),
                8,
                8,
                1.0F,
                DEFAULT_HUD_TITLE_COLOR,
                DEFAULT_HUD_PRIMARY_TEXT_COLOR,
                DEFAULT_HUD_SECONDARY_TEXT_COLOR,
                DEFAULT_HUD_ACCENT_COLOR,
                DEFAULT_HUD_BACKGROUND_COLOR,
                DEFAULT_HUD_BORDER_COLOR,
                DEFAULT_HUD_WARNING_COLOR,
                176,
                128,
                true,
                0.031F,
                1.75F,
                0.44F,
                0.065F,
                20,
                215,
                192,
                DEFAULT_CUSTOM_PRESET_NAME,
                DEFAULT_POINT_A_COLOR,
                DEFAULT_POINT_B_COLOR,
                DEFAULT_BOX_COLOR,
                DEFAULT_CONNECTION_LINE_COLOR,
                DEFAULT_X_GUIDE_COLOR,
                DEFAULT_Y_GUIDE_COLOR,
                DEFAULT_Z_GUIDE_COLOR,
                DEFAULT_LABEL_COLOR,
                DEFAULT_LABEL_BACKGROUND_COLOR,
                LabelBackgroundMode.SUBTLE.configValue(),
                DEFAULT_HUD_TITLE_COLOR,
                DEFAULT_HUD_PRIMARY_TEXT_COLOR,
                DEFAULT_HUD_SECONDARY_TEXT_COLOR,
                DEFAULT_HUD_ACCENT_COLOR,
                DEFAULT_HUD_BACKGROUND_COLOR,
                DEFAULT_HUD_BORDER_COLOR,
                DEFAULT_HUD_WARNING_COLOR,
                176,
                128,
                true,
                0.031F,
                1.75F,
                0.44F,
                0.065F,
                20,
                215,
                192
        );
    }

    public static AxisRulerConfig sanitize(AxisRulerConfig config) {
        return config == null ? defaults() : new AxisRulerConfig(
                config.enabled,
                config.hudEnabledDefault,
                config.hudCompactDefault,
                config.guidesEnabledDefault,
                config.renderEnabledDefault,
                config.labelsEnabledDefault,
                config.fillEnabledDefault,
                config.outlineEnabledDefault,
                config.lineEnabledDefault,
                config.showCenterMarkerDefault,
                config.showOnlyWithTwoPointsDefault,
                config.pointAColor,
                config.pointBColor,
                config.boxColor,
                config.connectionLineColor,
                config.xGuideColor,
                config.yGuideColor,
                config.zGuideColor,
                config.labelColor,
                config.labelBackgroundColor,
                config.labelBackgroundMode,
                config.labelBillboard,
                config.labelShowUnit,
                config.hudAnchor,
                config.hudOffsetX,
                config.hudOffsetY,
                config.hudScale,
                config.hudTitleColor,
                config.hudPrimaryTextColor,
                config.hudSecondaryTextColor,
                config.hudAccentColor,
                config.hudBackgroundColor,
                config.hudBorderColor,
                config.hudWarningColor,
                config.hudBackgroundAlpha,
                config.hudBorderAlpha,
                config.hudTextShadow,
                config.labelScale,
                config.lineThickness,
                config.calloutOffset,
                config.tickSize,
                config.boxFillAlpha,
                config.guideAlpha,
                config.lineAlpha,
                config.customPresetName,
                config.customPointAColor,
                config.customPointBColor,
                config.customBoxColor,
                config.customConnectionLineColor,
                config.customXGuideColor,
                config.customYGuideColor,
                config.customZGuideColor,
                config.customLabelColor,
                config.customLabelBackgroundColor,
                config.customLabelBackgroundMode,
                config.customHudTitleColor,
                config.customHudPrimaryTextColor,
                config.customHudSecondaryTextColor,
                config.customHudAccentColor,
                config.customHudBackgroundColor,
                config.customHudBorderColor,
                config.customHudWarningColor,
                config.customHudBackgroundAlpha,
                config.customHudBorderAlpha,
                config.customHudTextShadow,
                config.customLabelScale,
                config.customLineThickness,
                config.customCalloutOffset,
                config.customTickSize,
                config.customBoxFillAlpha,
                config.customGuideAlpha,
                config.customLineAlpha
        );
    }

    public static AxisRulerConfig requireValid(AxisRulerConfig config) {
        return Objects.requireNonNull(sanitize(config), "config");
    }

    public BehaviorConfig behavior() {
        return new BehaviorConfig(
                enabled,
                hudEnabledDefault,
                hudCompactDefault,
                guidesEnabledDefault,
                renderEnabledDefault,
                labelsEnabledDefault,
                fillEnabledDefault,
                outlineEnabledDefault,
                lineEnabledDefault,
                showCenterMarkerDefault,
                showOnlyWithTwoPointsDefault
        );
    }

    public HudConfig hud() {
        return new HudConfig(
                hudAnchorEnum(),
                hudOffsetX,
                hudOffsetY,
                hudScale,
                hudCompactDefault,
                hudTitleColor,
                hudPrimaryTextColor,
                hudSecondaryTextColor,
                hudAccentColor,
                hudBackgroundColor,
                hudBorderColor,
                hudWarningColor,
                hudBackgroundAlpha,
                hudBorderAlpha,
                hudTextShadow
        );
    }

    public VisualConfig visual() {
        return new VisualConfig(
                labelScale,
                lineThickness,
                calloutOffset,
                tickSize,
                boxFillAlpha,
                guideAlpha,
                lineAlpha,
                labelBackgroundModeEnum(),
                labelBillboard,
                labelShowUnit
        );
    }

    public ColorConfig colors() {
        return new ColorConfig(
                pointAColor,
                pointBColor,
                boxColor,
                connectionLineColor,
                xGuideColor,
                yGuideColor,
                zGuideColor,
                labelColor,
                labelBackgroundColor,
                hudTitleColor,
                hudPrimaryTextColor,
                hudSecondaryTextColor,
                hudAccentColor,
                hudBackgroundColor,
                hudBorderColor,
                hudWarningColor
        );
    }

    public int pointAColorArgb() {
        return parseColor(pointAColor);
    }

    public int pointBColorArgb() {
        return parseColor(pointBColor);
    }

    public int boxColorArgb() {
        return parseColor(boxColor);
    }

    public int connectionLineColorArgb() {
        return parseColor(connectionLineColor);
    }

    public int xGuideColorArgb() {
        return parseColor(xGuideColor);
    }

    public int yGuideColorArgb() {
        return parseColor(yGuideColor);
    }

    public int zGuideColorArgb() {
        return parseColor(zGuideColor);
    }

    public int labelColorArgb() {
        return parseColor(labelColor);
    }

    public int labelBackgroundColorArgb() {
        return parseColor(labelBackgroundColor);
    }

    public int hudTitleColorArgb() {
        return parseColor(hudTitleColor);
    }

    public int hudPrimaryTextColorArgb() {
        return parseColor(hudPrimaryTextColor);
    }

    public int hudSecondaryTextColorArgb() {
        return parseColor(hudSecondaryTextColor);
    }

    public int hudAccentColorArgb() {
        return parseColor(hudAccentColor);
    }

    public int hudBackgroundColorArgb() {
        return parseColor(hudBackgroundColor);
    }

    public int hudBorderColorArgb() {
        return parseColor(hudBorderColor);
    }

    public int hudWarningColorArgb() {
        return parseColor(hudWarningColor);
    }

    public int effectiveHudBackgroundColorArgb() {
        return withAlpha(hudBackgroundColorArgb(), hudBackgroundAlpha);
    }

    public int effectiveHudBorderColorArgb() {
        return withAlpha(hudBorderColorArgb(), hudBorderAlpha);
    }

    public int effectiveHudAccentColorArgb() {
        int baseAlpha = (hudAccentColorArgb() >>> 24) & 0xFF;
        return withAlpha(hudAccentColorArgb(), Math.min(baseAlpha, hudBorderAlpha));
    }

    public int labelTextColorArgb() {
        return labelColorArgb();
    }

    public int boxFillColorArgb() {
        return withAlpha(boxColorArgb(), boxFillAlpha);
    }

    public int guideColorArgb(int color) {
        return withAlpha(color, guideAlpha);
    }

    public int lineColorArgb(int color) {
        return withAlpha(color, lineAlpha);
    }

    public HudAnchor hudAnchorEnum() {
        return HudAnchor.fromName(hudAnchor);
    }

    public LabelBackgroundMode labelBackgroundModeEnum() {
        return LabelBackgroundMode.fromName(labelBackgroundMode);
    }

    public LabelBackgroundMode customLabelBackgroundModeEnum() {
        return LabelBackgroundMode.fromName(customLabelBackgroundMode);
    }

    public int resolveHudX(int screenWidth, int panelWidth) {
        return switch (hudAnchorEnum()) {
            case TOP_LEFT, BOTTOM_LEFT -> hudOffsetX;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - panelWidth) / 2 + hudOffsetX;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - panelWidth + hudOffsetX;
        };
    }

    public int resolveHudY(int screenHeight, int panelHeight) {
        return switch (hudAnchorEnum()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> hudOffsetY;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - panelHeight + hudOffsetY;
        };
    }

    public int effectiveLabelBackgroundColorArgb() {
        int base = labelBackgroundColorArgb();
        return switch (labelBackgroundModeEnum()) {
            case NONE -> withAlpha(base, 0);
            case SUBTLE -> withAlpha(base, Math.round(((base >>> 24) & 0xFF) * 0.78F));
            case SOLID -> base;
        };
    }

    public AxisRulerConfig withSavedCustomPreset(
            String presetName,
            String pointAColor,
            String pointBColor,
            String boxColor,
            String connectionLineColor,
            String xGuideColor,
            String yGuideColor,
            String zGuideColor,
            String labelColor,
            String labelBackgroundColor,
            String labelBackgroundMode,
            String hudTitleColor,
            String hudPrimaryTextColor,
            String hudSecondaryTextColor,
            String hudAccentColor,
            String hudBackgroundColor,
            String hudBorderColor,
            String hudWarningColor,
            int hudBackgroundAlpha,
            int hudBorderAlpha,
            boolean hudTextShadow,
            float labelScale,
            float lineThickness,
            float calloutOffset,
            float tickSize,
            int boxFillAlpha,
            int guideAlpha,
            int lineAlpha
    ) {
        return new AxisRulerConfig(
                enabled,
                hudEnabledDefault,
                hudCompactDefault,
                guidesEnabledDefault,
                renderEnabledDefault,
                labelsEnabledDefault,
                fillEnabledDefault,
                outlineEnabledDefault,
                lineEnabledDefault,
                showCenterMarkerDefault,
                showOnlyWithTwoPointsDefault,
                this.pointAColor,
                this.pointBColor,
                this.boxColor,
                this.connectionLineColor,
                this.xGuideColor,
                this.yGuideColor,
                this.zGuideColor,
                this.labelColor,
                this.labelBackgroundColor,
                this.labelBackgroundMode,
                labelBillboard,
                labelShowUnit,
                hudAnchor,
                hudOffsetX,
                hudOffsetY,
                hudScale,
                this.hudTitleColor,
                this.hudPrimaryTextColor,
                this.hudSecondaryTextColor,
                this.hudAccentColor,
                this.hudBackgroundColor,
                this.hudBorderColor,
                this.hudWarningColor,
                this.hudBackgroundAlpha,
                this.hudBorderAlpha,
                this.hudTextShadow,
                this.labelScale,
                this.lineThickness,
                this.calloutOffset,
                this.tickSize,
                this.boxFillAlpha,
                this.guideAlpha,
                this.lineAlpha,
                presetName,
                pointAColor,
                pointBColor,
                boxColor,
                connectionLineColor,
                xGuideColor,
                yGuideColor,
                zGuideColor,
                labelColor,
                labelBackgroundColor,
                labelBackgroundMode,
                hudTitleColor,
                hudPrimaryTextColor,
                hudSecondaryTextColor,
                hudAccentColor,
                hudBackgroundColor,
                hudBorderColor,
                hudWarningColor,
                hudBackgroundAlpha,
                hudBorderAlpha,
                hudTextShadow,
                labelScale,
                lineThickness,
                calloutOffset,
                tickSize,
                boxFillAlpha,
                guideAlpha,
                lineAlpha
        );
    }

    public AxisRulerConfig applyStoredCustomPreset() {
        return new AxisRulerConfig(
                enabled,
                hudEnabledDefault,
                hudCompactDefault,
                guidesEnabledDefault,
                renderEnabledDefault,
                labelsEnabledDefault,
                fillEnabledDefault,
                outlineEnabledDefault,
                lineEnabledDefault,
                showCenterMarkerDefault,
                showOnlyWithTwoPointsDefault,
                customPointAColor,
                customPointBColor,
                customBoxColor,
                customConnectionLineColor,
                customXGuideColor,
                customYGuideColor,
                customZGuideColor,
                customLabelColor,
                customLabelBackgroundColor,
                customLabelBackgroundMode,
                labelBillboard,
                labelShowUnit,
                hudAnchor,
                hudOffsetX,
                hudOffsetY,
                hudScale,
                customHudTitleColor,
                customHudPrimaryTextColor,
                customHudSecondaryTextColor,
                customHudAccentColor,
                customHudBackgroundColor,
                customHudBorderColor,
                customHudWarningColor,
                customHudBackgroundAlpha,
                customHudBorderAlpha,
                customHudTextShadow,
                customLabelScale,
                customLineThickness,
                customCalloutOffset,
                customTickSize,
                customBoxFillAlpha,
                customGuideAlpha,
                customLineAlpha,
                customPresetName,
                customPointAColor,
                customPointBColor,
                customBoxColor,
                customConnectionLineColor,
                customXGuideColor,
                customYGuideColor,
                customZGuideColor,
                customLabelColor,
                customLabelBackgroundColor,
                customLabelBackgroundMode,
                customHudTitleColor,
                customHudPrimaryTextColor,
                customHudSecondaryTextColor,
                customHudAccentColor,
                customHudBackgroundColor,
                customHudBorderColor,
                customHudWarningColor,
                customHudBackgroundAlpha,
                customHudBorderAlpha,
                customHudTextShadow,
                customLabelScale,
                customLineThickness,
                customCalloutOffset,
                customTickSize,
                customBoxFillAlpha,
                customGuideAlpha,
                customLineAlpha
        );
    }

    public AxisRulerConfig withHudEnabledDefault(boolean value) {
        return new AxisRulerConfig(
                enabled, value, hudCompactDefault, guidesEnabledDefault, renderEnabledDefault, labelsEnabledDefault,
                fillEnabledDefault, outlineEnabledDefault, lineEnabledDefault, showCenterMarkerDefault, showOnlyWithTwoPointsDefault,
                pointAColor, pointBColor, boxColor, connectionLineColor, xGuideColor, yGuideColor, zGuideColor, labelColor, labelBackgroundColor,
                labelBackgroundMode, labelBillboard, labelShowUnit, hudAnchor, hudOffsetX, hudOffsetY, hudScale,
                hudTitleColor, hudPrimaryTextColor, hudSecondaryTextColor, hudAccentColor, hudBackgroundColor, hudBorderColor, hudWarningColor,
                hudBackgroundAlpha, hudBorderAlpha, hudTextShadow, labelScale, lineThickness,
                calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha, customPresetName, customPointAColor, customPointBColor,
                customBoxColor, customConnectionLineColor, customXGuideColor, customYGuideColor, customZGuideColor, customLabelColor,
                customLabelBackgroundColor, customLabelBackgroundMode, customHudTitleColor, customHudPrimaryTextColor, customHudSecondaryTextColor,
                customHudAccentColor, customHudBackgroundColor, customHudBorderColor, customHudWarningColor, customHudBackgroundAlpha,
                customHudBorderAlpha, customHudTextShadow, customLabelScale, customLineThickness, customCalloutOffset,
                customTickSize, customBoxFillAlpha, customGuideAlpha, customLineAlpha
        );
    }

    public AxisRulerConfig withGuidesEnabledDefault(boolean value) {
        return new AxisRulerConfig(
                enabled, hudEnabledDefault, hudCompactDefault, value, renderEnabledDefault, labelsEnabledDefault,
                fillEnabledDefault, outlineEnabledDefault, lineEnabledDefault, showCenterMarkerDefault, showOnlyWithTwoPointsDefault,
                pointAColor, pointBColor, boxColor, connectionLineColor, xGuideColor, yGuideColor, zGuideColor, labelColor, labelBackgroundColor,
                labelBackgroundMode, labelBillboard, labelShowUnit, hudAnchor, hudOffsetX, hudOffsetY, hudScale,
                hudTitleColor, hudPrimaryTextColor, hudSecondaryTextColor, hudAccentColor, hudBackgroundColor, hudBorderColor, hudWarningColor,
                hudBackgroundAlpha, hudBorderAlpha, hudTextShadow, labelScale, lineThickness,
                calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha, customPresetName, customPointAColor, customPointBColor,
                customBoxColor, customConnectionLineColor, customXGuideColor, customYGuideColor, customZGuideColor, customLabelColor,
                customLabelBackgroundColor, customLabelBackgroundMode, customHudTitleColor, customHudPrimaryTextColor, customHudSecondaryTextColor,
                customHudAccentColor, customHudBackgroundColor, customHudBorderColor, customHudWarningColor, customHudBackgroundAlpha,
                customHudBorderAlpha, customHudTextShadow, customLabelScale, customLineThickness, customCalloutOffset,
                customTickSize, customBoxFillAlpha, customGuideAlpha, customLineAlpha
        );
    }

    public AxisRulerConfig withLabelsEnabledDefault(boolean value) {
        return new AxisRulerConfig(
                enabled, hudEnabledDefault, hudCompactDefault, guidesEnabledDefault, renderEnabledDefault, value,
                fillEnabledDefault, outlineEnabledDefault, lineEnabledDefault, showCenterMarkerDefault, showOnlyWithTwoPointsDefault,
                pointAColor, pointBColor, boxColor, connectionLineColor, xGuideColor, yGuideColor, zGuideColor, labelColor, labelBackgroundColor,
                labelBackgroundMode, labelBillboard, labelShowUnit, hudAnchor, hudOffsetX, hudOffsetY, hudScale,
                hudTitleColor, hudPrimaryTextColor, hudSecondaryTextColor, hudAccentColor, hudBackgroundColor, hudBorderColor, hudWarningColor,
                hudBackgroundAlpha, hudBorderAlpha, hudTextShadow, labelScale, lineThickness,
                calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha, customPresetName, customPointAColor, customPointBColor,
                customBoxColor, customConnectionLineColor, customXGuideColor, customYGuideColor, customZGuideColor, customLabelColor,
                customLabelBackgroundColor, customLabelBackgroundMode, customHudTitleColor, customHudPrimaryTextColor, customHudSecondaryTextColor,
                customHudAccentColor, customHudBackgroundColor, customHudBorderColor, customHudWarningColor, customHudBackgroundAlpha,
                customHudBorderAlpha, customHudTextShadow, customLabelScale, customLineThickness, customCalloutOffset,
                customTickSize, customBoxFillAlpha, customGuideAlpha, customLineAlpha
        );
    }

    public AxisRulerConfig withLineEnabledDefault(boolean value) {
        return new AxisRulerConfig(
                enabled, hudEnabledDefault, hudCompactDefault, guidesEnabledDefault, renderEnabledDefault, labelsEnabledDefault,
                fillEnabledDefault, outlineEnabledDefault, value, showCenterMarkerDefault, showOnlyWithTwoPointsDefault,
                pointAColor, pointBColor, boxColor, connectionLineColor, xGuideColor, yGuideColor, zGuideColor, labelColor, labelBackgroundColor,
                labelBackgroundMode, labelBillboard, labelShowUnit, hudAnchor, hudOffsetX, hudOffsetY, hudScale,
                hudTitleColor, hudPrimaryTextColor, hudSecondaryTextColor, hudAccentColor, hudBackgroundColor, hudBorderColor, hudWarningColor,
                hudBackgroundAlpha, hudBorderAlpha, hudTextShadow, labelScale, lineThickness,
                calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha, customPresetName, customPointAColor, customPointBColor,
                customBoxColor, customConnectionLineColor, customXGuideColor, customYGuideColor, customZGuideColor, customLabelColor,
                customLabelBackgroundColor, customLabelBackgroundMode, customHudTitleColor, customHudPrimaryTextColor, customHudSecondaryTextColor,
                customHudAccentColor, customHudBackgroundColor, customHudBorderColor, customHudWarningColor, customHudBackgroundAlpha,
                customHudBorderAlpha, customHudTextShadow, customLabelScale, customLineThickness, customCalloutOffset,
                customTickSize, customBoxFillAlpha, customGuideAlpha, customLineAlpha
        );
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static String sanitizeColor(String color, String fallback) {
        if (color == null) {
            return fallback;
        }
        String trimmed = color.trim();
        if (trimmed.matches("#[0-9a-fA-F]{6}")) {
            return ("#FF" + trimmed.substring(1)).toUpperCase(Locale.ROOT);
        }
        if (trimmed.matches("#[0-9a-fA-F]{8}")) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        if (trimmed.matches("[0-9a-fA-F]{6}")) {
            return ("#FF" + trimmed).toUpperCase(Locale.ROOT);
        }
        if (trimmed.matches("[0-9a-fA-F]{8}")) {
            return ("#" + trimmed).toUpperCase(Locale.ROOT);
        }
        return fallback;
    }

    private static String sanitizePresetName(String name) {
        if (name == null) {
            return DEFAULT_CUSTOM_PRESET_NAME;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? DEFAULT_CUSTOM_PRESET_NAME : trimmed;
    }

    private static int parseColor(String color) {
        return (int) Long.parseLong(color.substring(1), 16);
    }
}
