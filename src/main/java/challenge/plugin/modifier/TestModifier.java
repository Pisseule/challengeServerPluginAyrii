package challenge.plugin.modifier;

import challenge.plugin.Challenge;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.util.function.BiConsumer;

public class TestModifier{
    public static class testEvent implements BiConsumer<Event, Pair<Plugin, Challenge>> {
        @Override
        public void accept(Event event, Pair<Plugin, Challenge> pluginChallengePair) {
            pluginChallengePair.getLeft().getLogger().info("Test Modifier Event Triggered!");
        }
    }

    public static ChallengeModifier getTestModifier(Plugin plugin){
        ChallengeModifier testModifier =  new ChallengeModifier() {
            {
                name = "Test Modifier";
            }
            @Override
            public ItemStack getModifierItem(Plugin plugin) {
                ItemStack itemStack = new ItemStack(Material.DIAMOND, 1);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + this.name);

                NamespacedKey key = new NamespacedKey(plugin, "modifier_name");
                meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, this.name);
                itemStack.setItemMeta(meta);
                return itemStack;
            }
        };
        testModifier.events.put(PlayerPickupItemEvent.class, new testEvent());
        return testModifier;
    }
}
