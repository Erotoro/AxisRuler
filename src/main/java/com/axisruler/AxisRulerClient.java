package com.axisruler;

import com.axisruler.config.AxisRulerConfig;
import com.axisruler.config.ConfigManager;
import com.axisruler.input.ClientInputHandler;
import com.axisruler.input.TargetingService;
import com.axisruler.keybind.KeybindRegistrar;
import com.axisruler.measure.MeasurementService;
import com.axisruler.measure.SelectionState;
import com.axisruler.render.HudOverlayRenderer;
import com.axisruler.render.WorldOverlayRenderer;
import com.axisruler.util.ModConstants;
import net.fabricmc.api.ClientModInitializer;

public final class AxisRulerClient implements ClientModInitializer {
    private static ConfigManager configManager;
    private static SelectionState selectionState;
    private AxisRulerServices services;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager();
        AxisRulerConfig config = configManager.load();
        selectionState = new SelectionState();
        selectionState.applyConfigDefaults(config);
        MeasurementService measurementService = new MeasurementService(selectionState);
        KeybindRegistrar keybindRegistrar = new KeybindRegistrar();
        TargetingService targetingService = new TargetingService();
        ClientInputHandler inputHandler = new ClientInputHandler(
                keybindRegistrar,
                measurementService,
                targetingService,
                configManager
        );
        WorldOverlayRenderer overlayRenderer = new WorldOverlayRenderer(measurementService, configManager);
        HudOverlayRenderer hudOverlayRenderer = new HudOverlayRenderer(measurementService, configManager);

        services = new AxisRulerServices(
                keybindRegistrar,
                inputHandler,
                overlayRenderer,
                hudOverlayRenderer
        );
        services.initialize();

        ModConstants.LOGGER.info("{} initialized for client by Erotoro", ModConstants.MOD_NAME);
    }

    public static ConfigManager configManager() {
        return configManager;
    }

    public static SelectionState selectionState() {
        return selectionState;
    }
}
