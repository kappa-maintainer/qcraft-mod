package dan200.qcraft.tileentity;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import dan200.qcraft.item.ItemQuantumGoggle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class QBlockTileEntity extends TileEntity implements ITickable {

    public static int FUZZ_TIME = 9;
    private int m_entanglementFrequency;
    private int[] m_sideBlockTypes;

    // Replicated
    private long m_timeLastUpdated;

    private boolean m_currentlyObserved;
    private int m_currentDisplayedSide;

    private int m_currentlyForcedSide;
    private boolean[] m_forceObserved;

    // Client only
    public int m_timeSinceLastChange;
    private boolean m_goggles;
    private boolean m_wet;

    public static class PlaceParameter {
        World world;
        BlockPos pos;
        EnumFacing facing;
        float hitX;
        float hitY;
        float hitZ;
        int meta;
        EntityLivingBase placer;
        EnumHand hand;
    }
    private PlaceParameter pp;
    public QBlockTileEntity() {

    }
    @Override
    public void update() {
        if( !world.isRemote )
        {
            redetermineObservedSide();
        }

        // Update ticker, goggles and wetness
        m_timeSinceLastChange++;
        boolean goggles = world.isRemote && Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemQuantumGoggle;
        boolean wet = isTouchingLiquid();
        if( m_goggles != goggles || m_wet != wet || m_timeSinceLastChange == FUZZ_TIME )
        {
            m_wet = wet;
            m_goggles = goggles;
            this.updateContainingBlockInfo();
        }
    }
    private short currentState;
    private final IBlockState[] statelist = new IBlockState[6];
    public IBlockState getCurrentState() {
        return statelist[currentState];
    }

    private boolean isTouchingLiquid()
    {
        for( int i = 1; i < 6; ++i ) // ignore down
        {

            Block block = world.getBlockState(this.getPos().offset(EnumFacing.byIndex(i))).getBlock();
            if(block instanceof BlockLiquid)
            {
                return true;
            }
        }
        return false;
    }

    private void setDisplayedSide( boolean observed, boolean forced, int side )
    {
        m_currentlyObserved = observed;
        m_currentlyForcedSide = forced ? side : -1;
        if( m_currentDisplayedSide != side )
        {
            int oldSide = m_currentDisplayedSide;
            int oldType = getObservedType();
            m_currentDisplayedSide = side;
            int newSide = m_currentDisplayedSide;
            int newType = getObservedType();
            if( newType != oldType || (oldSide < 0 != newSide < 0) )
            {
                m_timeSinceLastChange = 0;
                blockUpdate();
            }
        }
    }


    private void blockUpdate()
    {
        world.markAndNotifyBlock( pos, world.getChunk(pos), statelist[currentState], statelist[currentState], 2 );
        world.scheduleBlockUpdate( pos, QCraftBlocks.blockQBlock, QCraftBlocks.blockQBlock.tickRate( world ), 0);
        //world.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, QCraft.Blocks.qBlock );
    }

    public int getObservedType()
    {
        if( m_currentDisplayedSide < 0 )
        {
            return m_sideBlockTypes[ 1 ];
        }
        return m_sideBlockTypes[ m_currentDisplayedSide ];
    }

    private void redetermineObservedSide()
    {
        // Tally the votes, and work out if we need to change appearance.
        long currentTime = world.getWorldInfo().getWorldTotalTime();
        int winner = getObservationResult( currentTime );
        if( winner >= 6 )
        {
            // Force observed
            winner -= 6;
            setDisplayedSide( true, true, winner );
        }
        else if( winner >= 0 )
        {
            // Passively observed
            if( (m_currentlyForcedSide >= 0) || !m_currentlyObserved )
            {
                setDisplayedSide( true, false, winner );
            }
        }
        else
        {
            // Not observed
            if( m_currentlyObserved )
            {
                if( m_currentlyForcedSide >= 0 )
                {
                    setDisplayedSide( false, false, -1 );
                }
                else
                {
                    setDisplayedSide( false, false, m_currentDisplayedSide );
                }
            }
        }
        m_timeLastUpdated = currentTime;
    }

    private int getObservationResult( long currentTime )
    {
        // Get observer votes from entangled twins
        int[] votes = new int[ 6 ];
        //[copied to the readFromNBT method]
        /*
        if( m_entanglementFrequency >= 0 )
        {
            List<QBlockTileEntity> twins = getEntanglementRegistry().getEntangledObjects( m_entanglementFrequency );
            if( twins != null )
            {
                for (QBlockTileEntity twin : twins) {
                    if (twin != this) {
                        //[/copied]
                        if (twin.m_currentlyObserved && twin.m_timeLastUpdated == currentTime) {
                            // If an entangled twin is already up to date, use its result
                            if (twin.m_currentlyForcedSide >= 0) {
                                return twin.m_currentlyForcedSide + 6;
                            } else {
                                return twin.m_currentDisplayedSide;
                            }
                        } else {
                            // Otherwise, add its votes to the pile
                            if (twin.m_currentlyForcedSide >= 0 && twin.m_forceObserved[m_currentlyForcedSide]) {
                                return twin.m_currentlyForcedSide + 6;
                            } else {
                                for (int i = 0; i < 6; ++i) {
                                    if (twin.m_forceObserved[i]) {
                                        return i + 6;
                                    }
                                }
                            }
                            votes = addVotes(votes, twin.collectVotes());
                        }
                    }
                }
            }
        }*/

        // Get local observer votes
        if( m_currentlyForcedSide >= 0 && m_forceObserved[ m_currentlyForcedSide ] )
        {
            return m_currentlyForcedSide + 6;
        }
        else
        {
            for( int i=0; i<6; ++i )
            {
                if( m_forceObserved[ i ] )
                {
                    return i + 6;
                }
            }
        }
        votes = addVotes( votes, collectVotes() );

        // Tally the votes
        return tallyVotes( votes );
    }


    private static int[] addVotes( int[] a, int[] b )
    {
        int[] c = new int[ 6 ];
        for( int i = 0; i < 6; ++i )
        {
            c[ i ] = a[ i ] + b[ i ];
        }
        return c;
    }

    private static int tallyVotes( int[] votes )
    {
        // Tally the votes:
        int winner = 0;
        int winnerVotes = 0;
        for( int i = 0; i < 6; ++i )
        {
            int vote = votes[ i ];
            if( vote > winnerVotes )
            {
                winner = i;
                winnerVotes = vote;
            }
        }

        if( winnerVotes > 0 )
        {
            return winner;
        }
        return -1;
    }
    public QBlockTileEntity setPlaceParameters(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        pp.world = world;
        pp.pos = pos;
        pp.facing = facing;
        pp.hitX = hitX;
        pp.hitY = hitY;
        pp.hitZ = hitZ;
        pp.placer = placer;
        pp.meta = meta;
        pp.hand = hand;
        return this;
    }
    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        for(int i = 0 ; i < 6; i++) {
            statelist[i] = ((ItemBlock)new ItemStack(compound.getCompoundTag(EnumFacing.byIndex(i).getName()))
                    .getItem()).getBlock().getStateForPlacement(pp.world, pp.pos, pp.facing, pp.hitX
                    , pp.hitY, pp.hitZ, pp.meta, pp.placer, pp.hand);
        }
    }

    private int[] collectVotes()
    {
        // Collect votes from all observers
        int[] votes = new int[ 6 ];
        double centerX = (double) pos.getX() + 0.5;
        double centerY = (double) pos.getY() + 0.5;
        double centerZ = (double) pos.getZ() + 0.5;

        // For each player:
        List<EntityPlayer> players = world.playerEntities;
        for (EntityPlayer entityPlayer : players) {
            // Determine whether they're looking at the block:
            if (entityPlayer != null) {
                // Check the player can see:

                ItemStack headGear = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (headGear.getItem() == QCraftItems.itemAntiObserveGoggle || headGear.getItem() == QCraftItems.itemQuantumGoggle) {
                    continue;
                }


                // Get position info:
                double x = entityPlayer.posX - centerX;
                double y = entityPlayer.posY + entityPlayer.getEyeHeight() - entityPlayer.getYOffset() - centerY;
                double z = entityPlayer.posZ - centerZ;

                // Check distance:
                double distance = Math.sqrt(x * x + y * y + z * z);
                if (distance < 96.0) {
                    // Get direction info:
                    double dx = x / distance;
                    double dy = y / distance;
                    double dz = z / distance;

                    // Get facing info:
                    float pitch = entityPlayer.rotationPitch;
                    float yaw = entityPlayer.rotationYaw;
                    float f3 = -MathHelper.cos(yaw * 0.017453292f);
                    float f4 = MathHelper.sin(yaw * 0.017453292f);
                    float f5 = MathHelper.cos(pitch * 0.017453292f);
                    float f6 = -MathHelper.sin(pitch * 0.017453292f);
                    float f7 = f4 * f5;
                    float f8 = f3 * f5;

                    // Compare facing and direction (must be close to opposite):
                    double dot = (double) f7 * dx + (double) f6 * dy + (double) f8 * dz;
                    if (dot < -0.4) {

                        // Block is being observed!

                        // Determine the major axis:
                        int majoraxis = -1;
                        double majorweight = 0.0f;

                        if (-dy >= majorweight) {
                            majoraxis = 0;
                            majorweight = -dy;
                        }
                        if (dy >= majorweight) {
                            majoraxis = 1;
                            majorweight = dy;
                        }
                        if (-dz >= majorweight) {
                            majoraxis = 2;
                            majorweight = -dz;
                        }
                        if (dz >= majorweight) {
                            majoraxis = 3;
                            majorweight = dz;
                        }
                        if (-dx >= majorweight) {
                            majoraxis = 4;
                            majorweight = -dx;
                        }
                        if (dx >= majorweight) {
                            majoraxis = 5;
                            majorweight = dx;
                        }

                        // Vote for this axis
                        if (majoraxis >= 0) {
                            votes[majoraxis]++;
                        }
                    }
                }
            }
        }

        return votes;
    }
}
