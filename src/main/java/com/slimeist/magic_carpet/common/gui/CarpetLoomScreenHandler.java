package com.slimeist.magic_carpet.common.gui;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import com.slimeist.magic_carpet.common.item.MagicCarpetItem;
import net.minecraft.block.Block;
import net.minecraft.block.DyedCarpetBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

public class CarpetLoomScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final Slot borderSlot;
    private final Slot centerSlot;
    private final Slot decorationSlot;
    private final Slot outputSlot;
    long lastTakeResultTime;
    Runnable inventoryChangeListener = () -> {};
    private final Inventory input = new SimpleInventory(3) {
        @Override
        public void markDirty() {
            super.markDirty();
            CarpetLoomScreenHandler.this.onContentChanged(this);
            CarpetLoomScreenHandler.this.inventoryChangeListener.run();
        }
    };

    private final Inventory output = new SimpleInventory(1) {
        @Override
        public void markDirty() {
            super.markDirty();
            CarpetLoomScreenHandler.this.inventoryChangeListener.run();
        }
    };

    public CarpetLoomScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public CarpetLoomScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(MagicCarpetMod.CARPET_LOOM_SCREEN_HANDLER, syncId);
        this.context = context;
        this.borderSlot = this.addSlot(new Slot(this.input, 0, 13, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(ItemTags.CARPETS);
            }
        });
        this.centerSlot = this.addSlot(new Slot(this.input, 1, 33, 26) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(ItemTags.CARPETS);
            }
        });
        this.decorationSlot = this.addSlot(new Slot(this.input, 2, 23, 45) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isIn(ItemTags.CARPETS);
            }
        });

        this.outputSlot = this.addSlot(new Slot(this.output, 0, 143, 57) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                CarpetLoomScreenHandler.this.borderSlot.takeStack(2);
                CarpetLoomScreenHandler.this.centerSlot.takeStack(3);
                CarpetLoomScreenHandler.this.decorationSlot.takeStack(1);
                context.run((world, pos) -> {
                    long l = world.getTime();
                    if (CarpetLoomScreenHandler.this.lastTakeResultTime != l) {
                        world.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        CarpetLoomScreenHandler.this.lastTakeResultTime = l;
                    }
                });
                super.onTakeItem(player, stack);
            }
        });
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, MagicCarpetMod.CARPET_LOOM_BLOCK);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        ItemStack borderStack = this.borderSlot.getStack();
        ItemStack centerStack = this.centerSlot.getStack();
        ItemStack decorationStack = this.decorationSlot.getStack();
        ItemStack outputStack = this.outputSlot.getStack();
        if (!outputStack.isEmpty() && (borderStack.getCount()<2 || centerStack.getCount()<3 || decorationStack.isEmpty())) {
            this.outputSlot.setStack(ItemStack.EMPTY);
        }
        this.updateOutputSlot();
        this.sendContentUpdates();
    }

    public void setInventoryChangeListener(Runnable inventoryChangeListener) {
        this.inventoryChangeListener = inventoryChangeListener;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index == this.outputSlot.id) {
                if (!this.insertItem(itemStack2, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(itemStack2, itemStack);
            } else if (index == this.borderSlot.id || index == this.centerSlot.id || index == this.decorationSlot.id ? !this.insertItem(itemStack2, 4, 40, false) :
                    (itemStack2.isIn(ItemTags.CARPETS) ? !this.insertItem(itemStack2, this.borderSlot.id, this.decorationSlot.id + 1, false) :
                            (index >= 4 && index < 31 ? !this.insertItem(itemStack2, 31, 40, false) :
                                    index >= 31 && index < 40 && !this.insertItem(itemStack2, 4, 31, false)))) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, itemStack2);
        }
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    @Nullable
    private static DyeColor getColorFromCarpet(ItemStack color) {
        Block block = Block.getBlockFromItem(color.getItem());
        if (block instanceof DyedCarpetBlock) {
            return ((DyedCarpetBlock)block).getDyeColor();
        }
        return null;
    }

    private void updateOutputSlot() {
        ItemStack borderStack = this.borderSlot.getStack();
        ItemStack centerStack = this.centerSlot.getStack();
        ItemStack decorationStack = this.decorationSlot.getStack();
        ItemStack outputStack = ItemStack.EMPTY;
        if (borderStack.getCount()>=2 && centerStack.getCount()>=3 && decorationStack.getCount()>=1) {
            outputStack = new ItemStack(MagicCarpetMod.MAGIC_CARPET_ITEM);
            DyeColor borderColor = getColorFromCarpet(borderStack);
            DyeColor centerColor = getColorFromCarpet(centerStack);
            DyeColor decorationColor = getColorFromCarpet(decorationStack);
            if (borderColor!=null && centerColor!=null && decorationColor!=null) {
                MagicCarpetItem.setLayerColor(outputStack, CarpetLayer.BORDER, borderColor);
                MagicCarpetItem.setLayerColor(outputStack, CarpetLayer.CENTER, centerColor);
                MagicCarpetItem.setLayerColor(outputStack, CarpetLayer.DECORATION, decorationColor);
            } else {
                outputStack = ItemStack.EMPTY;
            }
        }
        if (!ItemStack.areEqual(outputStack, this.outputSlot.getStack())) {
            this.outputSlot.setStack(outputStack);
        }
    }

    public Slot getBorderSlot() {
        return this.borderSlot;
    }

    public Slot getCenterSlot() {
        return this.centerSlot;
    }

    public Slot getDecorationSlot() {
        return this.decorationSlot;
    }

    public Slot getOutputSlot() {
        return this.outputSlot;
    }
}
