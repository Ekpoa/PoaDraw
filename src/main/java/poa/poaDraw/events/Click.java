package poa.poaDraw.events;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import poa.poaDraw.commands.DrawCMD;
import poa.poaDraw.util.Cube;
import poa.poaDraw.util.Palette;
import poa.poaDraw.util.PlayerData;

public class Click implements Listener {

    @EventHandler
    public void interactionClick(PlayerInteractEntityEvent e){
        final Player player = e.getPlayer();
        final Entity clicked = e.getRightClicked();
        if(!(clicked instanceof Interaction interaction))
            return;

        if(!DrawCMD.viewingPalette.containsKey(player))
            return;

        final Palette palette = DrawCMD.viewingPalette.get(player);

        final TextDisplay display = palette.getDisplay(interaction);
        if(display == null)
            return;

        final PlayerData playerData = PlayerData.getPlayerData(player);

        final String text = PlainTextComponentSerializer.plainText().serialize(display.text());
        if(text.equalsIgnoreCase(" ")) {
            final Color color = display.getBackgroundColor();
            if (color == null)
                return;

            playerData.setColor(color);

            palette.setCurrentColor(color);

            player.sendRichMessage("<" + toHex(color) + ">Color set");
            return;
        }

        switch (text.toLowerCase()){
            case "+" -> {
                final float size = playerData.getSize() + 0.1F;
                playerData.setSize(size);
                palette.setCurrentSize(size);
            }
            case "-" -> {
                float size = playerData.getSize() - 0.1F;
                if(size <= 0)
                    size = 0.1F;
                playerData.setSize(size);
                palette.setCurrentSize(size);
            }
            case "clear" -> playerData.clearAllDisplays();
        }




    }



    @EventHandler
    public void drawClick(PlayerInteractEvent e){
        final Player player = e.getPlayer();

        PlayerData playerData = PlayerData.getPlayerData(player);

        final Action action = e.getAction();

        if(action.isRightClick()){
           playerData.getDisplays().add(Cube.spawnBlock(player.getEyeLocation().clone().add(player.getLocation().getDirection().normalize().multiply(2)), playerData.getColor(), playerData.getSize()));
        }
    }



    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }

}
