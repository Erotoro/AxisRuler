package com.axisruler.input;

import com.axisruler.measure.MeasurePoint;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TargetingService {
    public Optional<MeasurePoint> targetedBlock(Minecraft client) {
        Objects.requireNonNull(client, "client");
        ClientLevel world = client.level;
        HitResult crosshairTarget = client.hitResult;
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
        return Optional.of(MeasurePoint.of(world.dimension(), blockPos));
    }
}
