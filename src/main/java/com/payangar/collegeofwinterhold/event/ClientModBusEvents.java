package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningMasterRenderer;
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
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_APPRENTICE.get(), LightningApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_ADEPT.get(), LightningAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_EXPERT.get(), LightningExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LIGHTNING_MASTER.get(), LightningMasterRenderer::new);
    }

    private ClientModBusEvents() {}
}
