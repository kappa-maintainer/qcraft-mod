package dan200.qcraft.tileentity;

import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import dan200.qcraft.block.CamouflageState;
import dan200.qcraft.block.ICamouflageableBlock;
import dan200.qcraft.block.IQuantumObservable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

import static net.minecraftforge.common.util.Constants.BlockFlags.*;

public class QBlockTileEntity extends TileEntity implements ITickable, ICamouflageableBlock, IQuantumObservable {

    public static final int FUZZ_TIME = 9;

    private static final IBlockState swirlBlockState = QCraftBlocks.blockSwirl.getDefaultState();
    private static final IBlockState fuzzBlockState = QCraftBlocks.blockFuzz.getDefaultState();

    private boolean prevBeingObserver = false;
    private boolean beingObserved = true;
    private short pendingSide = 0;
    private short currentSide = 0;
    private boolean isFuzz = false;
    
    private IBlockState[] stateList = new IBlockState[6];
    private final boolean[] faceBeingObserved = new boolean[] {false, false, false, false, false, false};
    private boolean isForceObserved = false;
    private short forceSide = 0;
    private short forceCounter = 0;
    private static final RandomUtils RANDOM = RandomUtils.insecure();
    
    public int transitionCounter = 10;
    private boolean wearingGoggle;
    
    private static final float degree2pi = (float) (Math.PI / 180.0F);
    
    public QBlockTileEntity() {
        blockType = QCraftBlocks.blockQBlock;
    }

    @Override
    public Block getBlockType()
    {
        return QCraftBlocks.blockQBlock;
    }
    
    @Override
    public void update() {
        if( !world.isRemote )
        {
            updateObserveState();
        } else {
            boolean goggles = Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem().equals(QCraftItems.itemQuantumGoggle);
            if (wearingGoggle != goggles) {
                wearingGoggle = goggles;
                IBlockState state = world.getBlockState(pos);
                if (state instanceof CamouflageState) {
                    if (wearingGoggle) {
                        world.setBlockState(pos, new CamouflageState(getBlockType(), fuzzBlockState), RERENDER_MAIN_THREAD | NO_OBSERVERS);
                    } else {
                        world.setBlockState(pos, new CamouflageState(getBlockType(), getCamouflageBlockState()), RERENDER_MAIN_THREAD | NO_OBSERVERS);
                    }
                }
            }
        }
    }

    public void setStateList(IBlockState[] list) {
        this.stateList = list;
    }
    

    private void updateObserveState()
    {
        if (!isForceObserved) {
            calculateObserves();
            boolean isTransition = !prevBeingObserver && beingObserved && pendingSide != currentSide;
            if (isTransition) {
                if (transitionCounter <= FUZZ_TIME) {
                    if (transitionCounter == 0) {
                        isFuzz = true;
                        world.setBlockState(pos, new CamouflageState(getBlockType(), swirlBlockState));
                        world.markBlockRangeForRenderUpdate(pos, pos);
                        markDirty();
                    }
                } else {
                    IBlockState oldType = getCamouflageBlockState();
                    currentSide = pendingSide;
                    IBlockState newType = getCamouflageBlockState();
                    if (newType != oldType) {
                        isFuzz = false;
                        updateCamouflageBlockState();

                    /*
                    IBlockState newSub = stateList[currentSide];
                    world.notifyBlockUpdate(pos, swirlBlockState, newSub, DEFAULT_AND_RERENDER);*/
                    }
                }
                transitionCounter++;
            }
        } else {
            if (transitionCounter == 0) {
                transitionCounter++;
                isFuzz = true;
                world.setBlockState(pos, new CamouflageState(getBlockType(), swirlBlockState));
                world.markBlockRangeForRenderUpdate(pos, pos);
                markDirty();
            } else if (transitionCounter == FUZZ_TIME) {
                IBlockState oldType = getCamouflageBlockState();
                forceSide = pendingSide;
                IBlockState newType = getCamouflageBlockState();
                if (newType != oldType) {
                    isFuzz = false;
                    updateCamouflageBlockState();
                }
            } else {
                transitionCounter++;
            }
        }
    }

