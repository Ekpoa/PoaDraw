package poa.poaDraw.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import poa.poaDraw.PoaDraw;
import poa.poaDraw.util.Globe;

import java.awt.image.BufferedImage;

public class GlobeCMD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player))
            return false;

        final Globe globe = new Globe(Integer.parseInt(args[0]));

        globe.spawn(player.getWorld(), player.getLocation().toVector(), Globe.loadTexture("day.png"));
        globe.start(
                player.getWorld(),
                player.getLocation().toVector(),
                Globe.loadTexture("day.png"),
                (float) Math.toRadians(23.4f),
                1.0f,
                (float) Math.toRadians(0.5f)
        );

        return false;
    }
}
