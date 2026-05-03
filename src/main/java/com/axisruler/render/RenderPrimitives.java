package com.axisruler.render;

import com.axisruler.config.LabelBackgroundMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RenderPrimitives {
    private static final VoxelShape FULL_BLOCK_SHAPE = Shapes.block();
    private static final int FULL_BRIGHT_LIGHT = 0x00F000F0;
    private static final float DEFAULT_MARKER_LINE_WIDTH = 1.75F;

    private RenderPrimitives() {
    }

    public static void drawBlockOutline(
            PoseStack matrices,
            MultiBufferSource consumers,
            BlockPos pos,
            int color,
            float lineWidth
    ) {
        drawOutline(matrices, consumers, FULL_BLOCK_SHAPE, pos.getX(), pos.getY(), pos.getZ(), color, lineWidth);
    }

    public static void drawBlockOutline(
            PoseStack matrices,
            MultiBufferSource consumers,
            BlockPos pos,
            int color
    ) {
        drawBlockOutline(matrices, consumers, pos, color, DEFAULT_MARKER_LINE_WIDTH);
    }

    public static void drawShapeOutline(
            PoseStack matrices,
            MultiBufferSource consumers,
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
            PoseStack matrices,
            MultiBufferSource consumers,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            int color
    ) {
        VertexConsumer consumer = consumers.getBuffer(RenderTypes.debugFilledBox());
        PoseStack.Pose entry = matrices.last();
        quad(consumer, entry, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, color);
        quad(consumer, entry, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, color);
        quad(consumer, entry, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, color);
        quad(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, color);
        quad(consumer, entry, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, color);
        quad(consumer, entry, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, color);
    }

    public static void drawWorldLabel(
            PoseStack matrices,
            MultiBufferSource consumers,
            Font textRenderer,
            Camera camera,
            String label,
            Vec3 position,
            float scale,
            int color,
            LabelBackgroundMode backgroundMode,
            int backgroundColor,
            boolean billboard
    ) {
            matrices.pushPose();
        try {
            matrices.translate(position.x, position.y, position.z);
            if (billboard) {
                matrices.mulPose(camera.rotation());
            }
            matrices.scale(scale, -scale, scale);

            float textWidth = textRenderer.width(label);
            float textOffsetX = -textWidth / 2.0F;
            int effectiveBackgroundColor = backgroundColor(backgroundMode, backgroundColor);

            textRenderer.drawInBatch(
                    label,
                    textOffsetX,
                    0.0F,
                    color,
                    false,
                    matrices.last().pose(),
                    consumers,
                    Font.DisplayMode.SEE_THROUGH,
                    effectiveBackgroundColor,
                    FULL_BRIGHT_LIGHT
            );
            textRenderer.drawInBatch(
                    label,
                    textOffsetX,
                    0.0F,
                    color,
                    false,
                    matrices.last().pose(),
                    consumers,
                    Font.DisplayMode.NORMAL,
                    0x00000000,
                    FULL_BRIGHT_LIGHT
            );
        } finally {
            matrices.popPose();
        }
    }

    private static void drawOutline(
            PoseStack matrices,
            MultiBufferSource consumers,
            VoxelShape shape,
            double offsetX,
            double offsetY,
            double offsetZ,
            int color,
            float lineWidth
    ) {
        VertexConsumer consumer = consumers.getBuffer(RenderTypes.lines());
        ShapeRenderer.renderShape(matrices, consumer, shape, offsetX, offsetY, offsetZ, color, lineWidth);
    }

    private static void quad(
            VertexConsumer consumer,
            PoseStack.Pose entry,
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
        consumer.addVertex(entry, (float) ax, (float) ay, (float) az).setColor(color);
        consumer.addVertex(entry, (float) bx, (float) by, (float) bz).setColor(color);
        consumer.addVertex(entry, (float) cx, (float) cy, (float) cz).setColor(color);
        consumer.addVertex(entry, (float) dx, (float) dy, (float) dz).setColor(color);
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
