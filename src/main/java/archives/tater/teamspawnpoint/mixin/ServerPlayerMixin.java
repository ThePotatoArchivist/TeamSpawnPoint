package archives.tater.teamspawnpoint.mixin;

import archives.tater.teamspawnpoint.TeamSpawnPoints;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static java.util.Objects.requireNonNullElse;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
		super(level, pos, yRot, gameProfile);
	}

	@ModifyExpressionValue(
			method = "<init>",
			at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;")
	)
	private BlockPos teamSpawnpoint(BlockPos original, MinecraftServer server, ServerLevel level) {
		return requireNonNullElse(TeamSpawnPoints.get((ServerPlayer) (Object) this), original);
	}
}