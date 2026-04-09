package com.axisruler.input;

import com.axisruler.config.ConfigManager;
import com.axisruler.keybind.KeybindRegistrar;
import com.axisruler.measure.MeasurePoint;
import com.axisruler.measure.MeasurementFormatter;
import com.axisruler.measure.MeasurementResult;
import com.axisruler.measure.MeasurementService;
import com.axisruler.measure.SelectionState;
import com.axisruler.util.AxisRulerText;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ClientInputHandler {
    private final KeybindRegistrar keybindRegistrar;
    private final MeasurementService measurementService;
    private final TargetingService targetingService;
    private final ConfigManager configManager;
    private String lastWorldKey;
    private boolean registered;

    public ClientInputHandler(
            KeybindRegistrar keybindRegistrar,
            MeasurementService measurementService,
            TargetingService targetingService,
            ConfigManager configManager
    ) {
        this.keybindRegistrar = keybindRegistrar;
        this.measurementService = measurementService;
        this.targetingService = targetingService;
        this.configManager = configManager;
    }

    public void register() {
        if (registered) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        registered = true;
    }

    private void onEndClientTick(MinecraftClient client) {
        updateCurrentWorldKey(client);
        if (client.player == null || client.currentScreen != null) {
            consumeAllPendingKeyPresses();
            return;
        }
        if (!isAltDown(client)) {
            consumeAllPendingKeyPresses();
            return;
        }

        handleSetPointA(client);
        handleSetPointB(client);
        handleClearPoints(client);
        handleSwapPoints(client);
        handleCycleMode(client);
        handleToggleHud(client);
        handleToggleGuides(client);
        handleToggleLabels(client);
        handleToggleLine(client);
        handleCopyMeasurement(client);
    }

    private void updateCurrentWorldKey(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            lastWorldKey = null;
            measurementService.selectionState().setCurrentWorldKey(null);
            measurementService.selectionState().clearPoints();
            return;
        }
        String worldKey = world.getRegistryKey().getValue().toString();
        if (lastWorldKey != null && !lastWorldKey.equals(worldKey)) {
            measurementService.selectionState().clearPoints();
        }
        lastWorldKey = worldKey;
        measurementService.selectionState().setCurrentWorldKey(worldKey);
    }

    private void handleSetPointA(MinecraftClient client) {
        while (keybindRegistrar.setPointA().wasPressed()) {
            Optional<MeasurePoint> point = targetingService.targetedBlock(client);
            if (point.isEmpty()) {
                sendActionBar(client, AxisRulerText.noBlockTarget());
                continue;
            }
            measurementService.selectionState().setPointA(point.get());
            sendActionBar(client, AxisRulerText.pointASet(point.get()));
        }
    }

    private void handleSetPointB(MinecraftClient client) {
        while (keybindRegistrar.setPointB().wasPressed()) {
            Optional<MeasurePoint> point = targetingService.targetedBlock(client);
            if (point.isEmpty()) {
                sendActionBar(client, AxisRulerText.noBlockTarget());
                continue;
            }
            measurementService.selectionState().setPointB(point.get());
            sendActionBar(client, AxisRulerText.pointBSet(point.get()));
        }
    }

    private void handleClearPoints(MinecraftClient client) {
        while (keybindRegistrar.clearPoints().wasPressed()) {
            measurementService.selectionState().clearPoints();
            sendActionBar(client, AxisRulerText.selectionCleared());
        }
    }

    private void handleSwapPoints(MinecraftClient client) {
        while (keybindRegistrar.swapPoints().wasPressed()) {
            if (measurementService.selectionState().swapPoints()) {
                sendActionBar(client, AxisRulerText.pointsSwapped());
            } else {
                sendActionBar(client, AxisRulerText.bothPointsRequired());
            }
        }
    }

    private void handleCycleMode(MinecraftClient client) {
        while (keybindRegistrar.cycleMode().wasPressed()) {
            SelectionState selectionState = measurementService.selectionState();
            selectionState.cycleMode();
            sendActionBar(client, AxisRulerText.mode(selectionState.mode()));
        }
    }

    private void handleToggleHud(MinecraftClient client) {
        while (keybindRegistrar.toggleHud().wasPressed()) {
            SelectionState selectionState = measurementService.selectionState();
            selectionState.toggleHud();
            configManager.update(configManager.config().withHudEnabledDefault(selectionState.hudEnabled()));
            sendActionBar(client, AxisRulerText.hud(selectionState.hudEnabled()));
        }
    }

    private void handleToggleGuides(MinecraftClient client) {
        while (keybindRegistrar.toggleGuides().wasPressed()) {
            SelectionState selectionState = measurementService.selectionState();
            selectionState.toggleGuides();
            configManager.update(configManager.config().withGuidesEnabledDefault(selectionState.guidesEnabled()));
            sendActionBar(client, AxisRulerText.guides(selectionState.guidesEnabled()));
        }
    }

    private void handleToggleLabels(MinecraftClient client) {
        while (keybindRegistrar.toggleLabels().wasPressed()) {
            SelectionState selectionState = measurementService.selectionState();
            selectionState.toggleLabels();
            configManager.update(configManager.config().withLabelsEnabledDefault(selectionState.labelsEnabled()));
            sendActionBar(client, AxisRulerText.labels(selectionState.labelsEnabled()));
        }
    }

    private void handleToggleLine(MinecraftClient client) {
        while (keybindRegistrar.toggleLine().wasPressed()) {
            SelectionState selectionState = measurementService.selectionState();
            selectionState.toggleLine();
            configManager.update(configManager.config().withLineEnabledDefault(selectionState.lineEnabled()));
            sendActionBar(client, AxisRulerText.line(selectionState.lineEnabled()));
        }
    }

    private void handleCopyMeasurement(MinecraftClient client) {
        while (keybindRegistrar.copyMeasurement().wasPressed()) {
            MeasurePoint pointA = measurementService.selectionState().pointAOrNull();
            MeasurePoint pointB = measurementService.selectionState().pointBOrNull();
            if (pointA == null || pointB == null) {
                sendActionBar(client, AxisRulerText.nothingToCopy());
                continue;
            }

            MeasurementResult result = measurementService.calculate(pointA, pointB);
            if (!result.valid()) {
                sendActionBar(client, AxisRulerText.copyDifferentWorlds());
                continue;
            }

            client.keyboard.setClipboard(clipboardSummary(pointA, pointB, result));
            sendActionBar(client, AxisRulerText.measurementCopied());
        }
    }

    private boolean isAltDown(MinecraftClient client) {
        return InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT)
                || InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_ALT);
    }

    private void sendActionBar(MinecraftClient client, Text message) {
        if (client.player != null) {
            client.player.sendMessage(message, true);
        }
    }

    private void consumeAllPendingKeyPresses() {
        keybindRegistrar.keyBindings().forEach(keyBinding -> {
            while (keyBinding.wasPressed()) {
            }
        });
    }

    private String clipboardSummary(MeasurePoint pointA, MeasurePoint pointB, MeasurementResult result) {
        String mode = I18n.translate(measurementService.selectionState().mode().translationKey());
        return String.join(
                System.lineSeparator(),
                "AxisRuler",
                "A: " + pointA.formatBlockPosition(),
                "B: " + pointB.formatBlockPosition(),
                "Min: " + MeasurementFormatter.formatBlockPos(result.minBlockPos()),
                "Max: " + MeasurementFormatter.formatBlockPos(result.maxBlockPos()),
                "Size: " + MeasurementFormatter.formatDimensions(result.sizeX(), result.sizeY(), result.sizeZ()),
                "Delta: " + MeasurementFormatter.formatAbsoluteDelta(result.absDx(), result.absDy(), result.absDz()),
                "Euclidean: " + MeasurementFormatter.formatDecimal(result.euclideanDistance()),
                "Manhattan: " + result.manhattanDistance(),
                "Floor: " + result.floorArea(),
                "Volume: " + result.volume(),
                "Mode: " + mode
        );
    }
}
