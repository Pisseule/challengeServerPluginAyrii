package challenge.plugin.EventListeners;
import challenge.plugin.ChallengeCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import static challenge.plugin.LobbyHelperMethods.givePlayerLobbyItems;

public class onPlayerJoinListener  implements Listener {
    private final JavaPlugin plugin;

    public onPlayerJoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) throws NoSuchFieldException, IllegalAccessException {
       if(event.getPlayer().getWorld().getName().equals("lobby")) {
           ChallengeCreator.setChallengeCreatorOpen(event.getPlayer(), plugin, false);
           if(!givePlayerLobbyItems(event.getPlayer(), plugin)){
               plugin.getLogger().severe("Failed to give lobby items to " + event.getPlayer().getName() + " =|>:ө(");
               return;
           }

           event.getPlayer().sendMessage("Are you a Schwigma >:ө\\ ?");
           event.getPlayer().sendMessage("Lets play a Game! =|:ө) <-- *(Game Master Schwein)*");

           //Bukkit.getConsoleSender().sendMessage("Test Console =|:ө)");
           //if(MultiverseCoreApi.isLoaded()){
           //    Collection<LoadedMultiverseWorld> worlds = MultiverseCoreApi.get().getWorldManager().getLoadedWorlds();
           //    for(LoadedMultiverseWorld world : worlds){
           //        plugin.getLogger().info("World found: " + world.getName() + " =|:ө)");

           //    }

           //}
           //else{
           //    plugin.getLogger().warning("Multiverse-Core plugin not loaded not very happy right now! =|>:ө(");
           //}
           //event.getPlayer().sendMessage("Are you a Schwigma >:ө\\ ?");
       }
   }


}
