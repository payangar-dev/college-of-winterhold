package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ender.EnderNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.holy.HolyNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.ice.IceNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.nature.NatureNoviceEntity;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = CollegeOfWinterhold.MODID)
public final class ModBusEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.LIGHTNING_NOVICE.get(), LightningNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.LIGHTNING_APPRENTICE.get(), LightningApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.LIGHTNING_ADEPT.get(), LightningAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.LIGHTNING_EXPERT.get(), LightningExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.LIGHTNING_MASTER.get(), LightningMasterEntity.prepareAttributes().build());
        event.put(ModEntityTypes.FIRE_NOVICE.get(), FireNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.FIRE_APPRENTICE.get(), FireApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.FIRE_ADEPT.get(), FireAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.FIRE_EXPERT.get(), FireExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.FIRE_MASTER.get(), FireMasterEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ICE_NOVICE.get(), IceNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ICE_APPRENTICE.get(), IceApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ICE_ADEPT.get(), IceAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ICE_EXPERT.get(), IceExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ICE_MASTER.get(), IceMasterEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ENDER_NOVICE.get(), EnderNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ENDER_APPRENTICE.get(), EnderApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ENDER_ADEPT.get(), EnderAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ENDER_EXPERT.get(), EnderExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.ENDER_MASTER.get(), EnderMasterEntity.prepareAttributes().build());
        event.put(ModEntityTypes.NATURE_NOVICE.get(), NatureNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.NATURE_APPRENTICE.get(), NatureApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.NATURE_ADEPT.get(), NatureAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.NATURE_EXPERT.get(), NatureExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.NATURE_MASTER.get(), NatureMasterEntity.prepareAttributes().build());
        event.put(ModEntityTypes.HOLY_NOVICE.get(), HolyNoviceEntity.prepareAttributes().build());
        event.put(ModEntityTypes.HOLY_APPRENTICE.get(), HolyApprenticeEntity.prepareAttributes().build());
        event.put(ModEntityTypes.HOLY_ADEPT.get(), HolyAdeptEntity.prepareAttributes().build());
        event.put(ModEntityTypes.HOLY_EXPERT.get(), HolyExpertEntity.prepareAttributes().build());
        event.put(ModEntityTypes.HOLY_MASTER.get(), HolyMasterEntity.prepareAttributes().build());
    }

    private ModBusEvents() {}
}
