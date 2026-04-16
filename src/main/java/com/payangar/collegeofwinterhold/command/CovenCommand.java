package com.payangar.collegeofwinterhold.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.world.VampireCovenSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Debug / testing command that pops a full vampire coven at the caller's
 * feet. Registered on the game bus via {@code RegisterCommandsEvent}.
 *
 * <p>Requires permission level 2 (ops / cheats enabled) — this is a content
 * shortcut, not a player-facing ability.
 *
 * <p>Usage : {@code /cow spawn_coven}
 */
public final class CovenCommand {

    private CovenCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cow")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("spawn_coven")
                                .executes(CovenCommand::executeSpawnCoven))
        );
    }

    private static int executeSpawnCoven(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx
    ) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        VampireCovenSpawner.spawnCoven(level, pos, level.getRandom());

        ctx.getSource().sendSuccess(
                () -> Component.literal("Spawned vampire coven at " + pos.toShortString()),
                true
        );
        CollegeOfWinterhold.LOGGER.info(
                "[CovenCommand] /cow spawn_coven fired at {} {} {} by {}",
                pos.getX(), pos.getY(), pos.getZ(), player.getName().getString()
        );
        return 1;
    }
}
