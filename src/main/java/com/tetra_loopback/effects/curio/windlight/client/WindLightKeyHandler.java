package com.tetra_loopback.effects.curio.windlight.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tetra_loopback.Tetra_loopback;
import com.tetra_loopback.network.TLbNetwork;
import com.tetra_loopback.network.packet.DashPacket;
import com.tetra_loopback.network.packet.DoubleJumpPacket;
import com.tetra_loopback.network.packet.FastFallPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Tetra_loopback.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WindLightKeyHandler {
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

    private static boolean wasJumpMousePressed = false;
    private static boolean wasDashMousePressed = false;
    private static boolean wasFallMousePressed = false;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        handleInput(event.getKey(), event.getAction(), false);
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        handleInput(event.getButton(), event.getAction(), true);
    }

    private static void handleInput(int key, int action, boolean isMouse) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        boolean hasEffect = ClientWindLightChecker.hasWindLightEffect(mc.player);
        if (!hasEffect) {
            wasJumpPressed = WindLightKeyHandler.DOUBLE_JUMP_KEY.isDown();
            wasDashPressed = WindLightKeyHandler.DASH_KEY.isDown();
            wasFallPressed = WindLightKeyHandler.FAST_FALL_KEY.isDown();
            return;
        }

        boolean isJumpKey = isKeyPressed(WindLightKeyHandler.DOUBLE_JUMP_KEY, key, isMouse);
        boolean isDashKey = isKeyPressed(WindLightKeyHandler.DASH_KEY, key, isMouse);
        boolean isFallKey = isKeyPressed(WindLightKeyHandler.FAST_FALL_KEY, key, isMouse);

        if (isJumpKey) {
            handleDoubleJump(mc, key, action, isMouse);
        }
        if (isDashKey) {
            handleDash(mc, key, action, isMouse);
        }
        if (isFallKey) {
            handleFastFall(mc, key, action, isMouse);
        }
    }

    private static boolean isKeyPressed(KeyMapping keyMapping, int keyCode, boolean isMouse) {
        if (keyMapping.isUnbound()) return false;

        InputConstants.Key mappingKey = keyMapping.getKey();
        if (isMouse) {
            //鼠标
            return mappingKey.getType() == InputConstants.Type.MOUSE &&
                    mappingKey.getValue() == keyCode;
        } else {
            //键盘
            return mappingKey.getType() == InputConstants.Type.KEYSYM &&
                    mappingKey.getValue() == keyCode;
        }
    }

    //处理二段跳
    private static void handleDoubleJump(Minecraft mc, int key, int action, boolean isMouse) {
        boolean currentPressed = (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        boolean wasPressed = isMouse ? wasJumpMousePressed : wasJumpPressed;

        if (currentPressed && !wasPressed) {
            //必须在空中
            if (!mc.player.onGround()) {
                TLbNetwork.CHANNEL.sendToServer(new DoubleJumpPacket());
            }
        }

        //更新状态
        if (isMouse) {
            wasJumpMousePressed = currentPressed;
        } else {
            wasJumpPressed = currentPressed;
        }
    }

    //处理Dash
    private static void handleDash(Minecraft mc, int key, int action, boolean isMouse) {
        boolean currentPressed = (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        boolean wasPressed = isMouse ? wasDashMousePressed : wasDashPressed;

        if (currentPressed && !wasPressed) {
            if (!mc.player.onGround()) {
                //计算方向和向量
                int direction = calculateDashDirection(mc);
                Vec3 dashVector = calculateDashVector(mc.player, direction);

                TLbNetwork.CHANNEL.sendToServer(new DashPacket(
                        dashVector.x, dashVector.y, dashVector.z, direction
                ));
            }
        }

        if (isMouse) {
            wasDashMousePressed = currentPressed;
        } else {
            wasDashPressed = currentPressed;
        }
    }

    //处理快速坠落
    private static void handleFastFall(Minecraft mc, int key, int action, boolean isMouse) {
        boolean currentPressed = (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT);
        boolean wasPressed = isMouse ? wasFallMousePressed : wasFallPressed;

        if (currentPressed && !wasPressed) {
            //必须在空中
            if (!mc.player.onGround()) {
                TLbNetwork.CHANNEL.sendToServer(new FastFallPacket());
            }
        }

        if (isMouse) {
            wasFallMousePressed = currentPressed;
        } else {
            wasFallPressed = currentPressed;
        }
    }

    private static Vec3 calculateDashVector(net.minecraft.world.entity.player.Player player, int direction) {
        Vec3 look = player.getLookAngle();
        Vec3 result = Vec3.ZERO;

        Vec3 horizontalLook = new Vec3(look.x, 0, look.z).normalize();
        if (horizontalLook.lengthSqr() == 0) {
            horizontalLook = new Vec3(0, 0, 1);
        }

        Vec3 left = new Vec3(horizontalLook.z, 0, -horizontalLook.x).normalize();
        Vec3 right = new Vec3(-horizontalLook.z, 0, horizontalLook.x).normalize();

        switch (direction) {
            case 0: //前
                result = horizontalLook;
                break;
            case 1: //后
                result = horizontalLook.scale(-1);
                break;
            case 2: //左
                result = left;
                break;
            case 3: //右
                result = right;
                break;
            case 4: //前左
                result = horizontalLook.add(left).normalize();
                break;
            case 5: //前右
                result = horizontalLook.add(right).normalize();
                break;
            case 6: //后左
                result = horizontalLook.scale(-1).add(left).normalize();
                break;
            case 7: //后右
                result = horizontalLook.scale(-1).add(right).normalize();
                break;
            default:
                result = horizontalLook;
                break;
        }

        if (result.lengthSqr() == 0) {
            result = horizontalLook;
        }

        float dashSpeed = com.tetra_loopback.Config.dashSpeed;
        float dashVerticalBoost = com.tetra_loopback.Config.dashVerticalBoost;
        result = result.scale(dashSpeed).add(0, dashVerticalBoost, 0);
        return result;
    }

    private static int calculateDashDirection(Minecraft mc) {
        boolean forward = mc.options.keyUp.isDown();
        boolean back = mc.options.keyDown.isDown();
        boolean left = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();

        if (forward && !back && !left && !right) return 0;//前
        if (!forward && back && !left && !right) return 1;//后
        if (!forward && !back && left && !right) return 2;//左
        if (!forward && !back && !left && right) return 3;//右
        if (forward && !back && left && !right) return 4;//前左
        if (forward && !back && !left && right) return 5;//前右
        if (!forward && back && left && !right) return 6;//后左
        if (!forward && back && !left && right) return 7;//后右

        return 0;
    }
}