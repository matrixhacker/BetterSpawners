package twdne.silktouchspawners.event;

import com.google.common.collect.Iterables;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;

public class EventHandler {

    /**
     * 	Called when player breaks a block. I wanted to do this in a loot table as json files are simpler.
     * 	Unfortunately I was unable to drop a spawner with a custom name based on the spawned entity.
     * 	This method is less simplistic but perfectly functional.
     */
    public boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

        // Leave if client or creative mode or not spawner
        if (world.isClient || player.isCreative() || !(state.getBlock() instanceof SpawnerBlock))
            return true;

        // Get item enchantments
        NbtList enchants = Iterables.get(player.getHandItems(), 0).getEnchantments();

        // Spawn spawner with correct name and data if item has silk touch
        if (enchants.asString().contains("silk_touch")) {
            MobSpawnerLogic logic = ((MobSpawnerBlockEntity)entity).getLogic();

            // Copy current spawner data to new spawner and apply custom name
            NbtCompound nbt = logic.writeNbt(new NbtCompound());
            NbtElement spawnData = nbt.get("SpawnData");
            assert spawnData != null : "This spawner contains no spawn data.";
            String[] entity_name = spawnData.toString().replaceAll(".*minecraft:(\\w+).*", "$1").split("_");
            for (int i = 0; i < entity_name.length; ++i) {
                entity_name[i] = entity_name[i].substring(0, 1).toUpperCase() + entity_name[i].substring(1);
            }
            ItemStack itemStack = new ItemStack(Registry.ITEM.get(new Identifier("spawner")));
            NbtCompound new_nbt = new NbtCompound();
            new_nbt.put("SpawnData", spawnData);
            new_nbt.put("SpawnPotentials", nbt.get("SpawnPotentials"));
            itemStack.setSubNbt("BlockEntityTag", new_nbt);
            itemStack.setCustomName(Text.of(String.join(" " , entity_name) + " Spawner"));

            // Get random fly-out position offsets
            double d0 = (double)(world.getRandom().nextFloat() * 0.6F) + (double)0.12F;
            double d1 = (double)(world.getRandom().nextFloat() * 0.6F) + (double)0.07F + 0.7D;
            double d2 = (double)(world.getRandom().nextFloat() * 0.6F) + (double)0.12F;

            // Create entity item and spawn
            ItemEntity entityItem = new ItemEntity(world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, itemStack);
            entityItem.setToDefaultPickupDelay();
            world.spawnEntity(entityItem);
        }

        return true;
    }
}
