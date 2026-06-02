package dev.erotoro.axisruler.core.config;

public record VisualConfig(
        float labelScale,
        float lineThickness,
        float calloutOffset,
        float tickSize,
        int boxFillAlpha,
        int guideAlpha,
        int lineAlpha,
        LabelBackgroundMode labelBackgroundMode,
        boolean labelBillboard,
        boolean labelShowUnit
) {
}
