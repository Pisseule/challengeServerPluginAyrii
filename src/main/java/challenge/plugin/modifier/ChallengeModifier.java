package challenge.plugin.modifier;

import challenge.plugin.Challenge;
import challenge.plugin.ChallengeCreator;
import challenge.plugin.ChallengeServerPlugin;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.function.BiConsumer;

public class ChallengeModifier {
    public String name;
    public Map<Class<? extends Event>, BiConsumer<? extends Event, Pair<Plugin, Challenge>>> events;

    public ChallengeModifier(){
        events = new java.util.HashMap<>();
    }
    public ItemStack getModifierItem(Plugin plugin){
       return new ItemStack(Material.AIR, 1);
    }
    public static ChallengeModifier getModifierByName(String name){
        for(ChallengeModifier modifier : ChallengeCreator.modifiers) {
           if(modifier.name.equals(name)){
               return modifier;
           }
        }
        return null;
    }

    public <T extends Event> void addEventHandler(Class<T> eventClass, BiConsumer<T, Pair<Plugin ,Challenge>> eventHandler){
        events.put(eventClass,(BiConsumer<? extends Event, Pair<Plugin, Challenge>>) eventHandler);
    }


}
