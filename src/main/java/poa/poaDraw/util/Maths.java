package poa.poaDraw.util;

public class Maths {

    public static float snapToCardinal90(float angle) {
        angle = ((angle % 360f) + 360f) % 360f;
        return (Math.round(angle / 90f) * 90f) % 360f;
    }
}
