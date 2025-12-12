package challenge.plugin.EventListeners;
import challenge.plugin.ChallengeServerPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static challenge.plugin.LobbyHelperMethods.givePlayerLobbyItems;

public class ChangeWorldToLobbyListener implements Listener {

    Plugin plugin;
    public ChangeWorldToLobbyListener(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld().getName().equalsIgnoreCase("lobby")) {
            player.sendMessage("§aWelcome to the Lobby!");
            if(givePlayerLobbyItems(player, plugin) == false){
                ChallengeServerPlugin.getPlugin(ChallengeServerPlugin.class).getLogger().severe("Failed to give lobby items to " + player.getName() + " =|>:ө(");
                return;
            }
        }
    }
}