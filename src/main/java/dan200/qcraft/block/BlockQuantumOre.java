package dan200.qcraft.block;

import dan200.qcraft.QCraft;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.QCraftItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockQuantumOre extends Block {
    public BlockQuantumOre(boolean isOn) {
        super(Material.ROCK);
        setTranslationKey("qcraft.ore");
        setCreativeTab(QCraft.QCRAT_TAB);
        setHardness(3.0F);
        setResistance(5.0F);
        if (isOn) {
            this.setTickRandomly(true);
            setLightLevel(0.625F);
            setRegistryName("qcraft:ore_glowing");
        } else {
            setRegistryName("qcraft:ore");
        }

        this.isOn = isOn;

    }

    private final boolean isOn;


    public int tickRate(World worldIn)
    {
        return 30;
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        this.activate(worldIn, pos);
        super.onBlockClicked(worldIn, pos, playerIn);
    }

    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        this.activate(worldIn, pos);
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        this.activate(worldIn, pos);
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    private void activate(World worldIn, BlockPos pos)
    {
        this.spawnParticles(worldIn, pos);

        if (this == QCraftBlocks.blockQuantumOre)
        {
            worldIn.setBlockState(pos, QCraftBlocks.blockQuantumOreOn.getDefaultState());
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this == QCraftBlocks.blockQuantumOreOn)
        {
            worldIn.setBlockState(pos, QCraftBlocks.blockQuantumOre.getDefaultState());
        }
    }


    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return QCraftItems.itemQuantumDust;
    }


    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }


    public int quantityDropped(Random random)
    {
        return random.nextInt(10);
    }


    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this))
        {
            return RANDOM.nextInt(10);
        }
        return 0;
    }


    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (this.isOn)
        {
            this.spawnParticles(worldIn, pos);
        }
    }

    private void spawnParticles(World worldIn, BlockPos pos)
    {
        Random random = worldIn.rand;

        for (int i = 0; i < 6; ++i)
        {
            double d1 = (float)pos.getX() + random.nextFloat();
            double d2 = (float)pos.getY() + random.nextFloat();
            double d3 = (float)pos.getZ() + random.nextFloat();

            if (i == 0 && !worldIn.getBlockState(pos.up()).isOpaqueCube())
            {
                d2 = (double)pos.getY() + 0.0625D + 1.0D;
            }

            if (i == 1 && !worldIn.getBlockState(pos.down()).isOpaqueCube())
            {
                d2 = (double)pos.getY() - 0.0625D;
            }

            if (i == 2 && !worldIn.getBlockState(pos.south()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() + 0.0625D + 1.0D;
            }

            if (i == 3 && !worldIn.getBlockState(pos.north()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() - 0.0625D;
            }

            if (i == 4 && !worldIn.getBlockState(pos.east()).isOpaqueCube())
            {
                d1 = (double)pos.getX() + 0.0625D + 1.0D;
            }

            if (i == 5 && !worldIn.getBlockState(pos.west()).isOpaqueCube())
            {
                d1 = (double)pos.getX() - 0.0625D;
            }

            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1))
            {
                worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.1D, 0.9D, 0.1D);
            }
        }
    }

    protected ItemStack getSilkTouchDrop(IBlockState state)
    {
        return new ItemStack(QCraftBlocks.blockQuantumOre);
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(QCraftBlocks.blockQuantumOre), 1, this.damageDropped(state));
    }
}
