package com.payangar.collegeofwinterhold.client.entity.wizard.ice;

import com.payangar.collegeofwinterhold.client.render.layer.SpellBookHipLayer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IceAdeptRenderer extends AbstractSpellCastingMobRenderer {
    public IceAdeptRenderer(EntityRendererProvider.Context context) {
        super(context, new IceAdeptModel());
        this.addRenderLayer(new SpellBookHipLayer(this));
    }
}
