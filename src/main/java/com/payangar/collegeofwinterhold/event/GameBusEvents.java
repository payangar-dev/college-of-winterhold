package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.command.CovenCommand;
import com.payangar.collegeofwinterhold.command.ExplorationCommand;
import com.payangar.collegeofwinterhold.entity.vampire.VampireEntity;
import com.payangar.collegeofwinterhold.entity.vampire.VampireHoundEntity;
import com.payangar.collegeofwinterhold.entity.villager.CapturedState;
import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.registry.ModAttachments;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
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
     * Injects target goals on every vanilla-style hostile that spawns, telling it
     * to attack our neutral mages on sight — same principle Minecraft hardcodes for
     * Iron Golems. Without this, zombies and the like would ignore a College wizard
     * entirely, breaking the "neutral but attackable by hostiles" contract.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Monster monster) {
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, LightningNoviceEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, LightningApprenticeEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, LightningAdeptEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, LightningExpertEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, LightningMasterEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, FireNoviceEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, FireApprenticeEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, FireAdeptEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, FireExpertEntity.class, true));
            monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                    monster, FireMasterEntity.class, true));
            // Vampires + hounds are Enemy-tagged but don't extend Monster, so
            // vanilla hostiles ignore them by default — same pattern as wizards.
            // Iron golems already target any Enemy via their stock goal, so no
            // injection is needed on them specifically.
            //
            // Undead mobs treat vampires as kin (both are in the
            // INVERTED_HEALING_AND_HARM tag), spiders are indifferent, and
            // creepers only ever target players in vanilla.
            boolean shouldTargetVampires = !monster.getType().is(EntityTypeTags.UNDEAD)
                    && !(monster instanceof Spider)
                    && !(monster instanceof Creeper);
            if (shouldTargetVampires) {
                monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                        monster, VampireEntity.class, true));
                monster.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                        monster, VampireHoundEntity.class, true));
            }
        }
        // College wizards target Monster.class in their own goal set, but
        // vampires aren't Monster — inject the two dedicated target goals here
        // so the wizard package stays free of vampire-side imports.
        if (event.getEntity() instanceof AbstractCollegeWizardEntity wizard) {
            wizard.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    wizard, VampireEntity.class, true));
            wizard.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    wizard, VampireHoundEntity.class, true));
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
     * Prevents any mob from targeting a villager that is currently captured by
     * a coven jailer. Without this, zombies and even the coven's own vampires
     * would attack the hostages and potentially kill them before the player
     * can rescue them.
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!(newTarget instanceof Villager villager)) return;
        if (!villager.hasData(ModAttachments.CAPTURED_STATE)) return;
        if (villager.getData(ModAttachments.CAPTURED_STATE).isCaptured()) {
            event.setCanceled(true);
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
