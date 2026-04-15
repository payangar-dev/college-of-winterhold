package com.payangar.collegeofwinterhold.world;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.entity.wizard.core.CollegeSchool;
import com.payangar.collegeofwinterhold.entity.wizard.core.WizardTier;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Map;

/**
 * One-shot village wizard spawning. Listens for {@link ChunkEvent.Load} on
 * freshly generated chunks only ({@link ChunkEvent.Load#isNewChunk()} guards
 * against re-spawning on reload) and populates any vanilla-or-modded village
 * start chunk with 0-2 wizards.
 *
 * <p>Idempotence comes from {@code isNewChunk()} itself : the event fires with
 * {@code isNewChunk == true} exactly once, when the chunk is promoted from
 * proto-chunk to full after generation. Every subsequent reload has
 * {@code isNewChunk == false} and is skipped — no SavedData, no persistent set,
 * no respawn ever.
 *
 * <p>Each wizard's school is resolved from its spawn biome via
 * {@link CollegeSchool#forBiome} and its tier via
 * {@link WizardTier#rollAdeptPlus} (70% Adept / 25% Expert / 5% Master). Count
 * distribution is 60% zero / 30% one / 10% two per village.
 */
@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class VillageWizardSpawner {

    private VillageWizardSpawner() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel server)) return;
        if (!event.isNewChunk()) return;

        ChunkAccess chunk = event.getChunk();
        Map<Structure, StructureStart> starts = chunk.getAllStarts();
        if (starts.isEmpty()) return;

        var structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (Map.Entry<Structure, StructureStart> entry : starts.entrySet()) {
            StructureStart start = entry.getValue();
            if (!start.isValid()) continue;

            Holder<Structure> holder = structureRegistry.wrapAsHolder(entry.getKey());
            if (!holder.is(StructureTags.VILLAGE)) continue;

            // getAllStarts() returns structures that start in THIS chunk, so we
            // process once per village at its start chunk — no cross-chunk dedup
            // needed. Spawn positions must stay inside the start chunk because
            // neighbour chunks aren't guaranteed loaded yet at this event's
            // firing time ; querying their heightmap returns the default
            // minY and wizards land in the void.
            spawnVillageWizards(server, chunk);
        }
    }

    private static void spawnVillageWizards(ServerLevel server, ChunkAccess chunk) {
        RandomSource rng = server.getRandom();
        int count = rollWizardCount(rng);
        if (count == 0) return;

        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < count; i++) {
            // Jitter inside the 16x16 bounds of the start chunk. Reading the
            // heightmap from the chunk we just received is always safe, unlike
            // ServerLevel.getHeight on neighbour chunks that may not be loaded.
            int x = chunkMinX + rng.nextInt(16);
            int z = chunkMinZ + rng.nextInt(16);
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
            BlockPos pos = new BlockPos(x, y, z);

            CollegeSchool school = CollegeSchool.forBiome(server.getBiome(pos));
            WizardTier tier = WizardTier.rollAdeptPlus(rng);
            EntityType<? extends AbstractCollegeWizardEntity> type = ModEntityTypes.wizardFor(school, tier);
            if (type == null) continue; // school not registered yet — skip silently

            AbstractCollegeWizardEntity wizard = type.create(server);
            if (wizard == null) continue;

            wizard.moveTo(x + 0.5, y, z + 0.5, rng.nextFloat() * 360f - 180f, 0f);
            wizard.finalizeSpawn(server, server.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
            server.addFreshEntityWithPassengers(wizard);
        }
    }

    /**
     * Rolls the number of wizards to spawn in a single village : 50% 0,
     * 25% 1, 25% 2. Half the villages have no wizard ; when they do, the
     * odds of a single versus a pair are even.
     */
    private static int rollWizardCount(RandomSource rng) {
        float r = rng.nextFloat();
        if (r < 0.50f) return 0;
        if (r < 0.75f) return 1;
        return 2;
    }
}
