package poa.poaDraw.util;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Palette {

    @Getter
    private Player owner;

    private final Location origin;
    private final int cols;
    private final int rows;

    private final float tileMul;
    private final float stepX;
    private final float stepY;
    private final float forward;

    private final float hitW;
    private final float hitH;

    private final float yaw;
    private final float pitch;

    private final Vector3f tileScale;

    private TextDisplay currentColor;
    private TextDisplay currentSize;

    private final World world;

    private final List<TextDisplay> displays = new ArrayList<>();
    private final List<Interaction> interactions = new ArrayList<>();
    private final Map<Interaction, TextDisplay> interactionToDisplay = new LinkedHashMap<>();

    public Palette(Player owner, Location origin) {
        this(owner, origin, 24, 10, 0.55f, 0.16f, 0.16f, 0.25f);
    }

    public Palette(Player owner, Location origin, int cols, int rows, float tileMul, float stepX, float stepY, float forward) {
        this.owner = owner;

        this.origin = origin.clone();
        this.cols = cols;
        this.rows = rows;

        this.world = origin.getWorld();

        this.tileMul = tileMul;
        this.stepX = stepX;
        this.stepY = stepY;
        this.forward = forward;

        this.hitW = 0.22f * tileMul;
        this.hitH = 0.22f * tileMul;

        this.yaw = Maths.snapToCardinal90(this.origin.getYaw());
        this.pitch = 0f;

        this.tileScale = new Vector3f(2f * tileMul, tileMul, 0f);
    }

    public Palette spawn() {
        if (!displays.isEmpty() || !interactions.isEmpty()) return this;

        final World world = origin.getWorld();
        if (world == null) return this;

        final float halfW = (cols - 1) * stepX * 0.5f;
        final float halfH = (rows - 1) * stepY * 0.5f;

        for (int y = 0; y < rows; y++) {
            final float v = 1.0f - (y / (float) (rows - 1)) * 0.85f;

            for (int x = 0; x < cols; x++) {
                final float h = x / (float) (cols - 1);
                final Color color = hsvToBukkitColor(h, 1.0f, v);

                final float tx = (x * stepX) - halfW;
                final float ty = halfH - (y * stepY);
                final float tz = forward;

                final TextDisplay td = world.spawn(origin, TextDisplay.class, d -> {
                    d.text(Component.text(" "));
                    d.setRotation(yaw, pitch);
                    d.setBackgroundColor(color);

                    final Transformation t = d.getTransformation();
                    d.setTransformation(new Transformation(
                            new Vector3f(tx, ty, tz),
                            t.getLeftRotation(),
                            tileScale,
                            t.getRightRotation()
                    ));
                });

                displays.add(td);

                final Location itLoc = offsetByLocal(origin, yaw, tx, ty, tz);
                final Interaction it = world.spawn(itLoc, Interaction.class, i -> {
                    i.setInteractionWidth(hitW);
                    i.setInteractionHeight(hitH);
                    i.setResponsive(true);
                });

                interactions.add(it);
                interactionToDisplay.put(it, td);
            }
        }


        final PlayerData playerData = PlayerData.getPlayerData(owner);

        final TextDisplay label = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text("Current Colour"));
            d.setRotation(yaw, pitch);

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(0f, -1f, forward),
                    t.getLeftRotation(),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    t.getRightRotation()
            ));
        });
        displays.add(label);

        this.currentColor = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text(" "));
            d.setRotation(yaw, pitch);

            d.setBackgroundColor(playerData.getColor());

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(0f, -1.2f, forward),
                    t.getLeftRotation(),
                    tileScale,
                    t.getRightRotation()
            ));
        });
        displays.add(this.currentColor);

        // --- Right-side controls: +, S, - ---
        final float controlX = halfW + (stepX * 1.35f);
        final float controlZ = forward;

        final float plusY = halfH;
        final float sizeY = halfH - stepY;
        final float minusY = halfH - (stepY * 2f);

        final Vector3f controlScale = new Vector3f(0.6f, 0.6f, 0.6f);

        final TextDisplay plus = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text("+"));
            d.setRotation(yaw, pitch);

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(controlX, plusY, controlZ),
                    t.getLeftRotation(),
                    controlScale,
                    t.getRightRotation()
            ));
        });
        displays.add(plus);

        this.currentSize = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text(String.format("%.1f", playerData.getSize())));
            d.setRotation(yaw, pitch);

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(controlX, sizeY, controlZ),
                    t.getLeftRotation(),
                    controlScale,
                    t.getRightRotation()
            ));
        });
        displays.add(currentSize);

        final TextDisplay minus = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text("-"));
            d.setRotation(yaw, pitch);

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(controlX, minusY, controlZ),
                    t.getLeftRotation(),
                    controlScale,
                    t.getRightRotation()
            ));
        });
        displays.add(minus);

        // Interactions for symbols (no transformations, so spawn at rotated world locations)
        final float controlHitW = hitW;
        final float controlHitH = hitH;

        final Location plusLoc = offsetByLocal(origin, yaw, controlX, plusY, controlZ);
        final Interaction plusIt = world.spawn(plusLoc, Interaction.class, i -> {
            i.setInteractionWidth(controlHitW);
            i.setInteractionHeight(controlHitH);
            i.setResponsive(true);
        });
        interactions.add(plusIt);
        interactionToDisplay.put(plusIt, plus);

        final Location minusLoc = offsetByLocal(origin, yaw, controlX, minusY, controlZ);
        final Interaction minusIt = world.spawn(minusLoc, Interaction.class, i -> {
            i.setInteractionWidth(controlHitW);
            i.setInteractionHeight(controlHitH);
            i.setResponsive(true);
        });
        interactions.add(minusIt);
        interactionToDisplay.put(minusIt, minus);


        final float clearX = -halfW - (stepX * 1.60f);
        final float clearY = halfH;
        final float clearZ = forward;

        final Vector3f clearScale = new Vector3f(0.5f, 0.5f, 0.5f);

        final TextDisplay clear = world.spawn(origin, TextDisplay.class, d -> {
            d.text(Component.text("Clear"));
            d.setRotation(yaw, pitch);

            final Transformation t = d.getTransformation();
            d.setTransformation(new Transformation(
                    new Vector3f(clearX, clearY, clearZ),
                    t.getLeftRotation(),
                    clearScale,
                    t.getRightRotation()
            ));
        });
        displays.add(clear);

