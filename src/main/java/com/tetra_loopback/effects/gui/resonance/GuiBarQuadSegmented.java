package com.tetra_loopback.effects.gui.resonance;

import net.minecraft.client.gui.GuiGraphics;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiBarQuadSegmented extends GuiBar {
    private final int maxValue = 20; // 最大值
    private int currentValue;
    private int barLength;

//    public GuiBarQuadSegmented(int x, int y, int barLength, double min, double max) {
//        super(x, y, barLength, min, max);
//        this.barLength = barLength;
//    }

    public GuiBarQuadSegmented(int x, int y, int barLength, double min, double max, boolean invertedDiff) {
        super(x, y, barLength, min, max, invertedDiff);
        this.barLength = barLength;
    }

    @Override
    protected void calculateBarLengths() {
        //计算当前值
        currentValue = (int) Math.round(value);
        //设置颜色
        diffColor = getColorForValue(currentValue);
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        //计算进度条长度
        int progressLength = (int) ((currentValue / 20.0) * barLength);
        //绘制背景
        drawRect(graphics,
                refX + x,
                refY + y + 6,
                refX + x + barLength,
                refY + y + 6 + height,
                0x333333, opacity * 0.5f);

        //绘制进度条
        if (progressLength > 0) {
            int colorWithAlpha = (diffColor & 0x00FFFFFF) | ((int)(opacity * 255) << 24);
            drawRect(graphics,
                    refX + x,
                    refY + y + 6,
                    refX + x + progressLength,
                    refY + y + 6 + height,
                    diffColor, opacity);
        }
    }

    private int getColorForValue(int value) {
        if (value >= 16) {
            return 0xFF0000; // 红色-极致 (16-20)
        } else if (value >= 11) {
            return 0xFF55FF; // 紫色-谐和 (11-15)
        } else if (value >= 6) {
            return 0x5555FF; // 蓝色-共鸣 (6-10)
        } else if (value >= 1) {
            return 0x55FF55; // 绿色-觉醒 (1-5)
        } else {
            return 0x555555; // 灰色-沉寂 (0)
        }
    }

    //文本
    public static int getStageColor(int value) {
        if (value >= 16) {
            return 0xFF0000; //红
        } else if (value >= 11) {
            return 0xFF55FF; //紫
        } else if (value >= 6) {
            return 0x5555FF; //蓝
        } else if (value >= 1) {
            return 0x55FF55; //绿
        } else {
            return 0x555555; //灰
        }
    }

    //获取当前阶段的名称
    public static String getStageName(int value) {
        if (value >= 16) {
            return "tetra.stats.tetra_loopback:resonance.stage4";
        } else if (value >= 11) {
            return "tetra.stats.tetra_loopback:resonance.stage3";
        } else if (value >= 6) {
            return "tetra.stats.tetra_loopback:resonance.stage2";
        } else if (value >= 1) {
            return "tetra.stats.tetra_loopback:resonance.stage1";
        } else {
            return "tetra.stats.tetra_loopback:resonance.stage0";
        }
    }

    //获取当前阶段的显示名称
    public static String getStageDisplayName(int value) {
        if (value >= 16) {
            return "tetra.stats.tetra_loopback:resonance.stage4";
        } else if (value >= 11) {
            return "tetra.stats.tetra_loopback:resonance.stage3";
        } else if (value >= 6) {
            return "tetra.stats.tetra_loopback:resonance.stage2";
        } else if (value >= 1) {
            return "tetra.stats.tetra_loopback:resonance.stage1";
        } else {
            return "tetra.stats.tetra_loopback:resonance.stage0";
        }
    }
}