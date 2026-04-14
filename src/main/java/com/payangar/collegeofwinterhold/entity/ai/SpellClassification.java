package com.payangar.collegeofwinterhold.entity.ai;

import java.util.Set;

public record SpellClassification(Set<SpellCategory> categories, SpellTargeting targeting) {
    public boolean hasCategory(SpellCategory category) {
        return categories.contains(category);
    }
}
