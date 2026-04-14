package com.payangar.collegeofwinterhold.entity.wizard.ender;

import com.payangar.collegeofwinterhold.entity.wizard.AbstractCollegeWizardEntity;
import com.payangar.collegeofwinterhold.entity.wizard.core.CollegeSchool;
import com.payangar.collegeofwinterhold.entity.wizard.core.WizardTier;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EnderNoviceEntity extends AbstractCollegeWizardEntity {
    public EnderNoviceEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return AbstractCollegeWizardEntity.buildAttributes(WizardTier.NOVICE);
    }

    @Override protected WizardTier tier()      { return WizardTier.NOVICE; }
    @Override protected CollegeSchool school() { return CollegeSchool.ENDER; }
}
