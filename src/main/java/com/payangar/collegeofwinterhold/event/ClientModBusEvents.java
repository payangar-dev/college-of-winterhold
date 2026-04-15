package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.client.entity.wizard.ender.EnderAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ender.EnderApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ender.EnderExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ender.EnderMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ender.EnderNoviceRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.fire.FireAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.fire.FireApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.fire.FireExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.fire.FireMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.fire.FireNoviceRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.holy.HolyAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.holy.HolyApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.holy.HolyExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.holy.HolyMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.holy.HolyNoviceRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ice.IceAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ice.IceApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ice.IceExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ice.IceMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.ice.IceNoviceRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.lightning.LightningNoviceRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.nature.NatureAdeptRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.nature.NatureApprenticeRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.nature.NatureExpertRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.nature.NatureMasterRenderer;
import com.payangar.collegeofwinterhold.client.entity.wizard.nature.NatureNoviceRenderer;
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
        event.registerEntityRenderer(ModEntityTypes.FIRE_NOVICE.get(), FireNoviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_APPRENTICE.get(), FireApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_ADEPT.get(), FireAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_EXPERT.get(), FireExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FIRE_MASTER.get(), FireMasterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_NOVICE.get(), IceNoviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_APPRENTICE.get(), IceApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_ADEPT.get(), IceAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_EXPERT.get(), IceExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_MASTER.get(), IceMasterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_NOVICE.get(), EnderNoviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_APPRENTICE.get(), EnderApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_ADEPT.get(), EnderAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_EXPERT.get(), EnderExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ENDER_MASTER.get(), EnderMasterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NATURE_NOVICE.get(), NatureNoviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NATURE_APPRENTICE.get(), NatureApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NATURE_ADEPT.get(), NatureAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NATURE_EXPERT.get(), NatureExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NATURE_MASTER.get(), NatureMasterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HOLY_NOVICE.get(), HolyNoviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HOLY_APPRENTICE.get(), HolyApprenticeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HOLY_ADEPT.get(), HolyAdeptRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HOLY_EXPERT.get(), HolyExpertRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.HOLY_MASTER.get(), HolyMasterRenderer::new);
    }

    private ClientModBusEvents() {}
}
