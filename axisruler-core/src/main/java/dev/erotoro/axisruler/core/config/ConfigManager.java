package dev.erotoro.axisruler.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.erotoro.axisruler.core.util.ModConstants;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ConfigManager {
    public static final String CONFIG_FILE_NAME = "axisruler.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path configPath;
    private AxisRulerConfig persistedConfig = AxisRulerConfig.defaults();
    private AxisRulerConfig previewConfig;

    public ConfigManager(Path configPath) {
        this.configPath = Objects.requireNonNull(configPath, "configPath");
    }

    public AxisRulerConfig load() {
        if (!Files.exists(configPath)) {
            save();
            return persistedConfig;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            persistedConfig = fromJson(root);
            previewConfig = null;
            save();
        } catch (IOException | RuntimeException exception) {
            ModConstants.LOGGER.warn("Failed to load AxisRuler config from {}, using defaults", configPath, exception);
            persistedConfig = AxisRulerConfig.defaults();
            previewConfig = null;
            save();
        }
        return persistedConfig;
    }

    public void save() {
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(persistedConfig, writer);
            }
        } catch (IOException exception) {
            ModConstants.LOGGER.warn("Failed to save AxisRuler config to {}", configPath, exception);
        }
    }

    public AxisRulerConfig config() {
        return previewConfig != null ? previewConfig : persistedConfig;
    }

    public AxisRulerConfig persistedConfig() {
        return persistedConfig;
    }

    public boolean previewActive() {
        return previewConfig != null;
    }

    public void beginPreview() {
        previewConfig = persistedConfig;
    }

    public void preview(AxisRulerConfig config) {
        previewConfig = AxisRulerConfig.requireValid(config);
    }

    public void discardPreview() {
        previewConfig = null;
    }

    public void commitPreview() {
        if (previewConfig == null) {
            return;
        }
        persistedConfig = previewConfig;
        previewConfig = null;
        save();
    }

    public void update(AxisRulerConfig config) {
        persistedConfig = AxisRulerConfig.requireValid(config);
        previewConfig = null;
        save();
    }

    public Path configPath() {
        return configPath;
    }

    private AxisRulerConfig fromJson(JsonElement root) {
        AxisRulerConfig defaults = AxisRulerConfig.defaults();
        if (root == null || !root.isJsonObject()) {
            return defaults;
        }

        JsonObject object = root.getAsJsonObject();
        return AxisRulerConfig.sanitize(new AxisRulerConfig(
                getBoolean(object, "enabled", defaults.enabled()),
                getBoolean(object, "hudEnabledDefault", defaults.hudEnabledDefault()),
                getBoolean(object, "hudCompactDefault", getBoolean(object, "hudCompact", defaults.hudCompactDefault())),
                getBoolean(object, "guidesEnabledDefault", defaults.guidesEnabledDefault()),
                getBoolean(object, "renderEnabledDefault", defaults.renderEnabledDefault()),
                getBoolean(object, "labelsEnabledDefault", getBoolean(object, "labelEnabled", defaults.labelsEnabledDefault())),
                getBoolean(object, "fillEnabledDefault", getBoolean(object, "fillEnabled", defaults.fillEnabledDefault())),
                getBoolean(object, "outlineEnabledDefault", getBoolean(object, "outlineEnabled", defaults.outlineEnabledDefault())),
                getBoolean(object, "lineEnabledDefault", getBoolean(object, "lineEnabled", defaults.lineEnabledDefault())),
                getBoolean(object, "showCenterMarkerDefault", getBoolean(object, "showCenterMarker", defaults.showCenterMarkerDefault())),
                getBoolean(object, "showOnlyWithTwoPointsDefault", getBoolean(object, "showOnlyWithTwoPoints", defaults.showOnlyWithTwoPointsDefault())),
                getString(object, "pointAColor", defaults.pointAColor()),
                getString(object, "pointBColor", defaults.pointBColor()),
                getString(object, "boxColor", defaults.boxColor()),
                getString(object, "connectionLineColor", defaults.connectionLineColor()),
                getString(object, "xGuideColor", defaults.xGuideColor()),
                getString(object, "yGuideColor", defaults.yGuideColor()),
                getString(object, "zGuideColor", defaults.zGuideColor()),
                getString(object, "labelColor", getString(object, "labelTextColor", defaults.labelColor())),
                getString(object, "labelBackgroundColor", defaults.labelBackgroundColor()),
                getString(object, "labelBackgroundMode", defaults.labelBackgroundMode()),
                getBoolean(object, "labelBillboard", defaults.labelBillboard()),
                getBoolean(object, "labelShowUnit", defaults.labelShowUnit()),
                getString(object, "hudAnchor", defaults.hudAnchor()),
                getInt(object, "hudOffsetX", getInt(object, "hudX", defaults.hudOffsetX())),
                getInt(object, "hudOffsetY", getInt(object, "hudY", defaults.hudOffsetY())),
                getFloat(object, "hudScale", defaults.hudScale()),
                getString(object, "hudTitleColor", defaults.hudTitleColor()),
                getString(object, "hudPrimaryTextColor", defaults.hudPrimaryTextColor()),
                getString(object, "hudSecondaryTextColor", defaults.hudSecondaryTextColor()),
                getString(object, "hudAccentColor", getString(object, "hudSeparatorColor", defaults.hudAccentColor())),
                getString(object, "hudBackgroundColor", defaults.hudBackgroundColor()),
                getString(object, "hudBorderColor", defaults.hudBorderColor()),
                getString(object, "hudWarningColor", defaults.hudWarningColor()),
                getInt(object, "hudBackgroundAlpha", defaults.hudBackgroundAlpha()),
                getInt(object, "hudBorderAlpha", defaults.hudBorderAlpha()),
                getBoolean(object, "hudTextShadow", defaults.hudTextShadow()),
                getFloat(object, "labelScale", getFloat(object, "textScale", defaults.labelScale())),
                getFloat(object, "lineThickness", defaults.lineThickness()),
                getFloat(object, "calloutOffset", defaults.calloutOffset()),
                getFloat(object, "tickSize", defaults.tickSize()),
                getInt(object, "boxFillAlpha", defaults.boxFillAlpha()),
                getInt(object, "guideAlpha", defaults.guideAlpha()),
                getInt(object, "lineAlpha", defaults.lineAlpha()),
                getString(object, "customPresetName", defaults.customPresetName()),
                getString(object, "customPointAColor", getString(object, "savedPointAColor", defaults.customPointAColor())),
                getString(object, "customPointBColor", getString(object, "savedPointBColor", defaults.customPointBColor())),
                getString(object, "customBoxColor", getString(object, "savedBoxColor", defaults.customBoxColor())),
                getString(object, "customConnectionLineColor", defaults.customConnectionLineColor()),
                getString(object, "customXGuideColor", defaults.customXGuideColor()),
                getString(object, "customYGuideColor", defaults.customYGuideColor()),
                getString(object, "customZGuideColor", defaults.customZGuideColor()),
                getString(object, "customLabelColor", defaults.customLabelColor()),
                getString(object, "customLabelBackgroundColor", defaults.customLabelBackgroundColor()),
                getString(object, "customLabelBackgroundMode", defaults.customLabelBackgroundMode()),
                getString(object, "customHudTitleColor", defaults.customHudTitleColor()),
                getString(object, "customHudPrimaryTextColor", defaults.customHudPrimaryTextColor()),
                getString(object, "customHudSecondaryTextColor", defaults.customHudSecondaryTextColor()),
                getString(object, "customHudAccentColor", getString(object, "customHudSeparatorColor", defaults.customHudAccentColor())),
                getString(object, "customHudBackgroundColor", defaults.customHudBackgroundColor()),
                getString(object, "customHudBorderColor", defaults.customHudBorderColor()),
                getString(object, "customHudWarningColor", defaults.customHudWarningColor()),
                getInt(object, "customHudBackgroundAlpha", defaults.customHudBackgroundAlpha()),
                getInt(object, "customHudBorderAlpha", defaults.customHudBorderAlpha()),
                getBoolean(object, "customHudTextShadow", defaults.customHudTextShadow()),
                getFloat(object, "customLabelScale", defaults.customLabelScale()),
                getFloat(object, "customLineThickness", defaults.customLineThickness()),
                getFloat(object, "customCalloutOffset", defaults.customCalloutOffset()),
                getFloat(object, "customTickSize", defaults.customTickSize()),
                getInt(object, "customBoxFillAlpha", defaults.customBoxFillAlpha()),
                getInt(object, "customGuideAlpha", defaults.customGuideAlpha()),
                getInt(object, "customLineAlpha", defaults.customLineAlpha())
        ));
    }

    private boolean getBoolean(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
                ? element.getAsBoolean()
                : fallback;
    }

    private int getInt(JsonObject object, String key, int fallback) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private String getString(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? element.getAsString()
                : fallback;
    }

    private float getFloat(JsonObject object, String key, float fallback) {
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        try {
            return element.getAsFloat();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }
}
