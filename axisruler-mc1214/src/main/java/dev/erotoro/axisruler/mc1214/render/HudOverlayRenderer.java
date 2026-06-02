package dev.erotoro.axisruler.mc1214.render;

import dev.erotoro.axisruler.core.config.AxisRulerConfig;
import dev.erotoro.axisruler.core.config.ConfigManager;
import dev.erotoro.axisruler.core.measure.MeasurePoint;
import dev.erotoro.axisruler.core.measure.MeasurementFormatter;
import dev.erotoro.axisruler.core.measure.MeasurementResult;
import dev.erotoro.axisruler.core.measure.MeasurementService;
import dev.erotoro.axisruler.core.measure.SelectionState;
import dev.erotoro.axisruler.core.text.AxisRulerKeys;
import java.util.Arrays;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;

public final class HudOverlayRenderer {
    private static final int PANEL_PADDING = 6;
    private static final int ROW_HEIGHT = 10;
    private static final int PANEL_MIN_WIDTH = 172;
    private static final int LABEL_COLUMN_WIDTH = 44;

    private final MeasurementService measurementService;
    private final ConfigManager configManager;
    private boolean registered;
    private HudData cachedHudData;
    private MeasurePoint cachedPointA;
    private MeasurePoint cachedPointB;
    private dev.erotoro.axisruler.core.measure.SelectionMode cachedMode;
    private int cachedConfigHash;

    public HudOverlayRenderer(MeasurementService measurementService, ConfigManager configManager) {
        this.measurementService = measurementService;
        this.configManager = configManager;
    }

    public void register() {
        if (registered) {
            return;
        }
        HudRenderCallback.EVENT.register(this::renderHud);
        registered = true;
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        AxisRulerConfig config = configManager.config();
        if (!config.enabled()) {
            return;
        }

        SelectionState state = measurementService.selectionState();
        if (!state.hudEnabled()) {
            return;
        }

        MeasurePoint pointA = state.pointAOrNull();
        MeasurePoint pointB = state.pointBOrNull();
        boolean previewing = state.hasLivePreview();
        if (pointA == null && pointB == null && !previewing) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null
                || client.textRenderer == null
                || client.world == null
                || client.player == null
                || client.currentScreen != null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        HudData hudData = previewing
                ? buildCompleteHudData(textRenderer, config, state, pointA, state.previewTargetOrNull())
                : hudData(textRenderer, config, state, pointA, pointB);
        int pinnedCount = state.pinned().countForWorld(client.world.getRegistryKey().getValue().toString());
        if (pinnedCount > 0) {
            hudData = withPinnedRow(hudData, pinnedCount, textRenderer, config);
        }
        int panelX = config.resolveHudX(context.getScaledWindowWidth(), hudData.width());
        int panelY = config.resolveHudY(context.getScaledWindowHeight(), hudData.height());
        drawPanel(context, config, panelX, panelY, hudData.width(), hudData.height());
        drawTitle(context, textRenderer, config, panelX, panelY, hudData.width(), hudData.title());
        for (int row = 0; row < hudData.labels().length; row++) {
            drawRow(context, textRenderer, config, panelX, panelY, row, hudData.labels()[row], hudData.values()[row], hudData.colors()[row]);
        }
    }

    private HudData hudData(TextRenderer textRenderer, AxisRulerConfig config, SelectionState state, MeasurePoint pointA, MeasurePoint pointB) {
        int configHash = config.hashCode();
        if (cachedHudData != null
                && Objects.equals(cachedPointA, pointA)
                && Objects.equals(cachedPointB, pointB)
                && cachedMode == state.mode()
                && cachedConfigHash == configHash) {
            return cachedHudData;
        }

        HudData hudData = pointA != null && pointB != null
                ? buildCompleteHudData(textRenderer, config, state, pointA, pointB)
                : buildPartialHudData(textRenderer, config, pointA, pointB);
        cachedPointA = pointA;
        cachedPointB = pointB;
        cachedMode = state.mode();
        cachedConfigHash = configHash;
        cachedHudData = hudData;
        return hudData;
    }

