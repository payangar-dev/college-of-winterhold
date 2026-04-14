package com.payangar.collegeofwinterhold.entity.ai;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollegeWizardAttackGoal extends WizardAttackGoal {
    private static final double MELEE_THRESHOLD_SQR = 4.0 * 4.0;
    private static final double SURROUNDED_RADIUS = 6.0;
    private static final int SURROUNDED_THRESHOLD = 3;
    private static final float CRITICAL_HP_RATIO = 0.30f;

    /** Per-spell cast level, populated by {@link #setLoadout(List)}. Allows native and
     * foreign spells of the same wizard to cast at different Iron's spell levels
     * (e.g. a Master casts native at level 8-9 and foreign at level 4-5). Iron's own
     * {@code minSpellQuality}/{@code maxSpellQuality} are global and cannot model this. */
    private final Map<String, Integer> spellLevels = new HashMap<>();

    public CollegeWizardAttackGoal(IMagicEntity mob, double speedModifier, int attackIntervalMin, int attackIntervalMax) {
        super(mob, speedModifier, attackIntervalMin, attackIntervalMax);
    }

    /**
     * Auto-classifies the wizard's loadout via {@link SpellClassificationRegistry}
     * and routes each spell into the correct Iron's bucket(s). A spell tagged with
     * multiple categories (e.g. Thunder Step = ATTACK_DISTANCE + MOVEMENT) lands in
     * every matching list. Each spell's cast level is stored separately.
     *
     * <ul>
     *   <li>{@code ATTACK_CAC} / {@code ATTACK_DISTANCE} → {@code attackSpells}
     *       (further filtered by distance + AOE bias in {@link #getNextSpellType()}).</li>
     *   <li>{@code DEFENSE} / {@code HEAL} → {@code defenseSpells}.
     *       HEAL force-pick at critical HP is TODO.</li>
     *   <li>{@code MOVEMENT} → {@code movementSpells}.</li>
     *   <li>{@code BUFF} → {@code supportSpells}.
     *       A dedicated pre-combat buff goal is TODO; meanwhile buffs fire mid-combat
     *       through Iron's HP-low weighting.</li>
     * </ul>
     */
    public CollegeWizardAttackGoal setLoadout(List<RolledSpell> loadout) {
        spellLevels.clear();
        List<AbstractSpell> attack = new ArrayList<>();
        List<AbstractSpell> defense = new ArrayList<>();
        List<AbstractSpell> movement = new ArrayList<>();
        List<AbstractSpell> support = new ArrayList<>();

        for (RolledSpell rolled : loadout) {
            AbstractSpell spell = rolled.spell();
            int level = Math.max(1, Math.min(rolled.level(), spell.getMaxLevel()));
            spellLevels.put(spell.getSpellId(), level);

            SpellClassification clazz = SpellClassificationRegistry.of(spell);
            if (clazz.hasCategory(SpellCategory.ATTACK_CAC) || clazz.hasCategory(SpellCategory.ATTACK_DISTANCE)) {
                attack.add(spell);
            }
            if (clazz.hasCategory(SpellCategory.DEFENSE) || clazz.hasCategory(SpellCategory.HEAL)) {
                defense.add(spell);
            }
            if (clazz.hasCategory(SpellCategory.MOVEMENT)) {
                movement.add(spell);
            }
            if (clazz.hasCategory(SpellCategory.BUFF)) {
                support.add(spell);
            }
        }
        setSpells(attack, defense, movement, support);
        return this;
    }

    /**
     * Mirrors Iron's {@code doSpellAction} but with two custom layers on top:
     * <ol>
     *   <li><b>Survival routine</b> at HP &lt; {@value #CRITICAL_HP_RATIO} :
     *       force-cast a {@code MOVEMENT} spell if surrounded (escape), or a
     *       {@code HEAL} spell otherwise (safe heal). Falls through to normal
     *       weighting if the wizard doesn't have the matching spell.</li>
     *   <li><b>Per-spell level resolution</b> : cast level is read from
     *       {@link #spellLevels} instead of Iron's global quality range, so
     *       native and foreign spells of the same wizard cast at different
     *       Iron's levels.</li>
     * </ol>
     * The single-use spell branch is preserved as-is.
     */
    @Override
    protected void doSpellAction() {
        if (!spellCastingMob.getHasUsedSingleAttack() && singleUseSpell != SpellRegistry.none() && singleUseDelay <= 0) {
            spellCastingMob.setHasUsedSingleAttack(true);
            spellCastingMob.initiateCastSpell(singleUseSpell, singleUseLevel);
            fleeCooldown = 7 + singleUseSpell.getCastTime(singleUseLevel);
            return;
        }

        AbstractSpell survival = pickSurvivalSpell();
        if (survival != null) {
            int level = spellLevels.getOrDefault(survival.getSpellId(), 1);
            if (!survival.shouldAIStopCasting(level, mob, target)) {
                spellCastingMob.initiateCastSpell(survival, level);
                fleeCooldown = 7 + survival.getCastTime(level);
                return;
            }
        }

        AbstractSpell spell = getNextSpellType();
        if (spell == null || spell == SpellRegistry.none()) {
            spellAttackDelay = 5;
            return;
        }
        int spellLevel = spellLevels.getOrDefault(spell.getSpellId(), 1);
        if (!spell.shouldAIStopCasting(spellLevel, mob, target)) {
            spellCastingMob.initiateCastSpell(spell, spellLevel);
            fleeCooldown = 7 + spell.getCastTime(spellLevel);
        } else {
            spellAttackDelay = 5;
        }
    }

    /**
     * Returns a MOVEMENT or HEAL spell to force-cast when the wizard is in
     * critical HP, or {@code null} if no override applies (normal weighting will
     * take over). Surrounded → escape via MOVEMENT. Otherwise → safe-zone HEAL.
     */
    private AbstractSpell pickSurvivalSpell() {
        if (target == null) return null;
        if (mob.getHealth() / mob.getMaxHealth() >= CRITICAL_HP_RATIO) return null;

        if (isSurrounded()) {
            if (!movementSpells.isEmpty()) {
                return movementSpells.get(mob.getRandom().nextInt(movementSpells.size()));
            }
        } else {
            // HEAL spells live in defenseSpells alongside DEFENSE — filter to HEAL only.
            for (AbstractSpell spell : defenseSpells) {
                if (SpellClassificationRegistry.of(spell).hasCategory(SpellCategory.HEAL)) {
                    return spell;
                }
            }
        }
        return null;
    }

    @Override
    protected AbstractSpell getNextSpellType() {
        if (target == null || attackSpells.isEmpty()) {
            return super.getNextSpellType();
        }
        double distSqr = mob.distanceToSqr(target.position());
        boolean targetIsClose = distSqr <= MELEE_THRESHOLD_SQR;

        // Step 1 — distance filter: keep only attack spells whose CAC/DISTANCE tag
        // matches the current target distance. A multi-category spell qualifies if
        // ANY of its categories matches the bracket.
        List<AbstractSpell> viable = new ArrayList<>(attackSpells.size());
        for (AbstractSpell spell : attackSpells) {
            SpellClassification clazz = SpellClassificationRegistry.of(spell);
            if (targetIsClose && clazz.hasCategory(SpellCategory.ATTACK_CAC)) {
                viable.add(spell);
            } else if (!targetIsClose && clazz.hasCategory(SpellCategory.ATTACK_DISTANCE)) {
                viable.add(spell);
            }
        }
        if (viable.isEmpty()) {
            return super.getNextSpellType();
        }

        // Step 2 — AOE bias: if the wizard is surrounded by multiple hostiles,
        // hard-prefer AOE over single-target. Falls back to the full viable list
        // if no AOE is available at the current range bracket.
        List<AbstractSpell> picked = viable;
        if (isSurrounded()) {
            List<AbstractSpell> aoeOnly = new ArrayList<>(viable.size());
            for (AbstractSpell spell : viable) {
                if (SpellClassificationRegistry.of(spell).targeting() == SpellTargeting.AOE) {
                    aoeOnly.add(spell);
                }
            }
            if (!aoeOnly.isEmpty()) {
                picked = aoeOnly;
            }
        }

        List<AbstractSpell> snapshot = new ArrayList<>(attackSpells);
        attackSpells.clear();
        attackSpells.addAll(picked);
        AbstractSpell result = super.getNextSpellType();
        attackSpells.clear();
        attackSpells.addAll(snapshot);
        return result;
    }

    private boolean isSurrounded() {
        return mob.level().getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(SURROUNDED_RADIUS),
                e -> e != mob && e.isAlive() && mob.canAttack(e)
        ).size() >= SURROUNDED_THRESHOLD;
    }
}
