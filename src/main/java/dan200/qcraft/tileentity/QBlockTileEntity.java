package dan200.qcraft.tileentity;

import dan200.qcraft.QCraft;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import dan200.qcraft.block.CamouflageBlockProperty;
import dan200.qcraft.block.ICamouflageableBlock;
import dan200.qcraft.item.ItemQuantumGoggle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.List;

import static net.minecraftforge.common.util.Constants.BlockFlags.*;

public class QBlockTileEntity extends TileEntity implements ITickable, ICamouflageableBlock {

    public static int FUZZ_TIME = 9;


    private boolean prevBeingObserver = false;
    private boolean beingObserved = true;
    private short pendingSide = -1;
    private short currentSide = -1;
    
    private IBlockState[] stateList = new IBlockState[6];
    private ItemStack[] stackList = new ItemStack[6];

    // Client only
    public int timeSinceLastChange = 10;
    private boolean wearingGoggle;
    private boolean isTouchingWater;
    
    private static final float degree2pi = (float) (Math.PI / 180.0F);
    
    public QBlockTileEntity() {}
    
    @Override
    public void update() {
        if (stateList[0] == null) return;
        if( !world.isRemote )
        {
            updateObserveState();
        }

        // Update ticker, goggles and wetness
        timeSinceLastChange++;
        boolean goggles = world.isRemote && Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemQuantumGoggle;
        boolean wet = isTouchingLiquid();
        if( wearingGoggle != goggles || isTouchingWater != wet || timeSinceLastChange < FUZZ_TIME )
        {
            isTouchingWater = wet;
            wearingGoggle = goggles;
        }
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

    private void updateObserveState()
    {
        calculateObserves();
        if (prevBeingObserver != beingObserved && (timeSinceLastChange > FUZZ_TIME || beingObserved)) {
            if( currentSide != pendingSide )
            {
                int oldSide = currentSide;
                IBlockState oldType = getCamouflageBlockState();
                currentSide = pendingSide;
                int newSide = currentSide;
                IBlockState newType = getCamouflageBlockState();
                if( newType != oldType || (oldSide < 0 != newSide < 0) )
                {
                    updateExtendedBlockState();
                    timeSinceLastChange = 0;

                    IBlockState oldSub;
                    IBlockState newSub;
                    if (oldSide < 0) {
                        oldSub = QCraftBlocks.blockSwirl.getDefaultState();
                    } else {
                        oldSub = stateList[oldSide];
                    }
                    if (currentSide < 0) {
                        newSub = QCraftBlocks.blockSwirl.getDefaultState();
                    } else {
                        newSub = stateList[currentSide];
                    }
                    world.notifyBlockUpdate(pos, oldSub, newSub, DEFAULT_AND_RERENDER);
                }
            }
        }
    }

    @Override
    protected void setWorldCreate(World worldIn)
    {
        world = worldIn;
        this.setWorld(worldIn);
    }


    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        for(int i = 0 ; i < 6; i++) {
            ItemStack stack = new ItemStack(compound.getCompoundTag(EnumFacing.byIndex(i).getName()));
            stackList[i] = stack;
            if(stack.getItem() instanceof ItemBlock) {
                stateList[i] = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(compound.getInteger(EnumFacing.byIndex(i).getName() + "-meta"));
            } else {
                stateList[i] = Blocks.AIR.getDefaultState();
            }
        }
        currentSide = compound.getShort("current");
        updateExtendedBlockState();
        world.notifyBlockUpdate(pos, QCraftBlocks.blockQBlock.getDefaultState(), QCraftBlocks.blockQBlock.getDefaultState(), DEFAULT_AND_RERENDER);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        for(int i = 0; i < 6; i++) {
            if(stateList[i] != null) {
                compound.setTag(EnumFacing.byIndex(i).getName(), stackList[i].serializeNBT());
                compound.setInteger(EnumFacing.byIndex(i).getName() + "-meta", stateList[i].getBlock().getMetaFromState(stateList[i]));
            }
        }
        compound.setShort("current", currentSide);
        return super.writeToNBT(compound);
    }

