package com.axisruler.config;

public record BehaviorConfig(
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
        boolean showOnlyWithTwoPointsDefault
) {
}
