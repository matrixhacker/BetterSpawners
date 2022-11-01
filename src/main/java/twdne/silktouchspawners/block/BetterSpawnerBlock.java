package twdne.silktouchspawners.block;

import com.google.common.collect.Iterables;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Objects;

public class BetterSpawnerBlock {

	private final World world;
	private final BlockPos spawnerBlockPos;
	private final EntityType<?> blankSpawnerType = EntityType.AREA_EFFECT_CLOUD;

	public static final PointOfInterestType POI_SPAWNER = PointOfInterestHelper.register(new Identifier("spawner"), 1, 2, Blocks.SPAWNER);

	/**
	 * Needed so that the POI is initialized before the registry is frozen.
	 */
	public static void init() {}

	public BetterSpawnerBlock(World world_, BlockPos spawnerBlockPos_) {
		world = world_;
		spawnerBlockPos = spawnerBlockPos_;
		if (world.getBlockState(spawnerBlockPos).getBlock() != Blocks.SPAWNER)
			throw new IllegalArgumentException("Block at pos " + spawnerBlockPos + " is not of type SPAWNER.");
	}

	/**
	 * I wanted to do this in a loot table as json files are simpler.
	 * Unfortunately I was unable to drop a spawner with a custom name based on the spawned entity.
	 * This method is less simplistic but perfectly functional.
	 * @return always true since false would break the callback cycle.
	 */
	public boolean onBlockBreak(PlayerEntity player, BlockEntity entity) {
		// Pass if disabled in config
//        if(ConfigValues.get("disable_silk_spawner") != 0)
//            return ActionResult.PASS;

		// Get item enchantments
		NbtList enchants = Iterables.get(player.getHandItems(), 0).getEnchantments();

		// Spawn spawner with correct name and data if item has silk touch
		if (!enchants.asString().contains("silk_touch"))
			return true;

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
		ItemEntity entityItem = new ItemEntity(world, (double) spawnerBlockPos.getX() + d0, (double) spawnerBlockPos.getY() + d1, (double) spawnerBlockPos.getZ() + d2, itemStack);
		entityItem.setToDefaultPickupDelay();
		world.spawnEntity(entityItem);

		return true;
	}

	/**
	 * Called when player right-clicks a spawner block.
	 * Will clear the spawn entity from a spawner if holding an end crystal.
	 * @return PASS when no action is required, SUCCESS otherwise
	 */
	public ActionResult onBlockInteract(PlayerEntity player, Hand hand) {
		// Pass if disabled in config
//        if(ConfigValues.get("disable_change_spawner") != 0)
//            return ActionResult.PASS;

		// Pass if off-hand action or not End Crystal
		if (hand == Hand.OFF_HAND || !(player.getMainHandStack().getItem() instanceof EndCrystalItem))
			return ActionResult.PASS;

		// Clear entity from spawner. Since a spawner must have an entity use blankSpawnerType.
		updateSpawnerEntity(blankSpawnerType);

		// Consume item if not in creative mode
		if (!player.isCreative())
			player.getMainHandStack().decrement(1);

		return ActionResult.SUCCESS;
	}

	/**
	 * Update the spawner with the supplied entity type.
	 */
	public void updateSpawnerEntity(EntityType<?> spawnEntityType) {
		BlockState spawnerState = world.getBlockState(spawnerBlockPos);
		MobSpawnerBlockEntity spawner = Objects.requireNonNull((MobSpawnerBlockEntity) world.getBlockEntity(spawnerBlockPos));
		spawner.getLogic().setEntityId(spawnEntityType);
		spawner.readNbt(spawner.getLogic().writeNbt(new NbtCompound()));
		spawner.markDirty();
		world.updateListeners(spawnerBlockPos, spawnerState, spawnerState, Block.NOTIFY_ALL);
	}

	/**
	 * Check to see if this spawner is blank and close enough to the given mob.
	 * @return true if blank and within configured distance; false otherwise
	 */
	public boolean isBlankSpawner() {
		return Objects.requireNonNull(((MobSpawnerBlockEntity) Objects.requireNonNull(world.getBlockEntity(spawnerBlockPos))).getLogic().getRenderedEntity(world)).getType() == blankSpawnerType;
	}
}