// wider hitbox to cover the word "Clear"
        final Location clearLoc = offsetByLocal(origin, yaw, clearX, clearY, clearZ);
        final Interaction clearIt = world.spawn(clearLoc, Interaction.class, i -> {
            i.setInteractionWidth(hitW * 2.6f);
            i.setInteractionHeight(hitH);
            i.setResponsive(true);
        });
        interactions.add(clearIt);
        interactionToDisplay.put(clearIt, clear);

        return this;
    }


    public void setCurrentColor(Color color) {
        this.currentColor.setBackgroundColor(color);
    }

    public void setCurrentSize(float size) {
        this.currentSize.text(Component.text(String.format("%.1f", size)));
    }

    public void destroy() {
        for (Interaction it : interactions) {
            if (it != null && it.isValid()) it.remove();
        }
        for (TextDisplay td : displays) {
            if (td != null && td.isValid()) td.remove();
        }
        interactions.clear();
        displays.clear();
        interactionToDisplay.clear();
    }

    public Location getOrigin() {
        return origin.clone();
    }

    public List<TextDisplay> getDisplays() {
        return Collections.unmodifiableList(displays);
    }

    public List<Interaction> getInteractions() {
        return Collections.unmodifiableList(interactions);
    }

    public Map<Interaction, TextDisplay> getInteractionToDisplayMap() {
        return Collections.unmodifiableMap(interactionToDisplay);
    }

    public TextDisplay getDisplay(Interaction interaction) {
        return interactionToDisplay.get(interaction);
    }

    private static Location offsetByLocal(Location origin, float yawDeg, float tx, float ty, float tz) {
        final double rad = Math.toRadians(yawDeg);

        final double fwdX = -Math.sin(rad);
        final double fwdZ = Math.cos(rad);

        final double rightX = Math.cos(rad);
        final double rightZ = Math.sin(rad);

        final double dx = rightX * tx + fwdX * tz;
        final double dz = rightZ * tx + fwdZ * tz;

        final Location loc = origin.clone();
        loc.setYaw(yawDeg);
        loc.setPitch(0f);
        loc.add(dx, ty, dz);
        return loc;
    }

    private static Color hsvToBukkitColor(float h, float s, float v) {
        int rgb = java.awt.Color.HSBtoRGB(clamp01(h), clamp01(s), clamp01(v));
        return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private static float clamp01(float f) {
        return f < 0f ? 0f : Math.min(f, 1f);
    }
}
