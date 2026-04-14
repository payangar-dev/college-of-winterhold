package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

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
        }
    }

    private GameBusEvents() {}
}
