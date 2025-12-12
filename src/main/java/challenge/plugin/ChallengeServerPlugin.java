package challenge.plugin;

import challenge.plugin.EventListeners.*;
import challenge.plugin.modifier.SharedHealthModifier;
import challenge.plugin.modifier.TestModifier;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ChallengeServerPlugin extends JavaPlugin {

    // @Iljia Busch
    // DONT JUDGE MY CLASS STRUCTURE ist über zeit gewachsen
    // jeder Modifier hat so einen BiConsomer pro Bukkit event in einer HashMap und die challenge classe registriert sie automatisch und called die BiConsumer bei bukkit events nur wenn die welt teil der challenge sind =|:ө)
    // die challenge macht die welten automatisch
    // das challenge start event nimmt einfach alle leute in der lobby weil ich keinen bock hatte ein menu mit spielern zu machen

    @Override
    public void onEnable() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");
        if(plugin instanceof MultiverseCore){
            getLogger().info("Multiverse-Core plugin found! =|:ө)");
            if(MultiverseCoreApi.isLoaded()){
                getLogger().info("Loaded Multiverse-Core plugin! =|:ө)");
            }
            else{
                getLogger().info("Multiverse-Core plugin not yet loaded trying again! =|:ө|");
                MultiverseCoreApi.whenLoaded(api -> getLogger().info("Multiverse-Core API is now loaded! =|:ө) " + api.toString()));
            }
        }
        try{
           LuckPerms luckPerms = LuckPermsProvider.get();
        }catch (Exception e){
            getLogger().info("LuckPerms Not Loaded not very happy right now! =|>:ө(");
        }

        getServer().getPluginManager().registerEvents(new onPlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new onItemUsed(this), this);
        getServer().getPluginManager().registerEvents(new InventoryInteractionsLobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new ChangeWorldToLobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(this), this);
        getServer().getPluginManager().registerEvents(new ChallengeCreatorInventoreClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDroppingSpecialItemLobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDroppingSwapSpecialItemLobbyListener(this), this);

        ChallengeCreator.modifiers.add(TestModifier.getTestModifier(this));
        ChallengeCreator.modifiers.add(SharedHealthModifier.getSharedHealthModifier(this));

        getCommand("lobby").setExecutor(new LobbyCommand(this));
        getLogger().info("Enabled! =|:ө)");
    }
    @Override
    public void onDisable() {
        getLogger().info("Disabled??!! =|:ө(");
    }
}
