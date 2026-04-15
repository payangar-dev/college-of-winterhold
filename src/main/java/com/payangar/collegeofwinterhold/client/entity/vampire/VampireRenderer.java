package com.payangar.collegeofwinterhold.client.entity.vampire;

import com.payangar.collegeofwinterhold.client.render.layer.SpellBookHipLayer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VampireRenderer extends AbstractSpellCastingMobRenderer {
    public VampireRenderer(EntityRendererProvider.Context context) {
        super(context, new VampireModel());
        this.addRenderLayer(new SpellBookHipLayer(this));
    }
}
