package dan200.qcraft.block;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockRandomQBlock extends BlockQBlock {
    public BlockRandomQBlock () {
        super(Material.GLASS);;
        setTranslationKey("qcraft.odb");
        setDefaultState(new CamouflageState(this, QCraftBlocks.blockSwirl.getDefaultState()));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        QBlockTileEntity te = new QBlockTileEntity();
        te.setWorld(world);
        return te;
    }
}
