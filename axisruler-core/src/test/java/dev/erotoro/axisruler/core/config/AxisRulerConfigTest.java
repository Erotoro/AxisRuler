package dev.erotoro.axisruler.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AxisRulerConfigTest {
    @Test
    void defaultsAreSelfConsistent() {
        AxisRulerConfig config = AxisRulerConfig.defaults();

        assertEquals(0xFF43D98C, config.pointAColorArgb());
        assertEquals(HudAnchor.TOP_LEFT, config.hudAnchorEnum());
        assertEquals(LabelBackgroundMode.SUBTLE, config.labelBackgroundModeEnum());
    }

    @Test
    void shortHexColorsGainOpaqueAlpha() {
        AxisRulerConfig config = withPointAColor("43D98C");
        assertEquals("#FF43D98C", config.pointAColor());
    }

    @Test
    void invalidColorFallsBackToDefault() {
        AxisRulerConfig config = withPointAColor("not-a-color");
        assertEquals(AxisRulerConfig.DEFAULT_POINT_A_COLOR, config.pointAColor());
    }

    @Test
    void hudScaleIsClampedToRange() {
        assertEquals(2.0F, withHudScale(99.0F).hudScale(), 1.0E-6F);
        assertEquals(0.75F, withHudScale(-5.0F).hudScale(), 1.0E-6F);
    }

    @Test
    void hudAnchorResolvesPosition() {
        AxisRulerConfig config = AxisRulerConfig.defaults();
        assertEquals(8, config.resolveHudX(1920, 200));
        assertEquals(8, config.resolveHudY(1080, 100));
    }

    @Test
    void sanitizeNullReturnsDefaults() {
        assertNotNull(AxisRulerConfig.sanitize(null));
        assertTrue(AxisRulerConfig.sanitize(null).enabled());
    }

    private static AxisRulerConfig withPointAColor(String color) {
        AxisRulerConfig d = AxisRulerConfig.defaults();
        return new AxisRulerConfig(
                d.enabled(), d.hudEnabledDefault(), d.hudCompactDefault(), d.guidesEnabledDefault(), d.renderEnabledDefault(),
                d.labelsEnabledDefault(), d.fillEnabledDefault(), d.outlineEnabledDefault(), d.lineEnabledDefault(),
                d.showCenterMarkerDefault(), d.showOnlyWithTwoPointsDefault(), color, d.pointBColor(), d.boxColor(),
                d.connectionLineColor(), d.xGuideColor(), d.yGuideColor(), d.zGuideColor(), d.labelColor(), d.labelBackgroundColor(),
                d.labelBackgroundMode(), d.labelBillboard(), d.labelShowUnit(), d.hudAnchor(), d.hudOffsetX(), d.hudOffsetY(),
                d.hudScale(), d.hudTitleColor(), d.hudPrimaryTextColor(), d.hudSecondaryTextColor(), d.hudAccentColor(),
                d.hudBackgroundColor(), d.hudBorderColor(), d.hudWarningColor(), d.hudBackgroundAlpha(), d.hudBorderAlpha(),
                d.hudTextShadow(), d.labelScale(), d.lineThickness(), d.calloutOffset(), d.tickSize(), d.boxFillAlpha(),
                d.guideAlpha(), d.lineAlpha(), d.customPresetName(), d.customPointAColor(), d.customPointBColor(), d.customBoxColor(),
                d.customConnectionLineColor(), d.customXGuideColor(), d.customYGuideColor(), d.customZGuideColor(), d.customLabelColor(),
                d.customLabelBackgroundColor(), d.customLabelBackgroundMode(), d.customHudTitleColor(), d.customHudPrimaryTextColor(),
                d.customHudSecondaryTextColor(), d.customHudAccentColor(), d.customHudBackgroundColor(), d.customHudBorderColor(),
                d.customHudWarningColor(), d.customHudBackgroundAlpha(), d.customHudBorderAlpha(), d.customHudTextShadow(),
                d.customLabelScale(), d.customLineThickness(), d.customCalloutOffset(), d.customTickSize(), d.customBoxFillAlpha(),
                d.customGuideAlpha(), d.customLineAlpha()
        );
    }

    private static AxisRulerConfig withHudScale(float scale) {
        AxisRulerConfig d = AxisRulerConfig.defaults();
        return new AxisRulerConfig(
                d.enabled(), d.hudEnabledDefault(), d.hudCompactDefault(), d.guidesEnabledDefault(), d.renderEnabledDefault(),
                d.labelsEnabledDefault(), d.fillEnabledDefault(), d.outlineEnabledDefault(), d.lineEnabledDefault(),
                d.showCenterMarkerDefault(), d.showOnlyWithTwoPointsDefault(), d.pointAColor(), d.pointBColor(), d.boxColor(),
                d.connectionLineColor(), d.xGuideColor(), d.yGuideColor(), d.zGuideColor(), d.labelColor(), d.labelBackgroundColor(),
                d.labelBackgroundMode(), d.labelBillboard(), d.labelShowUnit(), d.hudAnchor(), d.hudOffsetX(), d.hudOffsetY(),
                scale, d.hudTitleColor(), d.hudPrimaryTextColor(), d.hudSecondaryTextColor(), d.hudAccentColor(),
                d.hudBackgroundColor(), d.hudBorderColor(), d.hudWarningColor(), d.hudBackgroundAlpha(), d.hudBorderAlpha(),
                d.hudTextShadow(), d.labelScale(), d.lineThickness(), d.calloutOffset(), d.tickSize(), d.boxFillAlpha(),
                d.guideAlpha(), d.lineAlpha(), d.customPresetName(), d.customPointAColor(), d.customPointBColor(), d.customBoxColor(),
                d.customConnectionLineColor(), d.customXGuideColor(), d.customYGuideColor(), d.customZGuideColor(), d.customLabelColor(),
                d.customLabelBackgroundColor(), d.customLabelBackgroundMode(), d.customHudTitleColor(), d.customHudPrimaryTextColor(),
                d.customHudSecondaryTextColor(), d.customHudAccentColor(), d.customHudBackgroundColor(), d.customHudBorderColor(),
                d.customHudWarningColor(), d.customHudBackgroundAlpha(), d.customHudBorderAlpha(), d.customHudTextShadow(),
                d.customLabelScale(), d.customLineThickness(), d.customCalloutOffset(), d.customTickSize(), d.customBoxFillAlpha(),
                d.customGuideAlpha(), d.customLineAlpha()
        );
    }
}
