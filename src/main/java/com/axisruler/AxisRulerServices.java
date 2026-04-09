package com.axisruler;

import com.axisruler.input.ClientInputHandler;
import com.axisruler.keybind.KeybindRegistrar;
import com.axisruler.render.HudOverlayRenderer;
import com.axisruler.render.WorldOverlayRenderer;

public final class AxisRulerServices {
    private final KeybindRegistrar keybindRegistrar;
    private final ClientInputHandler inputHandler;
    private final WorldOverlayRenderer overlayRenderer;
    private final HudOverlayRenderer hudOverlayRenderer;

    public AxisRulerServices(
            KeybindRegistrar keybindRegistrar,
            ClientInputHandler inputHandler,
            WorldOverlayRenderer overlayRenderer,
            HudOverlayRenderer hudOverlayRenderer
    ) {
        this.keybindRegistrar = keybindRegistrar;
        this.inputHandler = inputHandler;
        this.overlayRenderer = overlayRenderer;
        this.hudOverlayRenderer = hudOverlayRenderer;
    }

    public void initialize() {
        keybindRegistrar.register();
        inputHandler.register();
        overlayRenderer.register();
        hudOverlayRenderer.register();
    }
}
