package dan200.qcraft.block;

import net.minecraft.world.World;

public interface IQuantumObservable {
    public boolean isObserved(World world, int x, int y, int z, int side );

    public void observe( World world, int x, int y, int z, int side );

    public void reset( World world, int x, int y, int z, int side );
}
