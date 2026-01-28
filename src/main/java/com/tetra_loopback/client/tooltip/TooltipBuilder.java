package com.tetra_loopback.client.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TooltipBuilder {
    private final TooltipConfiguration configuration = new TooltipConfiguration();
    private TooltipSection currentSection = null;
    private boolean isBuildingSection = false;

    private TooltipBuilder() {}

    public static TooltipBuilder create() {
        return new TooltipBuilder();
    }

    public static TooltipBuilder forItem(Item item) {
        //实际在TooltipRegistry中完成
        return create();
    }

    public static TooltipBuilder forBlockItem(Item blockItem) {
        //返回构建器，实际在register()中完成
        return create();
    }

    public TooltipBuilder addDescription(String translationKey) {
        return startSection()
                .addEntry(TooltipEntry.create(translationKey)
                        .withColor(ChatFormatting.GRAY)
                        .withItalic(true))
                .endSection();
    }

    public TooltipBuilder addTrait(String translationKey) {
        return addStyledEntry(translationKey, ChatFormatting.GOLD, true, false);
    }

    public TooltipBuilder addUsage(String translationKey) {
        return addStyledEntry(translationKey, ChatFormatting.BLUE, false, true);
    }

    public TooltipBuilder addNote(String translationKey) {
        return addStyledEntry(translationKey, ChatFormatting.DARK_PURPLE, false, true);
    }

    //addEntry
    public TooltipBuilder addEntry(String translationKey) {
        if (!isBuildingSection) {
            startSection();
        }

        currentSection.addEntry(TooltipEntry.create(translationKey));
        return this;
    }

    public TooltipBuilder addEntry(TooltipEntry entry) {
        if (!isBuildingSection) {
            startSection();
        }

        currentSection.addEntry(entry);
        return this;
    }

    public TooltipBuilder addStyledEntry(String translationKey, ChatFormatting color, boolean bold, boolean italic) {
        if (!isBuildingSection) {
            startSection();
        }

        currentSection.addEntry(TooltipEntry.create(translationKey)
                .withColor(color)
                .withBold(bold)
                .withItalic(italic));

        return this;
    }

    public TooltipBuilder startSection() {
        if (isBuildingSection) {
            endSection();
        }

        currentSection = new TooltipSection();
        isBuildingSection = true;
        return this;
    }

    public TooltipBuilder startSection(String titleKey) {
        startSection();
        currentSection.withTitle(titleKey, ChatFormatting.YELLOW);
        return this;
    }

    public TooltipBuilder withSeparator() {
        if (currentSection != null) {
            currentSection.withSeparator();
        }
        return this;
    }

    public TooltipBuilder withSeparator(ChatFormatting color) {
        if (currentSection != null) {
            currentSection.withSeparator().withSeparatorColor(color);
        }
        return this;
    }

    public TooltipBuilder addEmptyLine() {
        if (currentSection != null) {
            currentSection.addEntry(TooltipEntry.create(""));
        }
        return this;
    }

    public TooltipBuilder endSection() {
        if (currentSection != null) {
            configuration.addSection(currentSection);
            currentSection = null;
            isBuildingSection = false;
        }
        return this;
    }

    public TooltipConfiguration build() {
        if (isBuildingSection) {
            endSection();
        }
        return configuration;
    }

    //直接构建并注册
    public void register(Item item) {
        TooltipRegistry.registerItemTooltip(item, build());
    }

    //方块Tooltip
    public void register(Block block, Item blockItem) {
        TooltipRegistry.registerBlockTooltip(block, blockItem, build());
    }
}