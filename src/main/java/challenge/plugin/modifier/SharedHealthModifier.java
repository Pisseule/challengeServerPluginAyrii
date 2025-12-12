package challenge.plugin.modifier;

import challenge.plugin.Challenge;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;

public class SharedHealthModifier {
    public static class onPlayerDamageEvent implements BiConsumer<EntityDamageEvent, Pair<Plugin, Challenge>> {
        private boolean recentlydamage = false;
        @Override
        public void accept(EntityDamageEvent event, Pair<Plugin, Challenge>  pluginChallengePair ) {
            pluginChallengePair.getLeft().getLogger().info("SharedHealthModifier/onPlayerDamageEvent: recentlydamage value: " + String.valueOf(recentlydamage));
            if(event.getEntity() instanceof org.bukkit.entity.Player && event.isApplicable(EntityDamageEvent.DamageModifier.ABSORPTION) && !recentlydamage){

                recentlydamage = true;
                Bukkit.getScheduler().runTaskLater(pluginChallengePair.getLeft(), () -> {
                    recentlydamage = false;
                }, 8L);
                syncHealthOnDamage(pluginChallengePair.getLeft(), pluginChallengePair.getRight(), event);
            }
        }
    }

    public static class onDamagedByEntity implements BiConsumer<EntityDamageByEntityEvent, Pair<Plugin, Challenge>> {
        private boolean recentlydamage = false;
        @Override
        public void accept(EntityDamageByEntityEvent event, Pair<Plugin, Challenge>  pluginChallengePair ) {
            pluginChallengePair.getLeft().getLogger().info("SharedHealthModifier/onDamagedByEntity: recentlydamage value: " + String.valueOf(recentlydamage));
            if(event.getEntity() instanceof org.bukkit.entity.Player && event.isApplicable(EntityDamageEvent.DamageModifier.ABSORPTION) && !recentlydamage){
                Player player = ((Player) event.getEntity()).getPlayer();
                recentlydamage = true;
                Bukkit.getScheduler().runTaskLater(pluginChallengePair.getLeft(), () -> {
                    recentlydamage = false;
                }, 8L);
                syncHealthOnDamage(pluginChallengePair.getLeft(), pluginChallengePair.getRight(), event);
            }
        }
    }
    public static class onDmageByBlock implements BiConsumer<EntityDamageByBlockEvent, Pair<Plugin, Challenge>> {
        private boolean recentlyDamaged = false;
        @Override
        public void accept(EntityDamageByBlockEvent event, Pair<Plugin, Challenge> pluginChallengePair) {
            if(event.getEntity() instanceof org.bukkit.entity.Player && event.isApplicable(EntityDamageEvent.DamageModifier.ABSORPTION) && !recentlyDamaged){
                recentlyDamaged = true;
                Bukkit.getScheduler().runTaskLater(pluginChallengePair.getLeft(), () -> {
                    recentlyDamaged = false;
                }, 8L);
                syncHealthOnDamage(pluginChallengePair.getLeft(), pluginChallengePair.getRight(), event);
            }
        }
    }
    public static class onPlayerHeal implements BiConsumer<EntityRegainHealthEvent ,Pair<Plugin, Challenge>> {
        private boolean recentlyhealed = false;

        @Override
        public void accept(EntityRegainHealthEvent event, Pair<Plugin, Challenge> pluginChallengePair) {
            pluginChallengePair.getLeft().getLogger().info("Player Heal");
            if(event.getEntity() instanceof org.bukkit.entity.Player && recentlyhealed == false){
                recentlyhealed = true;
                Player player = (org.bukkit.entity.Player) event.getEntity();
                double amountHealed = event.getAmount();
                pluginChallengePair.getLeft().getLogger().info("Healing " + player.getName() + " for " + amountHealed);
                for(Player participant : pluginChallengePair.getRight().participants){
                    if(participant != player) {
                        participant.setHealth(Math.min(20.0, player.getHealth() + amountHealed));
                    }
                }
                Bukkit.getScheduler().runTaskLater(pluginChallengePair.getLeft(), () -> {
                    recentlyhealed = false;
                }, 20L);
            }
        }
    }
    public static class onNewEffect implements BiConsumer<EntityPotionEffectEvent, Pair<Plugin, Challenge>> {
        private boolean addEventRunning = false;
        @Override
        public void accept(EntityPotionEffectEvent event, Pair<Plugin, Challenge> pluginChallengePair) {
            if(addEventRunning) {
                return;
            }
            if(event.getNewEffect() == null) {
                return;
            }
            addEventRunning = true;
            pluginChallengePair.getLeft().getLogger().info("sharedHealth/onNewEffect: running event:" + event.hashCode());
            if(event.getEntity() instanceof org.bukkit.entity.Player &&
                    event.getNewEffect().getType() == PotionEffectType.ABSORPTION &&
                    (event.getAction() == EntityPotionEffectEvent.Action.ADDED)){
                PotionEffect newEffect = event.getNewEffect();
                pluginChallengePair.getLeft().getLogger().info("sharedHealth/onNewEffect: newEffect:" + newEffect.getType());
                Player player = (org.bukkit.entity.Player) event.getEntity();

                //wait onetick for the hearts and so type shit
                Bukkit.getScheduler().runTaskLater(pluginChallengePair.getLeft(), ()->{
                    for (Player participant : pluginChallengePair.getRight().participants) {
                        pluginChallengePair.getLeft().getLogger().info("NEW ABSORBTION FOR PLAYER "+ participant.getName());
                        if(participant != event.getEntity() && newEffect != null) {
                            PotionEffect cloneEffect = new PotionEffect(newEffect.getType(), newEffect.getDuration(), newEffect.getAmplifier());
                            participant.addPotionEffect(newEffect);
                            participant.setAbsorptionAmount(player.getAbsorptionAmount());
                        }
                    }
                }, 1L);
            }
            addEventRunning = false;
        }
    }
    public static ChallengeModifier getSharedHealthModifier(Plugin plugin){
        ChallengeModifier sharedHealthModifier = new ChallengeModifier() {
            {
                name = "Shared Health";
            }
            @Override
            public ItemStack getModifierItem(Plugin plugin) {
                ItemStack itemStack = new ItemStack(Material.SHEARS, 1);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + this.name);

                NamespacedKey key = new NamespacedKey(plugin, "modifier_name");
                meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, this.name);
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        };

