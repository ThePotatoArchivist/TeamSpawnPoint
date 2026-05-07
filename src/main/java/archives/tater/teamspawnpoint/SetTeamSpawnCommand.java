package archives.tater.teamspawnpoint;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.TeamArgument.getTeam;
import static net.minecraft.commands.arguments.TeamArgument.team;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;

public class SetTeamSpawnCommand {
    private SetTeamSpawnCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return literal("setteamspawn")
                .then(argument("team", team())
                        .executes(context -> setTeamSpawn(
                                context.getSource(),
                                getTeam(context, "team"),
                                BlockPos.containing(context.getSource().getPosition())
                        ))
                        .then(argument("pos", blockPos())
                                .executes(context -> setTeamSpawn(
                                        context.getSource(),
                                        getTeam(context, "team"),
                                        BlockPosArgument.getSpawnablePos(context, "pos")
                                ))
                        )
                );
    }

    private static int setTeamSpawn(CommandSourceStack source, PlayerTeam team, BlockPos pos) {
        if (source.getLevel().dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.translatable("commands.setworldspawn.failure.not_overworld"));
            return 0;
        }
        TeamSpawnPoints.computeIfAbsent(source.getServer()).set(team, pos);
        source.sendSuccess(() -> Component.translatable("commands.teamspawnpoint.setteamspawn.success", team.getFormattedDisplayName(), pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }
}
