package poa.poaDraw.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import poa.poaDraw.commands.Draw;
import poa.poaDraw.util.Palette;
import poa.poaDraw.util.PlayerData;

public class Leave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        final Player player = e.getPlayer();

        PlayerData.dataMap.remove(player);

        Draw.drawing.remove(player);

        final Palette palette = Draw.viewingPalette.get(player);
        if(palette != null) {
            palette.destroy();
            Draw.viewingPalette.remove(player);
        }
    }

}
