package com.payangar.collegeofwinterhold.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.world.WizardExplorationSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Debug / testing command that spawns a wizard exploration group at the
 * caller's feet. Registered alongside {@link CovenCommand} on the game
 * bus via {@code RegisterCommandsEvent}.
 *
 * <p>Requires permission level 2 (ops / cheats enabled).
 *
 * <p>Usage : {@code /cow spawn_exploration}
 */
public final class ExplorationCommand {

    private ExplorationCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cow")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("spawn_exploration")
                                .executes(ExplorationCommand::executeSpawnExploration))
        );
    }

    private static int executeSpawnExploration(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx
    ) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        WizardExplorationSpawner.spawnExplorationGroup(level, pos, level.getRandom());

        ctx.getSource().sendSuccess(
                () -> Component.literal("Spawned wizard exploration group at " + pos.toShortString()),
                true
        );
        CollegeOfWinterhold.LOGGER.info(
                "[ExplorationCommand] /cow spawn_exploration fired at {} {} {} by {}",
                pos.getX(), pos.getY(), pos.getZ(), player.getName().getString()
        );
        return 1;
    }
}
