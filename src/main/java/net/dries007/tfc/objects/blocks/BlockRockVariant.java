package net.dries007.tfc.objects.blocks;

import net.dries007.tfc.objects.Rock;
import net.dries007.tfc.objects.Type;
import net.dries007.tfc.objects.entity.EntityFallingBlockTFC;
import net.dries007.tfc.objects.items.ItemRock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IFallingBlock;
import net.dries007.tfc.util.InsertOnlyEnumTable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockRockVariant extends Block implements IFallingBlock
{
    private static final InsertOnlyEnumTable<Rock, Type, BlockRockVariant> TABLE = new InsertOnlyEnumTable<>(Rock.class, Type.class);

    public static BlockRockVariant get(Rock rock, Type type)
    {
        return TABLE.get(rock, type);
    }

    public final Type type;
    public final Rock rock;

    public BlockRockVariant(Type type, Rock rock)
    {
        super(type.material);
        TABLE.put(rock, type, this);
        this.type = type;
        this.rock = rock;
        if (type.isGrass) setTickRandomly(true);
        switch (type)
        {
            case BRICKS:
            case RAW:
                setSoundType(SoundType.STONE);
                setHardness(2.0F).setResistance(10.0F);
                setHarvestLevel("pickaxe", 0);
                break;
            case COBBLE:
            case SMOOTH:
                setSoundType(SoundType.STONE);
                setHardness(1.5F).setResistance(10.0F);
                setHarvestLevel("pickaxe", 0);
                break;
            case SAND:
                setSoundType(SoundType.SAND);
                setHardness(0.5F);
                setHarvestLevel("shovel", 0);
                break;
            case DIRT:
                setSoundType(SoundType.GROUND);
                setHardness(0.5F);
                setHarvestLevel("shovel", 0);
                break;
            case GRAVEL:
                setSoundType(SoundType.GROUND);
                setHardness(0.6F);
                setHarvestLevel("shovel", 0);
                break;
            case CLAY:
            case CLAY_GRASS:
            case GRASS:
            case DRY_GRASS:
                setSoundType(SoundType.PLANT);
                setHardness(0.6F);
                setHarvestLevel("shovel", 0);
                break;
        }
    }

    public BlockRockVariant getVariant(Type t)
    {
        return TABLE.get(rock, t);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        switch (type)
        {
            case RAW:
                return ItemRock.get(rock);
            case CLAY:
            case CLAY_GRASS:
                return Items.CLAY_BALL; // todo: own clay or event for clay making?
            default:
                return super.getItemDropped(state, rand, fortune);
            case GRASS:
            case DRY_GRASS:
                return Item.getItemFromBlock(get(rock, Type.DIRT));
        }
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random)
    {
        // todo: see how 1710 handles this
        switch (type)
        {
            case CLAY:
            case CLAY_GRASS:
                return 4;
            default:
                return super.quantityDropped(state, fortune, random);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return type.isGrass ? BlockRenderLayer.CUTOUT : BlockRenderLayer.SOLID;
    }

    @Override
    public boolean shouldFall(IBlockState state, World world, BlockPos pos)
    {
        return type.isAffectedByGravity && IFallingBlock.super.shouldFall(state, world, pos);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (shouldFall(state, worldIn, pos)) worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
    }

    // IDK what the alternative is supposed to be, so I'm gonna continue using this.
    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (shouldFall(state, worldIn, pos)) worldIn.scheduleUpdate(pos, this, tickRate(worldIn));
    }

    @Override
    public int tickRate(World worldIn)
    {
        return 1; // todo: tickrate in vanilla is 2, in tfc1710 it's 10
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);
        if (worldIn.isRemote) return;
        if (shouldFall(state, worldIn, pos))
        {
            if (!BlockFalling.fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32)))
            {
                worldIn.spawnEntity(new EntityFallingBlockTFC(worldIn, pos, this, worldIn.getBlockState(pos)));
            }
            else
            {
                worldIn.setBlockToAir(pos);
                pos = pos.add(0, -1, 0);
                while (canFallThrough(worldIn.getBlockState(pos)) && pos.getY() > 0)
                    pos = pos.add(0, -1, 0);
                if (pos.getY() > 0) worldIn.setBlockState(pos.up(), state); // Includes Forge's fix for data loss.
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (!this.type.isAffectedByGravity) return;
        if (rand.nextInt(16) != 0) return;
        if (shouldFall(stateIn, worldIn, pos))
        {
            double d0 = (double)((float)pos.getX() + rand.nextFloat());
            double d1 = (double)pos.getY() - 0.05D;
            double d2 = (double)((float)pos.getZ() + rand.nextFloat());
            worldIn.spawnParticle(EnumParticleTypes.FALLING_DUST, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(stateIn));
        }
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable)
    {
        EnumPlantType plantType = plantable.getPlantType(world, pos.offset(direction));

        switch (plantType)
        {
            case Plains: return type == Type.DIRT || type == Type.GRASS; // todo: dry grass?
            case Crop: return type == Type.DIRT || type == Type.GRASS; // todo: dry grass? Should this even be true? Might be required for wild crops.
            case Desert: return type == Type.SAND;
            case Cave: return true;
            case Water: return false;
            case Beach:
                return (type == Type.DIRT || type == Type.GRASS || type == Type.SAND || type == Type.DRY_GRASS) && // todo: dry grass?
                        (BlocksTFC.isWater(world.getBlockState(pos.add(1, 0, 0))) ||
                         BlocksTFC.isWater(world.getBlockState(pos.add(-1, 0, 0))) ||
                         BlocksTFC.isWater(world.getBlockState(pos.add(0, 0, 1))) ||
                         BlocksTFC.isWater(world.getBlockState(pos.add(0, 0, -1))));
            case Nether: return false;
        }

        return false;
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (world.isRemote) return;
        if (type.isGrass) Helpers.spreadGrass(world, pos, state, rand);
    }
}