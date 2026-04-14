package com.payangar.collegeofwinterhold.entity.wizard;

/**
 * Marker interface implemented by every wizard entity from the College of
 * Winterhold. Used to identify "fellow students/teachers" for the friendly-
 * faction logic in {@code isAlliedTo}, so wizards never aggro each other
 * through accidental AoE spillover.
 *
 * <p>Iron's {@link io.redspace.ironsspellbooks.damage.DamageSources#isFriendlyFireBetween}
 * falls through to {@code Entity.isAlliedTo} as its last check — overriding
 * that method on each entity to return {@code true} for other {@code CollegeWizard}
 * instances is enough to make all College mages mutually friendly across
 * schools, with no further wiring needed in Iron's spell pipeline.
 */
public interface CollegeWizard {
}
