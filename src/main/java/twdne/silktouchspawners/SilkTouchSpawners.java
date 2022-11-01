package twdne.silktouchspawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import twdne.silktouchspawners.block.BetterSpawnerBlock;
import twdne.silktouchspawners.event.EventHandler;

public class SilkTouchSpawners implements ModInitializer {

	private static final EventHandler eventHandler = new EventHandler();

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
		UseBlockCallback.EVENT.register(eventHandler::onBlockInteract);
		BetterSpawnerBlock.init();
	}
}
