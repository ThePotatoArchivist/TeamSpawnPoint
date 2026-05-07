package archives.tater.teamspawnpoint.mixin;

import archives.tater.teamspawnpoint.TeamSpawnPoints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;

import static java.util.Objects.requireNonNullElse;

@Mixin(DimensionTransition.class)
public class DimensionTransitionMixin {
    @ModifyExpressionValue(
            method = "findAdjustedSharedSpawnPos",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;")
    )
    private static BlockPos teamSpawnPoint(BlockPos original, ServerLevel level, Entity entity) {
        return requireNonNullElse(TeamSpawnPoints.get(entity), original);
    }
}
