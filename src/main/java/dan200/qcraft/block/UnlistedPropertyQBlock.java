package dan200.qcraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedPropertyQBlock implements IUnlistedProperty<IBlockState> {

    private final String name = "current";

    public UnlistedPropertyQBlock() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(IBlockState state) {
        return state.getBlock() != Blocks.AIR;
    }


    @Override
    public Class<IBlockState> getType() {
        return IBlockState.class;
    }

    @Override
    public String valueToString(IBlockState state) {
        return state.toString();
    }

}