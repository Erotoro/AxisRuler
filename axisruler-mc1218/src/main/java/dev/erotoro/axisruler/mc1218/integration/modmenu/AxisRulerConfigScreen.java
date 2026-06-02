package dev.erotoro.axisruler.mc1218.integration.modmenu;

import dev.erotoro.axisruler.core.config.AxisRulerConfig;
import dev.erotoro.axisruler.core.config.ConfigManager;
import dev.erotoro.axisruler.core.config.HudAnchor;
import dev.erotoro.axisruler.core.config.LabelBackgroundMode;
import dev.erotoro.axisruler.core.config.preset.PresetPalette;
import dev.erotoro.axisruler.core.config.preset.StylePreset;
import dev.erotoro.axisruler.core.measure.SelectionState;
import dev.erotoro.axisruler.core.util.ColorUtils;
import dev.erotoro.axisruler.mc1218.AxisRulerClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Lightweight, dependency-light configuration screen for the pre-1.21.6 line, where the
 * full NativeImage colour-wheel screen is not portable. Exposes every AxisRuler option via
 * vanilla widgets (toggles, cycles, sliders, hex colour fields) and the shared core presets.
 */
public final class AxisRulerConfigScreen extends Screen {
    private static final int TEXT_PRIMARY = 0xFFF4F8FC;
    private static final int TEXT_MUTED = 0xFF9AA6B2;

    private final Screen parent;
    private final ConfigManager configManager;
    private final SelectionState selectionState;
    private final EditorState state;
    private Page page = Page.BEHAVIOR;
    private boolean committed;

    private AxisRulerConfigScreen(Screen parent, ConfigManager configManager, SelectionState selectionState, AxisRulerConfig config) {
        super(Text.translatable("axisruler.config.title"));
        this.parent = parent;
        this.configManager = configManager;
        this.selectionState = selectionState;
        this.state = EditorState.from(config);
    }

    public static Screen create(Screen parent) {
        ConfigManager configManager = AxisRulerClient.configManager();
        SelectionState selectionState = AxisRulerClient.selectionState();
        AxisRulerConfig config = configManager != null ? configManager.persistedConfig() : AxisRulerConfig.defaults();
        if (configManager != null) {
            configManager.beginPreview();
            configManager.preview(config);
        }
        return new AxisRulerConfigScreen(parent, configManager, selectionState, config);
    }

