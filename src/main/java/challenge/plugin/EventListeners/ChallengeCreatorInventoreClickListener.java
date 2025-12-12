package challenge.plugin.EventListeners;

import challenge.plugin.ChallengeCreator;
import challenge.plugin.modifier.ChallengeModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.N;

public class ChallengeCreatorInventoreClickListener implements Listener {

    private final JavaPlugin plugin;

    public ChallengeCreatorInventoreClickListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == event.getView().getTopInventory() && ChallengeCreator.isChallengeCreatorOpen((Player) event.getWhoClicked(), plugin)){
            Player player = (Player) event.getWhoClicked();
            plugin.getLogger().info("Clicked in Challenge Creator");
            plugin.getLogger().info(ChallengeCreator.playerChallenges.get((Player) event.getWhoClicked()) + "");
            ItemStack item = event.getCurrentItem();
            plugin.getLogger().info("diggigigigiggigiigi");
            plugin.getLogger().info(item.getType().toString());
            plugin.getLogger().info(item.getItemMeta().getDisplayName());
            if (event.getCurrentItem() == null) {
                return;
            }
            plugin.getLogger().info(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
            if(ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Exit") && item.getType().equals(Material.RED_WOOL)){
                ChallengeCreator.setChallengeCreatorOpen(player, plugin, false);
                ChallengeCreator.playerChallenges.put(player.getUniqueId(), null);
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                return;
            }
            if(ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Accept") && item.getType().equals(Material.GREEN_WOOL)){
                ChallengeCreator.acceptChallenge(plugin,player);
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                return;
            }
            else{
                ItemMeta meta = item.getItemMeta();

                var modifiers = ChallengeCreator.getPlayerChallenge(player).modifiers;
                plugin.getLogger().info(String.valueOf(modifiers.size()));
                for(ChallengeModifier modifier : modifiers){
                    String itemModifiername = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "modifier_name"), PersistentDataType.STRING);
                    plugin.getLogger().info(itemModifiername);
                    plugin.getLogger().info(modifier.toString());
                    if(itemModifiername.equals(modifier.name)){
                        ChallengeCreator.removeModifier(player, modifier);
                        meta.setDisplayName(ChatColor.WHITE + ChatColor.stripColor(meta.getDisplayName()));
                        item.setItemMeta(meta);
                        item.removeEnchantment(Enchantment.MENDING);
                        event.setCancelled(true);
                        return;
                    }
                }

                meta.addEnchant(Enchantment.MENDING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.setDisplayName(ChatColor.GREEN + ChatColor.stripColor(meta.getDisplayName()));
                item.setItemMeta(meta);

                ChallengeModifier modifier = ChallengeModifier.getModifierByName(meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "modifier_name"), PersistentDataType.STRING));
                plugin.getLogger().info("Adding modifier: " + modifier.name);
                ChallengeCreator.addModifier(player, modifier);

                event.setCancelled(true);
                return;
            }
        }
    }
}
