package com.tetra_loopback.effects.gui.vision;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VisionFieldOverlay implements IGuiOverlay {
    private final Minecraft minecraft;
    private int effectLevel;

    //控制显示状态的变量
    private boolean isVisible = true;
    private long lastToggleTime = 0;
    private static final long TOGGLE_COOLDOWN = 200; // 防抖延迟（毫秒）

    //基础数据（实时更新）
    private BlockPos playerPos;
    private int enemyCount;
    private ItemStack heldItem;

    //缩放因子
    private float scaleFactor = 0.85f;

    //颜色常量
    private static final int HIGHLIGHT_COLOR = 0xFFFFD700; // 金色
    private static final int TEXT_COLOR = 0xFFDCDCDC;       // 浅灰色
    private static final int WARNING_COLOR = 0xFFFF6464;    // 红色
    private static final int GREEN_COLOR = 0xFF64FF64;      // 绿色

    public VisionFieldOverlay() {
        this.minecraft = Minecraft.getInstance();
        this.effectLevel = 0;
    }

    public void updateEffectLevel(int newLevel) {
        this.effectLevel = newLevel;
    }

    public void updatePlayerData(Player player) {
        if (effectLevel == 0) return;

        //实时更新基础数据
        this.playerPos = player.blockPosition();

        //3级：敌人数量
        if (effectLevel >= 3) {
            this.enemyCount = countNearbyEnemies(player, 16);
        }

        //5级：手持物品
        if (effectLevel >= 5) {
            this.heldItem = getHeldItem(player);
        }
    }

    //切换显示状态的方法
    public void toggleVisibility() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime > TOGGLE_COOLDOWN) {
            isVisible = !isVisible;
            lastToggleTime = currentTime;

            //显示切换提示
            if (minecraft.player != null) {
                String translationKey = isVisible ?
                        "message.tetra_loopback.vision_overlay.shown" :
                        "message.tetra_loopback.vision_overlay.hidden";
                minecraft.player.displayClientMessage(Component.translatable(translationKey), true);
            }
        }
    }

    //设置显示状态
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    //获取显示状态
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        //是否渲染
        if (!isVisible || effectLevel == 0 || minecraft.options.hideGui) return;

        Window window = minecraft.getWindow();
        int right = window.getGuiScaledWidth();

        //应用缩放
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);

        //缩放后的坐标
        float inverseScale = 1.0f / scaleFactor;
        int scaledRight = (int)(right * inverseScale);

        List<Component> lines = new ArrayList<>();

        //标题-添加状态指示器
        String title = isVisible ?
                "effect.tetra_loopback.vision_field.title" :
                "effect.tetra_loopback.vision_field.title_hidden";
        lines.add(Component.translatable(title, effectLevel));

        //1级-实时获取坐标和速度
        if (effectLevel >= 1) {
            String coordinates = String.format("%d, %d, %d", playerPos.getX(), playerPos.getY(), playerPos.getZ());
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.coordinates", coordinates));

            Player player = minecraft.player;
            double motionX = player.getX() - player.xOld;
            double motionZ = player.getZ() - player.zOld;
            String speed = String.format("%.2f", Math.sqrt(motionX * motionX + motionZ * motionZ) * 20);
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.speed", speed));

            String facing = getFacingDirection(player.getYRot());
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.facing", facing));
        }

        //2级-实时获取所有属性
        if (effectLevel >= 2) {
            Player player = minecraft.player;
            lines.add(Component.empty());
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.attributes.title"));

            //实时获取所有属性值
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.armor",
                    String.format("%.1f", getAttributeValue(player, Attributes.ARMOR))));
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.armor_toughness",
                    String.format("%.1f", getAttributeValue(player, Attributes.ARMOR_TOUGHNESS))));

            //获取攻击伤害
            double attackDamage = getWeaponDamage(player);
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.attack_damage",
                    String.format("%.1f", attackDamage)));

            lines.add(Component.translatable("effect.tetra_loopback.vision_field.attack_speed",
                    String.format("%.1f", getAttributeValue(player, Attributes.ATTACK_SPEED))));
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.knockback_resistance",
                    String.format("%.1f", getAttributeValue(player, Attributes.KNOCKBACK_RESISTANCE))));
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.luck",
                    String.format("%.1f", getAttributeValue(player, Attributes.LUCK))));
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.max_health",
                    String.format("%.1f", getAttributeValue(player, Attributes.MAX_HEALTH))));
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.movement_speed",
                    String.format("%.3f", getAttributeValue(player, Attributes.MOVEMENT_SPEED))));
        }

        //3级信息
        if (effectLevel >= 3) {
            lines.add(Component.empty());
            Component enemyInfo = Component.translatable("effect.tetra_loopback.vision_field.enemies", enemyCount);
            lines.add(enemyInfo);
        }

        //4级信息-实时获取时间天气和月相
        if (effectLevel >= 4) {
            lines.add(Component.empty());
            String timeWeather = getTimeWeatherInfo(minecraft.level);
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.time_weather", timeWeather));

            //添加月相信息（仅夜晚）
            if (isNightTime(minecraft.level)) {
                String moonPhase = getMoonPhase(minecraft.level);
                lines.add(Component.translatable("effect.tetra_loopback.vision_field.moon_phase", moonPhase));
            }
        }

        //5级信息
        if (effectLevel >= 5 && heldItem != null && !heldItem.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("effect.tetra_loopback.vision_field.holding", heldItem.getDisplayName()));
        }

        //从底部开始绘制，逐行向上
        int currentY = (int)(screenHeight * inverseScale) - 10;
        int lineHeight = 9;

        for (int i = lines.size() - 1; i >= 0; i--) {
            Component line = lines.get(i);
            if (line.getString().isEmpty()) {
                currentY -= lineHeight / 2;
                continue;
            }

            String text = line.getString();
            int textWidth = minecraft.font.width(text);

            //计算背景位置和大小 - 右对齐
            int padding = 3;
            int bgWidth = textWidth + padding * 2;
            int bgX = scaledRight - bgWidth - 5;
            int bgY = currentY - lineHeight + 2;

            //根据行类型确定颜色
            int bgColor;
            if (i == 0) {
                bgColor = 0x80000000; // 半透明黑色
            } else if (text.contains(Component.translatable("effect.tetra_loopback.vision_field.enemies").getString().split("%")[0]) && enemyCount > 0) {
                bgColor = 0x78500000; // 半透明暗红色
            } else if (text.contains(Component.translatable("effect.tetra_loopback.vision_field.attack_damage").getString().split("%")[0])) {
                bgColor = 0x78003200; // 半透明暗绿色
            } else {
                bgColor = 0x64000000; // 半透明黑色（更透明）
            }

            //绘制背景
            guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + lineHeight, bgColor);

            //绘制文字，向右对齐
            int textX = scaledRight - textWidth - padding - 5;
            int textY = currentY - lineHeight + 2;

            int textRenderColor;
            if (i == 0) {
                textRenderColor = HIGHLIGHT_COLOR;
            } else if (text.contains(Component.translatable("effect.tetra_loopback.vision_field.enemies").getString().split("%")[0]) && enemyCount > 0) {
                textRenderColor = WARNING_COLOR;
            } else if (text.contains(Component.translatable("effect.tetra_loopback.vision_field.attack_damage").getString().split("%")[0])) {
                textRenderColor = GREEN_COLOR;
            } else {
                textRenderColor = TEXT_COLOR;
            }

            guiGraphics.drawString(minecraft.font, text, textX, textY, textRenderColor);

            currentY -= lineHeight;
        }

        poseStack.popPose();
    }

    //武器伤害获取
    private double getWeaponDamage(Player player) {
        ItemStack stack = player.getMainHandItem();

        //基础空手伤害为1
        double baseDamage = 1.0;

        if (stack.isEmpty()) {
            return baseDamage;
        }

        double weaponBonus = stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(Attributes.ATTACK_DAMAGE)
                .stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        //总伤害 = 基础伤害 + 武器加成
        return baseDamage + weaponBonus;
    }

    //实时获取属性值
    private double getAttributeValue(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }

    //判断是否为夜晚
    private boolean isNightTime(Level level) {
        if (level == null) return false;
        long time = level.getDayTime() % 24000;
        return time > 13000 && time < 23000;
    }

    //获取月相信息
    private String getMoonPhase(Level level) {
        if (level == null) return Component.translatable("effect.tetra_loopback.vision_field.unknown").getString();

        int moonPhase = level.getMoonPhase();
        switch (moonPhase) {
            case 0: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.full").getString();
            case 1: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.waning_gibbous").getString();
            case 2: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.last_quarter").getString();
            case 3: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.waning_crescent").getString();
            case 4: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.new").getString();
            case 5: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.waxing_crescent").getString();
            case 6: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.first_quarter").getString();
            case 7: return Component.translatable("effect.tetra_loopback.vision_field.moon_phase.waxing_gibbous").getString();
            default: return Component.translatable("effect.tetra_loopback.vision_field.unknown").getString();
        }
    }

    private String getFacingDirection(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw < 45) return Component.translatable("effect.tetra_loopback.vision_field.direction.south").getString();
        else if (yaw < 135) return Component.translatable("effect.tetra_loopback.vision_field.direction.west").getString();
        else if (yaw < 225) return Component.translatable("effect.tetra_loopback.vision_field.direction.north").getString();
        else if (yaw < 315) return Component.translatable("effect.tetra_loopback.vision_field.direction.east").getString();
        else return Component.translatable("effect.tetra_loopback.vision_field.direction.south").getString();
    }

    private int countNearbyEnemies(Player player, double radius) {
        return player.level().getEntitiesOfClass(Monster.class,
                player.getBoundingBox().inflate(radius)).size();
    }

    private String getTimeWeatherInfo(Level level) {
        if (level == null) return Component.translatable("effect.tetra_loopback.vision_field.unknown").getString();

        long time = level.getDayTime() % 24000;
        String timeOfDay = getTimeOfDay(time);
        String weather = level.isRaining() ?
                (level.isThundering() ?
                        Component.translatable("effect.tetra_loopback.vision_field.weather.thunder").getString() :
                        Component.translatable("effect.tetra_loopback.vision_field.weather.rain").getString()) :
                Component.translatable("effect.tetra_loopback.vision_field.weather.clear").getString();

        return Component.translatable("effect.tetra_loopback.vision_field.time_weather_format", timeOfDay, weather).getString();
    }

    private String getTimeOfDay(long time) {
        if (time < 1000) return Component.translatable("effect.tetra_loopback.vision_field.time.dawn").getString();
        else if (time < 6000) return Component.translatable("effect.tetra_loopback.vision_field.time.morning").getString();
        else if (time < 12000) return Component.translatable("effect.tetra_loopback.vision_field.time.noon").getString();
        else if (time < 13000) return Component.translatable("effect.tetra_loopback.vision_field.time.dusk").getString();
        else return Component.translatable("effect.tetra_loopback.vision_field.time.night").getString();
    }

    private ItemStack getHeldItem(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        return !mainHand.isEmpty() ? mainHand : ItemStack.EMPTY;
    }

    //设置缩放因子
    public void setScaleFactor(float scale) {
        this.scaleFactor = Math.max(0.5f, Math.min(1.5f, scale));
    }
}