package dan200.qcraft.item;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockQBlock extends ItemBlock {
    public ItemBlockQBlock(Block qblock) {
        super(qblock);
        setTranslationKey("qcraft.qblock");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        EnumActionResult result = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        TileEntity te = worldIn.getTileEntity(pos.add(facing.getDirectionVec()));
        ItemStack stack = player.getHeldItem(hand);
        if(te instanceof QBlockTileEntity qte) {
            IBlockState[] stateList = new IBlockState[6];
            ItemStack innerStack;
            NBTTagCompound compound = stack.getTagCompound();
            if (compound == null) return EnumActionResult.FAIL;
            for(int i = 0; i < 6; i++) {
                innerStack = new ItemStack(compound.getCompoundTag(EnumFacing.byIndex(i).getName()));
                Item item = innerStack.getItem();
                if (item instanceof ItemAir) {
                    stateList[i] = Blocks.AIR.getDefaultState();
                } else {
                    IBlockState state = ((ItemBlock) item).getBlock().getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, item.getMetadata(innerStack.getMetadata()), player, hand);
                    if (state == null) {
                        state = ((ItemBlock) item).getBlock().getDefaultState();
                    }
                    if (state == null) {
                        state = QCraftBlocks.blockSwirl.getDefaultState();
                    }
                    stateList[i] = state;
                }
            }
            
            qte.setStateList(stateList);
            if (compound.hasKey("entangle")) {
                qte.setEntangle(NBTUtil.getUUIDFromTag((NBTTagCompound) compound.getTag("entangle")));
            }
        }
        return result;
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("entangled")) {
            return "tile.qcraft.odb_entangled.name";
        } else {
            return "tile.qcraft.odb.name";
        }
    }


}

