package dan200.qcraft.render;

import dan200.qcraft.QCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ItemModelMesherForge;

public class QBlockMeshDefinition implements ItemMeshDefinition {
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        if(QCraft.currentType == 6) return new ModelResourceLocation("qcraft:qblock");
        ItemStack currentStack;
        currentStack = new ItemStack(stack.getTagCompound().getCompoundTag(EnumFacing.byIndex(QCraft.currentType).getName()));
        if(currentStack.isEmpty()) { return new ModelResourceLocation("qcraft:transparent"); }
        return ((ItemModelMesherForge)Minecraft.getMinecraft().getRenderItem().getItemModelMesher()).getLocation(currentStack);
    }

}
