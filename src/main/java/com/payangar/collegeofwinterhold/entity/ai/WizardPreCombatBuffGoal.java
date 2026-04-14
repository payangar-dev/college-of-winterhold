package com.payangar.collegeofwinterhold.entity.ai;

import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Casts the wizard's {@code BUFF} spells <b>at the moment combat starts</b>,
 * before the attack goal takes over. This is the "I prepare myself" beat the
 * player sees right after a wizard aggros.
 *
 * <p>Iron's {@link io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal}
 * is target-gated and only handles attack-time weighting, so without this goal
 * buffs would either fire mid-combat through support weighting (wrong moment)
 * or fire on nothing while patrolling (also wrong, looks weird).
 *
 * <p>Each buff respects a per-spell cooldown stored on the owning
 * {@link AbstractCollegeWizardEntity} and seeded from the spell's own
 * {@link AbstractSpell#getSpellCooldown()} — the same value Iron's uses to gate
 * player re-casts. This prevents a wizard from re-summoning the same companion
 * or refreshing the same buff on every successive combat, which was the source
 * of companion accumulation before Iron's recast system — disabled for mobs in
 * {@code MagicData.getPlayerRecasts} — failed to stop the duplication.
 *
 * <p>Uses {@link Goal.Flag#LOOK} + {@link Goal.Flag#TARGET}. The TARGET flag is
 * the key — it conflicts with Iron's {@code WizardAttackGoal}'s own TARGET flag,
 * so as long as this goal sits at a higher priority in the goal selector, the
 * attack goal is blocked from claiming the slot until every castable buff has
 * been fired.
 */
public class WizardPreCombatBuffGoal extends Goal {
    private static final int CAST_GAP_TICKS = 20;

    private final AbstractCollegeWizardEntity mob;
    private final List<RolledSpell> buffs = new ArrayList<>();
    private int castDelay = 0;

    public WizardPreCombatBuffGoal(AbstractCollegeWizardEntity mob) {
        this.mob = mob;
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
        return this;
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() == null || buffs.isEmpty()) return false;
        if (mob.isCasting()) return false;
        return hasCastableBuff();
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getTarget() == null) return false;
        return hasCastableBuff();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (mob.isCasting()) return;
        if (--castDelay > 0) return;

        for (RolledSpell rs : buffs) {
            AbstractSpell spell = rs.spell();
            if (mob.isBuffOnCooldown(spell.getSpellId())) continue;
            mob.initiateCastSpell(spell, rs.level());
            mob.recordBuffCast(spell.getSpellId(), spell.getSpellCooldown());
            castDelay = CAST_GAP_TICKS + spell.getCastTime(rs.level());
            return;
        }
    }

    @Override
    public void stop() {
        castDelay = 0;
    }

    private boolean hasCastableBuff() {
        for (RolledSpell rs : buffs) {
            if (!mob.isBuffOnCooldown(rs.spell().getSpellId())) return true;
        }
        return false;
    }
}
