package com.tetra_loopback.client.tooltip;

import com.tetra_loopback.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TooltipSection {
    private Component title = null;
    private final List<TooltipEntry> entries = new ArrayList<>();
    private boolean showSeparator = false;
    private ChatFormatting separatorColor = ChatFormatting.DARK_GRAY;

    public TooltipSection withTitle(String translationKey) {
        this.title = Component.translatable(translationKey);
        return this;
    }

    public TooltipSection withTitle(String translationKey, ChatFormatting color) {
        this.title = Component.translatable(translationKey).withStyle(color);
        return this;
    }

    public TooltipSection addEntry(TooltipEntry entry) {
        this.entries.add(entry);
        return this;
    }

    public TooltipSection addEntry(String translationKey) {
        this.entries.add(TooltipEntry.create(translationKey));
        return this;
    }

    public TooltipSection withSeparator(boolean show) {
        this.showSeparator = show;
        return this;
    }

    public TooltipSection withSeparator() {
        return withSeparator(true);
    }

    public TooltipSection withSeparatorColor(ChatFormatting color) {
        this.separatorColor = color;
        return this;
    }

    public List<Component> buildComponents(ItemStack stack) {
        List<Component> components = new ArrayList<>();

        //添加分隔线
        if (showSeparator && Config.showSeparators) {
            components.add(Component.literal("")
                    .withStyle(separatorColor)
                    .append(Component.translatable("tooltip.tetra_loopback.separator")
                            .withStyle(ChatFormatting.STRIKETHROUGH)));
        }

        //添加标题
        if (title != null) {
            components.add(title);
        }

        //添加条目
        for (TooltipEntry entry : entries) {
            components.add(entry.buildComponent());
        }

        return components;
    }
}