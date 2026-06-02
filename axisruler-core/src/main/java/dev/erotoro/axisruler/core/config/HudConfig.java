package dev.erotoro.axisruler.core.config;

public record HudConfig(
        HudAnchor anchor,
        int offsetX,
        int offsetY,
        float scale,
        boolean compact,
        String titleColor,
        String primaryTextColor,
        String secondaryTextColor,
        String accentColor,
        String backgroundColor,
        String borderColor,
        String warningColor,
        int backgroundAlpha,
        int borderAlpha,
        boolean textShadow
) {
}
