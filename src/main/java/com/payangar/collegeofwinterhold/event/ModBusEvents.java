package com.payangar.collegeofwinterhold.event;

import com.payangar.collegeofwinterhold.CollegeOfWinterhold;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.fire.FireNoviceEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningAdeptEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningApprenticeEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningExpertEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningMasterEntity;
import com.payangar.collegeofwinterhold.entity.wizard.lightning.LightningNoviceEntity;
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
    }

    private ModBusEvents() {}
}
