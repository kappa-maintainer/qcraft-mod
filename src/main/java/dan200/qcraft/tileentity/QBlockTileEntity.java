package dan200.qcraft.tileentity;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import dan200.qcraft.block.BlockQBlock;
import dan200.qcraft.item.ItemQuantumGoggle;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class QBlockTileEntity extends TileEntity implements ITickable {

    public static int FUZZ_TIME = 9;
    private int m_entanglementFrequency;
    private int[] m_sideBlockTypes;

    // Replicated
    private long m_timeLastUpdated;

    private boolean beingObserved;
    private short currentSide;

    private short currentlyForcedSide;
    private boolean[] m_forceObserved;

    // Client only
    public int timeSinceLastChange;
    private boolean wearingGoggle;
    private boolean isTouchingWater;

    public QBlockTileEntity() {
        currentSide = 0;
    }
    @Override
    public void update() {
        if( !world.isRemote )
        {
            redetermineObservedSide();
        }

        // Update ticker, goggles and wetness
        timeSinceLastChange++;
        boolean goggles = world.isRemote && Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemQuantumGoggle;
        boolean wet = isTouchingLiquid();
        if( wearingGoggle != goggles || isTouchingWater != wet || timeSinceLastChange == FUZZ_TIME )
        {
            isTouchingWater = wet;
            wearingGoggle = goggles;
            this.updateContainingBlockInfo();
        }
    }
    private IBlockState[] stateList = new IBlockState[6];
    private ItemStack[] stackList = new ItemStack[6];
    public IBlockState getCurrentState() {
        return stateList[currentSide];
    }

    public void setStateList(IBlockState[] list) {
        this.stateList = list;
    }

    public void setStackList(ItemStack[] list) {
        this.stackList = list;
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

    private void setDisplayedSide( boolean observed, boolean forced, short side )
    {
        beingObserved = observed;
        currentlyForcedSide = forced ? side : -1;
        if( currentSide != side )
        {
            int oldSide = currentSide;
            IBlockState oldType = getObservedType();
            currentSide = side;
            int newSide = currentSide;
            IBlockState newType = getObservedType();
            if( newType != oldType || (oldSide < 0 != newSide < 0) )
            {
                timeSinceLastChange = 0;
                //blockUpdate();
                world.notifyBlockUpdate(pos, stateList[oldSide], stateList[oldSide], 2);
            }
        }
    }


    private void blockUpdate()
    {
        world.markAndNotifyBlock( pos, world.getChunk(pos), stateList[currentSide], stateList[currentSide], 2 );
        world.scheduleBlockUpdate( pos, QCraftBlocks.blockQBlock, QCraftBlocks.blockQBlock.tickRate( world ), 0);
        //world.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, QCraft.Blocks.qBlock );
    }

    public IBlockState getObservedType()
    {
        if( currentSide < 0 )
        {
            return stateList[ 1 ];
        }
        return stateList[currentSide];
    }

    private void redetermineObservedSide()
    {
        // Tally the votes, and work out if we need to change appearance.
        long currentTime = world.getWorldInfo().getWorldTotalTime();
        short winner = getObservationResult( currentTime );
        if( winner >= 6 )
        {
            // Force observed
            winner -= 6;
            setDisplayedSide( true, true, winner );
        }
        else if( winner >= 0 )
        {
            // Passively observed
            if( (currentlyForcedSide >= 0) || !beingObserved)
            {
                setDisplayedSide( true, false, winner );
            }
        }
        else
        {
            // Not observed
            if(beingObserved)
            {
                if( currentlyForcedSide >= 0 )
                {
                    setDisplayedSide( false, false, (short) -1);
                }
                else
                {
                    setDisplayedSide( false, false, currentSide);
                }
            }
        }
        m_timeLastUpdated = currentTime;
    }

    private short getObservationResult( long currentTime )
    {
        // Get local observer votes
        /*
        if( currentlyForcedSide >= 0 && m_forceObserved[currentlyForcedSide] )
        {
            return (short) (currentlyForcedSide + 6);
        }
        else
        {
            for( short i=0; i<6; ++i )
            {
                if( m_forceObserved[ i ] )
                {
                    return (short) (i + 6);
                }
            }
        }*/

        // Tally the votes
        return getFirstWatching();
    }



    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        for(int i = 0 ; i < 6; i++) {
            ItemStack stack = new ItemStack(compound.getCompoundTag(EnumFacing.byIndex(i).getName()));
            if(stack.getItem() instanceof ItemBlock) {
                stateList[i] = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());
            } else {
                stateList[i] = QCraftBlocks.blockQBlock.getDefaultState();
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        for(int i = 0; i < 6; i++) {
            if(stateList[i] != null) {
                compound.setTag(EnumFacing.byIndex(i).getName(), stackList[i].serializeNBT());
            }
        }
        return super.writeToNBT(compound);
    }

    private short getFirstWatching()
    {
        // Collect votes from all observers
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
                        short majoraxis = -1;
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
                            return majoraxis;
                        }
                    }
                }
            }
        }
        return -1;

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        NBTTagCompound nbtTag = new NBTTagCompound();
        //Write your data into the nbtTag
        nbtTag.setShort("new", currentSide);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        NBTTagCompound tag = pkt.getNbtCompound();
        //int old = currentSide;
        this.currentSide = tag.getShort("new");

        //Handle your Data
    }

    public class QTEUpdateMessage implements IMessage {
        // A default constructor is always required
        public QTEUpdateMessage(){}

        private int toSend;
        public QTEUpdateMessage(int newSide) {
            this.toSend = newSide;
        }

        @Override public void toBytes(ByteBuf buf) {
            // Writes the int into the buf
            buf.writeInt(toSend);
        }

        @Override public void fromBytes(ByteBuf buf) {
            // Reads the int back from the buf. Note that if you have multiple values, you must read in the same order you wrote.
            toSend = buf.readInt();
        }
    }

    public class MyMessageHandler implements IMessageHandler<QTEUpdateMessage, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case

        @Override public IMessage onMessage(QTEUpdateMessage message, MessageContext ctx) {

            // No response packet
            return null;
        }
    }
}
