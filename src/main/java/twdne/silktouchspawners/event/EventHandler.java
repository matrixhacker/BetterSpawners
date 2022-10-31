package twdne.silktouchspawners.event;

import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

import java.util.Objects;

public class EventHandler {

	/**
	 * Called when player breaks a block. I wanted to do this in a loot table as json files are simpler.
	 * Unfortunately I was unable to drop a spawner with a custom name based on the spawned entity.
	 * This method is less simplistic but perfectly functional.
	 */
	public boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

		// Leave if client or creative mode or not spawner
		if (world.isClient || player.isCreative() || !(state.getBlock() instanceof SpawnerBlock))
			return true;

		// Get item enchantments
		NbtList enchants = Iterables.get(player.getHandItems(), 0).getEnchantments();

		// Spawn spawner with correct name and data if item has silk touch
		if (enchants.asString().contains("silk_touch")) {
			MobSpawnerLogic logic = ((MobSpawnerBlockEntity) entity).getLogic();

			// Copy current spawner data to new spawner
			NbtCompound nbt = logic.writeNbt(new NbtCompound());
			ItemStack itemStack = new ItemStack(Registry.ITEM.get(new Identifier("spawner")));
			NbtCompound new_nbt = new NbtCompound();
			new_nbt.put("SpawnData", nbt.get("SpawnData"));
			new_nbt.put("SpawnPotentials", nbt.get("SpawnPotentials"));
			itemStack.setSubNbt("BlockEntityTag", new_nbt);

			// Apply custom name
			Entity spawn_entity = Objects.requireNonNull(logic.getRenderedEntity(world));
			if (!(spawn_entity instanceof AreaEffectCloudEntity)) {
				String entity_name = spawn_entity.getName().getString();
				itemStack.setCustomName(Text.of(entity_name + " Spawner"));
			}

			// Get random fly-out position offsets
			double d0 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.12F;
			double d1 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.07F + 0.7D;
			double d2 = (double) (world.getRandom().nextFloat() * 0.6F) + (double) 0.12F;

			// Create entity item and spawn
			ItemEntity entityItem = new ItemEntity(world, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemStack);
			entityItem.setToDefaultPickupDelay();
			world.spawnEntity(entityItem);
		}

		return true;
	}

	/**
	 * Called when player right-clicks a block. Will clear the spawn entity from a spawner if holding an end crystal.
	 */
	public ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

		// Pass if client
		if (world.isClient)
			return ActionResult.PASS;

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = world.getBlockState(pos);

		// Pass if not spawner or using off hand
		if (state.getBlock() != Blocks.SPAWNER || hand == Hand.OFF_HAND)
			return ActionResult.PASS;

//        if(ConfigValues.get("disable_egg_removal_from_spawner") != 0)
//            return ActionResult.PASS;

		// Pass if not End Crystal
		Item item = player.getMainHandStack().getItem();
		if (!(item instanceof EndCrystalItem))
			return ActionResult.PASS;

		// Clear entity from spawner. Since a spawner must have an entity use Area Effect Cloud
		MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(pos);
		assert spawner != null;
		spawner.getLogic().setEntityId(EntityType.AREA_EFFECT_CLOUD);
		spawner.readNbt(spawner.getLogic().writeNbt(new NbtCompound()));
		spawner.markDirty();
		world.updateListeners(pos, state, state, Block.NOTIFY_ALL);

		// Consume item
		player.getMainHandStack().decrement(1);

		return ActionResult.SUCCESS;
	}
}
