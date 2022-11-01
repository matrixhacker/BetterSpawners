package twdne.betterspawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import twdne.betterspawners.block.BetterSpawnerBlock;
import twdne.betterspawners.event.EventHandler;

public class BetterSpawners implements ModInitializer {

	private static final EventHandler eventHandler = new EventHandler();

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
		UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);
		BetterSpawnerBlock.init();
	}
}