    @Override
    protected void setWorldCreate(World worldIn)
    {
        world = worldIn;
        this.setWorld(worldIn);
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
            setIsObservingByPlayer(false);
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
                double y = entityPlayer.posY + entityPlayer.getEyeHeight() - centerY;// - entityPlayer.getYOffset() - centerY;
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
                    float f6 = MathHelper.sin(pitch * degree2pi);
                    float f7 = f4 * f5;
                    float f8 = f3 * f5;

                    // Compare facing and direction (must be close to opposite):
                    double dot = (double) f7 * dx + (double) f6 * dy + (double) f8 * dz;
                    
                    if (dot > 0.4) {
                        if (!beingObserved) {
                            setIsObservingByPlayer(true);

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
                                if (pendingSide != majoraxis) {
                                    pendingSide = majoraxis;
                                    transitionCounter = 0;
                                }
                            }
                        }
                    } else {
                        setIsObservingByPlayer(false);
                    }
                }
            }
        }
        if (!hasNearByPlayer) {
            setIsObservingByPlayer(false);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        for(int i = 0 ; i < 6; i++) {
            stateList[i] = NBTUtil.readBlockState(compound.getCompoundTag(EnumFacing.byIndex(i).getName()));
        }
        currentSide = compound.getShort("current");
        pendingSide = currentSide;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        for(int i = 0; i < 6; i++) {
            if(stateList[i] != null) {
                NBTTagCompound state = new NBTTagCompound();
                NBTUtil.writeBlockState(state, stateList[i]);
                compound.setTag(EnumFacing.byIndex(i).getName(), state);
            }
        }
        compound.setShort("current", currentSide);
        return super.writeToNBT(compound);
    }

    @Override
    public void onLoad()
    {
        updateCamouflageBlockState();
        world.notifyBlockUpdate(pos, QCraftBlocks.blockQBlock.getDefaultState(), getCamouflageBlockState(), DEFAULT_AND_RERENDER);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        NBTTagCompound nbtTag = new NBTTagCompound();
        //Write your data into the nbtTag
        nbtTag.setShort("current", isForceObserved ? forceSide : currentSide);
        nbtTag.setBoolean("fuzz", isFuzz);
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        
        NBTTagCompound tag = pkt.getNbtCompound();
        //int old = currentSide;
        this.isFuzz = tag.getBoolean("fuzz");
        if (isFuzz) {
            world.setBlockState(pos, new CamouflageState(getBlockType(), swirlBlockState));
            world.markBlockRangeForRenderUpdate(pos, pos);
        } else {
            this.currentSide = tag.getShort("current");
            pendingSide = currentSide;
            updateCamouflageBlockState();
        }
        //Handle your Data
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public IBlockState getCamouflageBlockState() {
        if( currentSide < 0 || stateList[0] == null)
        {
            return swirlBlockState;
        }
        if (isForceObserved) {
            return stateList[forceSide];
        } else {
            return stateList[currentSide];
        }
    }
    
    private void updateCamouflageBlockState() {
        IBlockState subState;
        if (isForceObserved) {
            subState = stateList[forceSide];
        } else {
            subState = stateList[currentSide];
        }
        
        world.setBlockState(pos, new CamouflageState(QCraftBlocks.blockQBlock, subState));
        world.markBlockRangeForRenderUpdate(pos, pos);
        markDirty();
    }
    
    private void setIsObservingByPlayer(boolean observed) {
        prevBeingObserver = beingObserved;
        beingObserved = observed;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        for(int i = 0; i < 6; i++) {
            if(stateList[i] != null) {
                String face = EnumFacing.byIndex(i).getName();
                compound.setInteger(face + "-id", Block.getIdFromBlock(stateList[i].getBlock()));
                compound.setInteger(face + "-meta", stateList[i].getBlock().getMetaFromState(stateList[i]));
            }
        }
        compound.setShort("current", currentSide);
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
        super.handleUpdateTag(compound);
        for(int i = 0 ; i < 6; i++) {
            String face = EnumFacing.byIndex(i).getName();
            stateList[i] = Block.getBlockById(compound.getInteger(face + "-id")).getStateFromMeta(compound.getInteger(face + "-meta"));
        }
        currentSide = compound.getShort("current");
        pendingSide = currentSide;
        updateCamouflageBlockState();
    }

    @Override
    public boolean isObserved() {
        return isForceObserved;
    }

    @Override
    public boolean isObserved(EnumFacing facing) {
        return faceBeingObserved[facing.ordinal()];
    }

    @Override
    public void observe(EnumFacing facing) {
        short ordinal = (short) facing.ordinal();
        if (faceBeingObserved[ordinal]) {
            return;
        } else {
            faceBeingObserved[facing.ordinal()] = true;
            forceCounter++;
        }
        if(!isForceObserved) {
            isForceObserved = true;
            forceSide = currentSide;
        }
        updateForceSide(ordinal);
    }

    @Override
    public void reset(EnumFacing facing) {
        int ordinal = facing.ordinal();
        if (!faceBeingObserved[ordinal]) {
            return;
        } else {
            faceBeingObserved[facing.ordinal()] = false;
            forceCounter--;
        }
        if (forceCounter == 0) {
            isForceObserved = false;
            transitionCounter = 0;
            currentSide = forceSide;
            return;
        }
        short random = (short) RANDOM.randomInt(1, forceCounter);
        for(short i = 0; i < 6; i++) {
            if (faceBeingObserved[i]) {
                random--;
            }
            if (random == 0) {
                updateForceSide(i);
            }
        }
    }
    
    private void updateForceSide(short newSide) {
        pendingSide = newSide;
        if (newSide != forceSide)
            transitionCounter = 0;
    }
}
