package challenge.plugin.EventListeners;

import challenge.plugin.ChallengeCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

public class InventoryCloseListener implements Listener {
    private final Plugin plugin;
    public InventoryCloseListener(Plugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
            plugin.getLogger().info(event.getPlayer().getName() + " inventorychallengecreatoropenen:" + ChallengeCreator.isChallengeCreatorOpen((org.bukkit.entity.Player) event.getPlayer(), plugin));
            plugin.getLogger().info("Inventory closed");
            if (ChallengeCreator.isChallengeCreatorOpen((org.bukkit.entity.Player) event.getPlayer(), plugin)) {
                plugin.getLogger().info(event.getPlayer().getName() + " inventorychallengecreatoropenen:" + ChallengeCreator.isChallengeCreatorOpen((org.bukkit.entity.Player) event.getPlayer(), plugin));
                plugin.getLogger().info("Closing Challenge Creator");
                ChallengeCreator.setChallengeCreatorOpen((org.bukkit.entity.Player) event.getPlayer(), plugin, false);
            }
    }
}
