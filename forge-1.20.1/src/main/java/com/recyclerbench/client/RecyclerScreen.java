package com.recyclerbench.client;

import java.util.ArrayList;
import java.util.List;

import com.recyclerbench.RecyclerBenchMod;
import com.recyclerbench.RecyclerConfig;
import com.recyclerbench.menu.RecyclerMenu;
import com.recyclerbench.recycling.Recycling;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RecyclerScreen extends AbstractContainerScreen<RecyclerMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(RecyclerBenchMod.MOD_ID, "textures/gui/recycler_bench.png");
    private static final int ARROW_X = 48;
    private static final int ARROW_Y = 35;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 16;
    private static final int DISCLAIMER_Y = 74;
    private static final int DISCLAIMER_LINE = 10;

    public RecyclerScreen(RecyclerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        graphics.blit(BACKGROUND, left, top, 0, 0, this.imageWidth, this.imageHeight);

        int max = this.menu.getMaxProgress();
        int progress = this.menu.getProgress();
        if (progress > 0 && max > 0) {
            int width = ARROW_WIDTH * Math.min(progress, max) / max;
            if (width > 0) {
                graphics.blit(BACKGROUND, left + ARROW_X, top + ARROW_Y, 176, 0, width, ARROW_HEIGHT);
            }
        }
        this.renderPreview(graphics, left, top);
    }

    private void renderPreview(GuiGraphics graphics, int left, int top) {
        for (int index = 1; index <= 9; index++) {
            if (this.menu.getSlot(index).hasItem()) {
                return;
            }
        }
        if (this.minecraft == null || this.minecraft.level == null || !this.menu.getSlot(0).hasItem()) {
            return;
        }
        List<ItemStack> preview = Recycling.resultsFor(this.minecraft.level, this.menu.getSlot(0).getItem());
        int shown = Math.min(preview.size(), 9);
        for (int index = 0; index < shown; index++) {
            Slot slot = this.menu.getSlot(index + 1);
            int x = left + slot.x;
            int y = top + slot.y;
            ItemStack stack = preview.get(index);
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(this.font, stack, x, y);
            graphics.fill(x, y, x + 16, y + 16, 0x66000000);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        List<Component> lines = this.disclaimerLines();
        if (!lines.isEmpty() && this.isHovering(8, DISCLAIMER_Y, 160, lines.size() * DISCLAIMER_LINE, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.translatable("gui.recyclerbench.custom.tooltip"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int percent = (int) Math.round(RecyclerConfig.returnRate() * 100.0D);
        Component rate = Component.translatable("gui.recyclerbench.rate", percent);
        int width = this.font.width(rate);
        graphics.drawString(this.font, rate, this.imageWidth - 8 - width, this.titleLabelY, 0x3F6B3F, false);

        List<Component> lines = this.disclaimerLines();
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(this.font, lines.get(index), 8, DISCLAIMER_Y + index * DISCLAIMER_LINE, 0x404040, false);
        }
    }

    private List<Component> disclaimerLines() {
        List<Component> lines = new ArrayList<>();
        double minimum = RecyclerConfig.minDurability();
        if (minimum > 0.0D) {
            lines.add(Component.translatable("gui.recyclerbench.durability", (int) Math.round(minimum * 100.0D)));
        }
        if (RecyclerConfig.netheriteIngotOnly()) {
            lines.add(Component.translatable("gui.recyclerbench.netherite"));
        }
        lines.add(Component.translatable("gui.recyclerbench.custom"));
        return lines;
    }
}
