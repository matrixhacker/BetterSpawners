package twdne.silktouchspawners.event;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import twdne.silktouchspawners.block.BetterSpawnerBlock;

public class EventHandler {

	/**
	 * Called when player breaks a block.
	 */
	public boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

		// Leave if client or creative mode or not spawner
		if (world.isClient || player.isCreative())
			return true;

		// Call handler based on block
		if (state.getBlock() == Blocks.SPAWNER)
			return (new BetterSpawnerBlock(world, pos)).onBlockBreak(player, entity);

		return true;
	}

	/**
	 * Called when player right-clicks a block.
	 */
	public ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

		// Pass if client
		if (world.isClient)
			return ActionResult.PASS;

		BlockPos pos = hitResult.getBlockPos();

		// Call handler based on block
		if (world.getBlockState(pos).getBlock() == Blocks.SPAWNER)
			return (new BetterSpawnerBlock(world, pos)).onBlockInteract(player, hand);

		return ActionResult.PASS;
	}
}
