package magnileve.chungamod.util;

import java.awt.Color;

import magnileve.chungamod.util.math.MathUtil;
import magnileve.chungamod.util.math.Vec3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

/**
 * Contains static utility methods for processing colors.
 * Unless otherwise noted, within this class, color components of type {@code float} are ranged from {@code 0} to {@code 1},
 * and color components of type {@code int} are ranged from {@code 0} to {@code 255}.
 * @author Magnileve
 */
public class Colors {

private Colors() {}

/**
 * Converts RGB to HSV.
 * @param r red
 * @param g green
 * @param b blue
 * @return a {@code Vec3f} containing hue, saturation, and value
 */
public static Vec3f rgbTohsv(float r, float g, float b) {
	float min = MathUtil.min(r, g, b),
			max = MathUtil.max(r, g, b),
			d = max - min,
			h = d == 0F ? 0F : (
				max == r ? (g - b) / d % 6 :
				max == g ? (b - r) / d + 2 :
				(r - g) / d + 4
			) / 6;
	return new Vec3f(h < 0F ? h + 1F : h,
			max == 0F ? 0F : d / max,
			max);
}

/**
 * Converts HSV to RGB.
 * @param h hue
 * @param s saturation
 * @param v value
 * @return a {@code Vec3f} containing red, green, and blue
 */
public static Vec3f hsvTorgb(float h, float s, float v) {
	float c = v * s, x = c * (1 - Math.abs(h * 6 % 2 - 1)), m = v - c;
	int i = MathHelper.floor(h * 6);
	Vec3f rgb;
	switch(i) {
	case 0: rgb = new Vec3f(c, x, 0F); break;
	case 1: rgb = new Vec3f(x, c, 0F); break;
	case 2: rgb = new Vec3f(0F, c, x); break;
	case 3: rgb = new Vec3f(0F, x, c); break;
	case 4: rgb = new Vec3f(x, 0F, c); break;
	case 5: case 6: rgb = new Vec3f(c, 0F, x); break;
	default: return hsvTorgb(h - MathHelper.floor(h), s, v);
	}
	return new Vec3f(rgb.getX() + m, rgb.getY() + m, rgb.getZ() + m);
}

/**
 * Converts RGB to HSV.
 * @param rgb a {@code Vec3f} containing red, green, and blue
 * @return a {@code Vec3f} containing hue, saturation, and value
 */
public static Vec3f rgbTohsv(Vec3f rgb) {
	return rgbTohsv(rgb.getX(), rgb.getY(), rgb.getZ());
}

/**
 * Converts HSV to RGB.
 * @param hsv a {@code Vec3f} containing hue, saturation, and value
 * @return a {@code Vec3f} containing red, green, and blue
 */
public static Vec3f hsvTorgb(Vec3f hsv) {
	return hsvTorgb(hsv.getX(), hsv.getY(), hsv.getZ());
}

/**
 * Scales an {@code int} of range {@code 0} to {@code 255} to a {@code float} of range {@code 0} to {@code 1}.
 * These are the typical scales used by color components.
 * @param i an {@code int} of range {@code 0} to {@code 255}
 * @return a similar {@code float} of range {@code 0} to {@code 1}
 */
public static float iToF(int i) {
	return (float) i / 255;
}

/**
 * Scales a {@code float} of range {@code 0} to {@code 1} to an {@code int} of range {@code 0} to {@code 255}.
 * These are the typical scales used by color components.
 * @param f a {@code float} of range {@code 0} to {@code 1}
 * @return a similar {@code int} of range {@code 0} to {@code 255}
 */
public static int fToI(float f) {
	return (int) (f * 255);
}

/**
 * Scales a {@code Vec3i} containing values of range {@code 0} to {@code 255}
 * to a {@code Vec3f} containing values of range {@code 0} to {@code 1}.
 * These are the typical scales used by color components.
 * @param ints a {@code Vec3i} containing values of range {@code 0} to {@code 255}
 * @return a similar {@code Vec3f} containing values of range {@code 0} to {@code 1}
 */
public static Vec3f iToF(Vec3i ints) {
	return new Vec3f((float) ints.getX() / 255, (float) ints.getY() / 255, (float) ints.getZ() / 255);
}

/**
 * Scales a {@code Vec3f} containing values of range {@code 0} to {@code 1}
 * to a {@code Vec3i} containing values of range {@code 0} to {@code 255}.
 * These are the typical scales used by color components.
 * @param floats a {@code Vec3f} containing values of range {@code 0} to {@code 1}
 * @return a similar {@code Vec3i} containing values of range {@code 0} to {@code 255}
 */
public static Vec3i fToI(Vec3f floats) {
	return new Vec3i((int) (floats.getX() * 255), (int) (floats.getY() * 255), (int) (floats.getZ() * 255));
}

/**
 * Creates a hexadecimal string representing a color.
 * @param color a color
 * @return {@code "#"} followed by the hexadecimal string of this color's components
 */
public static String toString(Color color) {
	return "#" + Integer.toHexString(color.getRGB()).toUpperCase();
}

/**
 * Parses a color from a hexadecimal value.
 * @param hex a hexadecimal value, optionally preceded by "#"
 * @return the color of the given hexadecimal value
 * @throws NumberFormatException if a hexadecimal value cannot be parsed from {@code hex}
 */
public static Color parseColor(String hex) throws NumberFormatException {
	return new Color(Integer.parseUnsignedInt(hex.startsWith("#") ? hex.substring(1) : hex, 16), true);
}

/**
 * Creates a new {@link Color} with the given components.
 * @param rgb red, green, and blue
 * @return a {@code Color} containing the given components and maximum alpha
 */
public static Color newColor(Vec3f rgb) {
	return newColor(fToI(rgb));
}

/**
 * Creates a new {@link Color} with the given components.
 * @param rgb red, green, and blue
 * @return a {@code Color} containing the given components and maximum alpha
 */
public static Color newColor(Vec3i rgb) {
	return new Color(rgb.getX(), rgb.getY(), rgb.getZ());
}

/**
 * Creates a new {@link Color} with the given components.
 * @param rgb red, green, and blue
 * @param a alpha
 * @return a {@code Color} containing the given components
 */
public static Color newColor(Vec3f rgb, int a) {
	return newColor(fToI(rgb), a);
}

/**
 * Creates a new {@link Color} with the given components.
 * @param rgb red, green, and blue
 * @param a alpha
 * @return a {@code Color} containing the given components
 */
public static Color newColor(Vec3i rgb, int a) {
	return new Color(rgb.getX(), rgb.getY(), rgb.getZ(), a);
}

/**
 * Gets the RGB components of a color.
 * @param color a color
 * @return the given color's red, green, and blue
 */
public static Vec3f getComponents(Color color) {
	float[] rgb = color.getRGBColorComponents(null);
	return new Vec3f(rgb[0], rgb[1], rgb[2]);
}

}