    private HudData buildPartialHudData(TextRenderer textRenderer, AxisRulerConfig config, MeasurePoint pointA, MeasurePoint pointB) {
        String title = translatedTitle();
        String value = pointA != null ? pointA.formatBlockPosition() : pointB.formatBlockPosition();
        String[] labels = {pointA != null ? tr(AxisRulerKeys.HUD_LABEL_A) : tr(AxisRulerKeys.HUD_LABEL_B)};
        String[] values = {value};
        int[] colors = {config.hudPrimaryTextColorArgb()};
        return createHudData(textRenderer, config, title, labels, values, colors);
    }

    private HudData buildCompleteHudData(TextRenderer textRenderer, AxisRulerConfig config, SelectionState state, MeasurePoint pointA, MeasurePoint pointB) {
        String title = translatedTitle();
        MeasurementResult result = measurementService.calculate(pointA, pointB);
        if (!result.valid()) {
            String[] labels = {tr(AxisRulerKeys.HUD_LABEL_WARN), tr(AxisRulerKeys.HUD_LABEL_A), tr(AxisRulerKeys.HUD_LABEL_B)};
            String[] values = {tr(AxisRulerKeys.HUD_WARNING_DIFFERENT_WORLDS), compactWorld(pointA), compactWorld(pointB)};
            int[] colors = {config.hudWarningColorArgb(), config.hudPrimaryTextColorArgb(), config.hudPrimaryTextColorArgb()};
            return createHudData(textRenderer, config, title, labels, values, colors);
        }

        String size = MeasurementFormatter.formatDimensions(result.sizeX(), result.sizeY(), result.sizeZ());
        String absoluteDelta = result.absDx() + " " + result.absDy() + " " + result.absDz();
        String euclideanDistance = MeasurementFormatter.formatDecimal(result.euclideanDistance());
        String manhattanDistance = Integer.toString(result.manhattanDistance());
        String volume = Long.toString(result.volume());
        String mode = tr(state.mode().translationKey());
        if (config.hudCompactDefault()) {
            String[] labels = {
                    tr(AxisRulerKeys.HUD_LABEL_A),
                    tr(AxisRulerKeys.HUD_LABEL_B),
                    tr(AxisRulerKeys.HUD_LABEL_SIZE),
                    tr(AxisRulerKeys.HUD_LABEL_DXYZ),
                    tr(AxisRulerKeys.HUD_LABEL_DIST),
                    tr(AxisRulerKeys.HUD_LABEL_VOL),
                    tr(AxisRulerKeys.HUD_LABEL_MODE)
            };
            String[] values = {
                    pointA.formatBlockPosition(),
                    pointB.formatBlockPosition(),
                    size,
                    absoluteDelta,
                    euclideanDistance + " | " + manhattanDistance,
                    volume,
                    mode
            };
            return createHudData(textRenderer, config, title, labels, values, fillColors(config.hudPrimaryTextColorArgb(), labels.length));
        }

        String[] labels = {
                tr(AxisRulerKeys.HUD_LABEL_A),
                tr(AxisRulerKeys.HUD_LABEL_B),
                tr(AxisRulerKeys.HUD_LABEL_SIZE),
                tr(AxisRulerKeys.HUD_LABEL_DXYZ),
                tr(AxisRulerKeys.HUD_LABEL_DIST),
                tr(AxisRulerKeys.HUD_LABEL_TAXI),
                tr(AxisRulerKeys.HUD_LABEL_FLOOR),
                tr(AxisRulerKeys.HUD_LABEL_VOL),
                tr(AxisRulerKeys.HUD_LABEL_MODE)
        };
        String[] values = {
                pointA.formatBlockPosition(),
                pointB.formatBlockPosition(),
                size,
                absoluteDelta,
                euclideanDistance,
                manhattanDistance,
                Long.toString(result.floorArea()),
                volume,
                mode
        };
        return createHudData(textRenderer, config, title, labels, values, fillColors(config.hudPrimaryTextColorArgb(), labels.length));
    }

    private HudData createHudData(TextRenderer textRenderer, AxisRulerConfig config, String title, String[] labels, String[] values, int[] colors) {
        int width = panelWidth(textRenderer, config, title, values);
        int height = panelHeight(config, labels.length);
        return new HudData(title, width, height, labels, values, colors);
    }