    private void calculateObserves()
    {
        // Collect votes from all observers
        double centerX = (double) pos.getX() + 0.5;
        double centerY = (double) pos.getY() + 0.5;
        double centerZ = (double) pos.getZ() + 0.5;

        // For each player:
        List<EntityPlayer> players = world.playerEntities;
        if (players.isEmpty()) {
            setIsObserving(false);
            pendingSide = -1;
            return;
        }
        
        boolean hasNearByPlayer = false;
        
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
                    if (!hasNearByPlayer) {
                        hasNearByPlayer = true;
                    }
                    // Get direction info:
                    double dx = x / distance;
                    double dy = y / distance;
                    double dz = z / distance;

                    // Get facing info:
                    float pitch = entityPlayer.rotationPitch;
                    float yaw = entityPlayer.rotationYaw;
                    float f3 = -MathHelper.cos(yaw * degree2pi);
                    float f4 = MathHelper.sin(yaw * degree2pi);
                    float f5 = MathHelper.cos(pitch * degree2pi);
                    float f6 = -MathHelper.sin(pitch * degree2pi);
                    float f7 = f4 * f5;
                    float f8 = f3 * f5;

                    // Compare facing and direction (must be close to opposite):
                    double dot = (double) f7 * dx + (double) f6 * dy + (double) f8 * dz;
                    if (dot < -0.4) {

                        // Block is being observed!
                        setIsObserving(false);

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
                        }

                        // Vote for this axis
                        if (majoraxis >= 0) {
                            pendingSide = majoraxis;
                        }
                    } else {
                        setIsObserving(true);
                    }
                }
            }
        }
        if (!hasNearByPlayer) {
            pendingSide = -1;
            setIsObserving(false);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        NBTTagCompound nbtTag = new NBTTagCompound();
        //Write your data into the nbtTag
        nbtTag.setShort("current", currentSide);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        
        NBTTagCompound tag = pkt.getNbtCompound();
        //int old = currentSide;
        this.currentSide = tag.getShort("current");
        updateExtendedBlockState();
        //Handle your Data
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return newSate.getBlock() instanceof BlockAir;
        //return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public IBlockState getCamouflageBlockState() {
        if( currentSide < 0 || stateList[0] == null)
        {
            return QCraftBlocks.blockSwirl.getDefaultState();
        }
        return stateList[currentSide];
    }
    
    private void updateExtendedBlockState() {
        IBlockState subState;
        if (currentSide < 0) {
            subState = QCraftBlocks.blockSwirl.getDefaultState();
        } else {
            subState = stateList[currentSide];
        }
        
        //world.setBlockState(pos, ((IExtendedBlockState) QCraftBlocks.blockQBlock.getExtendedState(QCraftBlocks.blockQBlock.getDefaultState(), world, pos)).withProperty(CamouflageBlockProperty.CURRENT_CAMOU, subState));
        world.setBlockState(pos, ((IExtendedBlockState) world.getBlockState(pos)).withProperty(CamouflageBlockProperty.CURRENT_CAMOU, subState));
        world.markBlockRangeForRenderUpdate(pos, pos);
        markDirty();
    }
    
    private void setIsObserving(boolean observed) {
        prevBeingObserver = beingObserved;
        beingObserved = observed;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        for(int i = 0; i < 6; i++) {
            if(stateList[i] != null) {
                compound.setTag(EnumFacing.byIndex(i).getName(), stackList[i].serializeNBT());
                compound.setInteger(EnumFacing.byIndex(i).getName() + "-meta", stateList[i].getBlock().getMetaFromState(stateList[i]));
            }
        }
        compound.setShort("current", currentSide);
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
        for(int i = 0 ; i < 6; i++) {
            ItemStack stack = new ItemStack(compound.getCompoundTag(EnumFacing.byIndex(i).getName()));
            stackList[i] = stack;
            if(stack.getItem() instanceof ItemBlock) {
                stateList[i] = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(compound.getInteger(EnumFacing.byIndex(i).getName() + "-meta"));
            } else {
                stateList[i] = Blocks.AIR.getDefaultState();
            }
        }
        currentSide = compound.getShort("current");
        updateExtendedBlockState();
    }
}
