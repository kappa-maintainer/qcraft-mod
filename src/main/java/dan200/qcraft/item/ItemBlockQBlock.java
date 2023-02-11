package dan200.qcraft.item;

import dan200.qcraft.QCraft;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemBlockQBlock extends ItemBlock {
    public ItemBlockQBlock(Block qblock) {
        super(qblock);
        setRegistryName("qcraft:qblock");
        setTranslationKey("qcraft.qblock");
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        //stack.setTagInfo("BlockEntityTag", stack.getTagCompound());
        boolean temp = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof QBlockTileEntity) {
            //((QBlockTileEntity) te).setPlaceParameters(world, pos, side, hitX, hitY, hitZ, stack.getMetadata(), player, EnumHand.MAIN_HAND);
            IBlockState[] stateList = new IBlockState[6];
            ItemStack innerStack;
            ItemStack[] stackList = new ItemStack[6];
            for(int i = 0; i<6; i++) {
                innerStack = new ItemStack(stack.getTagCompound().getCompoundTag(EnumFacing.byIndex(QCraft.currentType).getName()));
                stackList[i] = innerStack;
                stateList[i] = ((ItemBlock)innerStack.getItem()).getBlock().getStateFromMeta(innerStack.getMetadata());
            }
            ((QBlockTileEntity) te).setStateList(stateList);

            //blockstate can't be wrote to nbt, so stack is needed
            ((QBlockTileEntity) te).setStackList(stackList);
        }
        return temp;
    }

}

