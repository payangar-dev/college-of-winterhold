package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Casts the wizard's {@code BUFF} spells <b>at the moment combat starts</b>,
 * before the attack goal takes over. This is the "I prepare myself" beat the
 * player sees right after a wizard aggros.
 *
 * <p>Iron's {@link io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal}
 * is target-gated and only handles attack-time weighting, so without this goal
 * buffs would either fire mid-combat through support weighting (wrong moment)
 * or fire on nothing while patrolling (also wrong, looks weird). This goal
 * threads the needle: trigger on the no-target → target transition, cast all
 * known buffs sequentially while blocking the attack goal, then yield.
 *
 * <p>Lifecycle :
 * <ul>
 *   <li>{@code canUse()} returns {@code true} when {@code mob.getTarget() != null}
 *       AND there is at least one buff that hasn't been cast in the current cycle.</li>
 *   <li>The cycle resets on the no-target → target transition, so each new
 *       combat re-buffs the wizard.</li>
 *   <li>Casts each known buff once with a small gap between casts so the
 *       animations don't overlap.</li>
 *   <li>Once all buffs in the cycle are cast, {@code canUse()} returns {@code false}
 *       and the attack goal can claim the slot.</li>
 * </ul>
 *
 * <p>Uses {@link Goal.Flag#LOOK} + {@link Goal.Flag#TARGET}. The TARGET flag is
 * the key — it conflicts with Iron's {@code WizardAttackGoal}'s own TARGET flag,
 * so as long as this goal sits at a higher priority in the goal selector, the
 * attack goal is blocked from claiming the slot until buffing is done.
 */
public class WizardPreCombatBuffGoal extends Goal {
    private static final int CAST_GAP_TICKS = 20;

    private final IMagicEntity spellCastingMob;
    private final PathfinderMob mob;
    private final List<RolledSpell> buffs = new ArrayList<>();
    private final Set<String> castedThisCycle = new HashSet<>();
    private boolean wasInCombat = false;
    private int castDelay = 0;

    public WizardPreCombatBuffGoal(IMagicEntity mob) {
        if (!(mob instanceof PathfinderMob pm)) {
            throw new IllegalStateException("WizardPreCombatBuffGoal requires a PathfinderMob");
        }
        this.spellCastingMob = mob;
        this.mob = pm;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.TARGET));
    }

    /**
     * Replaces the goal's buff list. Called from the entity at the same time
     * the attack goal receives its loadout.
     */
    public WizardPreCombatBuffGoal setLoadout(List<RolledSpell> loadout) {
        buffs.clear();
        for (RolledSpell rs : loadout) {
            if (SpellClassificationRegistry.of(rs.spell()).hasCategory(SpellCategory.BUFF)) {
                buffs.add(rs);
            }
        }
        castedThisCycle.clear();
        wasInCombat = false;
        return this;
    }

    @Override
    public boolean canUse() {
        boolean hasTarget = mob.getTarget() != null;

        // Combat entry detection: when we just acquired a target, reset the
        // cast cycle so the new fight gets a fresh round of buffs.
        if (hasTarget && !wasInCombat) {
            castedThisCycle.clear();
        }
        wasInCombat = hasTarget;

        if (!hasTarget || buffs.isEmpty()) return false;
        if (spellCastingMob.isCasting()) return false;

        for (RolledSpell rs : buffs) {
            if (!castedThisCycle.contains(rs.spell().getSpellId())) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() == null) return false;
        for (RolledSpell rs : buffs) {
            if (!castedThisCycle.contains(rs.spell().getSpellId())) return true;
        }
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (spellCastingMob.isCasting()) return;
        if (--castDelay > 0) return;

        for (RolledSpell rs : buffs) {
            String id = rs.spell().getSpellId();
            if (!castedThisCycle.contains(id)) {
                spellCastingMob.initiateCastSpell(rs.spell(), rs.level());
                castedThisCycle.add(id);
                castDelay = CAST_GAP_TICKS + rs.spell().getCastTime(rs.level());
                return;
            }
        }
    }

    @Override
    public void stop() {
        castDelay = 0;
    }
}
