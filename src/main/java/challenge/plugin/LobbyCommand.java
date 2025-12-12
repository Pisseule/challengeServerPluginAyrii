package challenge.plugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LobbyCommand implements CommandExecutor {
    private final Plugin plugin;

    public LobbyCommand(Plugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (commandSender instanceof Player player) {
            World lobby = Bukkit.getWorld("lobby");
            if (lobby != null) {
                player.teleport(lobby.getSpawnLocation());
                player.sendMessage("§aTeleported to lobby!");
                return true;
            } else {
                player.sendMessage("§cLobby world not found!");
            }
        }
        return false;
    }
}
