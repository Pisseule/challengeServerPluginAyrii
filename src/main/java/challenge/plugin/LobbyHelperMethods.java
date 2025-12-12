package challenge.plugin;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static challenge.plugin.ChallengeCreator.activeChallenge;
import static challenge.plugin.ChallengeCreator.openChallengeCreator;

public class LobbyHelperMethods {

    public static boolean givePlayerLobbyItems(Player player, Plugin plugin){
        plugin.getLogger().info("Giving lobby items to " + player.getName() + " =|:ө)");
        PlayerInventory inventory = player.getInventory();

        inventory.clear();

        plugin.getLogger().info("Trying to create challenge Creator item");
        try{
            inventory.setItem(8, getChallengeCreatorItem(plugin));
        }
        catch (Exception e){
            plugin.getLogger().severe("Failed to create Challenge Creator item: " + e.getMessage());
            return false;
        }
        plugin.getLogger().info("Trying to create globe skull");
        try {
            inventory.setItem(4, getJoinSmpItem(plugin));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create SMP item: " + e.getMessage());
            return false;
        }

        try {
            inventory.setItem(0, getRejoinChallengeItem(plugin));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create Challenge rejoin item: " + e.getMessage());
            return false;
        }
        try{
            inventory.setItem(1, getStartChallengeItem(plugin));
        }
        catch (Exception e){
            plugin.getLogger().severe("Failed to create Challenge Creator item: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean isSpecialItem(ItemStack item, Plugin plugin){
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(new NamespacedKey(plugin, "challenge_creator"), PersistentDataType.STRING)) {
            plugin.getLogger().info("found challenge creator item");
            return true;
        }
        if (pdc.has(new NamespacedKey(plugin, "rejoin_challenge"), PersistentDataType.STRING)) {
            plugin.getLogger().info("found rejoin challenge item");
            return true;
        }
        if (pdc.has(new NamespacedKey(plugin, "join_smp"), PersistentDataType.STRING)) {
            plugin.getLogger().info("found join smp item");
            return true;
        }
        if(pdc.has(new NamespacedKey(plugin, "start_challenge"), PersistentDataType.STRING)){
            plugin.getLogger().info("found start challenge item");
            return true;
        }
        return false;
    }
    //[ChatGPT]\\
    public static ItemStack getChallengeCreatorItem(Plugin plugin){
        // Create the item stack
        ItemStack challengeBook = new ItemStack(Material.WRITABLE_BOOK, 1);

// Get the ItemMeta to edit item properties
        ItemMeta meta = challengeBook.getItemMeta();

// Set custom display name
        meta.setDisplayName("§6§l§nChallenge Creator");

        meta.addEnchant(Enchantment.MENDING, 1, true); // Dummy enchant for glint
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);          // Hide enchant tooltip

// Add custom persistent data
        NamespacedKey key = new NamespacedKey(plugin, "challenge_creator");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");

// Set the updated meta back to the ItemStack
        challengeBook.setItemMeta(meta);
        return challengeBook;
    }
    public static ItemStack getJoinSmpItem(Plugin plugin) throws MalformedURLException {
        UUID uuid = UUID.nameUUIDFromBytes("CustomHead".getBytes());  // Consistent UUID
        PlayerProfile profile = Bukkit.createPlayerProfile(uuid, "Player");  // Add name


        PlayerTextures textures = profile.getTextures();

        textures.setSkin(new URL("http://textures.minecraft.net/texture/b0aca013178a9f47913e894d3d0bfd4b0b66120825b9aab8a4d7d9bf0245abf"), PlayerTextures.SkinModel.CLASSIC);
        profile.setTextures(textures);
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile);

        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        NamespacedKey key = new NamespacedKey(plugin, "join_smp");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");

        meta.setDisplayName("§3§l§nJoin SMP");


