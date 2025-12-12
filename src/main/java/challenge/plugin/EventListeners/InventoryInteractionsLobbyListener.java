package challenge.plugin.EventListeners;

import challenge.plugin.ChallengeCreator;
import challenge.plugin.LobbyHelperMethods;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public class InventoryInteractionsLobbyListener implements Listener {

    private final JavaPlugin plugin;

    public InventoryInteractionsLobbyListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryInteractionLobby(InventoryClickEvent event) {
        if (event.getWhoClicked().getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                LobbyHelperMethods.executeItem((Player) event.getWhoClicked(), event.getCurrentItem(), plugin);
            }
        }
    }
    public boolean joinSmp(){
        return true;
    }
    public boolean rejoinChallenge(){
        return true;
    }
}
