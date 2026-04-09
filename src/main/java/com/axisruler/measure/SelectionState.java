package com.axisruler.measure;

import com.axisruler.config.AxisRulerConfig;
import java.util.Objects;
import java.util.Optional;

public final class SelectionState {
    private volatile MeasurePoint pointA;
    private volatile MeasurePoint pointB;
    private volatile String currentWorldKey;
    private volatile SelectionMode mode = SelectionMode.BOX;
    private volatile boolean hudEnabled = true;
    private volatile boolean guidesEnabled = true;
    private volatile boolean renderEnabled = true;
    private volatile boolean lineEnabled = true;
    private volatile boolean labelsEnabled = true;
    private volatile boolean fillEnabled = true;
    private volatile boolean outlineEnabled = true;
    private volatile boolean showCenterMarker;
    private volatile boolean showOnlyWithTwoPoints;

    public Optional<MeasurePoint> pointA() {
        return Optional.ofNullable(pointA);
    }

    public Optional<MeasurePoint> pointB() {
        return Optional.ofNullable(pointB);
    }

    public MeasurePoint pointAOrNull() {
        return pointA;
    }

    public MeasurePoint pointBOrNull() {
        return pointB;
    }

    public Optional<String> currentWorldKey() {
        return Optional.ofNullable(currentWorldKey);
    }

    public String currentWorldKeyOrNull() {
        return currentWorldKey;
    }

    public SelectionMode mode() {
        return mode;
    }

    public boolean hudEnabled() {
        return hudEnabled;
    }

    public boolean guidesEnabled() {
        return guidesEnabled;
    }

    public boolean renderEnabled() {
        return renderEnabled;
    }

    public boolean lineEnabled() {
        return lineEnabled;
    }

    public boolean labelsEnabled() {
        return labelsEnabled;
    }

    public boolean fillEnabled() {
        return fillEnabled;
    }

    public boolean outlineEnabled() {
        return outlineEnabled;
    }

    public boolean showCenterMarker() {
        return showCenterMarker;
    }

    public boolean showOnlyWithTwoPoints() {
        return showOnlyWithTwoPoints;
    }

    public boolean complete() {
        return pointA != null && pointB != null;
    }

    public void applyConfigDefaults(AxisRulerConfig config) {
        Objects.requireNonNull(config, "config");
        hudEnabled = config.hudEnabledDefault();
        guidesEnabled = config.guidesEnabledDefault();
        renderEnabled = config.renderEnabledDefault();
        lineEnabled = config.lineEnabledDefault();
        labelsEnabled = config.labelsEnabledDefault();
        fillEnabled = config.fillEnabledDefault();
        outlineEnabled = config.outlineEnabledDefault();
        showCenterMarker = config.showCenterMarkerDefault();
        showOnlyWithTwoPoints = config.showOnlyWithTwoPointsDefault();
    }

    public void setPointA(MeasurePoint pointA) {
        this.pointA = Objects.requireNonNull(pointA, "pointA");
    }

    public void setPointB(MeasurePoint pointB) {
        this.pointB = Objects.requireNonNull(pointB, "pointB");
    }

    public void setCurrentWorldKey(String currentWorldKey) {
        if (currentWorldKey == null || currentWorldKey.isBlank()) {
            this.currentWorldKey = null;
            return;
        }
        this.currentWorldKey = currentWorldKey;
    }

    public void clearPointA() {
        pointA = null;
    }

    public void clearPointB() {
        pointB = null;
    }

    public void clearPoints() {
        pointA = null;
        pointB = null;
    }

    public boolean swapPoints() {
        if (pointA == null || pointB == null) {
            return false;
        }
        MeasurePoint previousPointA = pointA;
        pointA = pointB;
        pointB = previousPointA;
        return true;
    }

    public void setMode(SelectionMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public void cycleMode() {
        mode = mode.next();
    }

    public void setHudEnabled(boolean hudEnabled) {
        this.hudEnabled = hudEnabled;
    }

    public void setGuidesEnabled(boolean guidesEnabled) {
        this.guidesEnabled = guidesEnabled;
    }

    public void setRenderEnabled(boolean renderEnabled) {
        this.renderEnabled = renderEnabled;
    }

    public void setLineEnabled(boolean lineEnabled) {
        this.lineEnabled = lineEnabled;
    }

    public void setLabelsEnabled(boolean labelsEnabled) {
        this.labelsEnabled = labelsEnabled;
    }

    public void toggleHud() {
        hudEnabled = !hudEnabled;
    }

    public void toggleGuides() {
        guidesEnabled = !guidesEnabled;
    }

    public void toggleRender() {
        renderEnabled = !renderEnabled;
    }

    public void toggleLine() {
        lineEnabled = !lineEnabled;
    }

    public void toggleLabels() {
        labelsEnabled = !labelsEnabled;
    }
}
