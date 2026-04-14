package com.payangar.collegeofwinterhold.client.entity.wizard.fire;

import com.payangar.collegeofwinterhold.client.render.layer.SpellBookHipLayer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireExpertRenderer extends AbstractSpellCastingMobRenderer {
    public FireExpertRenderer(EntityRendererProvider.Context context) {
        super(context, new FireExpertModel());
        this.addRenderLayer(new SpellBookHipLayer(this));
    }
}
