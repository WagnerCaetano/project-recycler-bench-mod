package com.recyclerbench.item;

import java.util.List;

import com.recyclerbench.RecyclerConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class RecyclerBenchItem extends BlockItem {
    public RecyclerBenchItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int percent = (int) Math.round(RecyclerConfig.returnRate() * 100.0D);
        tooltip.add(Component.translatable("tooltip.recyclerbench.summary", percent).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.recyclerbench.tools").withStyle(ChatFormatting.DARK_GRAY));
    }
}
