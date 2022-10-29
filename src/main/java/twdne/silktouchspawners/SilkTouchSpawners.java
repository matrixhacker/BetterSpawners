package twdne.silktouchspawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import twdne.silktouchspawners.event.EventHandler;

public class SilkTouchSpawners implements ModInitializer {

    public static final EventHandler eventHandler = new EventHandler();

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(eventHandler::onBlockBreak);
    }

}