    private HudData withPinnedRow(HudData base, int pinnedCount, TextRenderer textRenderer, AxisRulerConfig config) {
        int rows = base.labels().length;
        String[] labels = Arrays.copyOf(base.labels(), rows + 1);
        String[] values = Arrays.copyOf(base.values(), rows + 1);
        int[] colors = Arrays.copyOf(base.colors(), rows + 1);
        labels[rows] = tr(AxisRulerKeys.HUD_LABEL_PINNED);
        values[rows] = Integer.toString(pinnedCount);
        colors[rows] = config.hudPrimaryTextColorArgb();
        return createHudData(textRenderer, config, base.title(), labels, values, colors);
    }

    private int[] fillColors(int color, int size) {
        int[] colors = new int[size];
        for (int index = 0; index < size; index++) {
            colors[index] = color;
        }
        return colors;
    }

    private void drawPanel(DrawContext context, AxisRulerConfig config, int panelX, int panelY, int width, int height) {
        context.fill(panelX, panelY, panelX + width, panelY + height, config.effectiveHudBackgroundColorArgb());
        context.fill(panelX, panelY, panelX + 2, panelY + height, config.effectiveHudAccentColorArgb());
        context.drawBorder(panelX, panelY, width, height, config.effectiveHudBorderColorArgb());
    }

    private void drawTitle(DrawContext context, TextRenderer textRenderer, AxisRulerConfig config, int panelX, int panelY, int panelWidth, String title) {
        int padding = scaled(config, PANEL_PADDING);
        drawText(context, textRenderer, config, title, panelX + padding, panelY + padding, config.hudTitleColorArgb());
        int separatorY = panelY + padding + scaled(config, ROW_HEIGHT) - 3;
        context.fill(panelX + padding, separatorY, panelX + panelWidth - padding, separatorY + 1, config.effectiveHudAccentColorArgb());
    }

    private void drawRow(
            DrawContext context,
            TextRenderer textRenderer,
            AxisRulerConfig config,
            int panelX,
            int panelY,
            int row,
            String label,
            String value,
            int valueColor
    ) {
        int padding = scaled(config, PANEL_PADDING);
        int rowHeight = scaled(config, ROW_HEIGHT);
        int labelColumnWidth = scaled(config, LABEL_COLUMN_WIDTH);
        int y = panelY + padding + rowHeight + row * rowHeight;
        int labelX = panelX + padding;
        int valueX = labelX + labelColumnWidth;
        drawText(context, textRenderer, config, label, labelX, y, config.hudSecondaryTextColorArgb());
        drawText(context, textRenderer, config, value, valueX, y, valueColor);
    }

    private int panelWidth(TextRenderer textRenderer, AxisRulerConfig config, String title, String[] values) {
        int padding = scaled(config, PANEL_PADDING);
        int labelColumnWidth = scaled(config, LABEL_COLUMN_WIDTH);
        int longestValueWidth = textRenderer.getWidth(title);
        for (String value : values) {
            longestValueWidth = Math.max(longestValueWidth, textRenderer.getWidth(value));
        }
        return Math.max(scaled(config, PANEL_MIN_WIDTH), padding * 2 + labelColumnWidth + longestValueWidth);
    }

    private int panelHeight(AxisRulerConfig config, int rows) {
        int padding = scaled(config, PANEL_PADDING);
        int rowHeight = scaled(config, ROW_HEIGHT);
        return padding * 2 + rowHeight + rows * rowHeight;
    }

    private int scaled(AxisRulerConfig config, int value) {
        float scale = Math.max(1.0F, config.hudScale());
        return Math.max(1, Math.round(value * scale));
    }

    private void drawText(DrawContext context, TextRenderer textRenderer, AxisRulerConfig config, String text, int x, int y, int color) {
        if (config.hudTextShadow()) {
            context.drawTextWithShadow(textRenderer, text, x, y, color);
            return;
        }
        context.drawText(textRenderer, text, x, y, color, false);
    }

    private String compactWorld(MeasurePoint point) {
        if (point == null) {
            return "-";
        }
        int separator = point.worldKey().lastIndexOf(':');
        return separator >= 0 ? point.worldKey().substring(separator + 1) : point.worldKey();
    }

    private String tr(String key) {
        return I18n.translate(key);
    }

    private String translatedTitle() {
        return tr(AxisRulerKeys.HUD_TITLE);
    }

    private record HudData(String title, int width, int height, String[] labels, String[] values, int[] colors) {
    }
}
