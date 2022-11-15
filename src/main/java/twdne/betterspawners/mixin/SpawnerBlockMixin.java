package twdne.betterspawners.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SpawnerBlock.class)
public class SpawnerBlockMixin {

	/**
	 * Cancel XP drop from spawner if mined with silk touch.
	 * @param dropExperience1 - boolean to be altered
	 * @param state - the state of the block
	 * @param world - the world
	 * @param pos - the position of the block
	 * @param stack - the ItemStack which caused the drop
	 * @param dropExperience - boolean to be filled with return value
	 * @return true if XP drop should be cancelled, false otherwise
	 */
	@ModifyVariable(method = "onStacksDropped", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockWithEntity;onStacksDropped(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;Z)V", shift = At.Shift.BEFORE), argsOnly = true)
	public boolean cancelXP(boolean dropExperience1, BlockState state, ServerWorld world, BlockPos pos, ItemStack stack, boolean dropExperience) {
		return !EnchantmentHelper.get(stack).containsKey(Enchantments.SILK_TOUCH) && dropExperience1;
	}
}
