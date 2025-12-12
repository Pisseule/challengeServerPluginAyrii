package challenge.plugin.EventListeners;

import challenge.plugin.LobbyHelperMethods;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;

public class PreventDroppingSwapSpecialItemLobbyListener implements Listener {
    private final Plugin plugin;

    public PreventDroppingSwapSpecialItemLobbyListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if(event.getPlayer().getWorld().getName().equals("lobby")) {
            if (LobbyHelperMethods.isSpecialItem(event.getPlayer().getInventory().getItemInMainHand(), this.plugin) ||
                LobbyHelperMethods.isSpecialItem(event.getPlayer().getInventory().getItemInMainHand(), this.plugin)){
                plugin.getLogger().info("Prevented swapping special lobby item for player " + event.getPlayer().getName());
                event.setCancelled(true);
            }
        }
    }
}
