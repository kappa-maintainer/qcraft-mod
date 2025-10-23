package dan200.qcraft.crafting;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import dan200.qcraft.block.BlockQBlock;
import dan200.qcraft.item.ItemBlockQBlock;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EntangleRecipe implements IRecipe {
    private UUID entangleID = null;
    private static ResourceLocation name;
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        ItemStack eoe = null;
        boolean hasQBlock = false;
        ItemStack[] stackList = null;
        Item item = null;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == QCraftItems.itemEoE) {
                int north = i - inv.getWidth();
                int south = i + inv.getWidth();
                if(north < 0 || south > inv.getSizeInventory() || i % inv.getWidth() == 0 || i % inv.getWidth() == inv.getWidth() - 1) {
                    return false;
                }
                eoe = stack;
            } else if (stack.getItem() instanceof ItemBlock itemBlock && itemBlock.getBlock() instanceof BlockQBlock) {
                ItemStack[] current = new ItemStack[6];
                Item currentItem = stack.getItem();
                NBTTagCompound stackCompound = stack.getTagCompound();
                for (int j = 0; j < 6; j++) {
                    current[j] = new ItemStack(stackCompound.getCompoundTag(EnumFacing.byIndex(j).getName()));
                }
                if (item != null) {
                    if (item != currentItem) {
                        return false;
                    }
                } else {
                    item = currentItem;
                }
                if (stackList != null) {
                    for (int j = 0; j < 6; j++) {
                        if (stackList[j].getItem() != current[j].getItem()) {
                            return false;
                        }
                    }
                } else {
                    stackList = current;
                }
                hasQBlock = true;
            }
        }
        if (eoe != null && hasQBlock) {
            if (eoe.hasTagCompound() && eoe.getTagCompound().hasKey("entangle")) {
                entangleID = NBTUtil.getUUIDFromTag((NBTTagCompound) eoe.getTagCompound().getTag("entangle"));
            } else {
                entangleID = UUID.randomUUID();
            }
            return true;
        }
        return false;
        
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int counter = 0;
        ItemStack out = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlockQBlock) {
                counter++;
                if (out == null) {
                    out = stack;
                }
            }
        }
        if (counter == 0) {
            return ItemStack.EMPTY;
        } else {
            out.setCount(counter);
            out.getTagCompound().setTag("entangle", NBTUtil.createUUIDTag(entangleID));
            return out;
        }
    }

    @Override
    public boolean canFit(int width, int height) {
        return width > 2 && height > 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(QCraftBlocks.blockRandomQBlock);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
    
    @Override
    public IRecipe setRegistryName(ResourceLocation resourceLocation) {
        name = resourceLocation;
        return this;
    }

    @Override
    public @Nullable ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }
}