
package net.mcreator.concoction.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.mcreator.concoction.init.ConcoctionModItems;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.SpecialPlantable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;



public class CropPuffballBlock extends CropBlock {
	public static final int MAX_AGE = 2;
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

	public CropPuffballBlock() {
		super(BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).sound(SoundType.GRASS).instabreak().noCollission().noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		// Форма блока в зависимости от возраста
		return switch (state.getValue(AGE)) {
			default -> Block.box(0, 0, 0, 16, 1, 16);

			case 0 -> Block.box(0, 0, 0, 16, 1, 16);
			case 1 -> Block.box(6, 1, 6, 10, 4, 10);
			case 2 -> Block.box(4, 1, 4, 12, 7, 12);
		};
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		// Добавление свойства возраста в состояние блока
		builder.add(AGE);
	}
	
	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		// Предмет, получаемый при копировании блока на колёсико
		return new ItemStack(
            ConcoctionModItems.PUFFBALL_SPORES.get()
            );
	}

	@Override
    public boolean mayPlaceOn(BlockState p_52302_, BlockGetter p_52303_, BlockPos p_52304_) {
        return p_52302_.getBlock() instanceof FarmBlock || p_52302_.getBlock() instanceof SoullandBlock;
    }

	@Override
	public PathType getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
		// Тип пути для мобов
		return PathType.OPEN;
	}
	
	@Override
	protected ItemLike getBaseSeedId() {
		// Возвращает семена для посадки растения
		return ConcoctionModItems.PUFFBALL_SPORES.get();
	}

	@Override
    public int getMaxAge() {
        // Возвращает максимальный возраст растения
        return MAX_AGE; // не менять
    }

	@Override
	public IntegerProperty getAgeProperty() {
		// Возвращает свойство возраста растения
		return AGE; // не менять
	}
}
