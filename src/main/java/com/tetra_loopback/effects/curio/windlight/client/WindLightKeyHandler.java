package com.tetra_loopback.effects.curio.windlight.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WindLightKeyHandler {
    //按键绑定
    public static final KeyMapping DOUBLE_JUMP_KEY = new KeyMapping(
            "key.tetra_loopback.double_jump",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            "key.categories.tetra_loopback"
    );

    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.tetra_loopback.dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.tetra_loopback"
    );

    public static final KeyMapping FAST_FALL_KEY = new KeyMapping(
            "key.tetra_loopback.fast_fall",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.tetra_loopback"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DOUBLE_JUMP_KEY);
        event.register(DASH_KEY);
        event.register(FAST_FALL_KEY);
    }
}

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, value = Dist.CLIENT)
class WindLightInputHandler {
    private static boolean wasJumpPressed = false;
    private static boolean wasDashPressed = false;
    private static boolean wasFallPressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        //客户端检查
        boolean hasEffect = ClientWindLightChecker.hasWindLightEffect(mc.player);

        if (!hasEffect) {
            //没有效果则不处理任何按键
            wasJumpPressed = WindLightKeyHandler.DOUBLE_JUMP_KEY.isDown();
            wasDashPressed = WindLightKeyHandler.DASH_KEY.isDown();
            wasFallPressed = WindLightKeyHandler.FAST_FALL_KEY.isDown();
            return;
        }

        handleDoubleJump(mc, event);
        handleDash(mc, event);
        handleFastFall(mc, event);
    }

    //处理二段跳
    private static void handleDoubleJump(Minecraft mc, InputEvent.Key event) {
        boolean isJumpPressed = WindLightKeyHandler.DOUBLE_JUMP_KEY.isDown();

        if (isJumpPressed && !wasJumpPressed && event.getAction() == GLFW.GLFW_PRESS) {
            //必须在空中
            if (!mc.player.onGround()) {
                TLbNetwork.CHANNEL.sendToServer(new DoubleJumpPacket());
            }
        }

        wasJumpPressed = isJumpPressed;
    }

    //处理Dash
    private static void handleDash(Minecraft mc, InputEvent.Key event) {
        boolean isDashPressed = WindLightKeyHandler.DASH_KEY.isDown();

        if (isDashPressed && !wasDashPressed && event.getAction() == GLFW.GLFW_PRESS) {
            if (!mc.player.onGround()) {
                int direction = calculateDashDirection(mc);
                TLbNetwork.CHANNEL.sendToServer(new DashPacket(direction));
            }
        }

        wasDashPressed = isDashPressed;
    }

    //处理快速坠落
    private static void handleFastFall(Minecraft mc, InputEvent.Key event) {
        boolean isFallPressed = WindLightKeyHandler.FAST_FALL_KEY.isDown();

        if (isFallPressed && !wasFallPressed && event.getAction() == GLFW.GLFW_PRESS) {
            //必须在空中
            if (!mc.player.onGround()) {
                TLbNetwork.CHANNEL.sendToServer(new FastFallPacket());
            }
        }

        wasFallPressed = isFallPressed;
    }

    private static int calculateDashDirection(Minecraft mc) {
        boolean forward = mc.options.keyUp.isDown();
        boolean back = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();

        //方向映射与服务器端一致
        if (forward && !back && !left && !right) return 0;  //前
        if (!forward && back && !left && !right) return 1;  //后
        if (!forward && !back && left && !right) return 2;  //左
        if (!forward && !back && !left && right) return 3;  //右

        //组合方向
        if (forward && !back && left && !right) return 4;   //前左
        if (forward && !back && !left && right) return 5;   //前右
        if (!forward && back && left && !right) return 6;   //后左
        if (!forward && back && !left && right) return 7;   //后右

        return 0;//默认向前
    }
}