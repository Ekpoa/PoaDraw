package poa.poaDraw.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class Cube {

    private static final Component BLANK = Component.text(" ");

    private static final Vector3f BASE_SCALE = new Vector3f(2f, 1f, 0f);

    public static TextDisplay spawnBlock(Location location, Color color, float scale) {
        final World world = location.getWorld();

        final float baseYaw = Maths.snapToCardinal90(location.getYaw());
        final float basePitch = 0f;

        final Vector3f scaledText = new Vector3f(BASE_SCALE.x * scale, BASE_SCALE.y * scale, BASE_SCALE.z * scale);

        final TextDisplay tr = spawn(world, location, baseYaw + 0f, basePitch, vector(0f, 0f, 0.25f, scale), color, scaledText);
        tr.addPassenger(spawn(world, location, baseYaw + 90f, basePitch, vector(0.1f, 0f, 0.1f, scale), color, scaledText));
        tr.addPassenger(spawn(world, location, baseYaw + 180f, basePitch, vector(-0.05f, 0f, 0f, scale), color, scaledText));
        tr.addPassenger(spawn(world, location, baseYaw + 270f, basePitch, vector(-0.15f, 0f, 0.15f, scale), color, scaledText));

        tr.addPassenger(spawn(world, location, baseYaw, basePitch + 90f, vector(0f, 0f, 0f, scale), color, scaledText));
        tr.addPassenger(spawn(world, location, baseYaw, basePitch - 90f, vector(0f, -0.25f, 0.25f, scale), color, scaledText));

        return tr;
    }


    private static TextDisplay spawn(World world, Location location, float yaw, float pitch, Vector3f translation, Color background, Vector3f scaledText) {
        return world.spawn(location, TextDisplay.class, td -> {
            td.text(BLANK);
            td.setRotation(wrapDegrees(yaw), pitch);

            final Transformation t = td.getTransformation();
            td.setTransformation(new Transformation(
                    translation,
                    t.getLeftRotation(),
                    scaledText,
                    t.getRightRotation()
            ));

            td.setBackgroundColor(background);
        });

    }

    private static Vector3f vector(float x, float y, float z, float mul) {
        return new Vector3f(x * mul, y * mul, z * mul);
    }

    private static float wrapDegrees(float deg) {
        deg %= 360f;
        if (deg < 0f) deg += 360f;
        return deg;
    }
}
