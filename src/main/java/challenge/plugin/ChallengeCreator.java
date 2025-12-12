package challenge.plugin;

import challenge.plugin.modifier.ChallengeModifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class ChallengeCreator {

    public static Map<UUID, Challenge> playerChallenges = new HashMap<>();
    public  static List<ChallengeModifier> modifiers = new ArrayList<>();
    public static Challenge activeChallenge = null;

    public static void openChallengeCreator(Plugin plugin, Player player){
        NamespacedKey key = new NamespacedKey(plugin, "challenge_creator_opened");
        player.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

        Inventory inv = Bukkit.createInventory(null, 54, "Select Modifiers" );

        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL, 1) ;
        ItemMeta acceptItemMeta = acceptItem.getItemMeta();
        acceptItemMeta.setDisplayName("§2§l§nAccept");
        acceptItem.setItemMeta(acceptItemMeta);
        inv.setItem(53, acceptItem);

        ItemStack existItem =new ItemStack(Material.RED_WOOL, 1) ;
        ItemMeta existItemMeta = existItem.getItemMeta();
        existItemMeta.setDisplayName("§c§l§nExit");
        existItem.setItemMeta(existItemMeta);
        inv.setItem(45, existItem);
        for (int i = 0; modifiers.size() > i; i++) {
            inv.setItem(i, modifiers.get(i).getModifierItem(plugin));
        }
        player.openInventory(inv);

        setChallengeCreatorOpen(player, plugin, true);
        playerChallenges.put(player.getUniqueId(), new Challenge(player, plugin));

        player.sendMessage("Create a Challenge! =|:ө)");
    }
    public static boolean isChallengeCreatorOpen(Player player, Plugin plugin){
        NamespacedKey key = new NamespacedKey(plugin, "challenge_creator_opened");
        return player.getPersistentDataContainer().getOrDefault(key, PersistentDataType.BOOLEAN, false);
    }
    public static void setChallengeCreatorOpen(Player player, Plugin plugin, boolean isOpen){
        NamespacedKey key = new NamespacedKey(plugin, "challenge_creator_opened");
        player.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, isOpen);
    }
    public static boolean addModifier(Player player, ChallengeModifier modifier){
        Challenge challenge = playerChallenges.get(player.getUniqueId());
        if(!challenge.modifiers.contains(modifier)){
            challenge.modifiers.add(modifier);
            player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0f, 1.0f);
            return true;
        }
        return false;

    }
    public  static Boolean removeModifier(Player player, ChallengeModifier modifier){
        Challenge challenge = playerChallenges.get(player.getUniqueId());
        if(challenge.modifiers.remove(modifier)){
            player.playSound(player.getLocation(), "ui.button.click", 1.0f, 1.0f);
            return true;
        }
        return false;
    }
    public static void acceptChallenge(Plugin plugin, Player player){
        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "challenge_creator_opened"), PersistentDataType.BOOLEAN, false);
    }
    public static  Challenge getPlayerChallenge(Player player){
        return playerChallenges.get(player.getUniqueId());
    }
    public static boolean isPlayerInChallenge(Player player){
        return playerChallenges.containsKey(player.getUniqueId());
    }
    public static boolean startChallenge(Player player, Plugin plugin){
        Challenge challenge = playerChallenges.get(player.getUniqueId());
        if(challenge != null){
            List<Player> players = Bukkit.getWorld("lobby").getPlayers();
            if(players != null && players.size() > 0){
                challenge.participants.addAll(players);
            }


            challenge.startChallenge();
            activeChallenge = challenge;
            return true;
        }
        return false;
    }
}
