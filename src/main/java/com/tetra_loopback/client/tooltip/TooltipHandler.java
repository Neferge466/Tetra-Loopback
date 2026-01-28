package com.tetra_loopback.client.tooltip;

import com.tetra_loopback.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "tetra_loopback", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        //检查是否启用Tooltip
        if (!Config.enableTooltips) {
            return;
        }

        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<Component> tooltip = event.getToolTip();

        //获取物品的Tooltip配置
        TooltipConfiguration configuration = TooltipRegistry.getTooltipForItem(item);

        if (configuration != null) {
            //添加配置的Tooltip
            List<Component> customTooltips = configuration.buildTooltip(stack);
            tooltip.addAll(customTooltips);
        }
    }
}