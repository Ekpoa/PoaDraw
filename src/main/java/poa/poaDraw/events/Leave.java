package poa.poaDraw.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import poa.poaDraw.commands.DrawCMD;
import poa.poaDraw.util.Palette;
import poa.poaDraw.util.PlayerData;

public class Leave implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        final Player player = e.getPlayer();

        PlayerData.dataMap.remove(player);

        DrawCMD.drawing.remove(player);

        final Palette palette = DrawCMD.viewingPalette.get(player);
        if(palette != null) {
            palette.destroy();
            DrawCMD.viewingPalette.remove(player);
        }
    }

}
