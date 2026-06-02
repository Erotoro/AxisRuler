package dev.erotoro.axisruler.mc1218;

import dev.erotoro.axisruler.core.config.AxisRulerConfig;
import dev.erotoro.axisruler.core.config.ConfigManager;
import dev.erotoro.axisruler.core.measure.MeasurementService;
import dev.erotoro.axisruler.core.measure.SelectionState;
import dev.erotoro.axisruler.core.util.ModConstants;
import dev.erotoro.axisruler.mc1218.input.ClientInputHandler;
import dev.erotoro.axisruler.mc1218.input.TargetingService;
import dev.erotoro.axisruler.mc1218.keybind.KeybindRegistrar;
import dev.erotoro.axisruler.mc1218.render.HudOverlayRenderer;
import dev.erotoro.axisruler.mc1218.render.WorldOverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class AxisRulerClient implements ClientModInitializer {
    private static ConfigManager configManager;
    private static SelectionState selectionState;
    private AxisRulerServices services;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager(
                FabricLoader.getInstance().getConfigDir().resolve(ConfigManager.CONFIG_FILE_NAME)
        );
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
