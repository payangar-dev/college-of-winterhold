package com.payangar.collegeofwinterhold.client.entity.wizard.nature;

import com.payangar.collegeofwinterhold.client.render.layer.SpellBookHipLayer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NatureAdeptRenderer extends AbstractSpellCastingMobRenderer {
    public NatureAdeptRenderer(EntityRendererProvider.Context context) {
        super(context, new NatureAdeptModel());
        this.addRenderLayer(new SpellBookHipLayer(this));
    }
}
