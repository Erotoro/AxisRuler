package dev.erotoro.axisruler.core.measure;

import dev.erotoro.axisruler.core.config.AxisRulerConfig;
import java.util.Objects;
import java.util.Optional;

public final class SelectionState {
    private final PinnedMeasurements pinned = new PinnedMeasurements();
    private volatile MeasurePoint pointA;
    private volatile MeasurePoint pointB;
    private volatile MeasurePoint previewTarget;
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

    public PinnedMeasurements pinned() {
        return pinned;
    }

    /**
     * Transient block the crosshair currently targets while exactly Point A is set. Recomputed
     * every client tick by the input handler and consumed by the world/HUD renderers to draw the
     * live preview. Never persisted.
     */
    public MeasurePoint previewTargetOrNull() {
        return previewTarget;
    }

    public void setPreviewTarget(MeasurePoint previewTarget) {
        this.previewTarget = previewTarget;
    }

    /**
     * True when there is a meaningful live preview to draw: Point A is set, Point B is not yet,
     * and the crosshair currently targets a block in the same world as Point A.
     */
    public boolean hasLivePreview() {
        MeasurePoint a = pointA;
        MeasurePoint target = previewTarget;
        return a != null && pointB == null && target != null && a.worldKey().equals(target.worldKey());
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
