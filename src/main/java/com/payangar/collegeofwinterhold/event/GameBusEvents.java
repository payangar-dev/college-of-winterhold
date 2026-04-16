package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.command.CovenCommand;
import com.payangar.collegeofwinterhold.command.ExplorationCommand;
import com.payangar.collegeofwinterhold.entity.vampire.VampireEntity;
import com.payangar.collegeofwinterhold.entity.vampire.VampireHoundEntity;
import com.payangar.collegeofwinterhold.entity.villager.CapturedState;
import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.entity.wizard.CollegeWizard;
import com.payangar.collegeofwinterhold.registry.ModAttachments;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, bus = EventBusSubscriber.Bus.GAME)
// Explicit bus=GAME because EntityJoinLevelEvent fires on the game event bus; the
// mod bus (default for @EventBusSubscriber) only carries setup/registration events.
public final class GameBusEvents {

    /**
     * Injects target goals on every hostile mob that spawns so it attacks our
     * neutral mages on sight — same principle Minecraft hardcodes for Iron
     * Golems. Checks {@code Enemy} (interface) instead of {@code Monster}
     * (class) to also catch Iron's hostile casters (Necromancer, Dead King…).
     * <p>
     * A single goal on the parent class {@code AbstractCollegeWizardEntity} is
     * enough — no per-school / per-tier enumeration needed.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(mob instanceof Enemy)) return;

        // Every hostile mob targets our wizards.
        mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                mob, AbstractCollegeWizardEntity.class, true));

        // Vampires + hounds: undead treat vampires as kin, spiders are
        // indifferent, creepers only ever target players.
        boolean shouldTargetVampires = !mob.getType().is(EntityTypeTags.UNDEAD)
                && !(mob instanceof Spider)
                && !(mob instanceof Creeper);
        if (shouldTargetVampires) {
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    mob, VampireEntity.class, true));
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    mob, VampireHoundEntity.class, true));
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CovenCommand.register(event.getDispatcher());
        ExplorationCommand.register(event.getDispatcher());
    }

    /**
     * Handles two coven death triggers :
     * <ul>
     *   <li><b>Leader dies</b> → surviving followers enter frenzy (60 s rage).</li>
     *   <li><b>Jailer dies</b> → all captive villagers within 32 blocks are
     *       freed, transitioned to the {@code rescued} state, and their leash
     *       dropped. The {@link com.payangar.collegeofwinterhold.entity.villager.RescuedSafetyGoal}
     *       takes over to run the 10-second safety check before reward delivery.</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof VampireEntity deadVampire)) return;
        if (!(deadVampire.level() instanceof ServerLevel server)) return;

        // Frenzy : leader death propagates rage to followers
        if (deadVampire.isCovenLeader()) {
            final UUID deadLeaderUuid = deadVampire.getUUID();
            AABB scanBox = new AABB(deadVampire.blockPosition()).inflate(64.0);
            List<VampireEntity> followers = server.getEntitiesOfClass(
                    VampireEntity.class, scanBox,
                    v -> v.isAlive() && deadLeaderUuid.equals(v.getLeaderUuid())
            );
            for (VampireEntity follower : followers) {
                follower.enterFrenzy(VampireEntity.FRENZY_DURATION_TICKS);
            }
        }

        // Liberation : jailer death frees all captive villagers in the chain
        if (deadVampire.isJailer()) {
            AABB captiveBox = new AABB(deadVampire.blockPosition()).inflate(32.0);
            List<Villager> captives = server.getEntitiesOfClass(
                    Villager.class, captiveBox,
                    v -> v.hasData(ModAttachments.CAPTURED_STATE)
                            && v.getData(ModAttachments.CAPTURED_STATE).isCaptured()
            );
            for (Villager captive : captives) {
                captive.getData(ModAttachments.CAPTURED_STATE).markRescued();
                captive.dropLeash(true, true);
            }
        }
    }

    /**
     * If a rescued villager dies before the safety check completes, the reward
     * is forfeited silently — no posthumous delivery.
     */
    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (!villager.hasData(ModAttachments.CAPTURED_STATE)) return;
        CapturedState state = villager.getData(ModAttachments.CAPTURED_STATE);
        if (state.isActive()) {
            state.clear();
        }
    }

    /**
     * Two-part target-change handler:
     * <ol>
     *   <li><b>Captive protection</b> — prevents anything from targeting a
     *       villager currently held by a coven jailer.</li>
     *   <li><b>Reactive help</b> — when a hostile mob targets a village ally
     *       (villager, iron golem, fellow wizard, Guard Villagers guard…),
     *       every idle College wizard within 32 blocks aggros the attacker.
     *       Ally membership is defined by Iron's {@code VILLAGE_ALLIES} tag
     *       plus the {@link CollegeWizard} marker.</li>
     * </ol>
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) return;
        if (event.getEntity().level().isClientSide()) return;

        // ── Captive protection ──────────────────────────────────────────
        if (newTarget instanceof Villager villager
                && villager.hasData(ModAttachments.CAPTURED_STATE)
                && villager.getData(ModAttachments.CAPTURED_STATE).isCaptured()) {
            event.setCanceled(true);
            return;
        }

        // ── Reactive help — wizards defend village allies ───────────────
        if (!(event.getEntity() instanceof Mob attacker)) return;
        if (!(attacker instanceof Enemy)) return;

        boolean targetIsAlly = newTarget instanceof CollegeWizard
                || newTarget.getType().is(ModTags.VILLAGE_ALLIES);
        if (!targetIsAlly) return;

        AABB helpBox = newTarget.getBoundingBox().inflate(32.0);
        List<AbstractCollegeWizardEntity> helpers = newTarget.level().getEntitiesOfClass(
                AbstractCollegeWizardEntity.class, helpBox,
                w -> w.isAlive() && w.getTarget() == null
        );
        for (AbstractCollegeWizardEntity wizard : helpers) {
            if (wizard.canAttack(attacker)) {
                wizard.setTarget(attacker);
            }
        }
    }

    /**
     * Blocks the player from right-clicking (unleashing) a captured villager.
     * The only way to free a captive is to kill the jailer.
     */
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) return;
        if (!villager.hasData(ModAttachments.CAPTURED_STATE)) return;
        if (villager.getData(ModAttachments.CAPTURED_STATE).isCaptured()) {
            event.setCanceled(true);
        }
    }

    private GameBusEvents() {}
}
