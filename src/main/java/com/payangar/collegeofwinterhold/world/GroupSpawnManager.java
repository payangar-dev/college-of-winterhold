package com.payangar.collegeofwinterhold.world;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Single event-bus subscriber that delegates to all registered group spawners.
 *
 * <p>Each spawner manages its own scan interval and gating internally via
 * {@link AbstractGroupSpawner#tick(ServerLevel)}.
 */
@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class GroupSpawnManager {

    private static final List<AbstractGroupSpawner> SPAWNERS = List.of(
            VampireCovenSpawner.INSTANCE,
            WizardExplorationSpawner.INSTANCE
    );

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        for (AbstractGroupSpawner spawner : SPAWNERS) {
            spawner.tick(server);
        }
    }

    private GroupSpawnManager() {}
}
