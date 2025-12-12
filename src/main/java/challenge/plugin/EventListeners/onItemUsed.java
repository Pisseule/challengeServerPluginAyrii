package challenge.plugin.EventListeners;

import challenge.plugin.ChallengeCreator;
import challenge.plugin.LobbyHelperMethods;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.external.glassfish.hk2.utilities.EnableLookupExceptionsModule;

import static challenge.plugin.ChallengeCreator.openChallengeCreator;

public class onItemUsed implements Listener {
    private final JavaPlugin plugin;

    public onItemUsed(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getPlayer().getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
            if(event.hasBlock()){
                Block clickedBlock = event.getClickedBlock();
                plugin.getLogger().info(clickedBlock.getLocation().toString());
                if(clickedBlock.getLocation().equals(new Location(event.getPlayer().getWorld(), 37, 61, -33)) ||
                        clickedBlock.getLocation().equals(new Location(event.getPlayer().getWorld(), 37, 60, -33))){
                    event.setCancelled(false);
                    return;
                }
            }
            if(event.getItem() == null){
                return;
            }

            NamespacedKey key = new NamespacedKey(plugin, "white_monster_detection");
            ItemMeta meta = event.getItem().getItemMeta();

            if (meta != null && ChatColor.stripColor(meta.getDisplayName()).equals("white monster")) {
                    event.setCancelled(false);
                    return;
            }

            plugin.getLogger().info("interact");
            Player player = event.getPlayer();

            ItemStack item = player.getInventory().getItemInMainHand();
            if(ChallengeCreator.isChallengeCreatorOpen(player, plugin)){
                plugin.getLogger().info("challenge creator is open");
                return;
            }

            if (!(item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.PLAYER_HEAD || item.getType() == Material.ENDER_EYE || item.getType() == Material.RESPAWN_ANCHOR) || !item.hasItemMeta()) {
                return;
            }

            if(item != null && item.getType() == Material.WRITTEN_BOOK) {
                event.setCancelled(true);
            }
            LobbyHelperMethods.executeItem(player, item, plugin);

        }
    }

}
