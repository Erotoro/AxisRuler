package dev.erotoro.axisruler.mc1214.input;

import dev.erotoro.axisruler.core.measure.MeasurePoint;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public final class TargetingService {
    public Optional<MeasurePoint> targetedBlock(MinecraftClient client) {
        Objects.requireNonNull(client, "client");
        ClientWorld world = client.world;
        HitResult crosshairTarget = client.crosshairTarget;
        if (world == null || crosshairTarget == null) {
            return Optional.empty();
        }
        if (crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        if (!(crosshairTarget instanceof BlockHitResult blockHitResult)) {
            return Optional.empty();
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        String worldKey = world.getRegistryKey().getValue().toString();
        return Optional.of(MeasurePoint.of(worldKey, blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }
}
