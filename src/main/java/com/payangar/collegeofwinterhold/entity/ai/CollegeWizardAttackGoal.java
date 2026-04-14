package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;

import java.util.ArrayList;
import java.util.List;

public class CollegeWizardAttackGoal extends WizardAttackGoal {
    private static final double MELEE_THRESHOLD_SQR = 4.0 * 4.0;

    public CollegeWizardAttackGoal(IMagicEntity mob, double speedModifier, int attackIntervalMin, int attackIntervalMax) {
        super(mob, speedModifier, attackIntervalMin, attackIntervalMax);
    }

    @Override
    protected AbstractSpell getNextSpellType() {
        if (target == null || attackSpells.isEmpty()) {
            return super.getNextSpellType();
        }
        double distSqr = mob.distanceToSqr(target.position());
        boolean targetIsClose = distSqr <= MELEE_THRESHOLD_SQR;

        List<AbstractSpell> viable = new ArrayList<>(attackSpells.size());
        for (AbstractSpell spell : attackSpells) {
            SpellRange range = SpellRangeRegistry.of(spell);
            if (range == SpellRange.SELF || range == SpellRange.AOE) {
                viable.add(spell);
            } else if (targetIsClose && range == SpellRange.CAC) {
                viable.add(spell);
            } else if (!targetIsClose && range == SpellRange.DISTANCE) {
                viable.add(spell);
            }
        }
        if (viable.isEmpty()) {
            return super.getNextSpellType();
        }
        List<AbstractSpell> snapshot = new ArrayList<>(attackSpells);
        attackSpells.clear();
        attackSpells.addAll(viable);
        AbstractSpell picked = super.getNextSpellType();
        attackSpells.clear();
        attackSpells.addAll(snapshot);
        return picked;
    }
}
