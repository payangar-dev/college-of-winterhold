package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningNoviceRenderer;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CollegeOfWinterhold.MODID, value = Dist.CLIENT)
public final class ClientModBusEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_NOVICE.get(), LightningNoviceRenderer::new);
    }

    private ClientModBusEvents() {}
}
