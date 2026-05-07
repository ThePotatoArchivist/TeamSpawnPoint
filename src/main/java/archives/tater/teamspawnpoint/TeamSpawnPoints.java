package archives.tater.teamspawnpoint;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.PlayerTeam;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TeamSpawnPoints extends SavedData {
    private final Map<String, BlockPos> spawnpoints;

    private TeamSpawnPoints() {
        spawnpoints = new ConcurrentHashMap<>();
    }

    private TeamSpawnPoints(Map<String, BlockPos> spawnpoints) {
        this.spawnpoints = new ConcurrentHashMap<>(spawnpoints);
    }

    public static final Codec<TeamSpawnPoints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).fieldOf("spawnpoints").forGetter(s -> s.spawnpoints)
    ).apply(instance, TeamSpawnPoints::new));

    public static final Factory<TeamSpawnPoints> FACTORY = new Factory<>(TeamSpawnPoints::new, TeamSpawnPoints::load, null);

    public void set(PlayerTeam team, BlockPos spawnpoint) {
        spawnpoints.put(team.getName(), spawnpoint);
    }

    public @Nullable BlockPos get(@Nullable PlayerTeam team) {
        return team == null ? null: spawnpoints.get(team.getName());
    }

    public @Nullable BlockPos get(Player player) {
        return get(player.getTeam());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.encode(this, registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .ifError(error -> TeamSpawnPoint.LOGGER.error("Error serializing TeamSpawnPoints: {}", error.message()))
                .resultOrPartial()
                .flatMap(out -> out instanceof CompoundTag compoundTag ? Optional.of(compoundTag) : Optional.empty())
                .orElse(tag);
    }

    private static TeamSpawnPoints load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.decode(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .ifError(error -> TeamSpawnPoint.LOGGER.error("Error deserializing TeamSpawnPoints: {}", error.message()))
                .resultOrPartial()
                .map(Pair::getFirst)
                .orElseGet(TeamSpawnPoints::new);
    }

    public static @Nullable TeamSpawnPoints get(MinecraftServer server) {
        return server.overworld().getDataStorage().get(FACTORY, TeamSpawnPoint.MOD_ID);
    }

    public static TeamSpawnPoints computeIfAbsent(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, TeamSpawnPoint.MOD_ID);
    }

    public static @Nullable BlockPos get(ServerPlayer player) {
        var spawnpoints = get(player.server);
        if (spawnpoints == null) return null;
        return spawnpoints.get((Player) player);
    }

    public static @Nullable BlockPos get(Entity maybePlayer) {
        return maybePlayer instanceof ServerPlayer serverPlayer ? get(serverPlayer) : null;
    }
}
