package com.tetra_loopback.client.tooltip;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class TooltipRegistry {
    private static final Map<Item, TooltipConfiguration> itemTooltips = new HashMap<>();
    private static final Map<Block, Item> blockToItemMap = new HashMap<>();

    public static void registerItemTooltip(Item item, TooltipConfiguration configuration) {
        itemTooltips.put(item, configuration);
    }

    public static void registerBlockTooltip(Block block, Item blockItem, TooltipConfiguration configuration) {
        itemTooltips.put(blockItem, configuration);
        blockToItemMap.put(block, blockItem);
    }

    public static TooltipConfiguration getTooltipForItem(Item item) {
        return itemTooltips.get(item);
    }

    public static TooltipConfiguration getTooltipForBlock(Block block) {
        Item blockItem = blockToItemMap.get(block);
        return blockItem != null ? itemTooltips.get(blockItem) : null;
    }

    public static boolean hasTooltipForItem(Item item) {
        return itemTooltips.containsKey(item);
    }

    public static boolean hasTooltipForBlock(Block block) {
        return blockToItemMap.containsKey(block);
    }

    public static void clear() {
        itemTooltips.clear();
        blockToItemMap.clear();
    }
}