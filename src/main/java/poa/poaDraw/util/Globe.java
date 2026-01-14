package poa.poaDraw.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;


import net.kyori.adventure.text.Component;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.util.Vector;
import poa.poaDraw.PoaDraw;

import javax.imageio.ImageIO;


public final class Globe {
    private final Map<Key, TextDisplay> displays = new HashMap<>();
    private final List<Pixel> pixels;

    private float rotationRad = 0f;

    public Globe(int heightPixels) {
        this.pixels = generatePixels(heightPixels);
    }

    public static BufferedImage loadTexture(String resourcePath) {
        try (InputStream in = PoaDraw.INSTANCE.getResource(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourcePath, e);
        }
    }

    public void spawn(World world, Vector origin, BufferedImage texture) {
        for (Pixel p : pixels) {
            Key k = new Key(p.keyA, p.keyB, 0);

            TextDisplay td = world.spawn(origin.toLocation(world), TextDisplay.class, d -> {
                d.text(Component.text(" "));
                d.setBillboard(Display.Billboard.FIXED);
                d.setInterpolationDuration(1);
                d.setTeleportDuration(1);
                d.setBrightness(new Display.Brightness(15, 15));
                d.setBackgroundColor(sample(texture, p.u, p.v));
            });

            displays.put(k, td);
        }
    }

    public void start(World world, Vector origin, BufferedImage texture, float tiltRad, float scale, float speedRadPerTick) {
        Bukkit.getScheduler().runTaskTimer(PoaDraw.INSTANCE, task -> {
            if (displays.values().stream().anyMatch(d -> d == null || !d.isValid())) {
                task.cancel();
                return;
            }

            rotationRad += speedRadPerTick;

            Matrix4f sphere = new Matrix4f()
                    .rotateX(tiltRad)
                    .rotateY(rotationRad)
                    .scale(scale);

            for (Pixel p : pixels) {
                TextDisplay td = displays.get(new Key(p.keyA, p.keyB, 0));
                if (td == null || !td.isValid()) continue;

                Matrix4f m = new Matrix4f(sphere).mul(p.transform);

                // set transform
                applyMatrixToTextDisplay(td, m);

                // set color
                td.setBackgroundColor(sample(texture, p.u, p.v));
            }
        }, 1L, 1L);
    }

    // ---------- Pixel generation (same logic as the Kotlin) ----------

    private static List<Pixel> generatePixels(int ySteps) {
        List<Pixel> out = new ArrayList<>();

        float perimeter = (float) (Math.PI * 2);
        float particleSize = perimeter / ySteps / 2f;

        for (int yStep = 0; yStep <= ySteps; yStep++) {
            float y = yStep / (float) ySteps;

            float ringScale = (float) Math.sin(Math.PI * y);
            int rSteps = Math.max(1, (int) (ySteps * 2 * ringScale));

            for (int rStep = 0; rStep < rSteps; rStep++) {
                float r = rStep / (float) rSteps;

                float yRot = (float) (Math.PI * 2 * r);
                float xRot = (float) (Math.PI * y);

                // This matches the Kotlin build:
                // rotateYXZ(yRot, xRot, 0)
                // rotateX(PI/2)
                // translate(FORWARD)
                // scale(particleSize)
                Matrix4f matrix = new Matrix4f()
                        .rotateYXZ(yRot, xRot, 0f)
                        .rotateX((float) (Math.PI / 2.0))
                        .translate(0f, 0f, 1f)      // FORWARD_VECTOR
                        .scale(particleSize);

                // This is their "unit square" multiply. You can tune it to your quad size.
                Matrix4f unitSquare = textDisplayUnitSquare();

                Matrix4f pixelTransform = new Matrix4f(matrix)
                        .translate(-.5f, -.5f, 0f)
                        .mul(unitSquare);

                out.add(new Pixel(yStep, rStep, r, y, pixelTransform));
            }
        }

        return out;
    }

    private static Matrix4f textDisplayUnitSquare() {
        return new Matrix4f()
                .translate(-0.1f + .5f, -0.5f + .5f, 0f)
                .scale(8.0f, 4.0f, 1f);
    }

    // ---------- Texture sampling ----------

    private static org.bukkit.Color sample(BufferedImage img, float u, float v) {
        // wrap u, clamp v
        u = u - (float) Math.floor(u);
        v = Math.max(0f, Math.min(1f, v));

        int x = Math.min(img.getWidth() - 1, (int) (u * img.getWidth()));
        int y = Math.min(img.getHeight() - 1, (int) (v * img.getHeight()));

        int argb = img.getRGB(x, y);
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = (argb) & 0xFF;

        // If you want alpha=0 pixels to not render, you’d skip spawning/updating that display.
        return org.bukkit.Color.fromARGB(a, r, g, b);
    }

    // ---------- Matrix -> TextDisplay transformation ----------
    // You already know this area from your cube work.
    // For a first pass: extract translation + rotation (quaternion) + scale from the matrix and apply to Transformation.

    private static void applyMatrixToTextDisplay(TextDisplay td, Matrix4f m) {
        // Minimal: translation from matrix, scale left as-is.
        // Better: decompose into TRS.
        Vector3f translation = new Vector3f();
        m.getTranslation(translation);

        // naive scale (approx)
        Vector3f scale = new Vector3f();
        m.getScale(scale);

        // rotation quaternion
        Quaternionf rot = new Quaternionf();
        m.getUnnormalizedRotation(rot).normalize();

        Transformation old = td.getTransformation();
        td.setTransformation(new Transformation(
                translation,
                rot,
                scale,
                old.getRightRotation()
        ));
    }

    // ---------- data classes ----------
    private record Pixel(int keyA, int keyB, float u, float v, Matrix4f transform) {}

    private record Key(int a, int b, int inner) {}
}
