package dan200.qcraft.block;

import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockQBlock extends BlockFalling implements ITileEntityProvider {
    public BlockQBlock() {
        super(Material.AIR);
        setRegistryName("qcraft:qblock");
        setTranslationKey("qcraft.qblock");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new QBlockTileEntity();
    }
}
