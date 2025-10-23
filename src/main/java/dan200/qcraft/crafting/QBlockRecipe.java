package dan200.qcraft.crafting;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class QBlockRecipe implements IRecipe {
    private final NBTTagCompound resultnbt = new NBTTagCompound();
    private static ResourceLocation name;
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(inv.getStackInSlot(i).getItem() == QCraftItems.itemEoO || inv.getStackInSlot(i).getItem() == QCraftItems.itemEoS) {
                int north = i - inv.getWidth();
                int south = i + inv.getWidth();
                if(north < 0 || south > inv.getSizeInventory() || i % inv.getWidth() == 0 || i % inv.getWidth() == inv.getWidth() - 1) return false;
                if(inv.getStackInSlot(north + 1).isEmpty() && inv.getStackInSlot(south + 1).isEmpty()) {

                    //Just like EnumFacing index
                    int[] facesi = {south - 1, north - 1, south, north, i-1, i+1};
                    boolean allEmpty = true;//marks no block surrounded
                    for(int j = 0; j < 6; j++) {
                        ItemStack currrent = inv.getStackInSlot(facesi[j]);
                        allEmpty &= currrent.isEmpty();
                        if(currrent.getItem() instanceof ItemBlock || currrent.isEmpty()) {
                            if(currrent.isEmpty()) {
                                resultnbt.setTag(EnumFacing.byIndex(j).getName(), currrent.serializeNBT());
                                continue;
                            }
                            if(((ItemBlock) currrent.getItem()).getBlock().hasTileEntity()) {
                                return false;
                            }
                            resultnbt.setTag(EnumFacing.byIndex(j).getName(), currrent.serializeNBT());
                        }
                    }
                    return !allEmpty;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack result = null;
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            if(inv.getStackInSlot(i).getItem() == QCraftItems.itemEoO) {
                result = new ItemStack(QCraftItems.itemBlockQBlock);
            } else if (inv.getStackInSlot(i).getItem() == QCraftItems.itemEoS) {
                result = new ItemStack(QCraftItems.itemBlockRandomQBlock);
            }

        }
        if (result == null) {
            return ItemStack.EMPTY;
        }
        result.setTagCompound(resultnbt);
        return result.copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width > 2 && height > 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(QCraftBlocks.blockQBlock);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public IRecipe setRegistryName(ResourceLocation name) {
        QBlockRecipe.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return QBlockRecipe.name;
    }


    @Override
    public Class<IRecipe> getRegistryType() {
        return IRecipe.class;
    }
}
