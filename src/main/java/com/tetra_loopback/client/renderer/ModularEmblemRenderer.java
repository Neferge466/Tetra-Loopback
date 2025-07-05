package com.tetra_loopback.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class ModularEmblemRenderer implements ICurioRenderer {
    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource multiBufferSource,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        LivingEntity entity = slotContext.entity();

        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        //sneaking
        ICurioRenderer.translateIfSneaking(poseStack, entity);

        //position change
        float sideOffset = 0f;
        switch (com.tetra_loopback.Config.emblemPosition) {
            case "LEFT":
                sideOffset = -0.2F;
                break;
            case "RIGHT":
                sideOffset = 0.2F;
                break;
            case "CENTER":
            default:
                sideOffset = 0f;
        }

        //high
        float verticalOffset = entity instanceof Player ? 0.3F : 0.2F;

        //depth
        float baseDepth = (float) com.tetra_loopback.Config.emblemDepth;

        // check chest
        boolean wearingArmor = false;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            //check chest
            wearingArmor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        }

        //wearingArmor
        float depthOffset = -(baseDepth + (wearingArmor ? 0.1F : 0.05F));

        poseStack.translate(sideOffset, verticalOffset, depthOffset);

        //apply scale
        float scale = (float) com.tetra_loopback.Config.emblemScale;
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YP.rotationDegrees(0));//face direction
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));//up and down

        //item render
        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        entity,
                        stack,
                        ItemDisplayContext.FIXED,
                        false,
                        poseStack,
                        multiBufferSource,
                        entity.level(),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        0
                );

        poseStack.popPose();
    }


}


