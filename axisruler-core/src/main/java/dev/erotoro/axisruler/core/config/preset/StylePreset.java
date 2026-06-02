package dev.erotoro.axisruler.core.config.preset;

import dev.erotoro.axisruler.core.config.LabelBackgroundMode;

/**
 * Built-in visual presets shared by every version module. {@link #CUSTOM_SAVED} carries no
 * palette because it resolves to the user's own saved preset at edit time.
 */
public enum StylePreset {
    CLASSIC(
            "axisruler.config.preset.classic",
            "axisruler.config.preset.classic.desc",
            new PresetPalette(
                    0xFF35D07F, 0xFFE84C4C, 0xFFFFD54A, 0xFFD7B56D, 0xFFE07A68, 0xFF7FCB83, 0xFF6FA8DC, 0xFFEFE7D2,
                    0x8A101317, 0xFFFFD54A, 0xFFE5E9EC, 0xFF9FA7AE, 0xCCFFD54A, 0xFF14171B, 0xFF272D35, 0xFFFFB86C,
                    176, 128, true, LabelBackgroundMode.SUBTLE, 0.031F, 1.75F, 0.44F, 0.065F, 20, 215, 192
            )
    ),
    EMERALD(
            "axisruler.config.preset.emerald",
            "axisruler.config.preset.emerald.desc",
            new PresetPalette(
                    0xFF43D98C, 0xFF6AF0B0, 0xFF7BE8BC, 0xFF5AD6A1, 0xFF53E0A8, 0xFF7BF0BD, 0xFF9AF7D0, 0xFFF2FFF7,
                    0x82101A16, 0xFF57E0B6, 0xFFF2FFF7, 0xFF9FD8C8, 0xCC57E0B6, 0xFF0F1714, 0xFF224035, 0xFFFFC880,
                    172, 124, true, LabelBackgroundMode.SUBTLE, 0.031F, 1.70F, 0.42F, 0.062F, 18, 205, 186
            )
    ),
    MINIMAL_WHITE(
            "axisruler.config.preset.minimal_white",
            "axisruler.config.preset.minimal_white.desc",
            new PresetPalette(
                    0xFFF5F7FA, 0xFFE6EBF2, 0xFFD8E0EA, 0xFFEEF2F7, 0xFFF5F7FA, 0xFFE8EDF3, 0xFFDCE3EB, 0xFFFFFFFF,
                    0x78101418, 0xFFF5F7FA, 0xFFF1F4F8, 0xFFC5CDD7, 0x99DCE3EB, 0xFF171A1F, 0xFF46505D, 0xFFFFD38A,
                    164, 120, false, LabelBackgroundMode.SUBTLE, 0.030F, 1.55F, 0.40F, 0.058F, 16, 188, 176
            )
    ),
    BLUEPRINT(
            "axisruler.config.preset.blueprint",
            "axisruler.config.preset.blueprint.desc",
            new PresetPalette(
                    0xFF57D3C8, 0xFF86B7FF, 0xFF8FD3FF, 0xFF7EB8E8, 0xFF6AD0E0, 0xFF83E1A8, 0xFF86B7FF, 0xFFEAF7FF,
                    0x8A0E1722, 0xFF7BD8FF, 0xFFEAF7FF, 0xFF9AB8CC, 0xCC7BD8FF, 0xFF0E1722, 0xFF294A66, 0xFFFFC777,
                    180, 134, true, LabelBackgroundMode.SUBTLE, 0.032F, 1.80F, 0.48F, 0.070F, 20, 220, 196
            )
    ),
    WARM_BUILDER(
            "axisruler.config.preset.warm_builder",
            "axisruler.config.preset.warm_builder.desc",
            new PresetPalette(
                    0xFF90D96B, 0xFFFF9A62, 0xFFFFC96E, 0xFFE6BE8A, 0xFFFFAF7A, 0xFFCBE58E, 0xFF88BCEB, 0xFFFFF4DA,
                    0x8A1A1410, 0xFFFFC96E, 0xFFFFF4DA, 0xFFD4B596, 0xCCFFC96E, 0xFF1A1410, 0xFF5A4130, 0xFFFF8E5F,
                    180, 132, true, LabelBackgroundMode.SUBTLE, 0.032F, 1.78F, 0.46F, 0.068F, 22, 214, 194
            )
    ),
    CUSTOM_SAVED(
            "axisruler.config.preset.custom_saved",
            "axisruler.config.preset.custom_saved.desc",
            null
    );

    private final String translationKey;
    private final String descriptionKey;
    private final PresetPalette palette;

    StylePreset(String translationKey, String descriptionKey, PresetPalette palette) {
        this.translationKey = translationKey;
        this.descriptionKey = descriptionKey;
        this.palette = palette;
    }

    public String translationKey() {
        return translationKey;
    }

    public String descriptionKey() {
        return descriptionKey;
    }

    public boolean hasPalette() {
        return palette != null;
    }

    public PresetPalette palette() {
        return palette;
    }
}
