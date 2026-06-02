package dev.erotoro.axisruler.mc1218;

import dev.erotoro.axisruler.mc1218.input.ClientInputHandler;
import dev.erotoro.axisruler.mc1218.keybind.KeybindRegistrar;
import dev.erotoro.axisruler.mc1218.render.HudOverlayRenderer;
import dev.erotoro.axisruler.mc1218.render.WorldOverlayRenderer;

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
