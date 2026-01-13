package poa.poaDraw.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.checkerframework.common.value.qual.EnsuresMinLenIf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import poa.poaDraw.PoaDraw;
import poa.poaDraw.util.Cube;
import poa.poaDraw.util.Palette;
import poa.poaDraw.util.PlayerData;

import java.util.*;

public class Draw implements CommandExecutor, TabCompleter {

    public static final Map<Player, Palette> viewingPalette = new HashMap<>();
    public static final List<Player> drawing = new ArrayList<>();

    private Player player;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player))
            return true;

        this.player = player;

        if (args.length == 0) {
            player.sendRichMessage("<red>/draw <settings>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "settings" -> pallet();

        }

        return true;
    }

    public void pallet() {
        if (!viewingPalette.containsKey(player)) {
            final Location loc = player.getEyeLocation().clone().add(player.getLocation().getDirection().normalize().multiply(2));
            loc.addRotation(180, 0);

            final Palette palette = new Palette(player, loc);
            palette.spawn();
            viewingPalette.put(player, palette);
        } else {
            final Palette palette = viewingPalette.get(player);
            palette.destroy();
            viewingPalette.remove(player);
        }
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> list = new ArrayList<>();
        List<String> tr = new ArrayList<>();

        switch (args.length) {
            case 1 -> list = List.of("settings");
        }

        for (String s : list) {
            if (s.startsWith(args[args.length - 1].toLowerCase()))
                tr.add(s);
        }

        return tr;
    }
}
