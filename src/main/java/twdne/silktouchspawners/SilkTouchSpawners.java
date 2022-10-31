package twdne.silktouchspawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import twdne.silktouchspawners.event.EventHandler;

public class SilkTouchSpawners implements ModInitializer {

	private static final EventHandler eventHandler = new EventHandler();

	public static final PointOfInterestType SPAWNER = PointOfInterestHelper.register(new Identifier("spawner"), 1, 3, Blocks.SPAWNER);

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
		UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);
	}

}
