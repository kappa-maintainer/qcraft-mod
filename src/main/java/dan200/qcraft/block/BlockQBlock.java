package dan200.qcraft.block;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockQBlock extends BlockFalling implements ITileEntityProvider {
    public BlockQBlock() {
        super(Material.GLASS);
        setTranslationKey("qcraft.odb");
        setDefaultState(new CamouflageState(this, QCraftBlocks.blockSwirl.getDefaultState()));
    }

    public BlockQBlock(Material material) {
        super(material);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        QBlockTileEntity te = new QBlockTileEntity();
        te.setWorld(world);
        return te;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        if (state instanceof CamouflageState camouflageState) {
            IBlockState target = camouflageState.getCamouflageTarget();
            target.getBlock().randomTick(worldIn, pos, target, random);
        }
    }
    
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (state instanceof CamouflageState camouflageState) {
            IBlockState target = camouflageState.getCamouflageTarget();
            target.getBlock().updateTick(worldIn, pos, target, rand);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand)
    {
        if (state instanceof CamouflageState camouflageState) {
            IBlockState target = camouflageState.getCamouflageTarget();
            target.getBlock().randomDisplayTick(target, worldIn, pos, rand);
        }
    }

    @Override
    @Nullable
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return createTileEntity(worldIn, null);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return BlockRenderLayer.CUTOUT == layer || BlockRenderLayer.CUTOUT_MIPPED == layer;
    }
}