        sharedHealthModifier.events.put(EntityDamageEvent.class, new onPlayerDamageEvent());
        sharedHealthModifier.events.put(EntityRegainHealthEvent.class, new onPlayerHeal());
        sharedHealthModifier.events.put(EntityDamageByEntityEvent.class, new onDamagedByEntity());
        sharedHealthModifier.events.put(EntityDamageByBlockEvent.class, new onDmageByBlock());
        sharedHealthModifier.events.put(EntityPotionEffectEvent.class, new onNewEffect());
        return sharedHealthModifier;
    }


    private static void syncHealthOnDamage(Plugin plugin, Challenge challenge, EntityDamageEvent event){
        plugin.getLogger().info("-------------------------syncHealt-------------------------");
        plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: running " +  event.hashCode());
        Player damagedPlayer = (org.bukkit.entity.Player) event.getEntity();
        double damage = event.getFinalDamage();
        double preAbsorption = 0;
        for (EntityDamageEvent.DamageModifier mod : EntityDamageEvent.DamageModifier.values()) {
            if (mod != EntityDamageEvent.DamageModifier.ABSORPTION) {
                plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: damage " + mod + " " + event.getDamage(mod));
                preAbsorption += event.getDamage(mod);
            }
        }

        plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: PREABSORBTION " + preAbsorption);

        plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: finaldamage value: " +  event.getDamage());
        for(Player player : challenge.participants){
            plugin.getLogger().info(player.getDisplayName());
            if(event instanceof EntityDamageByEntityEvent) {
                player.sendMessage(ChatColor.GOLD + damagedPlayer.getName() + ChatColor.WHITE + " got hit by " + ChatColor.GREEN + ((EntityDamageByEntityEvent)event).getDamager().getName() + ChatColor.WHITE + " and took " + ChatColor.RED + damage / 2 + "❤" + ChatColor.WHITE + " of damage!");
            }
            else {
                player.sendMessage(ChatColor.GOLD + damagedPlayer.getName() + ChatColor.WHITE + " took " + ChatColor.RED + damage / 2 + "❤" + ChatColor.WHITE + " of damage!");
            }
            if(damagedPlayer != player) {
                if(preAbsorption > 0) {
                    if (preAbsorption == event.getFinalDamage()) {
                        player.setAbsorptionAmount(0.0f);
                        player.setHealth(Math.max(0, damagedPlayer.getHealth() - damage));
                    }
                    if (preAbsorption > event.getFinalDamage()) {
                        if(damagedPlayer.getAbsorptionAmount() > preAbsorption) {
                            plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: absorbtion smaller than pre absorbtion damage");
                            player.setAbsorptionAmount(player.getAbsorptionAmount() - preAbsorption);
                        }
                        else if(damagedPlayer.getAbsorptionAmount() > 0.0f) {
                            plugin.getLogger().info("SharedHealthModifier/syncHealtOnDamage: absorbtion between 0 and absorbtion damage");
                            double absorption = damagedPlayer.getAbsorptionAmount();
                            player.setAbsorptionAmount(0.0f);
                            player.removePotionEffect(PotionEffectType.ABSORPTION);
                            damagedPlayer.removePotionEffect(PotionEffectType.ABSORPTION);
                            player.setHealth(Math.max(0, damagedPlayer.getHealth() - preAbsorption + absorption));
                        }
                    }
                }
                plugin.getLogger().info(String.valueOf(damagedPlayer.getAbsorptionAmount()));
            }
        }

        plugin.getLogger().info("--------------------------------------------------");
    }
}
