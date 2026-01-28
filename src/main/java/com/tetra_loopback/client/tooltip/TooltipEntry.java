package com.tetra_loopback.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class TooltipEntry {
    private final String translationKey;
    private ChatFormatting color = ChatFormatting.GRAY;
    private boolean bold = false;
    private boolean italic = false;
    private boolean showInCreative = true;
    private Object[] args = new Object[0];

    private TooltipEntry(String translationKey) {
        this.translationKey = translationKey;
    }

    public static TooltipEntry create(String translationKey) {
        return new TooltipEntry(translationKey);
    }

    public TooltipEntry withColor(ChatFormatting color) {
        this.color = color;
        return this;
    }

    public TooltipEntry withBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public TooltipEntry withItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public TooltipEntry withArgs(Object... args) {
        this.args = args;
        return this;
    }

    public TooltipEntry showInCreativeOnly(boolean creativeOnly) {
        this.showInCreative = creativeOnly;
        return this;
    }

    public Component buildComponent() {
        Style style = Style.EMPTY;
        if (color != null) {
            style = style.withColor(color);
        }
        if (bold) {
            style = style.withBold(true);
        }
        if (italic) {
            style = style.withItalic(true);
        }

        return Component.translatable(translationKey, args).withStyle(style);
    }

    public boolean shouldShowInCreative(boolean isCreativeMode) {
        return !showInCreative || isCreativeMode;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}