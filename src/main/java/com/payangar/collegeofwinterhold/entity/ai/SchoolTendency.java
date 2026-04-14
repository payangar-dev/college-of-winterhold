package com.payangar.collegeofwinterhold.entity.ai;

/**
 * Per-school behavioral tuning applied to {@link CollegeWizardAttackGoal}. Each
 * school has its own personality : Holy supports, Fire rushes, Lightning kites,
 * Ender repositions, Nature/Ice stay neutral. The tuning is data-only — no new
 * AI code, just multipliers on Iron's existing category weights plus two range
 * knobs the goal applies at {@code setTendency} time.
 *
 * <p>The four {@code xxxWeightMult} fields multiply Iron's base weights inside
 * {@code getAttackWeight / getDefenseWeight / getMovementWeight / getSupportWeight}.
 * Values above 1.0 bias the goal towards that category at pick time ; values
 * below 1.0 away from it. Negative base weights in Iron's remain negative after
 * multiplication (e.g. defense {@code baseWeight = -20} → stays suppressive
 * until HP drops), so the absolute sign is preserved.
 *
 * <p>The {@code spellcastingRange} knob tunes the "sweet spot" distance the
 * wizard wants to hold. Smaller → the mob approaches closer before stopping
 * (Fire). Larger → it stops earlier and kites from afar (Lightning).
 *
 * <p>The {@code allowFleeing} knob disables Iron's proximity-flee behavior
 * entirely — set to {@code false} for rushers (Fire) so they don't back off
 * when the target closes the gap.
 */
public record SchoolTendency(
        float attackWeightMult,
        float defenseWeightMult,
        float movementWeightMult,
        float supportWeightMult,
        float spellcastingRange,
        boolean allowFleeing
) {
    public static final SchoolTendency NEUTRAL =
            new SchoolTendency(1.0f, 1.0f, 1.0f, 1.0f, 20f, true);

    public static final SchoolTendency HOLY =
            new SchoolTendency(0.7f, 1.5f, 1.0f, 1.5f, 20f, true);

    public static final SchoolTendency FIRE =
            new SchoolTendency(1.3f, 0.8f, 0.6f, 0.8f, 12f, false);

    public static final SchoolTendency LIGHTNING =
            new SchoolTendency(1.0f, 0.9f, 1.3f, 0.9f, 24f, true);

    public static final SchoolTendency ENDER =
            new SchoolTendency(1.0f, 1.0f, 1.2f, 1.0f, 20f, true);

    public static final SchoolTendency NATURE = NEUTRAL;

    public static final SchoolTendency ICE = NEUTRAL;
}
