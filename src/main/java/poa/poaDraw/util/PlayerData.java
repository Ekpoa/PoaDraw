package poa.poaDraw.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;

public class PlayerData {

    public static final Map<Player, PlayerData> dataMap = new HashMap<>();

    public static PlayerData getPlayerData(Player player) {
        if(dataMap.containsKey(player))
            return dataMap.get(player);
        return new PlayerData(player);
    }

    Player player;

    @Getter
    @Setter
    Color color = Color.BLACK;

    @Getter
    @Setter
    float size = 0.2F;

    @Getter
    Set<TextDisplay> displays = new HashSet<>();

    public PlayerData(Player player){
        this.player = player;
        dataMap.put(player, this);
    }

    public void clearAllDisplays(){
        for (TextDisplay display : displays) {
            display.getPassengers().forEach(Entity::remove);
            display.remove();
        }
    }

}
