package challenge.plugin.EventListeners;

import challenge.plugin.ChallengeCreator;
import challenge.plugin.LobbyHelperMethods;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class PreventDroppingSpecialItemLobbyListener implements Listener {

    private final Plugin plugin;

    public PreventDroppingSpecialItemLobbyListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(event.getPlayer().getWorld().getName().equals("lobby")) {
            Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemDrop().getItemStack();
            plugin.getLogger().info("Item dropped by " + player.getName());
            if (LobbyHelperMethods.isSpecialItem(droppedItem, plugin)) {
                plugin.getLogger().info("Prevented " + player.getName() + " from dropping a special lobby item.");
                event.setCancelled(true);
            }
        }
    }

}