    @Override
    protected void init() {
        int tabWidth = Math.min(120, (width - 40) / Page.values().length);
        int x = 20;
        for (Page tab : Page.values()) {
            Page target = tab;
            addDrawableChild(ButtonWidget.builder(Text.translatable(tab.titleKey), b -> {
                page = target;
                clearAndInit();
            }).dimensions(x, 6, tabWidth - 2, 18).build());
            x += tabWidth;
        }

        int contentTop = 34;
        switch (page) {
            case BEHAVIOR -> buildBehavior(contentTop);
            case HUD -> buildHud(contentTop);
            case OVERLAY -> buildOverlay(contentTop);
            case LABELS -> buildLabels(contentTop);
            case COLORS -> buildColors(contentTop);
            case PRESETS -> buildPresets(contentTop);
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("axisruler.config.action.done"), b -> saveAndClose())
                .dimensions(width / 2 - 154, height - 28, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("axisruler.config.action.cancel"), b -> close())
                .dimensions(width / 2 + 4, height - 28, 150, 20).build());
    }

    // ---- pages -------------------------------------------------------------------------

    private void buildBehavior(int top) {
        Grid g = new Grid(top);
        toggle(g, "axisruler.config.option.enabled", () -> state.enabled, v -> state.enabled = v);
        toggle(g, "axisruler.config.option.two_points_only", () -> state.showOnlyWithTwoPointsDefault, v -> state.showOnlyWithTwoPointsDefault = v);
        toggle(g, "axisruler.config.option.overlay_center_marker", () -> state.showCenterMarkerDefault, v -> state.showCenterMarkerDefault = v);
    }

    private void buildHud(int top) {
        Grid g = new Grid(top);
        toggle(g, "axisruler.config.option.hud_enabled", () -> state.hudEnabledDefault, v -> state.hudEnabledDefault = v);
        toggle(g, "axisruler.config.option.hud_compact", () -> state.hudCompactDefault, v -> state.hudCompactDefault = v);
        toggle(g, "axisruler.config.option.hud_text_shadow", () -> state.hudTextShadow, v -> state.hudTextShadow = v);
        cycleAnchor(g);
        slider(g, "axisruler.config.option.hud_scale", 0.75, 2.0, () -> state.hudScale, v -> state.hudScale = (float) v, false);
        slider(g, "axisruler.config.option.hud_offset_x", -256, 256, () -> state.hudOffsetX, v -> state.hudOffsetX = (int) Math.round(v), true);
        slider(g, "axisruler.config.option.hud_offset_y", -256, 256, () -> state.hudOffsetY, v -> state.hudOffsetY = (int) Math.round(v), true);
        slider(g, "axisruler.config.option.hud_background_alpha", 0, 255, () -> state.hudBackgroundAlpha, v -> state.hudBackgroundAlpha = (int) Math.round(v), true);
        slider(g, "axisruler.config.option.hud_border_alpha", 0, 255, () -> state.hudBorderAlpha, v -> state.hudBorderAlpha = (int) Math.round(v), true);
    }

    private void buildOverlay(int top) {
        Grid g = new Grid(top);
        toggle(g, "axisruler.config.option.overlay_enabled", () -> state.renderEnabledDefault, v -> state.renderEnabledDefault = v);
        toggle(g, "axisruler.config.option.overlay_fill", () -> state.fillEnabledDefault, v -> state.fillEnabledDefault = v);
        toggle(g, "axisruler.config.option.overlay_outline", () -> state.outlineEnabledDefault, v -> state.outlineEnabledDefault = v);
        toggle(g, "axisruler.config.option.overlay_guides", () -> state.guidesEnabledDefault, v -> state.guidesEnabledDefault = v);
        toggle(g, "axisruler.config.option.overlay_line", () -> state.lineEnabledDefault, v -> state.lineEnabledDefault = v);
        slider(g, "axisruler.config.option.overlay_line_thickness", 1.0, 3.5, () -> state.lineThickness, v -> state.lineThickness = (float) v, false);
        slider(g, "axisruler.config.option.overlay_callout_offset", 0.20, 1.25, () -> state.calloutOffset, v -> state.calloutOffset = (float) v, false);
        slider(g, "axisruler.config.option.overlay_tick_size", 0.03, 0.22, () -> state.tickSize, v -> state.tickSize = (float) v, false);
    }

    private void buildLabels(int top) {
        Grid g = new Grid(top);
        toggle(g, "axisruler.config.option.labels_enabled", () -> state.labelsEnabledDefault, v -> state.labelsEnabledDefault = v);
        toggle(g, "axisruler.config.option.label_billboard", () -> state.labelBillboard, v -> state.labelBillboard = v);
        toggle(g, "axisruler.config.option.label_unit", () -> state.labelShowUnit, v -> state.labelShowUnit = v);
        cycleLabelBackground(g);
        slider(g, "axisruler.config.option.label_scale", 0.01, 0.08, () -> state.labelScale, v -> state.labelScale = (float) v, false);
        slider(g, "axisruler.config.option.box_fill_alpha", 0, 255, () -> state.boxFillAlpha, v -> state.boxFillAlpha = (int) Math.round(v), true);
        slider(g, "axisruler.config.option.guide_alpha", 0, 255, () -> state.guideAlpha, v -> state.guideAlpha = (int) Math.round(v), true);
        slider(g, "axisruler.config.option.line_alpha", 0, 255, () -> state.lineAlpha, v -> state.lineAlpha = (int) Math.round(v), true);
    }

    private void buildColors(int top) {
        int colW = (width - 60) / 2;
        int leftX = 24;
        int rightX = leftX + colW + 12;
        int[] cursor = {top, top};
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_point_a", () -> state.pointAColor, v -> state.pointAColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_point_b", () -> state.pointBColor, v -> state.pointBColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_box", () -> state.boxColor, v -> state.boxColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_connection_line", () -> state.connectionLineColor, v -> state.connectionLineColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_x_guide", () -> state.xGuideColor, v -> state.xGuideColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_y_guide", () -> state.yGuideColor, v -> state.yGuideColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_z_guide", () -> state.zGuideColor, v -> state.zGuideColor = v);
        colorField(leftX, cursor, 0, colW, "axisruler.config.option.color_label", () -> state.labelColor, v -> state.labelColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.color_label_background", () -> state.labelBackgroundColor, v -> state.labelBackgroundColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_title", () -> state.hudTitleColor, v -> state.hudTitleColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_primary", () -> state.hudPrimaryTextColor, v -> state.hudPrimaryTextColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_secondary", () -> state.hudSecondaryTextColor, v -> state.hudSecondaryTextColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_accent", () -> state.hudAccentColor, v -> state.hudAccentColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_background", () -> state.hudBackgroundColor, v -> state.hudBackgroundColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_border", () -> state.hudBorderColor, v -> state.hudBorderColor = v);
        colorField(rightX, cursor, 1, colW, "axisruler.config.option.hud_color_warning", () -> state.hudWarningColor, v -> state.hudWarningColor = v);
    }

    private void buildPresets(int top) {
        Grid g = new Grid(top);
        for (StylePreset preset : StylePreset.values()) {
            StylePreset target = preset;
            addDrawableChild(ButtonWidget.builder(Text.translatable(preset.translationKey()), b -> {
                state.applyPreset(target);
                pushPreview();
            }).dimensions(g.x(), g.next(), Grid.W, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("axisruler.config.action.save_custom_preset"), b -> {
            state.saveAsCustomPreset();
            pushPreview();
        }).dimensions(g.x(), g.next(), Grid.W, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("axisruler.config.action.reset_all"), b -> {
            state.resetAll();
            pushPreview();
            clearAndInit();
        }).dimensions(g.x(), g.next(), Grid.W, 20).build());
    }

    // ---- widget helpers ----------------------------------------------------------------

    private void toggle(Grid g, String key, java.util.function.BooleanSupplier getter, Consumer<Boolean> setter) {
        int y = g.next();
        ButtonWidget[] holder = new ButtonWidget[1];
        holder[0] = ButtonWidget.builder(toggleLabel(key, getter.getAsBoolean()), b -> {
            setter.accept(!getter.getAsBoolean());
            b.setMessage(toggleLabel(key, getter.getAsBoolean()));
            pushPreview();
        }).dimensions(g.x(), y, Grid.W, 20).build();
        addDrawableChild(holder[0]);
    }

    private Text toggleLabel(String key, boolean value) {
        return Text.translatable(key).append(": ").append(Text.translatable(value ? "axisruler.message.enabled" : "axisruler.message.disabled"));
    }

    private void cycleAnchor(Grid g) {
        int y = g.next();
        ButtonWidget btn = ButtonWidget.builder(anchorLabel(), b -> {
            state.hudAnchor = state.hudAnchor.next();
            b.setMessage(anchorLabel());
            pushPreview();
        }).dimensions(g.x(), y, Grid.W, 20).build();
        addDrawableChild(btn);
    }

    private Text anchorLabel() {
        return Text.translatable("axisruler.config.option.hud_anchor").append(": ").append(Text.translatable(state.hudAnchor.translationKey()));
    }

    private void cycleLabelBackground(Grid g) {
        int y = g.next();
        ButtonWidget btn = ButtonWidget.builder(labelBgLabel(), b -> {
            state.labelBackgroundMode = state.labelBackgroundMode.next();
            b.setMessage(labelBgLabel());
            pushPreview();
        }).dimensions(g.x(), y, Grid.W, 20).build();
        addDrawableChild(btn);
    }

    private Text labelBgLabel() {
        return Text.translatable("axisruler.config.option.label_background").append(": ").append(Text.translatable(state.labelBackgroundMode.translationKey()));
    }

    private void slider(Grid g, String key, double min, double max, DoubleSupplier getter, DoubleConsumer setter, boolean integer) {
        int y = g.next();
        addDrawableChild(new OptionSlider(g.x(), y, Grid.W, 20, key, min, max, getter, value -> {
            setter.accept(value);
            pushPreview();
        }, integer));
    }

    private void colorField(int x, int[] cursor, int col, int width, String key, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        int y = cursor[col];
        cursor[col] += 30;
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y + 11, width, 16, Text.literal(key));
        field.setMaxLength(9);
        field.setText(ColorUtils.toHex(getter.getAsInt(), true));
        field.setChangedListener(value -> {
            int parsed = ColorUtils.parseHex(value, Integer.MIN_VALUE);
            if (parsed != Integer.MIN_VALUE) {
                setter.accept(parsed);
                pushPreview();
            }
        });
        addDrawableChild(field);
        colorLabels.add(new ColorLabel(key, x, y, getter));
    }

    private final List<ColorLabel> colorLabels = new ArrayList<>();

    // ---- rendering ---------------------------------------------------------------------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawText(textRenderer, title, 20, height - 44, TEXT_MUTED, false);
        if (page == Page.COLORS) {
            for (ColorLabel label : colorLabels) {
                context.drawText(textRenderer, Text.translatable(label.key), label.x, label.y, TEXT_PRIMARY, false);
                context.fill(label.x + width / 2 - 90, label.y - 1, label.x + width / 2 - 74, label.y + 9, label.getter.getAsInt());
            }
        }
    }

    @Override
    protected void clearChildren() {
        super.clearChildren();
        colorLabels.clear();
    }

    // ---- lifecycle ---------------------------------------------------------------------

    @Override
    public void close() {
        if (!committed && configManager != null) {
            configManager.discardPreview();
        }
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void saveAndClose() {
        if (configManager == null) {
            close();
            return;
        }
        AxisRulerConfig updated = state.toConfig();
        configManager.preview(updated);
        configManager.commitPreview();
        if (selectionState != null) {
            selectionState.applyConfigDefaults(updated);
        }
        committed = true;
        close();
    }

    private void pushPreview() {
        if (configManager != null) {
            configManager.preview(state.toConfig());
        }
    }

    private enum Page {
        BEHAVIOR("axisruler.config.tab.core"),
        HUD("axisruler.config.tab.hud"),
        OVERLAY("axisruler.config.tab.overlay"),
        LABELS("axisruler.config.tab.labels"),
        COLORS("axisruler.config.tab.palette"),
        PRESETS("axisruler.config.tab.presets");

        private final String titleKey;

        Page(String titleKey) {
            this.titleKey = titleKey;
        }
    }

    private record ColorLabel(String key, int x, int y, java.util.function.IntSupplier getter) {
    }

    private static final class Grid {
        private static final int W = 260;
        private final int startY;
        private int row;

        private Grid(int startY) {
            this.startY = startY;
        }

        private int x() {
            return 24;
        }

        private int next() {
            int y = startY + row * 24;
            row++;
            return y;
        }
    }

    private static final class OptionSlider extends SliderWidget {
        private final String key;
        private final double min;
        private final double max;
        private final DoubleConsumer setter;
        private final boolean integer;

        private OptionSlider(int x, int y, int width, int height, String key, double min, double max, DoubleSupplier getter, DoubleConsumer setter, boolean integer) {
            super(x, y, width, height, Text.empty(), clamp01((getter.getAsDouble() - min) / (max - min)));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.integer = integer;
            updateMessage();
        }

        private static double clamp01(double v) {
            return Math.max(0.0D, Math.min(1.0D, v));
        }

        private double current() {
            return min + value * (max - min);
        }

        @Override
        protected void updateMessage() {
            double c = current();
            String shown = integer ? Integer.toString((int) Math.round(c)) : String.format(Locale.ROOT, "%.3f", c);
            setMessage(Text.translatable(key).append(": ").append(shown));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }

    private static final class EditorState {
        private boolean enabled;
        private boolean hudEnabledDefault;
        private boolean hudCompactDefault;
        private boolean guidesEnabledDefault;
        private boolean renderEnabledDefault;
        private boolean labelsEnabledDefault;
        private boolean fillEnabledDefault;
        private boolean outlineEnabledDefault;
        private boolean lineEnabledDefault;
        private boolean showCenterMarkerDefault;
        private boolean showOnlyWithTwoPointsDefault;
        private int pointAColor;
        private int pointBColor;
        private int boxColor;
        private int connectionLineColor;
        private int xGuideColor;
        private int yGuideColor;
        private int zGuideColor;
        private int labelColor;
        private int labelBackgroundColor;
        private int hudTitleColor;
        private int hudPrimaryTextColor;
        private int hudSecondaryTextColor;
        private int hudBackgroundColor;
        private int hudBorderColor;
        private int hudWarningColor;
        private int hudAccentColor;
        private LabelBackgroundMode labelBackgroundMode;
        private boolean labelBillboard;
        private boolean labelShowUnit;
        private HudAnchor hudAnchor;
        private int hudOffsetX;
        private int hudOffsetY;
        private float hudScale;
        private int hudBackgroundAlpha;
        private int hudBorderAlpha;
        private boolean hudTextShadow;
        private float labelScale;
        private float lineThickness;
        private float calloutOffset;
        private float tickSize;
        private int boxFillAlpha;
        private int guideAlpha;
        private int lineAlpha;
        private String customPresetName;
        private AxisRulerConfig source;

        private static EditorState from(AxisRulerConfig c) {
            EditorState s = new EditorState();
            s.source = c;
            s.enabled = c.enabled();
            s.hudEnabledDefault = c.hudEnabledDefault();
            s.hudCompactDefault = c.hudCompactDefault();
            s.guidesEnabledDefault = c.guidesEnabledDefault();
            s.renderEnabledDefault = c.renderEnabledDefault();
            s.labelsEnabledDefault = c.labelsEnabledDefault();
            s.fillEnabledDefault = c.fillEnabledDefault();
            s.outlineEnabledDefault = c.outlineEnabledDefault();
            s.lineEnabledDefault = c.lineEnabledDefault();
            s.showCenterMarkerDefault = c.showCenterMarkerDefault();
            s.showOnlyWithTwoPointsDefault = c.showOnlyWithTwoPointsDefault();
            s.pointAColor = c.pointAColorArgb();
            s.pointBColor = c.pointBColorArgb();
            s.boxColor = c.boxColorArgb();
            s.connectionLineColor = c.connectionLineColorArgb();
            s.xGuideColor = c.xGuideColorArgb();
            s.yGuideColor = c.yGuideColorArgb();
            s.zGuideColor = c.zGuideColorArgb();
            s.labelColor = c.labelColorArgb();
            s.labelBackgroundColor = c.labelBackgroundColorArgb();
            s.hudTitleColor = c.hudTitleColorArgb();
            s.hudPrimaryTextColor = c.hudPrimaryTextColorArgb();
            s.hudSecondaryTextColor = c.hudSecondaryTextColorArgb();
            s.hudBackgroundColor = c.hudBackgroundColorArgb();
            s.hudBorderColor = c.hudBorderColorArgb();
            s.hudWarningColor = c.hudWarningColorArgb();
            s.hudAccentColor = c.hudAccentColorArgb();
            s.labelBackgroundMode = c.labelBackgroundModeEnum();
            s.labelBillboard = c.labelBillboard();
            s.labelShowUnit = c.labelShowUnit();
            s.hudAnchor = c.hudAnchorEnum();
            s.hudOffsetX = c.hudOffsetX();
            s.hudOffsetY = c.hudOffsetY();
            s.hudScale = c.hudScale();
            s.hudBackgroundAlpha = c.hudBackgroundAlpha();
            s.hudBorderAlpha = c.hudBorderAlpha();
            s.hudTextShadow = c.hudTextShadow();
            s.labelScale = c.labelScale();
            s.lineThickness = c.lineThickness();
            s.calloutOffset = c.calloutOffset();
            s.tickSize = c.tickSize();
            s.boxFillAlpha = c.boxFillAlpha();
            s.guideAlpha = c.guideAlpha();
            s.lineAlpha = c.lineAlpha();
            s.customPresetName = c.customPresetName();
            return s;
        }

        private void applyPreset(StylePreset preset) {
            if (preset == StylePreset.CUSTOM_SAVED || !preset.hasPalette()) {
                AxisRulerConfig stored = source.applyStoredCustomPreset();
                copyVisual(EditorState.from(stored));
                return;
            }
            PresetPalette p = preset.palette();
            pointAColor = p.pointAColor();
            pointBColor = p.pointBColor();
            boxColor = p.boxColor();
            connectionLineColor = p.connectionLineColor();
            xGuideColor = p.xGuideColor();
            yGuideColor = p.yGuideColor();
            zGuideColor = p.zGuideColor();
            labelColor = p.labelColor();
            labelBackgroundColor = p.labelBackgroundColor();
            hudTitleColor = p.hudTitleColor();
            hudPrimaryTextColor = p.hudPrimaryTextColor();
            hudSecondaryTextColor = p.hudSecondaryTextColor();
            hudAccentColor = p.hudAccentColor();
            hudBackgroundColor = p.hudBackgroundColor();
            hudBorderColor = p.hudBorderColor();
            hudWarningColor = p.hudWarningColor();
            hudBackgroundAlpha = p.hudBackgroundAlpha();
            hudBorderAlpha = p.hudBorderAlpha();
            hudTextShadow = p.hudTextShadow();
            labelBackgroundMode = p.labelBackgroundMode();
            labelScale = p.labelScale();
            lineThickness = p.lineThickness();
            calloutOffset = p.calloutOffset();
            tickSize = p.tickSize();
            boxFillAlpha = p.boxFillAlpha();
            guideAlpha = p.guideAlpha();
            lineAlpha = p.lineAlpha();
        }

        private void copyVisual(EditorState o) {
            pointAColor = o.pointAColor;
            pointBColor = o.pointBColor;
            boxColor = o.boxColor;
            connectionLineColor = o.connectionLineColor;
            xGuideColor = o.xGuideColor;
            yGuideColor = o.yGuideColor;
            zGuideColor = o.zGuideColor;
            labelColor = o.labelColor;
            labelBackgroundColor = o.labelBackgroundColor;
            hudTitleColor = o.hudTitleColor;
            hudPrimaryTextColor = o.hudPrimaryTextColor;
            hudSecondaryTextColor = o.hudSecondaryTextColor;
            hudAccentColor = o.hudAccentColor;
            hudBackgroundColor = o.hudBackgroundColor;
            hudBorderColor = o.hudBorderColor;
            hudWarningColor = o.hudWarningColor;
            hudBackgroundAlpha = o.hudBackgroundAlpha;
            hudBorderAlpha = o.hudBorderAlpha;
            hudTextShadow = o.hudTextShadow;
            labelBackgroundMode = o.labelBackgroundMode;
            labelScale = o.labelScale;
            lineThickness = o.lineThickness;
            calloutOffset = o.calloutOffset;
            tickSize = o.tickSize;
            boxFillAlpha = o.boxFillAlpha;
            guideAlpha = o.guideAlpha;
            lineAlpha = o.lineAlpha;
        }

        private void resetAll() {
            copyAll(EditorState.from(AxisRulerConfig.defaults()));
        }

        private void copyAll(EditorState o) {
            enabled = o.enabled;
            hudEnabledDefault = o.hudEnabledDefault;
            hudCompactDefault = o.hudCompactDefault;
            guidesEnabledDefault = o.guidesEnabledDefault;
            renderEnabledDefault = o.renderEnabledDefault;
            labelsEnabledDefault = o.labelsEnabledDefault;
            fillEnabledDefault = o.fillEnabledDefault;
            outlineEnabledDefault = o.outlineEnabledDefault;
            lineEnabledDefault = o.lineEnabledDefault;
            showCenterMarkerDefault = o.showCenterMarkerDefault;
            showOnlyWithTwoPointsDefault = o.showOnlyWithTwoPointsDefault;
            labelBillboard = o.labelBillboard;
            labelShowUnit = o.labelShowUnit;
            hudAnchor = o.hudAnchor;
            hudOffsetX = o.hudOffsetX;
            hudOffsetY = o.hudOffsetY;
            hudScale = o.hudScale;
            customPresetName = o.customPresetName;
            copyVisual(o);
        }

        private void saveAsCustomPreset() {
            source = toConfig().withSavedCustomPreset(
                    customPresetName,
                    ColorUtils.toHex(pointAColor, true), ColorUtils.toHex(pointBColor, true), ColorUtils.toHex(boxColor, true),
                    ColorUtils.toHex(connectionLineColor, true), ColorUtils.toHex(xGuideColor, true), ColorUtils.toHex(yGuideColor, true),
                    ColorUtils.toHex(zGuideColor, true), ColorUtils.toHex(labelColor, true), ColorUtils.toHex(labelBackgroundColor, true),
                    labelBackgroundMode.configValue(), ColorUtils.toHex(hudTitleColor, true), ColorUtils.toHex(hudPrimaryTextColor, true),
                    ColorUtils.toHex(hudSecondaryTextColor, true), ColorUtils.toHex(hudAccentColor, true), ColorUtils.toHex(hudBackgroundColor, true),
                    ColorUtils.toHex(hudBorderColor, true), ColorUtils.toHex(hudWarningColor, true), hudBackgroundAlpha, hudBorderAlpha,
                    hudTextShadow, labelScale, lineThickness, calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha
            );
        }

        private AxisRulerConfig toConfig() {
            return new AxisRulerConfig(
                    enabled, hudEnabledDefault, hudCompactDefault, guidesEnabledDefault, renderEnabledDefault, labelsEnabledDefault,
                    fillEnabledDefault, outlineEnabledDefault, lineEnabledDefault, showCenterMarkerDefault, showOnlyWithTwoPointsDefault,
                    ColorUtils.toHex(pointAColor, true), ColorUtils.toHex(pointBColor, true), ColorUtils.toHex(boxColor, true),
                    ColorUtils.toHex(connectionLineColor, true), ColorUtils.toHex(xGuideColor, true), ColorUtils.toHex(yGuideColor, true),
                    ColorUtils.toHex(zGuideColor, true), ColorUtils.toHex(labelColor, true), ColorUtils.toHex(labelBackgroundColor, true),
                    labelBackgroundMode.configValue(), labelBillboard, labelShowUnit, hudAnchor.configValue(), hudOffsetX, hudOffsetY,
                    hudScale, ColorUtils.toHex(hudTitleColor, true), ColorUtils.toHex(hudPrimaryTextColor, true), ColorUtils.toHex(hudSecondaryTextColor, true),
                    ColorUtils.toHex(hudAccentColor, true), ColorUtils.toHex(hudBackgroundColor, true), ColorUtils.toHex(hudBorderColor, true),
                    ColorUtils.toHex(hudWarningColor, true), hudBackgroundAlpha, hudBorderAlpha, hudTextShadow, labelScale, lineThickness,
                    calloutOffset, tickSize, boxFillAlpha, guideAlpha, lineAlpha, customPresetName,
                    source.customPointAColor(), source.customPointBColor(), source.customBoxColor(), source.customConnectionLineColor(),
                    source.customXGuideColor(), source.customYGuideColor(), source.customZGuideColor(), source.customLabelColor(),
                    source.customLabelBackgroundColor(), source.customLabelBackgroundMode(), source.customHudTitleColor(),
                    source.customHudPrimaryTextColor(), source.customHudSecondaryTextColor(), source.customHudAccentColor(),
                    source.customHudBackgroundColor(), source.customHudBorderColor(), source.customHudWarningColor(),
                    source.customHudBackgroundAlpha(), source.customHudBorderAlpha(), source.customHudTextShadow(),
                    source.customLabelScale(), source.customLineThickness(), source.customCalloutOffset(), source.customTickSize(),
                    source.customBoxFillAlpha(), source.customGuideAlpha(), source.customLineAlpha()
            );
        }
    }
}
