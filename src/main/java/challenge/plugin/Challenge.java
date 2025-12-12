package challenge.plugin;

import challenge.plugin.modifier.ChallengeModifier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.inventory.meta.ItemMeta;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.LoadWorldOptions;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Handler;
import java.util.stream.Collectors;

import static challenge.plugin.ChallengeCreator.activeChallenge;
import static challenge.plugin.ChallengeCreator.playerChallenges;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class Challenge {
    public List<ChallengeModifier> modifiers;
    public List<Player> participants;
    public Map<String, Location> playerLogoutLocations = new HashMap<>();
    public Listener challengeListener = new Listener() {};
    public Player creator;
    public String timerString;
    public Timestamp startTime;
    public boolean running;
    public World challengeOverworld;
    public World challengeNether;
    public World challengeEnd;
    private BukkitTask timerTask;
    public UUID challengeId = UUID.randomUUID();
    private BukkitTask checkIftpTask = null;
    private Plugin plugin;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());


    private static class ModifierEventExecutor implements EventExecutor {
        private final Plugin plugin;  // Assuming your main class
        private final ChallengeModifier modifier;
        private final Challenge challenge;

        public ModifierEventExecutor(Plugin plugin, ChallengeModifier modifier, Challenge challenge) {
            this.plugin = plugin;
            this.modifier = modifier;
            this.challenge = challenge;
        }

        @Override
        public void execute(@NotNull Listener listener, Event event) {
            if (challenge.executeEvent(event)) {
                plugin.getLogger().info("Challenge/ModifieerEventExecuter executing " + event.getEventName() + ": " + event.hashCode());
                for(Class<? extends Event> event1 : modifier.events.keySet()) {
                }
                BiConsumer<Event, Pair<Plugin, Challenge>> biCOnsumer = (BiConsumer<Event, Pair<Plugin, Challenge>>) modifier.events.get(event.getClass());
                if(biCOnsumer == null){
                    return;
                }
                biCOnsumer.accept(event, Pair.of(plugin, challenge));
            }
        }
    }

    private static class PlayerDeatEventExecutor implements EventExecutor {
        private final Plugin plugin;  // Assuming your main class
        private final Challenge challenge;

        public PlayerDeatEventExecutor(Plugin plugin, Challenge challenge) {
            this.plugin = plugin;
            this.challenge = challenge;
        }

        @Override
        public void execute(@NotNull Listener listener, Event event) {
            if(event instanceof EntityDamageEvent && ((EntityDamageEvent) event).getEntity()instanceof Player) {
                EntityDamageEvent entityDamageEvent = (EntityDamageEvent)event;
                Player player = (Player)entityDamageEvent.getEntity();
                plugin.getLogger().info("CHALLENGE EVENT : Player " + player.getName() + " took damage: " + entityDamageEvent.getFinalDamage());
                if(player.getHealth() < entityDamageEvent.getFinalDamage()){
                    if(challenge.participants.contains(player) && player.getWorld().getName().contains(challenge.challengeId.toString())) {
                        ((EntityDamageEvent) event).setCancelled(true);
                        for (Player participant : challenge.participants) {
                            participant.sendTitle("§cChallenge Over!", "§7" + player.getName() + " muss es an den Nagel hängen.");
                            participant.setHealth(20.0);
                            participant.setGameMode(org.bukkit.GameMode.SPECTATOR);
                            participant.getInventory().setItem(8, challenge.getSpectatorLobbyTeleporterItem());

                            TextComponent message = new TextComponent("§6Click to teleport to §elobby");
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/lobby"));
                            participant.spigot().sendMessage(message);
                        }
                        challenge.endChallenge();
                    }
                }
            }
        }
    }

    private  static class OnPlayerLogout implements EventExecutor {
        private final Plugin plugin;
        private final Challenge challenge;

        private OnPlayerLogout(Plugin plugin, Challenge challenge) {
            this.plugin = plugin;
            this.challenge = challenge;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            if(event instanceof PlayerQuitEvent) {
                PlayerQuitEvent playerQuitEvent = (PlayerQuitEvent) event;
                plugin.getLogger().info(playerQuitEvent.getPlayer().getUniqueId().toString());
                challenge.playerLogoutLocations.put(playerQuitEvent.getPlayer().getUniqueId().toString(), playerQuitEvent.getPlayer().getLocation());
            }
        }
    }

    private void setTask(BukkitTask task){
        timerTask = task;
    }
    public Challenge(Player creator, Plugin plugin) {
        this.creator = creator;
        this.plugin = plugin;
        this.modifiers = new ArrayList<>();
        this.participants = new ArrayList<>();
    }


    public void startChallenge() {
        for(Player player : participants){
            player.sendTitle("§aStarting Challenge!", "");
        }

        this.running = true;

        World lobby = Bukkit.getWorld("lobby");
        if(lobby == null){
            plugin.getLogger().severe("Lobby world not found!");
            return;
        }
        for(ChallengeModifier modifier : modifiers){
            plugin.getLogger().info(modifier.name + "|" + String.valueOf(modifier.events.size()));
            for(Class<? extends Event> event : modifier.events.keySet()){
                plugin.getLogger().info("registering Event: " + event.toString());
                getServer().getPluginManager().registerEvent(
                        event,
                        challengeListener,
                        EventPriority.NORMAL,
                        new ModifierEventExecutor(plugin, modifier, this),
                        plugin
                );
            }
        }
        getServer().getPluginManager().registerEvent(
                EntityDamageEvent.class,
                challengeListener,
                EventPriority.HIGHEST,
                new PlayerDeatEventExecutor(plugin, this),
                plugin
        );
        getServer().getPluginManager().registerEvent(
                PlayerQuitEvent.class,
                challengeListener,
                EventPriority.HIGHEST,
                new OnPlayerLogout(plugin, this),
                plugin
        );
        for(Player player : participants){
            player.sendTitle("§aGenerating Worlds!", "");
        }
        plugin.getLogger().info("Starting challenge with " + participants.size() + " participants.");


        if(MultiverseCoreApi.isLoaded()){
            challengeOverworld = createWorld(getChallengeWorldOptionsOverworld());
            challengeOverworld.setDifficulty(Difficulty.HARD);

            if(challengeOverworld == null){
                plugin.getLogger().severe("Failed to create challenge overworld!");
                return;
            }

            challengeNether = createWorld(getChallengeWorldOptionsNether());
            challengeNether.setDifficulty(Difficulty.HARD);

            if(challengeNether == null){
                plugin.getLogger().severe("Failed to create challenge nether!");
                return;
            }

            challengeEnd = createWorld(getChallengeWorldOptionsEnd());
            challengeEnd.setDifficulty(Difficulty.HARD);

            plugin.getLogger().info("-----------------------ChallengeinvshareGroupCOmmands---------------------");
            plugin.getLogger().info("Running Command: " + "mvinv create-group challenge" + challengeId.toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvinv create-group challenge" + challengeId.toString());

            plugin.getLogger().info("Running Command: " + "mvinv add-worlds challenge" + challengeId.toString() + " " + challengeOverworld.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvinv add-worlds challenge" + challengeId.toString() + " " + challengeOverworld.getName());

            plugin.getLogger().info("Running Command: " + "mvinv add-worlds challenge" + challengeId.toString() + " " + challengeNether.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvinv add-worlds challenge" + challengeId.toString() + " " + challengeNether.getName());

            plugin.getLogger().info("Running Command: " +"mvinv add-worlds challenge" + challengeId.toString() + " " + challengeEnd.getName() );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvinv add-worlds challenge" + challengeId.toString() + " " + challengeEnd.getName());

            plugin.getLogger().info("Running Command: " + "mvinv add-shares challenge" + challengeId.toString() + " *");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvinv add-shares challenge" + challengeId.toString() + " *");
            plugin.getLogger().info("--------------------------------------------");


            if(challengeEnd == null){
                plugin.getLogger().severe("Failed to create challenge end!");
                return;
            }

            Location spawn = challengeOverworld.getSpawnLocation();
            int spawnChunkX = spawn.getBlockX() >> 4;  // Convert to chunk coords
            int spawnChunkZ = spawn.getBlockZ() >> 4;

            // Force load 5x5 chunk area around spawn (adjust radius as needed)
            for (int x = spawnChunkX - 1; x <= spawnChunkX + 1; x++) {
                for (int z = spawnChunkZ - 1; z <= spawnChunkZ + 1; z++) {
                    challengeOverworld.loadChunk(x, z, true);
                }
            }

            checkIftpTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (challengeOverworld.isChunkLoaded(spawnChunkX, spawnChunkZ)) {
                    plugin.getLogger().info("Challenge overworld loaded!");

                    String modifiersString = modifiers.stream()
                            .map(m -> m.name)
                            .collect(Collectors.joining(", "));

                    for (Player player : participants) {
                        if (player.isOnline()) { // Safety check
                            plugin.getLogger().info("Teleporting Participant: " + player.getName());
                            player.teleport(challengeOverworld.getSpawnLocation());
                            player.setGameMode(GameMode.SURVIVAL);
                            player.sendTitle("Challenge Started", "Modifiers: " + modifiersString);
                            player.sendMessage("Challenge started");
                            player.sendMessage("Active modifiers: " + modifiersString);
                        }
                    }
                    checkIftpTask.cancel();
                }
            }, 40L, 20L);
        }
        else{
            plugin.getLogger().severe("Multiverse-Core plugin not loaded not very happy right now! =|>:ө(");
            plugin.getLogger().severe("Cannot start challenge without Multiverse-Core! Abborting! =|>:ө(");
            return;
        }


        this.startTime = new Timestamp(System.currentTimeMillis());


        setTask(new BukkitRunnable() {
            @Override
            public void run() {
                Instant instant = Instant.ofEpochMilli(System.currentTimeMillis() - startTime.getTime());
                timerString = formatter.format(instant);
                for(Player player : participants) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§f" + timerString));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L));

    }


    private World createWorld(CreateWorldOptions options){
        try {
            var worldManager = MultiverseCoreApi.get().getWorldManager();
            var worldAttempt = worldManager.createWorld(options);
            if (!worldAttempt.isSuccess()) {
                plugin.getLogger().severe("Failed to create challenge World :" + options.worldName() + " Because: " + worldAttempt.getFailureMessage());
                return null;
            }
            var createdWorld = worldAttempt.get();

            if (!worldManager.isLoadedWorld(createdWorld)) {
                plugin.getLogger().info("challenge world is not loaded for name: " + createdWorld.getName());
            }
            var bukkitWorld = createdWorld.getBukkitWorld();
            if (bukkitWorld.isEmpty()) {
                plugin.getLogger().severe("Challenge World was created but Bukkit world is not available!");
                return null;
            }
            plugin.getLogger().info(bukkitWorld.get().toString());
            return bukkitWorld.get();
        }
        catch (Exception e){
            plugin.getLogger().info(e.getMessage());
        }
        return null;
    }
    private CreateWorldOptions getChallengeWorldOptionsOverworld(){
        return CreateWorldOptions.worldName(challengeId + "_challenge")
                .worldType(WorldType.NORMAL)
                .environment(World.Environment.NORMAL)
                .useSpawnAdjust(true);
    }
    private CreateWorldOptions getChallengeWorldOptionsNether(){
        return CreateWorldOptions.worldName(challengeId + "_challenge_nether")
                .worldType(WorldType.NORMAL)
                .environment(World.Environment.NETHER)
                .useSpawnAdjust(true);
    }
    private CreateWorldOptions getChallengeWorldOptionsEnd(){
        return CreateWorldOptions.worldName(challengeId + "_challenge_the_end")
                .worldType(WorldType.NORMAL)
                .environment(World.Environment.THE_END)
                .useSpawnAdjust(true);
    }

    public void endChallenge() {
        running = false;
        activeChallenge = null;
        if(timerTask != null){
            timerTask.cancel();
        }
        HandlerList.unregisterAll(challengeListener);
        playerChallenges.remove(creator.getUniqueId());
        this.modifiers.clear();
        this.participants.clear();
        playerLogoutLocations.clear();
        getServer().getPluginManager().registerEvent(
                PlayerChangedWorldEvent.class,
                new Listener() {},
                EventPriority.NORMAL,
                (listener, event) -> {
                    if(event instanceof PlayerChangedWorldEvent){
                        World world = ((PlayerChangedWorldEvent) event).getFrom();
                        World to = ((PlayerChangedWorldEvent) event).getPlayer().getWorld();
                        if(to.getName().equals("lobby") && world.getName().contains(challengeId.toString())){
                            boolean deleteWorlds = true;
                            for(World world1 : Bukkit.getWorlds()){
                                if(world1.getName().contains(challengeId.toString())){
                                    if(!world1.getPlayers().isEmpty()){
                                       deleteWorlds = false;
                                    }
                                }
                            }
                            if(deleteWorlds){
                                WorldManager worldManager = MultiverseCoreApi.get().getWorldManager();

                                var overworldOption =worldManager.getWorld(challengeOverworld);
                                MultiverseWorld overworld = null;
                                if(!overworldOption.isEmpty()){
                                   overworld = overworldOption.get();
                                }
                                worldManager.deleteWorld(DeleteWorldOptions.world(overworld));


                                var netherOtions =worldManager.getWorld(challengeNether);
                                MultiverseWorld nether = null;
                                if(!netherOtions.isEmpty()){
                                    nether = netherOtions.get();
                                }
                                worldManager.deleteWorld(DeleteWorldOptions.world(nether));


                                var endOptions =worldManager.getWorld(challengeEnd);
                                MultiverseWorld end = null;
                                if(!endOptions.isEmpty()){
                                    end = endOptions.get();
                                }
                                worldManager.deleteWorld(DeleteWorldOptions.world(end));

                                HandlerList.unregisterAll(listener);
                            }
                        }
                    }
                },
                plugin
        );
    }
    public boolean executeEvent(Event event){
       if(event instanceof BlockEvent) {
           plugin.getLogger().info(((BlockEvent)event).getBlock().getWorld().getName() + "|" + this.challengeId.toString()+ " instece of BlockEvent");
           if(((BlockEvent)event).getBlock().getWorld().getName().contains(this.challengeId.toString())){
              return true;
           }
           else{
               return  false;
           }
       }
       if(event instanceof PlayerEvent){
           plugin.getLogger().info(((PlayerEvent)event).getPlayer().getWorld().getName() + "|" + this.challengeId.toString()+ " instece of PlayerEvent");
           if(((PlayerEvent) event).getPlayer().getWorld().getName().contains(this.challengeId.toString())) {
               plugin.getLogger().info(((PlayerEvent)event).getPlayer().getWorld().getName() + "|" + this.challengeId.toString()+ " instece of PlayerEvent");
              return true;
          }
          else{
              return  false;
          }
       }
       if (event instanceof EntityEvent){
           plugin.getLogger().info(((EntityEvent)event).getEntity().getWorld().getName() + "|" + this.challengeId.toString()+ " instece of EntityEvent");
           if(((EntityEvent) event).getEntity().getWorld().getName().contains(this.challengeId.toString())) {
               plugin.getLogger().info(((EntityEvent)event).getEntity().getWorld().getName() + "|" + this.challengeId.toString()+ " instece of EntityEvent");
               return true;
           }
           else{
               return  false;
           }

       }
       return false;
    }
    private ItemStack getSpectatorLobbyTeleporterItem(){
        ItemStack item = new ItemStack(Material.COMPASS); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Spectator Teleporter");
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "spectator_lobby_teleporter"), PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return new ItemStack(org.bukkit.Material.COMPASS, 1);
    }
}