        head.setItemMeta(meta);
        return head;
    }
    public static ItemStack getRejoinChallengeItem(Plugin plugin){
        // Create the item stack
        ItemStack challengeBook = new ItemStack(Material.RESPAWN_ANCHOR, 1);

// Get the ItemMeta to edit item properties
        ItemMeta meta = challengeBook.getItemMeta();

// Set custom display name
        meta.setDisplayName("§c§l§nRejoin Challenge");

        meta.addEnchant(Enchantment.MENDING, 1, true); // Dummy enchant for glint
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);          // Hide enchant tooltip

// Add custom persistent data
        NamespacedKey key = new NamespacedKey(plugin, "rejoin_challenge");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");

// Set the updated meta back to the ItemStack
        challengeBook.setItemMeta(meta);
        return challengeBook;
    }
    public static ItemStack getStartChallengeItem(Plugin plugin){
       ItemStack startChallengeItem = new ItemStack(Material.ENDER_EYE, 1);

         ItemMeta meta = startChallengeItem.getItemMeta();

         meta.setDisplayName("§a§l§nStart Challenge");

         NamespacedKey key = new NamespacedKey(plugin, "start_challenge");
         meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");

         startChallengeItem.setItemMeta(meta);
         return  startChallengeItem;
    }
    static boolean startingChallenge = false;
    public static boolean executeItem(Player player, ItemStack item, Plugin plugin){

        plugin.getLogger().info("after challenge item check");
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        plugin.getLogger().info(pdc.toString());
        if (pdc.has(new NamespacedKey(plugin, "challenge_creator"), PersistentDataType.STRING)) {
                player.playSound(player.getLocation(), "item.book.page_turn", 1.0f, 1.0f);
                openChallengeCreator(plugin, player);
                return true;
        }
        if (pdc.has(new NamespacedKey(plugin, "rejoin_challenge"), PersistentDataType.STRING)) {
            plugin.getLogger().info("rejoin challenge");
            player.sendMessage("Trying to rejoin challenge =|:ө) ...");
                activeChallenge.participants.remove(player);
                activeChallenge.participants.add(player);
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(activeChallenge.playerLogoutLocations.get(player.getUniqueId().toString()));

            return true;
        }
        else{
            plugin.getLogger().info("rejoin challenge item not found");
        }
        if (pdc.has(new NamespacedKey(plugin, "join_smp"), PersistentDataType.STRING)) {

            plugin.getLogger().info("join smp");
            player.sendMessage("Joining SMP =|:ө) ...");
            return true;
        }
        else{
            plugin.getLogger().info("smp item not found");
        }
        if(pdc.has(new NamespacedKey(plugin, "start_challenge"), PersistentDataType.STRING)){
            if(player.hasPermission("challenge.creation")) {
                    if (ChallengeCreator.getPlayerChallenge(player) == null) {
                        player.sendMessage("You have no challenge to start! Create One! =|:ө)");
                        return true;
                    } else {
                        if (startingChallenge) {
                            player.sendMessage("Chill du kleiner go wir sind schon am starten!! =|>:ө(");
                            return true;
                        }
                        startingChallenge = true;
                        if (activeChallenge != null && activeChallenge.running == true) {
                            player.sendMessage("Failed to start challenge! =|:ө(");
                            player.sendMessage("Another Challenge is running! =|:ө(");
                            return true;
                        }
                        if (ChallengeCreator.startChallenge(player, plugin)) {
                            player.sendTitle("Challenge Started!", "", 10, 70, 20);
                        } else {
                            player.sendMessage("Failed to start challenge! =|:ө(");
                            player.sendMessage("Something went wrong! =|:ө(");
                        }
                        startingChallenge = false;
                    }
                    return true;
            }
            else{
                player.sendMessage(ChatColor.RED + "You dont have permission to start a Challenge! =|:ө(");
                return true;
            }
        }
        else{
            plugin.getLogger().info("no special lobby item");
        }
        return false;

    }
}
