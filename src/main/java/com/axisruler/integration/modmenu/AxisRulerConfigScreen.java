package com.axisruler.integration.modmenu;

import com.axisruler.AxisRulerClient;
import com.axisruler.config.AxisRulerConfig;
import com.axisruler.config.ConfigManager;
import com.axisruler.config.HudAnchor;
import com.axisruler.config.LabelBackgroundMode;
import com.axisruler.measure.SelectionState;
import com.axisruler.util.ColorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class AxisRulerConfigScreen extends Screen {
    private static final int BACKGROUND = 0xE60C1016;
    private static final int PANEL = 0xCC121821;
    private static final int PANEL_ALT = 0xAA0E141C;
    private static final int PANEL_SUBTLE = 0xB0161E29;
    private static final int PANEL_BORDER = 0x66344252;
    private static final int PANEL_ACCENT = 0xFF7BD8FF;
    private static final int TEXT_PRIMARY = 0xFFF4F8FC;
    private static final int TEXT_SECONDARY = 0xFFB4C0CB;
    private static final int TEXT_MUTED = 0xFF8D98A5;
    private static final int TEXT_DISABLED = 0xFF68727D;
    private static final int FIELD_BORDER = 0x80516374;
    private static final int FIELD_BORDER_ACTIVE = 0xFF7BD8FF;
    private static final int FIELD_BORDER_DISABLED = 0x40384757;
    private static final int ROW_BACKGROUND = 0x68141B25;
    private static final int ROW_BACKGROUND_HOVER = 0x8418222D;
    private static final int CONTROL_BACKGROUND = 0xD118202A;
    private static final int HEADER_HEIGHT = 46;
    private static final int PANEL_PADDING = 18;
    private static final int SECTION_GAP = 18;
    private static final int BLOCK_GAP = 12;
    private static final int ROW_GAP = 8;
    private static final int ROW_HEIGHT = 54;
    private static final int LABEL_GAP = 6;
    private static final int CONTROL_HEIGHT = 20;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final int PRESET_CARD_HEIGHT = 44;
    private static final int PRESET_MIN_WIDTH = 156;
    private static final int PALETTE_SWATCH_SIZE = 24;
    private static final int PALETTE_ALPHA_HEIGHT = 12;
    private static final int PRESET_CYAN = 0xFF58D6FF;
    private static final int PRESET_EMERALD = 0xFF47D98D;
    private static final int PRESET_RED = 0xFFFF6B5B;
    private static final int PRESET_YELLOW = 0xFFFFD364;
    private static final int PRESET_WHITE = 0xFFFFFFFF;
    private static final List<Integer> QUICK_COLORS = List.of(PRESET_CYAN, PRESET_EMERALD, PRESET_RED, PRESET_YELLOW, PRESET_WHITE);

    private final Screen parent;
    private final ConfigManager configManager;
    private final SelectionState selectionState;
    private final MutableConfig state;
    private final List<ClickableWidget> dynamicWidgets = new ArrayList<>();
    private Tab activeTab = Tab.CORE;
    private ColorGroup activePaletteGroup = ColorGroup.OVERLAY;
    private ColorRole activeColorRole = ColorRole.POINT_A;
    private boolean committed;
    private ColorPickerWidget colorPicker;
    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;
    private TextFieldWidget hexField;
    private TextFieldWidget customPresetField;
    private boolean synchronizingFields;

    private AxisRulerConfigScreen(Screen parent, ConfigManager configManager, SelectionState selectionState, AxisRulerConfig config) {
        super(Text.translatable("axisruler.config.title"));
        this.parent = parent;
        this.configManager = configManager;
        this.selectionState = selectionState;
        this.state = MutableConfig.from(config);
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
        rebuild();
    }

    private void rebuild() {
        if (colorPicker != null) {
            colorPicker.close();
            colorPicker = null;
        }
        clearChildren();
        dynamicWidgets.clear();
        int top = 20;
        int left = 24;
        int right = width - 24;
        int contentY = 60;
        int contentHeight = height - 110;
        int bottomY = height - 34;

        int tabGap = 10;
        int tabWidth = Math.max(84, (right - left - tabGap * (Tab.values().length - 1)) / Tab.values().length);
        int x = left;
        for (Tab tab : Tab.values()) {
            TabButtonWidget tabButton = new TabButtonWidget(x, top, tabWidth, 24, tab, tab == activeTab, () -> {
                activeTab = tab;
                rebuild();
            });
            addDrawableChild(tabButton);
            x += tabWidth + tabGap;
        }

        addControl(new AxisButtonWidget(width - 220, bottomY, 96, 20, Text.translatable("axisruler.config.action.done"), ButtonVariant.PRIMARY, this::saveAndClose));
        addControl(new AxisButtonWidget(width - 116, bottomY, 92, 20, Text.translatable("axisruler.config.action.cancel"), ButtonVariant.GHOST, this::close));

        switch (activeTab) {
            case CORE -> buildCoreTab(left, contentY, right - left, contentHeight);
            case HUD_LAYOUT -> buildHudTab(left, contentY, right - left, contentHeight);
            case OVERLAY -> buildOverlayTab(left, contentY, right - left, contentHeight);
            case DIMENSION_LABELS -> buildLabelsTab(left, contentY, right - left, contentHeight);
            case PALETTE -> buildPaletteTab(left, contentY, right - left, contentHeight);
            case STYLE_PRESETS -> buildPresetsTab(left, contentY, right - left, contentHeight);
        }
    }

    private void buildCoreTab(int x, int y, int width, int height) {
        FormGrid grid = formGrid(x, y, width);
        addControl(toggleButton(grid.controlX(), grid.rowY(0), grid.controlWidth(), () -> state.enabled, value -> state.enabled = value, "axisruler.config.option.enabled", "axisruler.config.option.enabled.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(1), grid.controlWidth(), () -> state.showOnlyWithTwoPointsDefault, value -> state.showOnlyWithTwoPointsDefault = value, "axisruler.config.option.two_points_only", "axisruler.config.option.two_points_only.desc"));
        addControl(actionButton(grid.controlX(), grid.actionsY(), "axisruler.config.action.reset_core", ButtonVariant.DANGER, () -> {
            state.resetCore();
            pushPreview();
            rebuild();
        }, "axisruler.config.action.reset_core.desc", grid.controlWidth()));
    }

    private void buildHudTab(int x, int y, int width, int height) {
        FormGrid grid = formGrid(x, y, width);
        addControl(toggleButton(grid.controlX(), grid.rowY(0), grid.controlWidth(), () -> state.hudEnabledDefault, value -> state.hudEnabledDefault = value, "axisruler.config.option.hud_enabled", "axisruler.config.option.hud_enabled.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(1), grid.controlWidth(), () -> state.hudCompactDefault, value -> state.hudCompactDefault = value, "axisruler.config.option.hud_compact", "axisruler.config.option.hud_compact.desc"));
        addControl(cycleButton(grid.controlX(), grid.rowY(2), grid.controlWidth(), state.hudAnchor.localized(), "axisruler.config.option.hud_anchor.desc", () -> {
            state.hudAnchor = state.hudAnchor.next();
            rebuild();
            pushPreview();
        }));
        addControl(floatSlider(grid.controlX(), grid.rowY(3), grid.controlWidth(), 0.75F, 2.0F, state.hudScale, value -> state.hudScale = value, "axisruler.config.option.hud_scale.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(4), grid.controlWidth(), -256, 256, state.hudOffsetX, value -> state.hudOffsetX = value, "axisruler.config.option.hud_offset_x.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(5), grid.controlWidth(), -256, 256, state.hudOffsetY, value -> state.hudOffsetY = value, "axisruler.config.option.hud_offset_y.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(6), grid.controlWidth(), 0, 255, state.hudBackgroundAlpha, value -> state.hudBackgroundAlpha = value, "axisruler.config.option.hud_background_alpha.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(7), grid.controlWidth(), 0, 255, state.hudBorderAlpha, value -> state.hudBorderAlpha = value, "axisruler.config.option.hud_border_alpha.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(8), grid.controlWidth(), () -> state.hudTextShadow, value -> state.hudTextShadow = value, "axisruler.config.option.hud_text_shadow", "axisruler.config.option.hud_text_shadow.desc"));
    }

    private void buildOverlayTab(int x, int y, int width, int height) {
        FormGrid grid = formGrid(x, y, width);
        addControl(toggleButton(grid.controlX(), grid.rowY(0), grid.controlWidth(), () -> state.renderEnabledDefault, value -> state.renderEnabledDefault = value, "axisruler.config.option.overlay_enabled", "axisruler.config.option.overlay_enabled.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(1), grid.controlWidth(), () -> state.fillEnabledDefault, value -> state.fillEnabledDefault = value, "axisruler.config.option.overlay_fill", "axisruler.config.option.overlay_fill.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(2), grid.controlWidth(), () -> state.outlineEnabledDefault, value -> state.outlineEnabledDefault = value, "axisruler.config.option.overlay_outline", "axisruler.config.option.overlay_outline.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(3), grid.controlWidth(), () -> state.guidesEnabledDefault, value -> state.guidesEnabledDefault = value, "axisruler.config.option.overlay_guides", "axisruler.config.option.overlay_guides.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(4), grid.controlWidth(), () -> state.lineEnabledDefault, value -> state.lineEnabledDefault = value, "axisruler.config.option.overlay_line", "axisruler.config.option.overlay_line.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(5), grid.controlWidth(), () -> state.showCenterMarkerDefault, value -> state.showCenterMarkerDefault = value, "axisruler.config.option.overlay_center_marker", "axisruler.config.option.overlay_center_marker.desc"));
        addControl(floatSlider(grid.controlX(), grid.rowY(6), grid.controlWidth(), 1.0F, 3.5F, state.lineThickness, value -> state.lineThickness = value, "axisruler.config.option.overlay_line_thickness.desc"));
        addControl(floatSlider(grid.controlX(), grid.rowY(7), grid.controlWidth(), 0.20F, 1.25F, state.calloutOffset, value -> state.calloutOffset = value, "axisruler.config.option.overlay_callout_offset.desc"));
        addControl(floatSlider(grid.controlX(), grid.rowY(8), grid.controlWidth(), 0.03F, 0.22F, state.tickSize, value -> state.tickSize = value, "axisruler.config.option.overlay_tick_size.desc"));
    }

    private void buildLabelsTab(int x, int y, int width, int height) {
        FormGrid grid = formGrid(x, y, width);
        addControl(toggleButton(grid.controlX(), grid.rowY(0), grid.controlWidth(), () -> state.labelsEnabledDefault, value -> state.labelsEnabledDefault = value, "axisruler.config.option.labels_enabled", "axisruler.config.option.labels_enabled.desc"));
        addControl(cycleButton(grid.controlX(), grid.rowY(1), grid.controlWidth(), state.labelBackgroundMode.localized(), "axisruler.config.option.label_background.desc", () -> {
            state.labelBackgroundMode = state.labelBackgroundMode.next();
            rebuild();
            pushPreview();
        }));
        addControl(toggleButton(grid.controlX(), grid.rowY(2), grid.controlWidth(), () -> state.labelBillboard, value -> state.labelBillboard = value, "axisruler.config.option.label_billboard", "axisruler.config.option.label_billboard.desc"));
        addControl(toggleButton(grid.controlX(), grid.rowY(3), grid.controlWidth(), () -> state.labelShowUnit, value -> state.labelShowUnit = value, "axisruler.config.option.label_unit", "axisruler.config.option.label_unit.desc"));
        addControl(floatSlider(grid.controlX(), grid.rowY(4), grid.controlWidth(), 0.01F, 0.08F, state.labelScale, value -> state.labelScale = value, "axisruler.config.option.label_scale.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(5), grid.controlWidth(), 0, 255, state.boxFillAlpha, value -> state.boxFillAlpha = value, "axisruler.config.option.box_fill_alpha.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(6), grid.controlWidth(), 0, 255, state.guideAlpha, value -> state.guideAlpha = value, "axisruler.config.option.guide_alpha.desc"));
        addControl(intSlider(grid.controlX(), grid.rowY(7), grid.controlWidth(), 0, 255, state.lineAlpha, value -> state.lineAlpha = value, "axisruler.config.option.line_alpha.desc"));
    }

    private void buildPaletteTab(int x, int y, int width, int height) {
        PaletteLayout layout = paletteLayout(x, y, width, height);
        int chipY = paletteGroupY(layout);
        int chipWidth = (layout.listWidth - 34 - ROW_GAP) / 2;
        addControl(new PaletteGroupButtonWidget(layout.listX + 14, chipY, chipWidth, 20, ColorGroup.OVERLAY, activePaletteGroup == ColorGroup.OVERLAY, widget -> {
            activePaletteGroup = ColorGroup.OVERLAY;
            activeColorRole = firstRoleInGroup(ColorGroup.OVERLAY);
            rebuild();
        }));
        addControl(new PaletteGroupButtonWidget(layout.listX + 14 + chipWidth + ROW_GAP, chipY, chipWidth, 20, ColorGroup.HUD, activePaletteGroup == ColorGroup.HUD, widget -> {
            activePaletteGroup = ColorGroup.HUD;
            activeColorRole = firstRoleInGroup(ColorGroup.HUD);
            rebuild();
        }));

        int buttonY = paletteRoleButtonStartY(layout);
        for (ColorRole role : ColorRole.values()) {
            if (role.group != activePaletteGroup) {
                continue;
            }
            addControl(new ColorRoleButtonWidget(layout.listX + 14, buttonY, layout.listWidth - 28, 26, role, activeColorRole == role, state.color(role), widget -> {
                activeColorRole = role;
                activePaletteGroup = role.group;
                rebuild();
            }));
            buttonY += 26 + ROW_GAP;
        }

        int pickerY = palettePickerY(layout);
        colorPicker = new ColorPickerWidget(layout.centerX + 16, pickerY, layout.pickerWidth(), palettePickerHeight(layout), this::activeColor, this::setActiveColor);
        addControl(colorPicker);

        int fieldX = layout.rightX + 52;
        int fieldWidth = layout.rightWidth - (fieldX - layout.rightX) - 14;
        int cursorY = paletteFieldStartY(layout);
        redField = channelField(fieldX, cursorY, fieldWidth, this::setActiveRed);
        cursorY += CONTROL_HEIGHT + ROW_GAP;
        greenField = channelField(fieldX, cursorY, fieldWidth, this::setActiveGreen);
        cursorY += CONTROL_HEIGHT + ROW_GAP;
        blueField = channelField(fieldX, cursorY, fieldWidth, this::setActiveBlue);
        cursorY += CONTROL_HEIGHT + BLOCK_GAP;
        hexField = textField(fieldX, cursorY, fieldWidth, this::setActiveHex);
        syncColorFields();
        addControl(redField);
        addControl(greenField);
        addControl(blueField);
        addControl(hexField);

        int swatchGap = 8;
        int totalSwatchWidth = QUICK_COLORS.size() * PALETTE_SWATCH_SIZE + (QUICK_COLORS.size() - 1) * swatchGap;
        int presetX = layout.centerX + 16 + Math.max(0, (layout.pickerWidth() - totalSwatchWidth) / 2);
        int presetY = paletteSwatchY(layout);
        for (int i = 0; i < QUICK_COLORS.size(); i++) {
            int swatchColor = QUICK_COLORS.get(i);
            addControl(new SwatchButtonWidget(
                    presetX + i * (PALETTE_SWATCH_SIZE + swatchGap),
                    presetY,
                    PALETTE_SWATCH_SIZE,
                    PALETTE_SWATCH_SIZE,
                    swatchColor,
                    (activeColor() & 0x00FFFFFF) == (swatchColor & 0x00FFFFFF),
                    button -> {
                setActiveColor(ColorUtils.withAlpha(swatchColor, ColorUtils.alpha(activeColor())));
                syncColorFields();
            }));
        }

        int actionY = layout.actionsY + 16;
        int actionWidth = (layout.actionsWidth - ROW_GAP * 2) / 3;
        addControl(actionButton(layout.actionsX, actionY, "axisruler.config.action.copy_hex", ButtonVariant.SECONDARY, () -> {
            if (client != null) {
                client.keyboard.setClipboard(ColorUtils.toHex(activeColor(), true));
            }
        }, "axisruler.config.action.copy_hex.desc", actionWidth));
        addControl(actionButton(layout.actionsX + actionWidth + ROW_GAP, actionY, "axisruler.config.action.paste_hex", ButtonVariant.SECONDARY, () -> {
            if (client != null) {
                setActiveHex(client.keyboard.getClipboard());
                syncColorFields();
            }
        }, "axisruler.config.action.paste_hex.desc", actionWidth));
        addControl(actionButton(layout.actionsX + (actionWidth + ROW_GAP) * 2, actionY, "axisruler.config.action.reset_color", ButtonVariant.DANGER, () -> {
            setActiveColor(state.defaultColor(activeColorRole));
            syncColorFields();
        }, "axisruler.config.action.reset_color.desc", actionWidth));
    }

    private void buildPresetsTab(int x, int y, int width, int height) {
        PresetLayout layout = presetLayout(x, y, width, height);
        StylePreset activePreset = state.activeStylePreset();
        for (StylePreset preset : StylePreset.values()) {
            int presetIndex = preset.ordinal();
            int column = presetIndex % layout.columns();
            int row = presetIndex / layout.columns();
            int buttonX = layout.gridX() + column * (layout.cardWidth() + BLOCK_GAP);
            int buttonY = layout.gridY() + row * (PRESET_CARD_HEIGHT + ROW_GAP);
            PresetButtonWidget button = new PresetButtonWidget(buttonX, buttonY, layout.cardWidth(), PRESET_CARD_HEIGHT, preset, preset == activePreset, () -> {
                state.applyPreset(preset);
                pushPreview();
                rebuild();
            });
            button.setTooltip(Tooltip.of(Text.translatable(preset.descriptionKey)));
            addControl(button);
        }

        customPresetField = textField(layout.formX(), layout.fieldY(), layout.fieldWidth(), value -> {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                state.customPresetName = trimmed;
            }
        });
        customPresetField.setText(state.customPresetName);
        addControl(customPresetField);
        addControl(actionButton(layout.saveButtonX(), layout.fieldY(), "axisruler.config.action.save_custom_preset", ButtonVariant.PRIMARY, () -> {
            state.saveAsCustomPreset();
            pushPreview();
            rebuild();
        }, "axisruler.config.action.save_custom_preset.desc", layout.saveButtonWidth()));
        addControl(actionButton(layout.formX(), layout.resetY(), "axisruler.config.action.reset_all", ButtonVariant.DANGER, () -> {
            state.resetAll();
            pushPreview();
            rebuild();
        }, "axisruler.config.action.reset_all.desc", Math.min(186, layout.formWidth())));
    }

    private ClickableWidget actionButton(int x, int y, String key, ButtonVariant variant, PressAction action, String descriptionKey, int width) {
        AxisButtonWidget widget = new AxisButtonWidget(x, y, width, 20, Text.translatable(key), variant, action::onPress);
        widget.setTooltip(Tooltip.of(Text.translatable(descriptionKey)));
        return widget;
    }

    private ClickableWidget toggleButton(
            int x,
            int y,
            int width,
            BooleanSupplier getter,
            Consumer<Boolean> setter,
            String labelKey,
            String descriptionKey
    ) {
        ToggleWidget widget = new ToggleWidget(x, y, width, 22, getter.getAsBoolean(), () -> {
            setter.accept(!getter.getAsBoolean());
            pushPreview();
            rebuild();
        });
        widget.setTooltip(Tooltip.of(Text.translatable(descriptionKey)));
        return widget;
    }

    private ClickableWidget cycleButton(int x, int y, int width, Text value, String descriptionKey, PressAction action) {
        AxisButtonWidget widget = new AxisButtonWidget(x, y, width, 22, value, ButtonVariant.SECONDARY, action::onPress);
        widget.setTooltip(Tooltip.of(Text.translatable(descriptionKey)));
        return widget;
    }

    private ClickableWidget floatSlider(
            int x,
            int y,
            int width,
            float min,
            float max,
            float current,
            Consumer<Float> setter,
            String descriptionKey
    ) {
        SliderWidget slider = new SliderWidget(x, y, width, 20, min, max, current, value -> {
            setter.accept(value);
            pushPreview();
        });
        slider.setTooltip(Tooltip.of(Text.translatable(descriptionKey)));
        return slider;
    }

    private ClickableWidget intSlider(
            int x,
            int y,
            int width,
            int min,
            int max,
            int current,
            Consumer<Integer> setter,
            String descriptionKey
    ) {
        SliderWidget slider = new SliderWidget(x, y, width, 20, min, max, current, value -> {
            setter.accept(Math.round(value));
            pushPreview();
        });
        slider.setTooltip(Tooltip.of(Text.translatable(descriptionKey)));
        return slider;
    }

    private TextFieldWidget channelField(int x, int y, int width, Consumer<String> changeHandler) {
        return textField(x, y, width, changeHandler);
    }

    private TextFieldWidget textField(int x, int y, int width, Consumer<String> changeHandler) {
        TextFieldWidget field = new AxisTextFieldWidget(textRenderer, x, y, width, CONTROL_HEIGHT);
        field.setMaxLength(32);
        field.setDrawsBackground(false);
        field.setEditableColor(TEXT_PRIMARY);
        field.setUneditableColor(TEXT_DISABLED);
        field.setChangedListener(changeHandler::accept);
        return field;
    }

    private void addControl(ClickableWidget widget) {
        dynamicWidgets.add(widget);
        addDrawableChild(widget);
    }

    private void addControl(TextFieldWidget field) {
        addDrawableChild(field);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawStableBackground(context);
        int left = 24;
        int right = width - 24;
        int contentY = 60;
        int contentHeight = height - 110;
        drawPanel(context, left, contentY, right - left, contentHeight, PANEL);
        drawPanel(context, left + 1, contentY + 1, right - left - 2, 40, PANEL_ALT);
        context.drawTextWithShadow(textRenderer, title, left + 18, contentY + 14, TEXT_PRIMARY);
        context.drawText(textRenderer, Text.translatable(activeTab.descriptionKey), left + 18, contentY + 30, TEXT_MUTED, false);

        switch (activeTab) {
            case CORE -> renderCoreLabels(context, left, contentY);
            case HUD_LAYOUT -> renderHudLabels(context, left, contentY);
            case OVERLAY -> renderOverlayLabels(context, left, contentY);
            case DIMENSION_LABELS -> renderLabelLabels(context, left, contentY);
            case PALETTE -> renderPaletteLabels(context, left, contentY);
            case STYLE_PRESETS -> renderPresetLabels(context, left, contentY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawStableBackground(DrawContext context) {
        int midY = height / 2;
        context.fillGradient(0, 0, width, midY, 0xF010141A, BACKGROUND);
        context.fillGradient(0, midY, width, height, BACKGROUND, 0xF0080B10);
        context.fillGradient(0, 0, width, height, 0x14000000, 0x00000000);
    }

    @Override
    public void close() {
        if (colorPicker != null) {
            colorPicker.close();
            colorPicker = null;
        }
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

    private void drawPanel(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
        context.fill(x, y, x + width, y + 1, PANEL_BORDER);
        context.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER);
        context.fill(x, y, x + 1, y + height, PANEL_BORDER);
        context.fill(x + width - 1, y, x + width, y + height, PANEL_BORDER);
    }

    private void renderCoreLabels(DrawContext context, int x, int y) {
        FormGrid grid = formGrid(x, y, width - 48);
        drawFormRow(context, grid, 0, "axisruler.config.option.enabled", "axisruler.config.option.enabled.desc");
        drawFormRow(context, grid, 1, "axisruler.config.option.two_points_only", "axisruler.config.option.two_points_only.desc");
    }

    private void renderHudLabels(DrawContext context, int x, int y) {
        FormGrid grid = formGrid(x, y, width - 48);
        drawFormRow(context, grid, 0, "axisruler.config.option.hud_enabled", "axisruler.config.option.hud_enabled.desc");
        drawFormRow(context, grid, 1, "axisruler.config.option.hud_compact", "axisruler.config.option.hud_compact.desc");
        drawFormRow(context, grid, 2, "axisruler.config.option.hud_anchor", "axisruler.config.option.hud_anchor.desc");
        drawFormRow(context, grid, 3, "axisruler.config.option.hud_scale", "axisruler.config.option.hud_scale.desc");
        drawFormRow(context, grid, 4, "axisruler.config.option.hud_offset_x", "axisruler.config.option.hud_offset_x.desc");
        drawFormRow(context, grid, 5, "axisruler.config.option.hud_offset_y", "axisruler.config.option.hud_offset_y.desc");
        drawFormRow(context, grid, 6, "axisruler.config.option.hud_background_alpha", "axisruler.config.option.hud_background_alpha.desc");
        drawFormRow(context, grid, 7, "axisruler.config.option.hud_border_alpha", "axisruler.config.option.hud_border_alpha.desc");
        drawFormRow(context, grid, 8, "axisruler.config.option.hud_text_shadow", "axisruler.config.option.hud_text_shadow.desc");
        drawHudPreview(context, grid.rowX(), grid.rowY(9) + SECTION_GAP, grid.rowWidth(), 96);
    }

    private void renderOverlayLabels(DrawContext context, int x, int y) {
        FormGrid grid = formGrid(x, y, width - 48);
        drawFormRow(context, grid, 0, "axisruler.config.option.overlay_enabled", "axisruler.config.option.overlay_enabled.desc");
        drawFormRow(context, grid, 1, "axisruler.config.option.overlay_fill", "axisruler.config.option.overlay_fill.desc");
        drawFormRow(context, grid, 2, "axisruler.config.option.overlay_outline", "axisruler.config.option.overlay_outline.desc");
        drawFormRow(context, grid, 3, "axisruler.config.option.overlay_guides", "axisruler.config.option.overlay_guides.desc");
        drawFormRow(context, grid, 4, "axisruler.config.option.overlay_line", "axisruler.config.option.overlay_line.desc");
        drawFormRow(context, grid, 5, "axisruler.config.option.overlay_center_marker", "axisruler.config.option.overlay_center_marker.desc");
        drawFormRow(context, grid, 6, "axisruler.config.option.overlay_line_thickness", "axisruler.config.option.overlay_line_thickness.desc");
        drawFormRow(context, grid, 7, "axisruler.config.option.overlay_callout_offset", "axisruler.config.option.overlay_callout_offset.desc");
        drawFormRow(context, grid, 8, "axisruler.config.option.overlay_tick_size", "axisruler.config.option.overlay_tick_size.desc");
    }

    private void renderLabelLabels(DrawContext context, int x, int y) {
        FormGrid grid = formGrid(x, y, width - 48);
        drawFormRow(context, grid, 0, "axisruler.config.option.labels_enabled", "axisruler.config.option.labels_enabled.desc");
        drawFormRow(context, grid, 1, "axisruler.config.option.label_background", "axisruler.config.option.label_background.desc");
        drawFormRow(context, grid, 2, "axisruler.config.option.label_billboard", "axisruler.config.option.label_billboard.desc");
        drawFormRow(context, grid, 3, "axisruler.config.option.label_unit", "axisruler.config.option.label_unit.desc");
        drawFormRow(context, grid, 4, "axisruler.config.option.label_scale", "axisruler.config.option.label_scale.desc");
        drawFormRow(context, grid, 5, "axisruler.config.option.box_fill_alpha", "axisruler.config.option.box_fill_alpha.desc");
        drawFormRow(context, grid, 6, "axisruler.config.option.guide_alpha", "axisruler.config.option.guide_alpha.desc");
        drawFormRow(context, grid, 7, "axisruler.config.option.line_alpha", "axisruler.config.option.line_alpha.desc");
    }

    private void renderPaletteLabels(DrawContext context, int x, int y) {
        PaletteLayout layout = paletteLayout(x, y, width - 48, height - 110);
        drawPanel(context, layout.listX, layout.listY, layout.listWidth, layout.listHeight, PANEL_SUBTLE);
        drawPanel(context, layout.centerX, layout.centerY, layout.centerWidth, layout.centerHeight, PANEL_SUBTLE);
        drawPanel(context, layout.rightX, layout.rightY, layout.rightWidth, layout.rightHeight, PANEL_SUBTLE);
        drawPanel(context, layout.actionsX, layout.actionsY, layout.actionsWidth, layout.actionsHeight, PANEL_SUBTLE);

        drawSectionHeader(context, layout.listX + 14, layout.listY + 14, "axisruler.config.palette.role_title", "axisruler.config.palette.role_desc", layout.listWidth - 28);
        drawSectionHeader(context, layout.centerX + 16, layout.centerY + 14, "axisruler.config.palette.editor_title", activeColorRole.descriptionKey, layout.centerWidth - 32);
        drawSectionHeader(context, layout.rightX + 14, layout.rightY + 14, activeColorRole.labelKey, activeColorRole.descriptionKey, layout.rightWidth - 28);
        drawPaletteGroups(context, layout);

        int rightContentY = paletteRightContentY(layout);
        int previewX = layout.rightX + 14;
        int previewY = rightContentY;
        int previewWidth = layout.rightWidth - 28;
        if (activeColorRole.isHud()) {
            drawHudPreviewPanel(context, previewX, previewY, previewWidth, previewBlockHeight());
        } else {
            context.fill(previewX, previewY, previewX + previewWidth, previewY + previewBlockHeight(), 0x50131A23);
            context.fill(previewX + 1, previewY + 1, previewX + previewWidth - 1, previewY + previewBlockHeight() - 1, activeColor());
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(ColorUtils.toHex(activeColor(), true)), previewX + previewWidth / 2, previewY + 9, ColorUtils.alpha(activeColor()) < 90 ? TEXT_PRIMARY : 0xFF0A0E13);
        }

        int cursorY = paletteFieldStartY(layout) - 6;
        drawFieldLabel(context, layout.rightX + 14, cursorY, "R");
        cursorY += CONTROL_HEIGHT + ROW_GAP;
        drawFieldLabel(context, layout.rightX + 14, cursorY, "G");
        cursorY += CONTROL_HEIGHT + ROW_GAP;
        drawFieldLabel(context, layout.rightX + 14, cursorY, "B");
        cursorY += CONTROL_HEIGHT + BLOCK_GAP;
        drawFieldLabel(context, layout.rightX + 14, cursorY, "HEX");
    }

    private void renderPresetLabels(DrawContext context, int x, int y) {
        PresetLayout layout = presetLayout(x, y, width - 48, height - 110);
        drawSectionHeader(
                context,
                layout.contentX(),
                layout.headerY(),
                "axisruler.config.presets.title",
                "axisruler.config.presets.desc",
                layout.contentWidth()
        );
        context.drawTextWithShadow(
                textRenderer,
                Text.translatable("axisruler.config.presets.custom_name"),
                layout.formX(),
                layout.labelY(),
                TEXT_PRIMARY
        );
    }

    private void drawFormRow(DrawContext context, FormGrid grid, int row, String titleKey, String descriptionKey) {
        int rowY = grid.rowY(row);
        int rowBottom = rowY + ROW_HEIGHT - 6;
        context.fill(grid.rowX(), rowY - 6, grid.rowX() + grid.rowWidth(), rowBottom, row % 2 == 0 ? ROW_BACKGROUND : ROW_BACKGROUND_HOVER);
        context.drawTextWithShadow(textRenderer, Text.translatable(titleKey), grid.labelX(), rowY, TEXT_PRIMARY);
        drawWrappedText(context, Text.translatable(descriptionKey), grid.labelX(), rowY + 14, grid.labelWidth(), TEXT_SECONDARY);
    }

    private void drawSectionHeader(DrawContext context, int x, int y, String titleKey, String descriptionKey, int width) {
        context.drawTextWithShadow(textRenderer, Text.translatable(titleKey), x, y, TEXT_PRIMARY);
        drawWrappedText(context, Text.translatable(descriptionKey), x, y + 14, width, TEXT_SECONDARY);
    }

    private void drawWrappedText(DrawContext context, Text text, int x, int y, int width, int color) {
        int drawY = y;
        for (OrderedText line : textRenderer.wrapLines(text, Math.max(10, width))) {
            context.drawText(textRenderer, line, x, drawY, color, false);
            drawY += TEXT_LINE_HEIGHT;
        }
    }

    private void drawFieldLabel(DrawContext context, int x, int y, String value) {
        context.drawText(textRenderer, Text.literal(value), x, y, TEXT_MUTED, false);
    }

    private void drawPaletteGroups(DrawContext context, PaletteLayout layout) {
        int chipY = paletteGroupY(layout);
        int listTitleY = chipY + 20 + BLOCK_GAP;
        context.drawTextWithShadow(textRenderer, Text.translatable(activePaletteGroup.titleKey), layout.listX + 14, listTitleY, TEXT_MUTED);
    }

    private void drawHudPreview(DrawContext context, int x, int y, int width, int height) {
        drawPanel(context, x, y, width, height, PANEL_SUBTLE);
        context.drawTextWithShadow(textRenderer, Text.translatable("axisruler.config.hud_preview.title"), x + 14, y + 12, TEXT_PRIMARY);
        context.drawText(textRenderer, Text.translatable("axisruler.config.hud_preview.desc"), x + 14, y + 24, TEXT_SECONDARY, false);
        drawHudPreviewPanel(context, x + 14, y + 40, Math.min(250, width - 28), height - 54);
    }

    private void drawHudPreviewPanel(DrawContext context, int x, int y, int width, int height) {
        int panelWidth = Math.max(160, width);
        int panelHeight = Math.max(44, height);
        context.fill(x, y, x + panelWidth, y + panelHeight, state.effectiveHudBackgroundColor());
        context.fill(x, y, x + 2, y + panelHeight, state.effectiveHudAccentColor());
        context.drawStrokedRectangle(x, y, panelWidth, panelHeight, state.effectiveHudBorderColor());
        int titleY = y + 6;
        drawPreviewText(context, Text.translatable("axisruler.hud.title"), x + 8, titleY, state.hudTitleColor);
        int separatorY = y + 16;
        context.fill(x + 8, separatorY, x + panelWidth - 8, separatorY + 1, state.effectiveHudAccentColor());
        int rowY = y + 22;
        drawPreviewText(context, Text.translatable("axisruler.hud.label.size"), x + 8, rowY, state.hudSecondaryTextColor);
        drawPreviewText(context, Text.literal("48 x 16 x 32"), x + 58, rowY, state.hudPrimaryTextColor);
        drawPreviewText(context, Text.translatable("axisruler.hud.label.warn"), x + 8, rowY + 10, state.hudSecondaryTextColor);
        drawPreviewText(context, Text.translatable("axisruler.hud.warning.different_worlds"), x + 58, rowY + 10, state.hudWarningColor);
    }

    private void drawPreviewText(DrawContext context, Text text, int x, int y, int color) {
        if (state.hudTextShadow) {
            context.drawTextWithShadow(textRenderer, text, x, y, color);
            return;
        }
        context.drawText(textRenderer, text, x, y, color, false);
    }

    private int paletteRightContentY(PaletteLayout layout) {
        int cursorY = layout.rightY + 14;
        cursorY += sectionHeaderHeight(activeColorRole.labelKey, activeColorRole.descriptionKey, layout.rightWidth - 28);
        cursorY += BLOCK_GAP;
        return cursorY;
    }

    private int paletteGroupY(PaletteLayout layout) {
        return layout.listY + 14 + sectionHeaderHeight("axisruler.config.palette.role_title", "axisruler.config.palette.role_desc", layout.listWidth - 28) + BLOCK_GAP;
    }

    private int paletteRoleButtonStartY(PaletteLayout layout) {
        return paletteGroupY(layout) + CONTROL_HEIGHT + BLOCK_GAP + 12;
    }

    private int palettePickerY(PaletteLayout layout) {
        return layout.centerY + 14 + sectionHeaderHeight("axisruler.config.palette.editor_title", activeColorRole.descriptionKey, layout.centerWidth - 32) + BLOCK_GAP;
    }

    private int palettePickerHeight(PaletteLayout layout) {
        int headerHeight = sectionHeaderHeight("axisruler.config.palette.editor_title", activeColorRole.descriptionKey, layout.centerWidth - 32);
        return layout.pickerHeight(headerHeight);
    }

    private int paletteSwatchY(PaletteLayout layout) {
        return palettePickerY(layout) + palettePickerHeight(layout) + BLOCK_GAP;
    }

    private int paletteFieldStartY(PaletteLayout layout) {
        return paletteRightContentY(layout) + previewBlockHeight() + BLOCK_GAP;
    }

    private int presetsContentStartY(int y) {
        return y + HEADER_HEIGHT + PANEL_PADDING;
    }

    private int presetsHeaderHeight(int contentWidth) {
        return 14 + wrappedTextHeight(Text.translatable("axisruler.config.presets.desc"), contentWidth);
    }

    private int sectionHeaderHeight(String titleKey, String descriptionKey, int width) {
        return 14 + wrappedTextHeight(Text.translatable(descriptionKey), width);
    }

    private int wrappedTextHeight(Text text, int width) {
        return Math.max(TEXT_LINE_HEIGHT, textRenderer.wrapLines(text, Math.max(10, width)).size() * TEXT_LINE_HEIGHT);
    }

    private int previewBlockHeight() {
        return activeColorRole.isHud() ? 56 : 26;
    }

    private ColorRole firstRoleInGroup(ColorGroup group) {
        for (ColorRole role : ColorRole.values()) {
            if (role.group == group) {
                return role;
            }
        }
        return ColorRole.POINT_A;
    }

    private FormGrid formGrid(int x, int y, int width) {
        int contentX = x + PANEL_PADDING;
        int bodyY = y + HEADER_HEIGHT + PANEL_PADDING;
        int availableWidth = width - PANEL_PADDING * 2;
        int controlWidth = MathHelper.clamp(availableWidth / 3, 170, 260);
        int labelWidth = availableWidth - controlWidth - LABEL_GAP;
        return new FormGrid(contentX, bodyY, labelWidth, controlWidth, availableWidth);
    }

    private PresetLayout presetLayout(int x, int y, int width, int height) {
        int contentX = x + PANEL_PADDING;
        int contentWidth = width - PANEL_PADDING * 2;
        int headerY = presetsContentStartY(y) - BLOCK_GAP;
        int headerHeight = presetsHeaderHeight(contentWidth);
        int columns = Math.max(1, Math.min(3, (contentWidth + BLOCK_GAP) / (PRESET_MIN_WIDTH + BLOCK_GAP)));
        int cardWidth = (contentWidth - BLOCK_GAP * (columns - 1)) / columns;
        int rows = (StylePreset.values().length + columns - 1) / columns;
        int gridY = headerY + headerHeight + SECTION_GAP;
        int labelY = gridY + rows * PRESET_CARD_HEIGHT + Math.max(0, rows - 1) * ROW_GAP + SECTION_GAP;
        int fieldY = labelY + 14 + ROW_GAP;
        int formWidth = contentWidth;
        int saveButtonWidth = MathHelper.clamp(formWidth / 3, 148, 210);
        int fieldWidth = Math.max(140, formWidth - saveButtonWidth - BLOCK_GAP);
        int resetY = fieldY + CONTROL_HEIGHT + SECTION_GAP;
        return new PresetLayout(contentX, contentWidth, headerY, gridY, columns, cardWidth, labelY, fieldY, formWidth, fieldWidth, resetY);
    }

    private PaletteLayout paletteLayout(int x, int y, int width, int height) {
        int contentX = x + PANEL_PADDING;
        int contentY = y + HEADER_HEIGHT + PANEL_PADDING;
        int contentWidth = width - PANEL_PADDING * 2;
        int actionsHeight = 52;
        int topHeight = Math.max(228, height - HEADER_HEIGHT - PANEL_PADDING * 2 - actionsHeight - 14);
        int gap = 14;
        int listWidth = MathHelper.clamp(contentWidth / 4, 170, 210);
        int rightWidth = MathHelper.clamp(contentWidth / 4, 180, 220);
        int centerWidth = contentWidth - listWidth - rightWidth - gap * 2;
        if (centerWidth < 260) {
            int shortage = 260 - centerWidth;
            int reduceLeft = Math.min(shortage / 2, Math.max(0, listWidth - 160));
            listWidth -= reduceLeft;
            shortage -= reduceLeft;
            int reduceRight = Math.min(shortage, Math.max(0, rightWidth - 168));
            rightWidth -= reduceRight;
            centerWidth = contentWidth - listWidth - rightWidth - gap * 2;
        }
        int listX = contentX;
        int centerX = listX + listWidth + gap;
        int rightX = centerX + centerWidth + gap;
        int actionsY = contentY + topHeight + gap;
        return new PaletteLayout(
                listX, contentY, listWidth, topHeight,
                centerX, contentY, centerWidth, topHeight,
                rightX, contentY, rightWidth, topHeight,
                contentX, actionsY, contentWidth, actionsHeight
        );
    }

    private int activeColor() {
        return state.color(activeColorRole);
    }

    private void setActiveColor(int argb) {
        state.setColor(activeColorRole, argb);
        syncColorFields();
        pushPreview();
    }

    private void setActiveRed(String value) {
        if (synchronizingFields || !isInteger(value)) {
            return;
        }
        int color = activeColor();
        setActiveColor(ColorUtils.withAlpha(ColorUtils.rgb(parseInt(value), ColorUtils.green(color), ColorUtils.blue(color)), ColorUtils.alpha(color)));
    }

    private void setActiveGreen(String value) {
        if (synchronizingFields || !isInteger(value)) {
            return;
        }
        int color = activeColor();
        setActiveColor(ColorUtils.withAlpha(ColorUtils.rgb(ColorUtils.red(color), parseInt(value), ColorUtils.blue(color)), ColorUtils.alpha(color)));
    }

    private void setActiveBlue(String value) {
        if (synchronizingFields || !isInteger(value)) {
            return;
        }
        int color = activeColor();
        setActiveColor(ColorUtils.withAlpha(ColorUtils.rgb(ColorUtils.red(color), ColorUtils.green(color), parseInt(value)), ColorUtils.alpha(color)));
    }

    private void setActiveHex(String value) {
        if (synchronizingFields) {
            return;
        }
        int parsed = ColorUtils.parseHex(value, Integer.MIN_VALUE);
        if (parsed != Integer.MIN_VALUE) {
            setActiveColor(parsed);
        }
    }

    private void syncColorFields() {
        if (redField == null || greenField == null || blueField == null || hexField == null) {
            return;
        }
        synchronizingFields = true;
        int color = activeColor();
        redField.setText(Integer.toString(ColorUtils.red(color)));
        greenField.setText(Integer.toString(ColorUtils.green(color)));
        blueField.setText(Integer.toString(ColorUtils.blue(color)));
        hexField.setText(ColorUtils.toHex(color, true));
        synchronizingFields = false;
    }

    private boolean isInteger(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 0 && parsed <= 255;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private int parseInt(String value) {
        return MathHelper.clamp(Integer.parseInt(value), 0, 255);
    }

    @FunctionalInterface
    private interface PressAction {
        void onPress();
    }

    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }

    @FunctionalInterface
    private interface IntSupplier {
        int getAsInt();
    }

    @FunctionalInterface
    private interface IntConsumer {
        void accept(int value);
    }

    private record FormGrid(int contentX, int bodyY, int labelWidth, int controlWidth, int rowWidth) {
        private int labelX() {
            return contentX;
        }

        private int controlX() {
            return contentX + labelWidth + LABEL_GAP;
        }

        private int rowX() {
            return contentX - 8;
        }

        private int rowY(int row) {
            return bodyY + row * ROW_HEIGHT;
        }

        private int actionsY() {
            return bodyY + 2 * ROW_HEIGHT + SECTION_GAP;
        }
    }

    private record PresetLayout(
            int contentX,
            int contentWidth,
            int headerY,
            int gridY,
            int columns,
            int cardWidth,
            int labelY,
            int fieldY,
            int formWidth,
            int fieldWidth,
            int resetY
    ) {
        private int gridX() {
            return contentX;
        }

        private int formX() {
            return contentX;
        }

        private int saveButtonX() {
            return formX() + fieldWidth + BLOCK_GAP;
        }

        private int saveButtonWidth() {
            return formWidth - fieldWidth - BLOCK_GAP;
        }
    }

    private record PaletteLayout(
            int listX, int listY, int listWidth, int listHeight,
            int centerX, int centerY, int centerWidth, int centerHeight,
            int rightX, int rightY, int rightWidth, int rightHeight,
            int actionsX, int actionsY, int actionsWidth, int actionsHeight
    ) {
        private int pickerWidth() {
            return centerWidth - 32;
        }

        private int pickerHeight(int headerHeight) {
            int availableHeight = centerHeight - 14 - headerHeight - BLOCK_GAP - PALETTE_SWATCH_SIZE - BLOCK_GAP - PALETTE_ALPHA_HEIGHT - 14;
            return Math.max(132, availableHeight);
        }
    }

    private enum Tab {
        CORE("axisruler.config.tab.core", "axisruler.config.tab.core.desc"),
        HUD_LAYOUT("axisruler.config.tab.hud", "axisruler.config.tab.hud.desc"),
        OVERLAY("axisruler.config.tab.overlay", "axisruler.config.tab.overlay.desc"),
        DIMENSION_LABELS("axisruler.config.tab.labels", "axisruler.config.tab.labels.desc"),
        PALETTE("axisruler.config.tab.palette", "axisruler.config.tab.palette.desc"),
        STYLE_PRESETS("axisruler.config.tab.presets", "axisruler.config.tab.presets.desc");

        private final String titleKey;
        private final String descriptionKey;

        Tab(String titleKey, String descriptionKey) {
            this.titleKey = titleKey;
            this.descriptionKey = descriptionKey;
        }
    }

    private enum StylePreset {
        CLASSIC("axisruler.config.preset.classic", "axisruler.config.preset.classic.desc"),
        EMERALD("axisruler.config.preset.emerald", "axisruler.config.preset.emerald.desc"),
        MINIMAL_WHITE("axisruler.config.preset.minimal_white", "axisruler.config.preset.minimal_white.desc"),
        BLUEPRINT("axisruler.config.preset.blueprint", "axisruler.config.preset.blueprint.desc"),
        WARM_BUILDER("axisruler.config.preset.warm_builder", "axisruler.config.preset.warm_builder.desc"),
        CUSTOM_SAVED("axisruler.config.preset.custom_saved", "axisruler.config.preset.custom_saved.desc");

        private final String translationKey;
        private final String descriptionKey;

        StylePreset(String translationKey, String descriptionKey) {
            this.translationKey = translationKey;
            this.descriptionKey = descriptionKey;
        }
    }

    private enum ColorRole {
        POINT_A(ColorGroup.OVERLAY, "axisruler.config.option.color_point_a", "axisruler.config.option.color_point_a.desc"),
        POINT_B(ColorGroup.OVERLAY, "axisruler.config.option.color_point_b", "axisruler.config.option.color_point_b.desc"),
        BOX(ColorGroup.OVERLAY, "axisruler.config.option.color_box", "axisruler.config.option.color_box.desc"),
        CONNECTION_LINE(ColorGroup.OVERLAY, "axisruler.config.option.color_connection_line", "axisruler.config.option.color_connection_line.desc"),
        X_GUIDE(ColorGroup.OVERLAY, "axisruler.config.option.color_x_guide", "axisruler.config.option.color_x_guide.desc"),
        Y_GUIDE(ColorGroup.OVERLAY, "axisruler.config.option.color_y_guide", "axisruler.config.option.color_y_guide.desc"),
        Z_GUIDE(ColorGroup.OVERLAY, "axisruler.config.option.color_z_guide", "axisruler.config.option.color_z_guide.desc"),
        LABEL_TEXT(ColorGroup.OVERLAY, "axisruler.config.option.color_label", "axisruler.config.option.color_label.desc"),
        LABEL_BACKGROUND(ColorGroup.OVERLAY, "axisruler.config.option.color_label_background", "axisruler.config.option.color_label_background.desc"),
        HUD_TITLE(ColorGroup.HUD, "axisruler.config.option.hud_color_title", "axisruler.config.option.hud_color_title.desc"),
        HUD_PRIMARY_TEXT(ColorGroup.HUD, "axisruler.config.option.hud_color_primary", "axisruler.config.option.hud_color_primary.desc"),
        HUD_SECONDARY_TEXT(ColorGroup.HUD, "axisruler.config.option.hud_color_secondary", "axisruler.config.option.hud_color_secondary.desc"),
        HUD_BACKGROUND(ColorGroup.HUD, "axisruler.config.option.hud_color_background", "axisruler.config.option.hud_color_background.desc"),
        HUD_BORDER(ColorGroup.HUD, "axisruler.config.option.hud_color_border", "axisruler.config.option.hud_color_border.desc"),
        HUD_WARNING(ColorGroup.HUD, "axisruler.config.option.hud_color_warning", "axisruler.config.option.hud_color_warning.desc"),
        HUD_ACCENT(ColorGroup.HUD, "axisruler.config.option.hud_color_accent", "axisruler.config.option.hud_color_accent.desc");

        private final ColorGroup group;
        private final String labelKey;
        private final String descriptionKey;

        ColorRole(ColorGroup group, String labelKey, String descriptionKey) {
            this.group = group;
            this.labelKey = labelKey;
            this.descriptionKey = descriptionKey;
        }

        private boolean isHud() {
            return group == ColorGroup.HUD;
        }
    }

    private enum ColorGroup {
        OVERLAY("axisruler.config.palette.group_overlay"),
        HUD("axisruler.config.palette.group_hud");

        private final String titleKey;

        ColorGroup(String titleKey) {
            this.titleKey = titleKey;
        }
    }

    private enum ButtonVariant {
        PRIMARY(0xE0223447, 0xFF7BD8FF, 0xFF101922, 0xFFEDF8FF, 0xFF9AE4FF, 0xFF131E29, 0x80121A24),
        SECONDARY(0xD118202A, 0x80516374, 0xFF1A2430, 0xFFF4F8FC, 0xFF7BD8FF, 0xFF121820, 0x80131A24),
        DANGER(0xD12A1B22, 0xA0C95C74, 0xFF382029, 0xFFFFF1F3, 0xFFFF9BB3, 0xFF1D1418, 0x801A1417),
        TAB(0x7A1A2330, 0x80516374, 0xFF24384D, 0xFFF4F8FC, 0xFF7BD8FF, 0xFF10161E, 0x70202834),
        GHOST(0x7A141B24, 0x70516374, 0xA61A2430, 0xFFE7EEF5, 0xFF7BD8FF, 0xFF10161E, 0x50131921),
        SUBTLE(0xAA18222D, 0x70516374, 0xCC213140, 0xFFF0F5FA, 0xFF8FDEFF, 0xFF111920, 0x70131A22);

        private final int fill;
        private final int border;
        private final int hoverFill;
        private final int text;
        private final int hoverGlow;
        private final int pressedFill;
        private final int disabledFill;

        ButtonVariant(int fill, int border, int hoverFill, int text, int hoverGlow, int pressedFill, int disabledFill) {
            this.fill = fill;
            this.border = border;
            this.hoverFill = hoverFill;
            this.text = text;
            this.hoverGlow = hoverGlow;
            this.pressedFill = pressedFill;
            this.disabledFill = disabledFill;
        }
    }

    private static final class SavedCustomPreset {
        private final String pointAColor;
        private final String pointBColor;
        private final String boxColor;
        private final String connectionLineColor;
        private final String xGuideColor;
        private final String yGuideColor;
        private final String zGuideColor;
        private final String labelColor;
        private final String labelBackgroundColor;
        private final String labelBackgroundMode;
        private final String hudTitleColor;
        private final String hudPrimaryTextColor;
        private final String hudSecondaryTextColor;
        private final String hudAccentColor;
        private final String hudBackgroundColor;
        private final String hudBorderColor;
        private final String hudWarningColor;
        private final int hudBackgroundAlpha;
        private final int hudBorderAlpha;
        private final boolean hudTextShadow;
        private final float labelScale;
        private final float lineThickness;
        private final float calloutOffset;
        private final float tickSize;
        private final int boxFillAlpha;
        private final int guideAlpha;
        private final int lineAlpha;

        private SavedCustomPreset(
                String pointAColor,
                String pointBColor,
                String boxColor,
                String connectionLineColor,
                String xGuideColor,
                String yGuideColor,
                String zGuideColor,
                String labelColor,
                String labelBackgroundColor,
                String labelBackgroundMode,
                String hudTitleColor,
                String hudPrimaryTextColor,
                String hudSecondaryTextColor,
                String hudAccentColor,
                String hudBackgroundColor,
                String hudBorderColor,
                String hudWarningColor,
                int hudBackgroundAlpha,
                int hudBorderAlpha,
                boolean hudTextShadow,
                float labelScale,
                float lineThickness,
                float calloutOffset,
                float tickSize,
                int boxFillAlpha,
                int guideAlpha,
                int lineAlpha
        ) {
            this.pointAColor = pointAColor;
            this.pointBColor = pointBColor;
            this.boxColor = boxColor;
            this.connectionLineColor = connectionLineColor;
            this.xGuideColor = xGuideColor;
            this.yGuideColor = yGuideColor;
            this.zGuideColor = zGuideColor;
            this.labelColor = labelColor;
            this.labelBackgroundColor = labelBackgroundColor;
            this.labelBackgroundMode = labelBackgroundMode;
            this.hudTitleColor = hudTitleColor;
            this.hudPrimaryTextColor = hudPrimaryTextColor;
            this.hudSecondaryTextColor = hudSecondaryTextColor;
            this.hudAccentColor = hudAccentColor;
            this.hudBackgroundColor = hudBackgroundColor;
            this.hudBorderColor = hudBorderColor;
            this.hudWarningColor = hudWarningColor;
            this.hudBackgroundAlpha = hudBackgroundAlpha;
            this.hudBorderAlpha = hudBorderAlpha;
            this.hudTextShadow = hudTextShadow;
            this.labelScale = labelScale;
            this.lineThickness = lineThickness;
            this.calloutOffset = calloutOffset;
            this.tickSize = tickSize;
            this.boxFillAlpha = boxFillAlpha;
            this.guideAlpha = guideAlpha;
            this.lineAlpha = lineAlpha;
        }
    }

    private static final class MutableConfig {
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
        private SavedCustomPreset savedCustomPreset;

        private static MutableConfig from(AxisRulerConfig config) {
            MutableConfig state = new MutableConfig();
            state.enabled = config.enabled();
            state.hudEnabledDefault = config.hudEnabledDefault();
            state.hudCompactDefault = config.hudCompactDefault();
            state.guidesEnabledDefault = config.guidesEnabledDefault();
            state.renderEnabledDefault = config.renderEnabledDefault();
            state.labelsEnabledDefault = config.labelsEnabledDefault();
            state.fillEnabledDefault = config.fillEnabledDefault();
            state.outlineEnabledDefault = config.outlineEnabledDefault();
            state.lineEnabledDefault = config.lineEnabledDefault();
            state.showCenterMarkerDefault = config.showCenterMarkerDefault();
            state.showOnlyWithTwoPointsDefault = config.showOnlyWithTwoPointsDefault();
            state.pointAColor = config.pointAColorArgb();
            state.pointBColor = config.pointBColorArgb();
            state.boxColor = config.boxColorArgb();
            state.connectionLineColor = config.connectionLineColorArgb();
            state.xGuideColor = config.xGuideColorArgb();
            state.yGuideColor = config.yGuideColorArgb();
            state.zGuideColor = config.zGuideColorArgb();
            state.labelColor = config.labelColorArgb();
            state.labelBackgroundColor = config.labelBackgroundColorArgb();
            state.hudTitleColor = config.hudTitleColorArgb();
            state.hudPrimaryTextColor = config.hudPrimaryTextColorArgb();
            state.hudSecondaryTextColor = config.hudSecondaryTextColorArgb();
            state.hudBackgroundColor = config.hudBackgroundColorArgb();
            state.hudBorderColor = config.hudBorderColorArgb();
            state.hudWarningColor = config.hudWarningColorArgb();
            state.hudAccentColor = config.hudAccentColorArgb();
            state.labelBackgroundMode = config.labelBackgroundModeEnum();
            state.labelBillboard = config.labelBillboard();
            state.labelShowUnit = config.labelShowUnit();
            state.hudAnchor = config.hudAnchorEnum();
            state.hudOffsetX = config.hudOffsetX();
            state.hudOffsetY = config.hudOffsetY();
            state.hudScale = config.hudScale();
            state.hudBackgroundAlpha = config.hudBackgroundAlpha();
            state.hudBorderAlpha = config.hudBorderAlpha();
            state.hudTextShadow = config.hudTextShadow();
            state.labelScale = config.labelScale();
            state.lineThickness = config.lineThickness();
            state.calloutOffset = config.calloutOffset();
            state.tickSize = config.tickSize();
            state.boxFillAlpha = config.boxFillAlpha();
            state.guideAlpha = config.guideAlpha();
            state.lineAlpha = config.lineAlpha();
            state.customPresetName = config.customPresetName();
            state.savedCustomPreset = new SavedCustomPreset(
                    config.customPointAColor(),
                    config.customPointBColor(),
                    config.customBoxColor(),
                    config.customConnectionLineColor(),
                    config.customXGuideColor(),
                    config.customYGuideColor(),
                    config.customZGuideColor(),
                    config.customLabelColor(),
                    config.customLabelBackgroundColor(),
                    config.customLabelBackgroundMode(),
                    config.customHudTitleColor(),
                    config.customHudPrimaryTextColor(),
                    config.customHudSecondaryTextColor(),
                    config.customHudAccentColor(),
                    config.customHudBackgroundColor(),
                    config.customHudBorderColor(),
                    config.customHudWarningColor(),
                    config.customHudBackgroundAlpha(),
                    config.customHudBorderAlpha(),
                    config.customHudTextShadow(),
                    config.customLabelScale(),
                    config.customLineThickness(),
                    config.customCalloutOffset(),
                    config.customTickSize(),
                    config.customBoxFillAlpha(),
                    config.customGuideAlpha(),
                    config.customLineAlpha()
            );
            return state;
        }

        private void resetCore() {
            AxisRulerConfig defaults = AxisRulerConfig.defaults();
            enabled = defaults.enabled();
            showOnlyWithTwoPointsDefault = defaults.showOnlyWithTwoPointsDefault();
        }

        private void resetAll() {
            MutableConfig defaults = from(AxisRulerConfig.defaults());
            enabled = defaults.enabled;
            hudEnabledDefault = defaults.hudEnabledDefault;
            hudCompactDefault = defaults.hudCompactDefault;
            guidesEnabledDefault = defaults.guidesEnabledDefault;
            renderEnabledDefault = defaults.renderEnabledDefault;
            labelsEnabledDefault = defaults.labelsEnabledDefault;
            fillEnabledDefault = defaults.fillEnabledDefault;
            outlineEnabledDefault = defaults.outlineEnabledDefault;
            lineEnabledDefault = defaults.lineEnabledDefault;
            showCenterMarkerDefault = defaults.showCenterMarkerDefault;
            showOnlyWithTwoPointsDefault = defaults.showOnlyWithTwoPointsDefault;
            pointAColor = defaults.pointAColor;
            pointBColor = defaults.pointBColor;
            boxColor = defaults.boxColor;
            connectionLineColor = defaults.connectionLineColor;
            xGuideColor = defaults.xGuideColor;
            yGuideColor = defaults.yGuideColor;
            zGuideColor = defaults.zGuideColor;
            labelColor = defaults.labelColor;
            labelBackgroundColor = defaults.labelBackgroundColor;
            hudTitleColor = defaults.hudTitleColor;
            hudPrimaryTextColor = defaults.hudPrimaryTextColor;
            hudSecondaryTextColor = defaults.hudSecondaryTextColor;
            hudBackgroundColor = defaults.hudBackgroundColor;
            hudBorderColor = defaults.hudBorderColor;
            hudWarningColor = defaults.hudWarningColor;
            hudAccentColor = defaults.hudAccentColor;
            labelBackgroundMode = defaults.labelBackgroundMode;
            labelBillboard = defaults.labelBillboard;
            labelShowUnit = defaults.labelShowUnit;
            hudAnchor = defaults.hudAnchor;
            hudOffsetX = defaults.hudOffsetX;
            hudOffsetY = defaults.hudOffsetY;
            hudScale = defaults.hudScale;
            hudBackgroundAlpha = defaults.hudBackgroundAlpha;
            hudBorderAlpha = defaults.hudBorderAlpha;
            hudTextShadow = defaults.hudTextShadow;
            labelScale = defaults.labelScale;
            lineThickness = defaults.lineThickness;
            calloutOffset = defaults.calloutOffset;
            tickSize = defaults.tickSize;
            boxFillAlpha = defaults.boxFillAlpha;
            guideAlpha = defaults.guideAlpha;
            lineAlpha = defaults.lineAlpha;
            customPresetName = defaults.customPresetName;
            savedCustomPreset = defaults.savedCustomPreset;
        }

        private void applyPreset(StylePreset preset) {
            switch (preset) {
                case CLASSIC -> applyVisual(0xFF35D07F, 0xFFE84C4C, 0xFFFFD54A, 0xFFD7B56D, 0xFFE07A68, 0xFF7FCB83, 0xFF6FA8DC, 0xFFEFE7D2, 0x8A101317, 0xFFFFD54A, 0xFFE5E9EC, 0xFF9FA7AE, 0xCCFFD54A, 0xFF14171B, 0xFF272D35, 0xFFFFB86C, 176, 128, true, LabelBackgroundMode.SUBTLE, 0.031F, 1.75F, 0.44F, 0.065F, 20, 215, 192);
                case EMERALD -> applyVisual(0xFF43D98C, 0xFF6AF0B0, 0xFF7BE8BC, 0xFF5AD6A1, 0xFF53E0A8, 0xFF7BF0BD, 0xFF9AF7D0, 0xFFF2FFF7, 0x82101A16, 0xFF57E0B6, 0xFFF2FFF7, 0xFF9FD8C8, 0xCC57E0B6, 0xFF0F1714, 0xFF224035, 0xFFFFC880, 172, 124, true, LabelBackgroundMode.SUBTLE, 0.031F, 1.70F, 0.42F, 0.062F, 18, 205, 186);
                case MINIMAL_WHITE -> applyVisual(0xFFF5F7FA, 0xFFE6EBF2, 0xFFD8E0EA, 0xFFEEF2F7, 0xFFF5F7FA, 0xFFE8EDF3, 0xFFDCE3EB, 0xFFFFFFFF, 0x78101418, 0xFFF5F7FA, 0xFFF1F4F8, 0xFFC5CDD7, 0x99DCE3EB, 0xFF171A1F, 0xFF46505D, 0xFFFFD38A, 164, 120, false, LabelBackgroundMode.SUBTLE, 0.030F, 1.55F, 0.40F, 0.058F, 16, 188, 176);
                case BLUEPRINT -> applyVisual(0xFF57D3C8, 0xFF86B7FF, 0xFF8FD3FF, 0xFF7EB8E8, 0xFF6AD0E0, 0xFF83E1A8, 0xFF86B7FF, 0xFFEAF7FF, 0x8A0E1722, 0xFF7BD8FF, 0xFFEAF7FF, 0xFF9AB8CC, 0xCC7BD8FF, 0xFF0E1722, 0xFF294A66, 0xFFFFC777, 180, 134, true, LabelBackgroundMode.SUBTLE, 0.032F, 1.80F, 0.48F, 0.070F, 20, 220, 196);
                case WARM_BUILDER -> applyVisual(0xFF90D96B, 0xFFFF9A62, 0xFFFFC96E, 0xFFE6BE8A, 0xFFFFAF7A, 0xFFCBE58E, 0xFF88BCEB, 0xFFFFF4DA, 0x8A1A1410, 0xFFFFC96E, 0xFFFFF4DA, 0xFFD4B596, 0xCCFFC96E, 0xFF1A1410, 0xFF5A4130, 0xFFFF8E5F, 180, 132, true, LabelBackgroundMode.SUBTLE, 0.032F, 1.78F, 0.46F, 0.068F, 22, 214, 194);
                case CUSTOM_SAVED -> applySavedCustomPreset();
            }
        }

        private void applySavedCustomPreset() {
            pointAColor = ColorUtils.parseHex(savedCustomPreset.pointAColor, pointAColor);
            pointBColor = ColorUtils.parseHex(savedCustomPreset.pointBColor, pointBColor);
            boxColor = ColorUtils.parseHex(savedCustomPreset.boxColor, boxColor);
            connectionLineColor = ColorUtils.parseHex(savedCustomPreset.connectionLineColor, connectionLineColor);
            xGuideColor = ColorUtils.parseHex(savedCustomPreset.xGuideColor, xGuideColor);
            yGuideColor = ColorUtils.parseHex(savedCustomPreset.yGuideColor, yGuideColor);
            zGuideColor = ColorUtils.parseHex(savedCustomPreset.zGuideColor, zGuideColor);
            labelColor = ColorUtils.parseHex(savedCustomPreset.labelColor, labelColor);
            labelBackgroundColor = ColorUtils.parseHex(savedCustomPreset.labelBackgroundColor, labelBackgroundColor);
            labelBackgroundMode = LabelBackgroundMode.fromName(savedCustomPreset.labelBackgroundMode);
            hudTitleColor = ColorUtils.parseHex(savedCustomPreset.hudTitleColor, hudTitleColor);
            hudPrimaryTextColor = ColorUtils.parseHex(savedCustomPreset.hudPrimaryTextColor, hudPrimaryTextColor);
            hudSecondaryTextColor = ColorUtils.parseHex(savedCustomPreset.hudSecondaryTextColor, hudSecondaryTextColor);
            hudBackgroundColor = ColorUtils.parseHex(savedCustomPreset.hudBackgroundColor, hudBackgroundColor);
            hudBorderColor = ColorUtils.parseHex(savedCustomPreset.hudBorderColor, hudBorderColor);
            hudWarningColor = ColorUtils.parseHex(savedCustomPreset.hudWarningColor, hudWarningColor);
            hudAccentColor = ColorUtils.parseHex(savedCustomPreset.hudAccentColor, hudAccentColor);
            hudBackgroundAlpha = savedCustomPreset.hudBackgroundAlpha;
            hudBorderAlpha = savedCustomPreset.hudBorderAlpha;
            hudTextShadow = savedCustomPreset.hudTextShadow;
            labelScale = savedCustomPreset.labelScale;
            lineThickness = savedCustomPreset.lineThickness;
            calloutOffset = savedCustomPreset.calloutOffset;
            tickSize = savedCustomPreset.tickSize;
            boxFillAlpha = savedCustomPreset.boxFillAlpha;
            guideAlpha = savedCustomPreset.guideAlpha;
            lineAlpha = savedCustomPreset.lineAlpha;
        }

        private void saveAsCustomPreset() {
            savedCustomPreset = new SavedCustomPreset(
                    ColorUtils.toHex(pointAColor, true),
                    ColorUtils.toHex(pointBColor, true),
                    ColorUtils.toHex(boxColor, true),
                    ColorUtils.toHex(connectionLineColor, true),
                    ColorUtils.toHex(xGuideColor, true),
                    ColorUtils.toHex(yGuideColor, true),
                    ColorUtils.toHex(zGuideColor, true),
                    ColorUtils.toHex(labelColor, true),
                    ColorUtils.toHex(labelBackgroundColor, true),
                    labelBackgroundMode.configValue(),
                    ColorUtils.toHex(hudTitleColor, true),
                    ColorUtils.toHex(hudPrimaryTextColor, true),
                    ColorUtils.toHex(hudSecondaryTextColor, true),
                    ColorUtils.toHex(hudAccentColor, true),
                    ColorUtils.toHex(hudBackgroundColor, true),
                    ColorUtils.toHex(hudBorderColor, true),
                    ColorUtils.toHex(hudWarningColor, true),
                    hudBackgroundAlpha,
                    hudBorderAlpha,
                    hudTextShadow,
                    labelScale,
                    lineThickness,
                    calloutOffset,
                    tickSize,
                    boxFillAlpha,
                    guideAlpha,
                    lineAlpha
            );
        }

        private StylePreset activeStylePreset() {
            for (StylePreset preset : StylePreset.values()) {
                MutableConfig candidate = from(AxisRulerConfig.defaults());
                candidate.savedCustomPreset = savedCustomPreset;
                candidate.customPresetName = customPresetName;
                candidate.applyPreset(preset);
                if (visualsMatch(candidate)) {
                    return preset;
                }
            }
            return null;
        }

        private boolean visualsMatch(MutableConfig other) {
            return pointAColor == other.pointAColor
                    && pointBColor == other.pointBColor
                    && boxColor == other.boxColor
                    && connectionLineColor == other.connectionLineColor
                    && xGuideColor == other.xGuideColor
                    && yGuideColor == other.yGuideColor
                    && zGuideColor == other.zGuideColor
                    && labelColor == other.labelColor
                    && labelBackgroundColor == other.labelBackgroundColor
                    && hudTitleColor == other.hudTitleColor
                    && hudPrimaryTextColor == other.hudPrimaryTextColor
                    && hudSecondaryTextColor == other.hudSecondaryTextColor
                    && hudBackgroundColor == other.hudBackgroundColor
                    && hudBorderColor == other.hudBorderColor
                    && hudWarningColor == other.hudWarningColor
                    && hudAccentColor == other.hudAccentColor
                    && labelBackgroundMode == other.labelBackgroundMode
                    && hudBackgroundAlpha == other.hudBackgroundAlpha
                    && hudBorderAlpha == other.hudBorderAlpha
                    && hudTextShadow == other.hudTextShadow
                    && Float.compare(labelScale, other.labelScale) == 0
                    && Float.compare(lineThickness, other.lineThickness) == 0
                    && Float.compare(calloutOffset, other.calloutOffset) == 0
                    && Float.compare(tickSize, other.tickSize) == 0
                    && boxFillAlpha == other.boxFillAlpha
                    && guideAlpha == other.guideAlpha
                    && lineAlpha == other.lineAlpha;
        }

        private void applyVisual(
                int pointAColor,
                int pointBColor,
                int boxColor,
                int connectionLineColor,
                int xGuideColor,
                int yGuideColor,
                int zGuideColor,
                int labelColor,
                int labelBackgroundColor,
                int hudTitleColor,
                int hudPrimaryTextColor,
                int hudSecondaryTextColor,
                int hudAccentColor,
                int hudBackgroundColor,
                int hudBorderColor,
                int hudWarningColor,
                int hudBackgroundAlpha,
                int hudBorderAlpha,
                boolean hudTextShadow,
                LabelBackgroundMode labelBackgroundMode,
                float labelScale,
                float lineThickness,
                float calloutOffset,
                float tickSize,
                int boxFillAlpha,
                int guideAlpha,
                int lineAlpha
        ) {
            this.pointAColor = pointAColor;
            this.pointBColor = pointBColor;
            this.boxColor = boxColor;
            this.connectionLineColor = connectionLineColor;
            this.xGuideColor = xGuideColor;
            this.yGuideColor = yGuideColor;
            this.zGuideColor = zGuideColor;
            this.labelColor = labelColor;
            this.labelBackgroundColor = labelBackgroundColor;
            this.hudTitleColor = hudTitleColor;
            this.hudPrimaryTextColor = hudPrimaryTextColor;
            this.hudSecondaryTextColor = hudSecondaryTextColor;
            this.hudBackgroundColor = hudBackgroundColor;
            this.hudBorderColor = hudBorderColor;
            this.hudWarningColor = hudWarningColor;
            this.hudAccentColor = hudAccentColor;
            this.hudBackgroundAlpha = hudBackgroundAlpha;
            this.hudBorderAlpha = hudBorderAlpha;
            this.hudTextShadow = hudTextShadow;
            this.labelBackgroundMode = labelBackgroundMode;
            this.labelScale = labelScale;
            this.lineThickness = lineThickness;
            this.calloutOffset = calloutOffset;
            this.tickSize = tickSize;
            this.boxFillAlpha = boxFillAlpha;
            this.guideAlpha = guideAlpha;
            this.lineAlpha = lineAlpha;
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
                    savedCustomPreset.pointAColor, savedCustomPreset.pointBColor, savedCustomPreset.boxColor, savedCustomPreset.connectionLineColor,
                    savedCustomPreset.xGuideColor, savedCustomPreset.yGuideColor, savedCustomPreset.zGuideColor, savedCustomPreset.labelColor,
                    savedCustomPreset.labelBackgroundColor, savedCustomPreset.labelBackgroundMode, savedCustomPreset.hudTitleColor,
                    savedCustomPreset.hudPrimaryTextColor, savedCustomPreset.hudSecondaryTextColor, savedCustomPreset.hudAccentColor,
                    savedCustomPreset.hudBackgroundColor, savedCustomPreset.hudBorderColor, savedCustomPreset.hudWarningColor,
                    savedCustomPreset.hudBackgroundAlpha, savedCustomPreset.hudBorderAlpha, savedCustomPreset.hudTextShadow,
                    savedCustomPreset.labelScale, savedCustomPreset.lineThickness, savedCustomPreset.calloutOffset, savedCustomPreset.tickSize,
                    savedCustomPreset.boxFillAlpha, savedCustomPreset.guideAlpha, savedCustomPreset.lineAlpha
            );
        }

        private int color(ColorRole role) {
            return switch (role) {
                case POINT_A -> pointAColor;
                case POINT_B -> pointBColor;
                case BOX -> boxColor;
                case CONNECTION_LINE -> connectionLineColor;
                case X_GUIDE -> xGuideColor;
                case Y_GUIDE -> yGuideColor;
                case Z_GUIDE -> zGuideColor;
                case LABEL_TEXT -> labelColor;
                case LABEL_BACKGROUND -> labelBackgroundColor;
                case HUD_TITLE -> hudTitleColor;
                case HUD_PRIMARY_TEXT -> hudPrimaryTextColor;
                case HUD_SECONDARY_TEXT -> hudSecondaryTextColor;
                case HUD_BACKGROUND -> hudBackgroundColor;
                case HUD_BORDER -> hudBorderColor;
                case HUD_WARNING -> hudWarningColor;
                case HUD_ACCENT -> hudAccentColor;
            };
        }

        private int defaultColor(ColorRole role) {
            AxisRulerConfig defaults = AxisRulerConfig.defaults();
            return switch (role) {
                case POINT_A -> defaults.pointAColorArgb();
                case POINT_B -> defaults.pointBColorArgb();
                case BOX -> defaults.boxColorArgb();
                case CONNECTION_LINE -> defaults.connectionLineColorArgb();
                case X_GUIDE -> defaults.xGuideColorArgb();
                case Y_GUIDE -> defaults.yGuideColorArgb();
                case Z_GUIDE -> defaults.zGuideColorArgb();
                case LABEL_TEXT -> defaults.labelColorArgb();
                case LABEL_BACKGROUND -> defaults.labelBackgroundColorArgb();
                case HUD_TITLE -> defaults.hudTitleColorArgb();
                case HUD_PRIMARY_TEXT -> defaults.hudPrimaryTextColorArgb();
                case HUD_SECONDARY_TEXT -> defaults.hudSecondaryTextColorArgb();
                case HUD_BACKGROUND -> defaults.hudBackgroundColorArgb();
                case HUD_BORDER -> defaults.hudBorderColorArgb();
                case HUD_WARNING -> defaults.hudWarningColorArgb();
                case HUD_ACCENT -> defaults.hudAccentColorArgb();
            };
        }

        private void setColor(ColorRole role, int color) {
            switch (role) {
                case POINT_A -> pointAColor = color;
                case POINT_B -> pointBColor = color;
                case BOX -> boxColor = color;
                case CONNECTION_LINE -> connectionLineColor = color;
                case X_GUIDE -> xGuideColor = color;
                case Y_GUIDE -> yGuideColor = color;
                case Z_GUIDE -> zGuideColor = color;
                case LABEL_TEXT -> labelColor = color;
                case LABEL_BACKGROUND -> labelBackgroundColor = color;
                case HUD_TITLE -> hudTitleColor = color;
                case HUD_PRIMARY_TEXT -> hudPrimaryTextColor = color;
                case HUD_SECONDARY_TEXT -> hudSecondaryTextColor = color;
                case HUD_BACKGROUND -> hudBackgroundColor = color;
                case HUD_BORDER -> hudBorderColor = color;
                case HUD_WARNING -> hudWarningColor = color;
                case HUD_ACCENT -> hudAccentColor = color;
            }
        }

        private int effectiveHudBackgroundColor() {
            return ColorUtils.withAlpha(hudBackgroundColor, hudBackgroundAlpha);
        }

        private int effectiveHudBorderColor() {
            return ColorUtils.withAlpha(hudBorderColor, hudBorderAlpha);
        }

        private int effectiveHudAccentColor() {
            int alpha = Math.min(ColorUtils.alpha(hudAccentColor), hudBorderAlpha);
            return ColorUtils.withAlpha(hudAccentColor, alpha);
        }
    }

    private static class AxisButtonWidget extends ClickableWidget {
        private final ButtonVariant variant;
        private final Runnable action;
        private float hoverProgress;
        private int pressedTicks;

        private AxisButtonWidget(int x, int y, int width, int height, Text label, ButtonVariant variant, Runnable action) {
            super(x, y, width, height, label);
            this.variant = variant;
            this.action = action;
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            if (!active) {
                return;
            }
            pressedTicks = 3;
            action.run();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            float targetHover = active && isHovered() ? 1.0F : 0.0F;
            hoverProgress += (targetHover - hoverProgress) * 0.35F;
            if (pressedTicks > 0) {
                pressedTicks--;
            }

            boolean pressed = pressedTicks > 0;
            int fill = !active
                    ? variant.disabledFill
                    : pressed
                    ? variant.pressedFill
                    : blend(variant.fill, variant.hoverFill, hoverProgress);
            int border = !active
                    ? FIELD_BORDER_DISABLED
                    : blend(variant.border, variant.hoverGlow, hoverProgress);
            int glow = !active
                    ? 0x00000000
                    : withAlpha(blend(variant.hoverGlow, PANEL_ACCENT, hoverProgress), Math.round(48.0F * hoverProgress));
            int textColor = active ? variant.text : TEXT_DISABLED;

            if (glow != 0) {
                context.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, glow);
            }
            context.fill(getX(), getY(), getX() + width, getY() + height, fill);
            context.fillGradient(getX(), getY(), getX() + width, getY() + height / 2, 0x14000000, 0x00000000);
            context.fill(getX(), getY(), getX() + width, getY() + 1, border);
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, border);
            context.fill(getX(), getY(), getX() + 1, getY() + height, border);
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, border);
            renderContent(context, textColor, pressed);
        }

        protected void renderContent(DrawContext context, int textColor, boolean pressed) {
            int textY = getY() + (height - 8) / 2 + (pressed ? 1 : 0);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + width / 2, textY, textColor);
        }

        private static int blend(int from, int to, float progress) {
            float clamped = MathHelper.clamp(progress, 0.0F, 1.0F);
            int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * clamped);
            int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * clamped);
            int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * clamped);
            int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private static int withAlpha(int color, int alpha) {
            return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }

    private static final class TabButtonWidget extends AxisButtonWidget {
        private final boolean activeTab;

        private TabButtonWidget(int x, int y, int width, int height, Tab tab, boolean active, PressAction action) {
            super(x, y, width, height, Text.translatable(tab.titleKey), ButtonVariant.TAB, action::onPress);
            this.activeTab = active;
        }

        @Override
        protected void renderContent(DrawContext context, int textColor, boolean pressed) {
            super.renderContent(context, activeTab ? TEXT_PRIMARY : textColor, pressed);
            if (activeTab) {
                context.fill(getX() + 1, getY() + height - 2, getX() + width - 1, getY() + height, PANEL_ACCENT);
            }
        }
    }

    private static final class PresetButtonWidget extends AxisButtonWidget {
        private final StylePreset preset;
        private final boolean selected;

        private PresetButtonWidget(int x, int y, int width, int height, StylePreset preset, boolean selected, PressAction action) {
            super(x, y, width, height, Text.translatable(preset.translationKey), selected ? ButtonVariant.TAB : ButtonVariant.SUBTLE, action::onPress);
            this.preset = preset;
            this.selected = selected;
        }

        @Override
        protected void renderContent(DrawContext context, int textColor, boolean pressed) {
            int insetY = pressed ? 1 : 0;
            int textX = getX() + 10;
            int titleY = getY() + 7 + insetY;
            int descY = titleY + 13;
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable(preset.translationKey), textX, titleY, selected ? TEXT_PRIMARY : textColor, false);
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.translatable(selected ? "axisruler.config.preset.status.active" : "axisruler.config.preset.status.apply"),
                    textX,
                    descY,
                    selected ? PANEL_ACCENT : TEXT_MUTED,
                    false
            );
            if (selected) {
                context.fill(getX() + 1, getY() + height - 2, getX() + width - 1, getY() + height, PANEL_ACCENT);
            }
        }
    }

    private static final class PaletteGroupButtonWidget extends AxisButtonWidget {
        private final Consumer<PaletteGroupButtonWidget> action;

        private PaletteGroupButtonWidget(int x, int y, int width, int height, ColorGroup group, boolean active, Consumer<PaletteGroupButtonWidget> action) {
            super(x, y, width, height, Text.translatable(group.titleKey), active ? ButtonVariant.TAB : ButtonVariant.SUBTLE, () -> { });
            this.action = action;
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            if (!active) {
                return;
            }
            action.accept(this);
        }
    }

    private static final class ToggleWidget extends ClickableWidget {
        private boolean enabled;
        private final PressAction action;

        private ToggleWidget(int x, int y, int width, int height, boolean active, PressAction action) {
            super(x, y, width, height, Text.empty());
            this.enabled = active;
            this.action = action;
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            enabled = !enabled;
            action.onPress();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int pillColor = enabled ? 0xFF2EAA82 : 0xFF3A4453;
            int border = isHovered() ? 0xFF8AD8FF : 0x80415062;
            context.fill(getX(), getY(), getX() + width, getY() + height, CONTROL_BACKGROUND);
            context.fill(getX(), getY(), getX() + width, getY() + 1, border);
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, border);
            context.fill(getX(), getY(), getX() + 1, getY() + height, border);
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, border);
            context.fill(getX() + width - 56, getY() + 3, getX() + width - 10, getY() + height - 3, pillColor);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, enabled ? ScreenTexts.ON : ScreenTexts.OFF, getX() + width - 33, getY() + 6, TEXT_PRIMARY);
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }

    private static final class AxisTextFieldWidget extends TextFieldWidget {
        private AxisTextFieldWidget(net.minecraft.client.font.TextRenderer textRenderer, int x, int y, int width, int height) {
            super(textRenderer, x, y, width, height, Text.empty());
            setTextPredicate(value -> value.length() <= 32);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int border = !active
                    ? FIELD_BORDER_DISABLED
                    : isFocused()
                    ? FIELD_BORDER_ACTIVE
                    : isHovered()
                    ? PANEL_ACCENT
                    : FIELD_BORDER;
            context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), CONTROL_BACKGROUND);
            context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, border);
            context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), border);
            context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), border);
            context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), border);
            super.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    private static final class SliderWidget extends ClickableWidget {
        private final float min;
        private final float max;
        private final Consumer<Float> setter;
        private float value;
        private boolean dragging;

        private SliderWidget(int x, int y, int width, int height, float min, float max, float current, Consumer<Float> setter) {
            super(x, y, width, height, Text.empty());
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.value = MathHelper.clamp(current, min, max);
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            dragging = true;
            updateFromMouse(click.x());
        }

        @Override
        protected void onDrag(Click click, double deltaX, double deltaY) {
            if (dragging) {
                updateFromMouse(click.x());
            }
        }

        @Override
        public void onRelease(Click click) {
            dragging = false;
        }

        private void updateFromMouse(double mouseX) {
            float progress = MathHelper.clamp((float) ((mouseX - (getX() + 8)) / (width - 16.0D)), 0.0F, 1.0F);
            value = min + progress * (max - min);
            setter.accept(value);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int border = isHovered() ? FIELD_BORDER_ACTIVE : FIELD_BORDER;
            context.fill(getX(), getY(), getX() + width, getY() + height, CONTROL_BACKGROUND);
            context.fill(getX(), getY(), getX() + width, getY() + 1, border);
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, border);
            context.fill(getX(), getY(), getX() + 1, getY() + height, border);
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, border);
            context.fill(getX() + 8, getY() + height / 2, getX() + width - 8, getY() + height / 2 + 2, 0xFF314051);
            float progress = (value - min) / (max - min);
            int knobX = getX() + 8 + Math.round(progress * (width - 16));
            context.fill(getX() + 8, getY() + height / 2, knobX, getY() + height / 2 + 2, PANEL_ACCENT);
            context.fill(knobX - 3, getY() + 4, knobX + 3, getY() + height - 4, PANEL_ACCENT);
            String number;
            if (Math.abs(max - Math.round(max)) < 0.0001F && max > 10.0F) {
                number = Integer.toString(Math.round(value));
            } else if (max <= 1.5F) {
                number = String.format(Locale.ROOT, "%.3f", value);
            } else {
                number = String.format(Locale.ROOT, "%.2f", value);
            }
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(number);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(number), getX() + width - textWidth - 8, getY() + 6, TEXT_PRIMARY, false);
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }

    private static final class ColorRoleButtonWidget extends AxisButtonWidget {
        private final int previewColor;
        private final boolean selected;
        private final Consumer<ColorRoleButtonWidget> action;

        private ColorRoleButtonWidget(int x, int y, int width, int height, ColorRole role, boolean active, int previewColor, Consumer<ColorRoleButtonWidget> action) {
            super(x, y, width, height, Text.translatable(role.labelKey), active ? ButtonVariant.TAB : ButtonVariant.SECONDARY, () -> { });
            this.previewColor = previewColor;
            this.selected = active;
            this.action = action;
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            if (!active) {
                return;
            }
            action.accept(this);
        }

        @Override
        protected void renderContent(DrawContext context, int textColor, boolean pressed) {
            int insetY = pressed ? 1 : 0;
            context.fill(getX() + 8, getY() + 5 + insetY, getX() + 24, getY() + height - 5 + insetY, previewColor);
            context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + 32, getY() + 9 + insetY, selected ? TEXT_PRIMARY : textColor, false);
        }
    }

    private static final class SwatchButtonWidget extends ClickableWidget {
        private final int color;
        private final boolean selected;
        private final Consumer<SwatchButtonWidget> action;

        private SwatchButtonWidget(int x, int y, int width, int height, int color, boolean selected, Consumer<SwatchButtonWidget> action) {
            super(x, y, width, height, Text.empty());
            this.color = color;
            this.selected = selected;
            this.action = action;
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            action.accept(this);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int inset = selected ? 0 : 1;
            int border = selected ? PANEL_ACCENT : isHovered() ? FIELD_BORDER_ACTIVE : 0x801A222D;
            if (selected) {
                context.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0x44101822);
            }
            context.fill(getX() + inset, getY() + inset, getX() + width - inset, getY() + height - inset, color);
            context.fill(getX(), getY(), getX() + width, getY() + 1, border);
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, border);
            context.fill(getX(), getY(), getX() + 1, getY() + height, border);
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, border);
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }

    private static final class ColorPickerWidget extends ClickableWidget {
        private final IntSupplier getter;
        private final IntConsumer setter;
        private final Identifier wheelTextureId;
        private final Identifier valueTextureId;
        private final Identifier alphaTextureId;
        private NativeImageBackedTexture wheelTexture;
        private NativeImageBackedTexture valueTexture;
        private NativeImageBackedTexture alphaTexture;
        private int cachedWheelSize = -1;
        private int cachedValueWidth = -1;
        private float currentHue;
        private float currentSaturation;
        private float currentValue;
        private int currentAlpha;
        private int currentArgb = Integer.MIN_VALUE;
        private boolean wheelDirty = true;
        private boolean valueDirty = true;
        private boolean alphaDirty = true;
        private boolean draggingWheel;
        private boolean draggingValue;
        private boolean draggingAlpha;

        private ColorPickerWidget(int x, int y, int width, int height, IntSupplier getter, IntConsumer setter) {
            super(x, y, width, height, Text.empty());
            this.getter = getter;
            this.setter = setter;
            int instanceId = System.identityHashCode(this);
            this.wheelTextureId = Identifier.of("axisruler", "palette_wheel_" + Integer.toUnsignedString(instanceId));
            this.valueTextureId = Identifier.of("axisruler", "palette_value_" + Integer.toUnsignedString(instanceId));
            this.alphaTextureId = Identifier.of("axisruler", "palette_alpha_" + Integer.toUnsignedString(instanceId));
            syncColorState(getter.getAsInt());
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            handle(click.x(), click.y());
        }

        @Override
        protected void onDrag(Click click, double deltaX, double deltaY) {
            if (draggingWheel || draggingValue || draggingAlpha) {
                handle(click.x(), click.y());
            }
        }

        @Override
        public void onRelease(Click click) {
            draggingWheel = false;
            draggingValue = false;
            draggingAlpha = false;
        }

        private void handle(double mouseX, double mouseY) {
            syncColorState(getter.getAsInt());
            int wheelSize = Math.min(height - 28, width - 34);
            int valueX = getX() + wheelSize + 12;
            int alphaY = getY() + wheelSize + 14;
            int valueWidth = Math.max(14, width - wheelSize - 12);

            if (inside(mouseX, mouseY, getX(), getY(), wheelSize, wheelSize) || draggingWheel) {
                draggingWheel = true;
                float centerX = getX() + wheelSize / 2.0F;
                float centerY = getY() + wheelSize / 2.0F;
                float dx = (float) mouseX - centerX;
                float dy = (float) mouseY - centerY;
                float saturation = Math.min(1.0F, (float) Math.sqrt(dx * dx + dy * dy) / (wheelSize / 2.0F));
                float hue = (float) Math.toDegrees(Math.atan2(dy, dx));
                if (hue < 0.0F) {
                    hue += 360.0F;
                }
                int updatedArgb = ColorUtils.withAlpha(ColorUtils.hsvToRgb(hue, saturation, currentValue), currentAlpha);
                setter.accept(updatedArgb);
                syncColorState(updatedArgb);
                return;
            }

            if (inside(mouseX, mouseY, valueX, getY(), valueWidth, wheelSize) || draggingValue) {
                draggingValue = true;
                float value = 1.0F - MathHelper.clamp((float) ((mouseY - getY()) / wheelSize), 0.0F, 1.0F);
                int updatedArgb = ColorUtils.withAlpha(ColorUtils.hsvToRgb(currentHue, currentSaturation, value), currentAlpha);
                setter.accept(updatedArgb);
                syncColorState(updatedArgb);
                return;
            }

            if (inside(mouseX, mouseY, getX(), alphaY, wheelSize, 12) || draggingAlpha) {
                draggingAlpha = true;
                int newAlpha = Math.round(MathHelper.clamp((float) ((mouseX - getX()) / wheelSize), 0.0F, 1.0F) * 255.0F);
                int updatedArgb = ColorUtils.withAlpha(currentArgb, newAlpha);
                setter.accept(updatedArgb);
                syncColorState(updatedArgb);
            }
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            syncColorState(getter.getAsInt());
            int wheelSize = Math.min(height - 28, width - 34);
            int radius = wheelSize / 2;
            int border = isHovered() ? FIELD_BORDER_ACTIVE : FIELD_BORDER;
            int valueX = getX() + wheelSize + 12;
            int valueWidth = Math.max(14, width - wheelSize - 12);
            int alphaY = getY() + wheelSize + 14;

            ensureTextures(wheelSize, valueWidth);
            uploadDirtyTextures();

            context.fill(getX() - 4, getY() - 4, getX() + width + 4, getY() + height + 4, 0x22101822);
            context.fill(getX() - 4, getY() - 4, getX() + width + 4, getY() - 3, border);
            context.fill(getX() - 4, getY() + height + 3, getX() + width + 4, getY() + height + 4, border);
            context.fill(getX() - 4, getY() - 4, getX() - 3, getY() + height + 4, border);
            context.fill(getX() + width + 3, getY() - 4, getX() + width + 4, getY() + height + 4, border);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, wheelTextureId, getX(), getY(), 0.0F, 0.0F, wheelSize, wheelSize, wheelSize, wheelSize);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, valueTextureId, valueX, getY(), 0.0F, 0.0F, valueWidth, wheelSize, valueWidth, wheelSize);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, alphaTextureId, getX(), alphaY, 0.0F, 0.0F, wheelSize, 12, wheelSize, 12);

            int selectorX = getX() + radius + Math.round((float) Math.cos(Math.toRadians(currentHue)) * currentSaturation * radius);
            int selectorY = getY() + radius + Math.round((float) Math.sin(Math.toRadians(currentHue)) * currentSaturation * radius);
            context.fill(selectorX - 2, selectorY - 2, selectorX + 2, selectorY + 2, 0xFFFFFFFF);
            int valueMarkerY = getY() + Math.round((1.0F - currentValue) * wheelSize);
            context.fill(valueX - 2, valueMarkerY - 1, valueX + valueWidth + 2, valueMarkerY + 1, 0xFFFFFFFF);
            int alphaMarkerX = getX() + Math.round((currentAlpha / 255.0F) * wheelSize);
            context.fill(alphaMarkerX - 1, alphaY - 2, alphaMarkerX + 1, alphaY + 14, 0xFFFFFFFF);
        }

        private void syncColorState(int argb) {
            if (argb == currentArgb) {
                return;
            }
            float previousValue = currentValue;
            currentArgb = argb;
            currentAlpha = ColorUtils.alpha(argb);
            updateHsv(argb);
            if (Math.abs(previousValue - currentValue) > 0.0001F) {
                wheelDirty = true;
            }
            valueDirty = true;
            alphaDirty = true;
        }

        private void updateHsv(int argb) {
            float red = ColorUtils.red(argb) / 255.0F;
            float green = ColorUtils.green(argb) / 255.0F;
            float blue = ColorUtils.blue(argb) / 255.0F;
            float max = Math.max(red, Math.max(green, blue));
            float min = Math.min(red, Math.min(green, blue));
            float delta = max - min;

            currentValue = max;
            currentSaturation = max <= 0.0F ? 0.0F : delta / max;
            if (delta <= 0.0F) {
                currentHue = 0.0F;
                return;
            }
            if (max == red) {
                currentHue = 60.0F * (((green - blue) / delta) % 6.0F);
            } else if (max == green) {
                currentHue = 60.0F * (((blue - red) / delta) + 2.0F);
            } else {
                currentHue = 60.0F * (((red - green) / delta) + 4.0F);
            }
            if (currentHue < 0.0F) {
                currentHue += 360.0F;
            }
        }

        private void ensureTextures(int wheelSize, int valueWidth) {
            boolean wheelSizeChanged = wheelSize != cachedWheelSize;
            if (wheelSizeChanged) {
                cachedWheelSize = wheelSize;
                recreateWheelTexture(wheelSize);
                recreateAlphaTexture(wheelSize);
                wheelDirty = true;
                alphaDirty = true;
            }
            if (valueWidth != cachedValueWidth || wheelSizeChanged || valueTexture == null) {
                cachedValueWidth = valueWidth;
                recreateValueTexture(valueWidth, wheelSize);
                valueDirty = true;
            }
        }

        private void recreateWheelTexture(int wheelSize) {
            destroyTexture(wheelTextureId, wheelTexture);
            wheelTexture = new NativeImageBackedTexture("axisruler_palette_wheel", wheelSize, wheelSize, false);
            MinecraftClient.getInstance().getTextureManager().registerTexture(wheelTextureId, wheelTexture);
        }

        private void recreateValueTexture(int valueWidth, int wheelSize) {
            destroyTexture(valueTextureId, valueTexture);
            valueTexture = new NativeImageBackedTexture("axisruler_palette_value", valueWidth, wheelSize, false);
            MinecraftClient.getInstance().getTextureManager().registerTexture(valueTextureId, valueTexture);
        }

        private void recreateAlphaTexture(int wheelSize) {
            destroyTexture(alphaTextureId, alphaTexture);
            alphaTexture = new NativeImageBackedTexture("axisruler_palette_alpha", wheelSize, 12, false);
            MinecraftClient.getInstance().getTextureManager().registerTexture(alphaTextureId, alphaTexture);
        }

        private void uploadDirtyTextures() {
            if (wheelDirty && wheelTexture != null) {
                rebuildWheelTexture();
                wheelTexture.upload();
                wheelDirty = false;
            }
            if (valueDirty && valueTexture != null) {
                rebuildValueTexture();
                valueTexture.upload();
                valueDirty = false;
            }
            if (alphaDirty && alphaTexture != null) {
                rebuildAlphaTexture();
                alphaTexture.upload();
                alphaDirty = false;
            }
        }

        private void rebuildWheelTexture() {
            NativeImage image = wheelTexture.getImage();
            if (image == null) {
                return;
            }
            int wheelSize = cachedWheelSize;
            int radius = wheelSize / 2;
            for (int py = 0; py < wheelSize; py++) {
                float dy = py - radius + 0.5F;
                for (int px = 0; px < wheelSize; px++) {
                    float dx = px - radius + 0.5F;
                    float distance = MathHelper.sqrt(dx * dx + dy * dy);
                    if (distance > radius) {
                        image.setColorArgb(px, py, 0x00000000);
                        continue;
                    }
                    float saturation = radius == 0 ? 0.0F : distance / radius;
                    float hue = (float) Math.toDegrees(Math.atan2(dy, dx));
                    if (hue < 0.0F) {
                        hue += 360.0F;
                    }
                    image.setColorArgb(px, py, ColorUtils.hsvToRgb(hue, saturation, currentValue));
                }
            }
        }

        private void rebuildValueTexture() {
            NativeImage image = valueTexture.getImage();
            if (image == null) {
                return;
            }
            int height = cachedWheelSize;
            for (int py = 0; py < height; py++) {
                float value = 1.0F - py / (float) Math.max(1, height - 1);
                int color = ColorUtils.hsvToRgb(currentHue, currentSaturation, value);
                for (int px = 0; px < cachedValueWidth; px++) {
                    image.setColorArgb(px, py, color);
                }
            }
        }

        private void rebuildAlphaTexture() {
            NativeImage image = alphaTexture.getImage();
            if (image == null) {
                return;
            }
            for (int px = 0; px < cachedWheelSize; px++) {
                int alpha = Math.round((px / (float) Math.max(1, cachedWheelSize - 1)) * 255.0F);
                int color = ColorUtils.withAlpha(currentArgb, alpha);
                for (int py = 0; py < 12; py++) {
                    image.setColorArgb(px, py, color);
                }
            }
        }

        private void destroyTexture(Identifier id, NativeImageBackedTexture texture) {
            if (texture == null) {
                return;
            }
            MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
            texture.close();
        }

        private void close() {
            destroyTexture(wheelTextureId, wheelTexture);
            destroyTexture(valueTextureId, valueTexture);
            destroyTexture(alphaTextureId, alphaTexture);
            wheelTexture = null;
            valueTexture = null;
            alphaTexture = null;
        }

        private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }
}
