package com.payangar.collegeofwinterhold.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.payangar.collegeofwinterhold.entity.wizard.HipSpellbookHolder;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

/**
 * Renders the mage's spellbook at the hip, mirroring the position Iron's Spellbooks
 * uses for the player's Curios belt slot. Offsets copied verbatim from their
 * {@code SpellBookCurioRenderer} so mage and player books align visually.
 */
@OnlyIn(Dist.CLIENT)
public class SpellBookHipLayer extends GeoRenderLayer<AbstractSpellCastingMob> {
    public SpellBookHipLayer(GeoEntityRenderer<AbstractSpellCastingMob> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, AbstractSpellCastingMob animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (!(animatable instanceof HipSpellbookHolder holder)) return;
        ItemStack stack = holder.getHipSpellbook();
        if (stack.isEmpty()) return;

        bakedModel.getBone("body").ifPresent(bodyBone -> {
            poseStack.pushPose();

            // yBodyRot isn't in the GeoRenderLayer poseStack — Iron's
            // HumanoidRenderer applies it manually for its hard-coded cape layer
            // (lines 199-200) for the same reason.
            float yBodyRot = Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - yBodyRot));

            // Cumulates the full parent chain for the body bone.
            RenderUtil.prepMatrixForBone(poseStack, bodyBone);
            // GeckoLib bone space → vanilla axis space, so Iron's offsets (which
            // were calibrated against humanoidModel.body.translateAndRotate) match.
            poseStack.scale(-1f, -1f, 1f);
            // Vertical compensation: after prepMatrixForBone + axis flip, the
            // poseStack sits at the GeoBone pivot, which is ~1.5 blocks above
            // where humanoidModel.body.translateAndRotate would leave it. Iron's
            // HumanoidRenderer applies the exact same -1.501 offset for its cape.
            poseStack.translate(0f, -1.501f, 0f);

            float chestOffset = animatable.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? -4.5f : -5.5f;
            poseStack.translate(chestOffset * 0.0625f, 9f * 0.0625f, 0f);
            poseStack.mulPose(Axis.YP.rotation(Mth.PI));
            poseStack.mulPose(Axis.ZP.rotation(Mth.PI - 5f * Mth.DEG_TO_RAD));
            poseStack.scale(0.625f, 0.625f, 0.625f);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, animatable.level(), 0);

            poseStack.popPose();
        });
    }
}
