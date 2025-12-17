package com.tetra_loopback.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class ModularGogglesRenderer implements ICurioRenderer {

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

        //潜行时调整位置
        ICurioRenderer.translateIfSneaking(poseStack, entity);

        //获取头部位置
        //头部大约在Y轴+1.62位置
        float headHeight = entity instanceof Player ? 1.62F : 1.4F;

        //调整到头部位置
        float verticalOffset = headHeight - entity.getEyeHeight();
        float sideOffset = 0.0F;
        float depthOffset = 0.0F;

        //应用基础偏移
        poseStack.translate(sideOffset, verticalOffset, depthOffset);

        //直接使用传入的头部旋转参数
        poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        //在头部旋转后的坐标系中调整护目镜位置
        float gogglesVerticalOffset = -0.3F; //从头部中心到眼部
        float gogglesDepthOffset = -0.1F; //在面部前方

        //应用配置偏移
        gogglesVerticalOffset += (float) com.tetra_loopback.Config.gogglesVerticalOffset;
        gogglesDepthOffset += (float) com.tetra_loopback.Config.gogglesDepthOffset;

        //检查是否戴头盔
        boolean wearingHelmet = false;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            wearingHelmet = !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        }

        //戴头盔时向后调整
        if (wearingHelmet) {
            gogglesDepthOffset -= 0.03F;
        }

        //应用护目镜位置调整
        poseStack.translate(0.0F, gogglesVerticalOffset, gogglesDepthOffset);

        //应用缩放
        float scale = (float) com.tetra_loopback.Config.gogglesScale;
        poseStack.scale(scale, scale, scale);

        //应用固定旋转
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // 上下翻转
        //应用配置旋转
        poseStack.mulPose(Axis.XP.rotationDegrees((float) com.tetra_loopback.Config.gogglesRotationX));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) com.tetra_loopback.Config.gogglesRotationY));

        //渲染物品
        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        multiBufferSource,
                        entity.level(),
                        0
                );

        poseStack.popPose();
    }
}