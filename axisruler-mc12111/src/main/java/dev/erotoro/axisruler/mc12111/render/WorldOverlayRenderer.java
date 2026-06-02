package dev.erotoro.axisruler.mc12111.render;

import dev.erotoro.axisruler.core.config.AxisRulerConfig;
import dev.erotoro.axisruler.core.config.ConfigManager;
import dev.erotoro.axisruler.core.geometry.BlockPoint;
import dev.erotoro.axisruler.core.measure.MeasurePoint;
import dev.erotoro.axisruler.core.measure.MeasurementResult;
import dev.erotoro.axisruler.core.measure.Pinned;
import dev.erotoro.axisruler.core.measure.MeasurementService;
import dev.erotoro.axisruler.core.measure.SelectionMode;
import dev.erotoro.axisruler.core.measure.SelectionState;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

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
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(this::renderSelection);
    }

    private void renderSelection(WorldRenderContext context) {
        AxisRulerConfig config = configManager.config();
        if (!config.enabled()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        SelectionState state = measurementService.selectionState();
        if (!state.renderEnabled()) {
            return;
        }

        ClientWorld world = client.world;
        if (world == null) {
            return;
        }

        String currentWorldKey = world.getRegistryKey().getValue().toString();
        MeasurePoint pointA = state.pointAOrNull();
        MeasurePoint pointB = state.pointBOrNull();
        MeasurePoint previewTarget = state.previewTargetOrNull();

        boolean hasLive = shouldRenderAny(state, currentWorldKey, pointA, pointB);
        boolean hasPinned = state.pinned().countForWorld(currentWorldKey) > 0;
        boolean hasPreview = state.hasLivePreview() && isInCurrentWorld(currentWorldKey, previewTarget);
        if (!hasLive && !hasPinned && !hasPreview) {
            return;
        }

        VertexConsumerProvider consumers = context.consumers();
        MatrixStack matrices = context.matrices();
        matrices.push();
        try {
            matrices.translate(
                    -context.worldState().cameraRenderState.pos.x,
                    -context.worldState().cameraRenderState.pos.y,
                    -context.worldState().cameraRenderState.pos.z
            );

            if (hasPinned) {
                renderPinned(context, matrices, consumers, config, state, currentWorldKey);
            }

            if (hasPreview) {
                renderPreview(context, matrices, consumers, config, state, pointA, previewTarget);
            }

            if (isInCurrentWorld(currentWorldKey, pointA)) {
                RenderPrimitives.drawBlockOutline(matrices, consumers, toBlockPos(pointA.blockPos()), config.pointAColorArgb(), POINT_OUTLINE_WIDTH);
            }
            if (isInCurrentWorld(currentWorldKey, pointB)) {
                RenderPrimitives.drawBlockOutline(matrices, consumers, toBlockPos(pointB.blockPos()), config.pointBColorArgb(), POINT_OUTLINE_WIDTH);
            }

            if (isSameWorldSelection(currentWorldKey, pointA, pointB)) {
                MeasurementResult result = measurementService.calculate(pointA, pointB);
                if (result.valid()) {
                    renderSelectionMode(context, matrices, consumers, config, state, result);
                }
            }
        } finally {
            matrices.pop();
        }
    }

    private void renderPinned(
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            SelectionState state,
            String currentWorldKey
    ) {
        for (Pinned entry : state.pinned().listForWorld(currentWorldKey)) {
            MeasurementResult result = measurementService.calculate(entry.a(), entry.b());
            if (result.valid()) {
                renderStaticBox(context, matrices, consumers, config, result, entry.colorArgb(), 1.0F, state.labelsEnabled());
            }
        }
    }

    private void renderPreview(
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            SelectionState state,
            MeasurePoint pointA,
            MeasurePoint previewTarget
    ) {
        RenderPrimitives.drawBlockOutline(matrices, consumers, toBlockPos(previewTarget.blockPos()), withAlpha(config.pointBColorArgb(), 120), POINT_OUTLINE_WIDTH);
        MeasurementResult result = measurementService.calculate(pointA, previewTarget);
        if (result.valid()) {
            renderStaticBox(context, matrices, consumers, config, result, withAlpha(config.boxColorArgb(), 130), 0.5F, state.labelsEnabled());
        }
    }

    private void renderStaticBox(
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result,
            int boxColorArgb,
            float labelAlphaScale,
            boolean showLabel
    ) {
        VoxelShape shape = VoxelShapes.cuboid(0.0D, 0.0D, 0.0D, result.sizeX(), result.sizeY(), result.sizeZ());
        RenderPrimitives.drawShapeOutline(
                matrices,
                consumers,
                shape,
                result.minBlockPos().x(),
                result.minBlockPos().y(),
                result.minBlockPos().z(),
                boxColorArgb,
                CLEAN_OUTLINE_WIDTH
        );
        if (!showLabel) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        Vec3d labelPos = depthBiased(toVec3d(result.center()), cameraPos);
        String text = result.sizeX() + "x" + result.sizeY() + "x" + result.sizeZ();
        RenderPrimitives.drawWorldLabel(
                matrices,
                consumers,
                client.textRenderer,
                context.worldState().cameraRenderState,
                text,
                labelPos,
                distanceScaledLabelSize(config, cameraPos, labelPos),
                scaleAlpha(config.labelTextColorArgb(), labelAlphaScale),
                config.labelBackgroundModeEnum(),
                scaleAlpha(config.effectiveLabelBackgroundColorArgb(), labelAlphaScale),
                config.labelBillboard()
        );
    }

    private int scaleAlpha(int color, float scale) {
        int alpha = Math.round(((color >>> 24) & 0xFF) * scale);
        return withAlpha(color, Math.max(0, Math.min(255, alpha)));
    }

    private void renderSelectionMode(
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
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
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        double minX = result.minBlockPos().x();
        double minY = result.minBlockPos().y();
        double minZ = result.minBlockPos().z();
        double maxX = result.maxBlockPos().x() + 1.0D;
        double maxY = result.maxBlockPos().y() + 1.0D;
        double maxZ = result.maxBlockPos().z() + 1.0D;
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
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        VoxelShape shape = selectionShape(result.sizeX(), result.sizeY(), result.sizeZ());
        RenderPrimitives.drawShapeOutline(
                matrices,
                consumers,
                shape,
                result.minBlockPos().x(),
                result.minBlockPos().y(),
                result.minBlockPos().z(),
                config.boxColorArgb(),
                CLEAN_OUTLINE_WIDTH
        );
    }

    private void renderLineModePath(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        MeasurePoint pointA = result.pointAOrNull();
        MeasurePoint pointB = result.pointBOrNull();
        if (pointA == null || pointB == null) {
            return;
        }

        Vec3d start = blockCenter(pointA.blockPos());
        Vec3d xTurn = new Vec3d(pointB.x() + 0.5D, pointA.y() + 0.5D, pointA.z() + 0.5D);
        Vec3d yTurn = new Vec3d(pointB.x() + 0.5D, pointB.y() + 0.5D, pointA.z() + 0.5D);
        Vec3d end = blockCenter(pointB.blockPos());
        int color = config.lineColorArgb(config.connectionLineColorArgb());

        drawThinAxisSegment(matrices, consumers, config, start, xTurn, color);
        drawThinAxisSegment(matrices, consumers, config, xTurn, yTurn, color);
        drawThinAxisSegment(matrices, consumers, config, yTurn, end, color);
    }

    private void renderCenterMarker(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        Vec3d center = toVec3d(result.center());
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
            WorldRenderContext context,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            MeasurementResult result
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        CalloutLayout layout = calloutLayout(result, context.worldState().cameraRenderState.pos, config);

        renderCallout(matrices, consumers, textRenderer, context, layout.x(), Integer.toString(result.sizeX()), config.guideColorArgb(config.xGuideColorArgb()), config);
        renderCallout(matrices, consumers, textRenderer, context, layout.y(), Integer.toString(result.sizeY()), config.guideColorArgb(config.yGuideColorArgb()), config);
        renderCallout(matrices, consumers, textRenderer, context, layout.z(), Integer.toString(result.sizeZ()), config.guideColorArgb(config.zGuideColorArgb()), config);

        if (ENABLE_LABEL_SANITY_CHECK) {
            RenderPrimitives.drawWorldLabel(
                    matrices,
                    consumers,
                    textRenderer,
                    context.worldState().cameraRenderState,
                    "TEST",
                    toVec3d(result.center()).add(0.0D, 1.1D, 0.0D),
                    TEST_LABEL_SCALE,
                    0xFFFFFFFF,
                    config.labelBackgroundModeEnum(),
                    config.effectiveLabelBackgroundColorArgb(),
                    config.labelBillboard()
            );
        }
    }

    private void renderCallout(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            TextRenderer textRenderer,
            WorldRenderContext context,
            DimensionCallout callout,
            String label,
            int lineColor,
            AxisRulerConfig config
    ) {
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        Vec3d lineStart = depthBiased(callout.lineStart(), cameraPos);
        Vec3d lineEnd = depthBiased(callout.lineEnd(), cameraPos);
        Vec3d anchorStart = depthBiased(callout.anchorStart(), cameraPos);
        Vec3d anchorEnd = depthBiased(callout.anchorEnd(), cameraPos);
        Vec3d anchor2Start = depthBiased(callout.anchor2Start(), cameraPos);
        Vec3d anchor2End = depthBiased(callout.anchor2End(), cameraPos);
        Vec3d tickStartA = depthBiased(callout.tickStartA(), cameraPos);
        Vec3d tickStartB = depthBiased(callout.tickStartB(), cameraPos);
        Vec3d tickEndA = depthBiased(callout.tickEndA(), cameraPos);
        Vec3d tickEndB = depthBiased(callout.tickEndB(), cameraPos);
        Vec3d labelPosition = depthBiased(callout.labelPosition(), cameraPos);

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
                context.worldState().cameraRenderState,
                label,
                labelPosition,
                labelScale,
                config.labelTextColorArgb(),
                config.labelBackgroundModeEnum(),
                config.effectiveLabelBackgroundColorArgb(),
                config.labelBillboard()
        );
    }

    private CalloutLayout calloutLayout(MeasurementResult result, Vec3d cameraPos, AxisRulerConfig config) {
        double minX = result.minBlockPos().x();
        double minY = result.minBlockPos().y();
        double minZ = result.minBlockPos().z();
        double maxX = result.maxBlockPos().x() + 1.0D;
        double maxY = result.maxBlockPos().y() + 1.0D;
        double maxZ = result.maxBlockPos().z() + 1.0D;
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
                new Vec3d(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3d(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3d(minX, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3d(maxX, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ),
                new Vec3d(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ - tickHalfLength),
                new Vec3d(minX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ + tickHalfLength),
                new Vec3d(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ - tickHalfLength),
                new Vec3d(maxX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ + tickHalfLength),
                new Vec3d(centerX, maxY + CALLOUT_LINE_Y_OFFSET, topGuideZ)
        );

        DimensionCallout yCallout = new DimensionCallout(
                new Vec3d(sideGuideX, minY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(nearWest ? minX : maxX, minY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX, minY, nearNorth ? minZ : maxZ),
                new Vec3d(nearWest ? minX : maxX, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX - tickHalfLength, minY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX + tickHalfLength, minY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX - tickHalfLength, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX + tickHalfLength, maxY, nearNorth ? minZ : maxZ),
                new Vec3d(sideGuideX, centerY, nearNorth ? minZ : maxZ)
        );

        DimensionCallout zCallout = new DimensionCallout(
                new Vec3d(lowerGuideX, lowerGuideY, minZ),
                new Vec3d(lowerGuideX, lowerGuideY, maxZ),
                new Vec3d(nearWest ? minX : maxX, minY, minZ),
                new Vec3d(lowerGuideX, lowerGuideY, minZ),
                new Vec3d(nearWest ? minX : maxX, minY, maxZ),
                new Vec3d(lowerGuideX, lowerGuideY, maxZ),
                new Vec3d(lowerGuideX, lowerGuideY - tickHalfLength, minZ),
                new Vec3d(lowerGuideX, lowerGuideY + tickHalfLength, minZ),
                new Vec3d(lowerGuideX, lowerGuideY - tickHalfLength, maxZ),
                new Vec3d(lowerGuideX, lowerGuideY + tickHalfLength, maxZ),
                new Vec3d(lowerGuideX, lowerGuideY, centerZ)
        );

        return new CalloutLayout(xCallout, yCallout, zCallout);
    }

    private void drawThinAxisSegment(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            Vec3d start,
            Vec3d end,
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
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            AxisRulerConfig config,
            Vec3d start,
            Vec3d end,
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

    private float distanceScaledLabelSize(AxisRulerConfig config, Vec3d cameraPos, Vec3d labelPos) {
        double distance = Math.sqrt(cameraPos.squaredDistanceTo(labelPos));
        float baseScale = config.labelScale();
        float scaled = baseScale + (float) distance * LABEL_DISTANCE_FACTOR;
        return Math.max(Math.max(MIN_LABEL_SCALE, baseScale * 0.82F), Math.min(MAX_LABEL_SCALE, scaled));
    }

    private Vec3d depthBiased(Vec3d point, Vec3d cameraPos) {
        Vec3d toCamera = cameraPos.subtract(point);
        double lengthSquared = toCamera.lengthSquared();
        if (lengthSquared < 1.0E-6D) {
            return point;
        }
        return point.add(toCamera.multiply(CALLOUT_DEPTH_BIAS / Math.sqrt(lengthSquared)));
    }

    private double lineHalfThickness(AxisRulerConfig config) {
        return BASE_LINE_HALF_THICKNESS * config.lineThickness();
    }

    private double calloutHalfThickness(AxisRulerConfig config) {
        return BASE_CALLOUT_HALF_THICKNESS * config.lineThickness();
    }

    private Vec3d blockCenter(BlockPoint pos) {
        return new Vec3d(pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D);
    }

    private Vec3d toVec3d(dev.erotoro.axisruler.core.geometry.Vec3d vec) {
        return new Vec3d(vec.x(), vec.y(), vec.z());
    }

    private BlockPos toBlockPos(BlockPoint pos) {
        return new BlockPos(pos.x(), pos.y(), pos.z());
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private VoxelShape selectionShape(int sizeX, int sizeY, int sizeZ) {
        if (cachedSelectionShape == null || cachedSizeX != sizeX || cachedSizeY != sizeY || cachedSizeZ != sizeZ) {
            cachedSelectionShape = VoxelShapes.cuboid(0.0D, 0.0D, 0.0D, sizeX, sizeY, sizeZ);
            cachedSizeX = sizeX;
            cachedSizeY = sizeY;
            cachedSizeZ = sizeZ;
        }
        return cachedSelectionShape;
    }

    private record DimensionCallout(
            Vec3d lineStart,
            Vec3d lineEnd,
            Vec3d anchorStart,
            Vec3d anchorEnd,
            Vec3d anchor2Start,
            Vec3d anchor2End,
            Vec3d tickStartA,
            Vec3d tickStartB,
            Vec3d tickEndA,
            Vec3d tickEndB,
            Vec3d labelPosition
    ) {
    }

    private record CalloutLayout(
            DimensionCallout x,
            DimensionCallout y,
            DimensionCallout z
    ) {
    }
}
