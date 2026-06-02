package dev.erotoro.axisruler.mc12111.render;

import dev.erotoro.axisruler.core.config.LabelBackgroundMode;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class RenderPrimitives {
    private static final VoxelShape FULL_BLOCK_SHAPE = VoxelShapes.fullCube();
    private static final int FULL_BRIGHT_LIGHT = 0x00F000F0;
    private static final float DEFAULT_MARKER_LINE_WIDTH = 1.75F;

    private RenderPrimitives() {
    }

    public static void drawBlockOutline(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            BlockPos pos,
            int color,
            float lineWidth
    ) {
        drawOutline(matrices, consumers, FULL_BLOCK_SHAPE, pos.getX(), pos.getY(), pos.getZ(), color, lineWidth);
    }

    public static void drawBlockOutline(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            BlockPos pos,
            int color
    ) {
        drawBlockOutline(matrices, consumers, pos, color, DEFAULT_MARKER_LINE_WIDTH);
    }

    public static void drawShapeOutline(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            VoxelShape shape,
            double offsetX,
            double offsetY,
            double offsetZ,
            int color,
            float lineWidth
    ) {
        drawOutline(matrices, consumers, shape, offsetX, offsetY, offsetZ, color, lineWidth);
    }

    public static void drawFilledBox(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            int color
    ) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayers.debugFilledBox());
        MatrixStack.Entry entry = matrices.peek();
        quad(consumer, entry, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, color);
        quad(consumer, entry, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
        quad(consumer, entry, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, color);
        quad(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
        quad(consumer, entry, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        quad(consumer, entry, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, color);
    }

    public static void drawWorldLabel(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            TextRenderer textRenderer,
            CameraRenderState cameraRenderState,
            String label,
            Vec3d position,
            float scale,
            int color,
            LabelBackgroundMode backgroundMode,
            int backgroundColor,
            boolean billboard
    ) {
        matrices.push();
        try {
            matrices.translate(position.x, position.y, position.z);
            if (billboard) {
                matrices.multiply(cameraRenderState.orientation);
            }
            matrices.scale(scale, -scale, scale);

            float textWidth = textRenderer.getWidth(label);
            float textOffsetX = -textWidth / 2.0F;
            int effectiveBackgroundColor = backgroundColor(backgroundMode, backgroundColor);

            textRenderer.draw(
                    label,
                    textOffsetX,
                    0.0F,
                    color,
                    false,
                    matrices.peek().getPositionMatrix(),
                    consumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    effectiveBackgroundColor,
                    FULL_BRIGHT_LIGHT
            );
            textRenderer.draw(
                    label,
                    textOffsetX,
                    0.0F,
                    color,
                    false,
                    matrices.peek().getPositionMatrix(),
                    consumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0x00000000,
                    FULL_BRIGHT_LIGHT
            );
        } finally {
            matrices.pop();
        }
    }

    private static void drawOutline(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            VoxelShape shape,
            double offsetX,
            double offsetY,
            double offsetZ,
            int color,
            float lineWidth
    ) {
        VertexConsumer consumer = consumers.getBuffer(RenderLayers.lines());
        VertexRendering.drawOutline(matrices, consumer, shape, offsetX, offsetY, offsetZ, color, lineWidth);
    }

    private static void quad(
            VertexConsumer consumer,
            MatrixStack.Entry entry,
            double ax,
            double ay,
            double az,
            double bx,
            double by,
            double bz,
            double cx,
            double cy,
            double cz,
            double dx,
            double dy,
            double dz,
            int color
    ) {
        consumer.vertex(entry, (float) ax, (float) ay, (float) az).color(color);
        consumer.vertex(entry, (float) bx, (float) by, (float) bz).color(color);
        consumer.vertex(entry, (float) cx, (float) cy, (float) cz).color(color);
        consumer.vertex(entry, (float) dx, (float) dy, (float) dz).color(color);
    }

    private static int backgroundColor(LabelBackgroundMode mode, int color) {
        return switch (mode) {
            case NONE -> 0x00000000;
            case SUBTLE -> withAlpha(color, Math.round(((color >>> 24) & 0xFF) * 0.78F));
            case SOLID -> color;
        };
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

}
