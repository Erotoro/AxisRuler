package com.axisruler.render;

import com.axisruler.config.AxisRulerConfig;
import com.axisruler.config.ConfigManager;
import com.axisruler.measure.MeasurePoint;
import com.axisruler.measure.MeasurementResult;
import com.axisruler.measure.MeasurementService;
import com.axisruler.measure.SelectionMode;
import com.axisruler.measure.SelectionState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WorldOverlayRenderer {
    private static final boolean ENABLE_LABEL_SANITY_CHECK = false;
    private static final float CLEAN_OUTLINE_WIDTH = 1.20F;
    private static final float POINT_OUTLINE_WIDTH = 1.45F;
    private static final float MIN_LABEL_SCALE = 0.021F;
    private static final float MAX_LABEL_SCALE = 0.060F;
    private static final float LABEL_DISTANCE_FACTOR = 0.00105F;
    private static final float TEST_LABEL_SCALE = 0.065F;
    private static final double VERTICAL_CALLOUT_OFFSET = 0.44D;
    private static final double LOWER_CALLOUT_DROP = 0.38D;
    private static final double LOWER_CALLOUT_SIDE = 0.28D;
    private static final double CALLOUT_LINE_Y_OFFSET = 0.05D;
    private static final double CALLOUT_DEPTH_BIAS = 0.040D;
    private static final double CENTER_MARKER_RADIUS = 0.10D;
    private static final double BASE_LINE_HALF_THICKNESS = 0.0057D;
    private static final double BASE_CALLOUT_HALF_THICKNESS = 0.0038D;
    private final MeasurementService measurementService;
    private final ConfigManager configManager;
    private VoxelShape cachedSelectionShape;
    private int cachedSizeX = -1;
    private int cachedSizeY = -1;
    private int cachedSizeZ = -1;

    public WorldOverlayRenderer(MeasurementService measurementService, ConfigManager configManager) {
        this.measurementService = measurementService;
        this.configManager = configManager;
    }

    public void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(this::renderSelection);
    }

    private void renderSelection(LevelRenderContext context) {
        AxisRulerConfig config = configManager.config();
        if (!config.enabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        SelectionState state = measurementService.selectionState();
        if (!state.renderEnabled()) {
            return;
        }

        ClientLevel world = client.level;
        if (world == null) {
            return;
        }

        String currentWorldKey = world.dimension().identifier().toString();
        MeasurePoint pointA = state.pointAOrNull();
        MeasurePoint pointB = state.pointBOrNull();
        if (!shouldRenderAny(state, currentWorldKey, pointA, pointB)) {
            return;
        }

        MultiBufferSource consumers = context.bufferSource();
        PoseStack matrices = context.poseStack();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        matrices.pushPose();
        try {
            matrices.translate(
                    -cameraPos.x,
                    -cameraPos.y,
                    -cameraPos.z
            );

            if (isInCurrentWorld(currentWorldKey, pointA)) {
                RenderPrimitives.drawBlockOutline(matrices, consumers, pointA.blockPos(), config.pointAColorArgb(), POINT_OUTLINE_WIDTH);
            }
            if (isInCurrentWorld(currentWorldKey, pointB)) {
                RenderPrimitives.drawBlockOutline(matrices, consumers, pointB.blockPos(), config.pointBColorArgb(), POINT_OUTLINE_WIDTH);
            }

            if (!isSameWorldSelection(currentWorldKey, pointA, pointB)) {
                return;
            }

            MeasurementResult result = measurementService.calculate(pointA, pointB);
            if (!result.valid()) {
                return;
            }

            renderSelectionMode(context, matrices, consumers, config, state, result);
        } finally {
            matrices.popPose();
        }
    }

    private void renderSelectionMode(
            LevelRenderContext context,
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            SelectionState state,
            MeasurementResult result
    ) {
        SelectionMode mode = state.mode();
        if (mode != SelectionMode.LINE && mode != SelectionMode.MINIMAL) {
            if (state.fillEnabled()) {
                renderSelectionFill(matrices, consumers, config, result);
            }
            if (state.outlineEnabled()) {
                renderSelectionOutline(matrices, consumers, config, result);
            }
        }

        if (state.lineEnabled() && mode == SelectionMode.LINE) {
            renderLineModePath(matrices, consumers, config, result);
        }

        if (state.labelsEnabled() && mode != SelectionMode.MINIMAL) {
            renderDimensionCallouts(context, matrices, consumers, config, result);
        }

        if (state.showCenterMarker()) {
            renderCenterMarker(matrices, consumers, config, result);
        }
    }

    private boolean shouldRenderAny(SelectionState state, String currentWorldKey, MeasurePoint pointA, MeasurePoint pointB) {
        boolean hasVisiblePoint = isInCurrentWorld(currentWorldKey, pointA) || isInCurrentWorld(currentWorldKey, pointB);
        if (!hasVisiblePoint) {
            return false;
        }
        return !state.showOnlyWithTwoPoints() || isSameWorldSelection(currentWorldKey, pointA, pointB);
    }

    private boolean isInCurrentWorld(String currentWorldKey, MeasurePoint point) {
        return point != null && point.worldKey().equals(currentWorldKey);
    }

    private boolean isSameWorldSelection(String currentWorldKey, MeasurePoint pointA, MeasurePoint pointB) {
        return pointA != null
                && pointB != null
                && pointA.worldKey().equals(currentWorldKey)
                && pointA.worldKey().equals(pointB.worldKey());
    }

    private void renderSelectionFill(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        double minX = result.minBlockPos().getX();
        double minY = result.minBlockPos().getY();
        double minZ = result.minBlockPos().getZ();
        double maxX = result.maxBlockPos().getX() + 1.0D;
        double maxY = result.maxBlockPos().getY() + 1.0D;
        double maxZ = result.maxBlockPos().getZ() + 1.0D;
        RenderPrimitives.drawFilledBox(
                matrices,
                consumers,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                config.boxFillColorArgb()
        );
    }

    private void renderSelectionOutline(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        VoxelShape shape = selectionShape(result.sizeX(), result.sizeY(), result.sizeZ());
        RenderPrimitives.drawShapeOutline(
                matrices,
                consumers,
                shape,
                result.minBlockPos().getX(),
                result.minBlockPos().getY(),
                result.minBlockPos().getZ(),
                config.boxColorArgb(),
                CLEAN_OUTLINE_WIDTH
        );
    }

    private void renderLineModePath(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        MeasurePoint pointA = result.pointAOrNull();
        MeasurePoint pointB = result.pointBOrNull();
        if (pointA == null || pointB == null) {
            return;
        }

        Vec3 start = blockCenter(pointA.blockPos());
        Vec3 xTurn = new Vec3(pointB.x() + 0.5D, pointA.y() + 0.5D, pointA.z() + 0.5D);
        Vec3 yTurn = new Vec3(pointB.x() + 0.5D, pointB.y() + 0.5D, pointA.z() + 0.5D);
        Vec3 end = blockCenter(pointB.blockPos());
        int color = config.lineColorArgb(config.connectionLineColorArgb());

        drawThinAxisSegment(matrices, consumers, config, start, xTurn, color);
        drawThinAxisSegment(matrices, consumers, config, xTurn, yTurn, color);
        drawThinAxisSegment(matrices, consumers, config, yTurn, end, color);
    }

    private void renderCenterMarker(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        Vec3 center = result.center();
        RenderPrimitives.drawFilledBox(
                matrices,
                consumers,
                center.x - CENTER_MARKER_RADIUS,
                center.y - CENTER_MARKER_RADIUS,
                center.z - CENTER_MARKER_RADIUS,
                center.x + CENTER_MARKER_RADIUS,
                center.y + CENTER_MARKER_RADIUS,
                center.z + CENTER_MARKER_RADIUS,
                withAlpha(config.boxColorArgb(), Math.min(120, config.boxFillAlpha() + 32))
        );
    }

    private void renderDimensionCallouts(
            LevelRenderContext context,
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.font == null) {
            return;
        }

        Font textRenderer = client.font;
        CalloutLayout layout = calloutLayout(result, Minecraft.getInstance().gameRenderer.getMainCamera().position(), config);

        renderCallout(matrices, consumers, textRenderer, context, layout.x(), Integer.toString(result.sizeX()), config.guideColorArgb(config.xGuideColorArgb()), config);
        renderCallout(matrices, consumers, textRenderer, context, layout.y(), Integer.toString(result.sizeY()), config.guideColorArgb(config.yGuideColorArgb()), config);
        renderCallout(matrices, consumers, textRenderer, context, layout.z(), Integer.toString(result.sizeZ()), config.guideColorArgb(config.zGuideColorArgb()), config);

        if (ENABLE_LABEL_SANITY_CHECK) {
            RenderPrimitives.drawWorldLabel(
                    matrices,
                    consumers,
                    textRenderer,
                    Minecraft.getInstance().gameRenderer.getMainCamera(),
                    "TEST",
                    result.center().add(0.0D, 1.1D, 0.0D),
                    TEST_LABEL_SCALE,
                    0xFFFFFFFF,
                    config.labelBackgroundModeEnum(),
                    config.effectiveLabelBackgroundColorArgb(),
                    config.labelBillboard()
            );
        }
    }

    private void renderCallout(
            PoseStack matrices,
            MultiBufferSource consumers,
            Font textRenderer,
            LevelRenderContext context,
            DimensionCallout callout,
            String label,
            int lineColor,
            AxisRulerConfig config
    ) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 lineStart = depthBiased(callout.lineStart(), cameraPos);
        Vec3 lineEnd = depthBiased(callout.lineEnd(), cameraPos);
        Vec3 anchorStart = depthBiased(callout.anchorStart(), cameraPos);
        Vec3 anchorEnd = depthBiased(callout.anchorEnd(), cameraPos);
        Vec3 anchor2Start = depthBiased(callout.anchor2Start(), cameraPos);
        Vec3 anchor2End = depthBiased(callout.anchor2End(), cameraPos);
        Vec3 tickStartA = depthBiased(callout.tickStartA(), cameraPos);
        Vec3 tickStartB = depthBiased(callout.tickStartB(), cameraPos);
        Vec3 tickEndA = depthBiased(callout.tickEndA(), cameraPos);
        Vec3 tickEndB = depthBiased(callout.tickEndB(), cameraPos);
        Vec3 labelPosition = depthBiased(callout.labelPosition(), cameraPos);

        drawCalloutSegment(matrices, consumers, config, lineStart, lineEnd, lineColor);
        drawCalloutSegment(matrices, consumers, config, anchorStart, anchorEnd, lineColor);
        drawCalloutSegment(matrices, consumers, config, anchor2Start, anchor2End, lineColor);
        drawCalloutSegment(matrices, consumers, config, tickStartA, tickStartB, lineColor);
        drawCalloutSegment(matrices, consumers, config, tickEndA, tickEndB, lineColor);
        float labelScale = distanceScaledLabelSize(config, cameraPos, labelPosition);

        RenderPrimitives.drawWorldLabel(
                matrices,
                consumers,
                textRenderer,
                Minecraft.getInstance().gameRenderer.getMainCamera(),
                label,
                labelPosition,
                labelScale,
                config.labelTextColorArgb(),
                config.labelBackgroundModeEnum(),
                config.effectiveLabelBackgroundColorArgb(),
                config.labelBillboard()
        );
    }

    private CalloutLayout calloutLayout(MeasurementResult result, Vec3 cameraPos, AxisRulerConfig config) {
        double minX = result.minBlockPos().getX();
        double minY = result.minBlockPos().getY();
        double minZ = result.minBlockPos().getZ();
        double maxX = result.maxBlockPos().getX() + 1.0D;
        double maxY = result.maxBlockPos().getY() + 1.0D;
        double maxZ = result.maxBlockPos().getZ() + 1.0D;
        double centerX = (minX + maxX) * 0.5D;
        double centerY = (minY + maxY) * 0.5D;
        double centerZ = (minZ + maxZ) * 0.5D;
        boolean nearWest = cameraPos.x < centerX;
        boolean nearNorth = cameraPos.z < centerZ;
        double horizontalOffset = config.calloutOffset();
        double tickHalfLength = config.tickSize();

        double topGuideZ = nearNorth ? minZ - horizontalOffset : maxZ + horizontalOffset;
        double sideGuideX = nearWest ? minX - Math.max(VERTICAL_CALLOUT_OFFSET, horizontalOffset) : maxX + Math.max(VERTICAL_CALLOUT_OFFSET, horizontalOffset);
        double lowerGuideX = nearWest ? minX - Math.max(LOWER_CALLOUT_SIDE, horizontalOffset * 0.65D) : maxX + Math.max(LOWER_CALLOUT_SIDE, horizontalOffset * 0.65D);
        double lowerGuideY = minY - Math.max(LOWER_CALLOUT_DROP, horizontalOffset * 0.86D);

        DimensionCallout xCallout = new DimensionCallout(
                new Vec3(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3(minX, maxY, nearNorth ? minZ : maxZ),
                new Vec3(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3(maxX, maxY, nearNorth ? minZ : maxZ),
                new Vec3(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ - tickHalfLength),
                new Vec3(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ + tickHalfLength),
                new Vec3(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ - tickHalfLength),
                new Vec3(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ + tickHalfLength),
                new Vec3(centerX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ)
        );

        DimensionCallout yCallout = new DimensionCallout(
                new Vec3(sideGuideX, minY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX, maxY, nearNorth ? minZ : maxZ),
                new Vec3(nearWest ? minX : maxX, minY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX, minY, nearNorth ? minZ : maxZ),
                new Vec3(nearWest ? minX : maxX, maxY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX, maxY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX - tickHalfLength, minY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX + tickHalfLength, minY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX - tickHalfLength, maxY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX + tickHalfLength, maxY, nearNorth ? minZ : maxZ),
                new Vec3(sideGuideX, centerY, nearNorth ? minZ : maxZ)
        );

        DimensionCallout zCallout = new DimensionCallout(
                new Vec3(lowerGuideX, lowerGuideY, minZ),
                new Vec3(lowerGuideX, lowerGuideY, maxZ),
                new Vec3(nearWest ? minX : maxX, minY, minZ),
                new Vec3(lowerGuideX, lowerGuideY, minZ),
                new Vec3(nearWest ? minX : maxX, minY, maxZ),
                new Vec3(lowerGuideX, lowerGuideY, maxZ),
                new Vec3(lowerGuideX, lowerGuideY - tickHalfLength, minZ),
                new Vec3(lowerGuideX, lowerGuideY + tickHalfLength, minZ),
                new Vec3(lowerGuideX, lowerGuideY - tickHalfLength, maxZ),
                new Vec3(lowerGuideX, lowerGuideY + tickHalfLength, maxZ),
                new Vec3(lowerGuideX, lowerGuideY, centerZ)
        );

        return new CalloutLayout(xCallout, yCallout, zCallout);
    }

    private void drawThinAxisSegment(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            Vec3 start,
            Vec3 end,
            int color
    ) {
        double halfThickness = lineHalfThickness(config);
        double minX = Math.min(start.x, end.x) - halfThickness;
        double minY = Math.min(start.y, end.y) - halfThickness;
        double minZ = Math.min(start.z, end.z) - halfThickness;
        double maxX = Math.max(start.x, end.x) + halfThickness;
        double maxY = Math.max(start.y, end.y) + halfThickness;
        double maxZ = Math.max(start.z, end.z) + halfThickness;
        RenderPrimitives.drawFilledBox(matrices, consumers, minX, minY, minZ, maxX, maxY, maxZ, color);
    }

    private void drawCalloutSegment(
            PoseStack matrices,
            MultiBufferSource consumers,
            AxisRulerConfig config,
            Vec3 start,
            Vec3 end,
            int color
    ) {
        double halfThickness = calloutHalfThickness(config);
        double minX = Math.min(start.x, end.x) - halfThickness;
        double minY = Math.min(start.y, end.y) - halfThickness;
        double minZ = Math.min(start.z, end.z) - halfThickness;
        double maxX = Math.max(start.x, end.x) + halfThickness;
        double maxY = Math.max(start.y, end.y) + halfThickness;
        double maxZ = Math.max(start.z, end.z) + halfThickness;
        RenderPrimitives.drawFilledBox(matrices, consumers, minX, minY, minZ, maxX, maxY, maxZ, color);
    }

    private float distanceScaledLabelSize(AxisRulerConfig config, Vec3 cameraPos, Vec3 labelPos) {
        double distance = Math.sqrt(cameraPos.distanceToSqr(labelPos));
        float baseScale = config.labelScale();
        float scaled = baseScale + (float) distance * LABEL_DISTANCE_FACTOR;
        return Math.max(Math.max(MIN_LABEL_SCALE, baseScale * 0.82F), Math.min(MAX_LABEL_SCALE, scaled));
    }

    private Vec3 depthBiased(Vec3 point, Vec3 cameraPos) {
        Vec3 toCamera = cameraPos.subtract(point);
        double lengthSquared = toCamera.lengthSqr();
        if (lengthSquared < 1.0E-6D) {
            return point;
        }
        return point.add(toCamera.scale(CALLOUT_DEPTH_BIAS / Math.sqrt(lengthSquared)));
    }

    private double lineHalfThickness(AxisRulerConfig config) {
        return BASE_LINE_HALF_THICKNESS * config.lineThickness();
    }

    private double calloutHalfThickness(AxisRulerConfig config) {
        return BASE_CALLOUT_HALF_THICKNESS * config.lineThickness();
    }

    private Vec3 blockCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private VoxelShape selectionShape(int sizeX, int sizeY, int sizeZ) {
        if (cachedSelectionShape == null || cachedSizeX != sizeX || cachedSizeY != sizeY || cachedSizeZ != sizeZ) {
            cachedSelectionShape = Shapes.box(0.0D, 0.0D, 0.0D, sizeX, sizeY, sizeZ);
            cachedSizeX = sizeX;
            cachedSizeY = sizeY;
            cachedSizeZ = sizeZ;
        }
        return cachedSelectionShape;
    }

    private record DimensionCallout(
            Vec3 lineStart,
            Vec3 lineEnd,
            Vec3 anchorStart,
            Vec3 anchorEnd,
            Vec3 anchor2Start,
            Vec3 anchor2End,
            Vec3 tickStartA,
            Vec3 tickStartB,
            Vec3 tickEndA,
            Vec3 tickEndB,
            Vec3 labelPosition
    ) {
    }

    private record CalloutLayout(
            DimensionCallout x,
            DimensionCallout y,
            DimensionCallout z
    ) {
    }
}
