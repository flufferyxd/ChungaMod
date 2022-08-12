package magnileve.chungamod.util.math;

import com.google.common.base.MoreObjects;

/**
 * A two-dimensional vector containing {@code double} values.
 * @author Magnileve
 */
public final class Vec2d implements Comparable<Vec2d> {

/**
 * 0, 0
 */
public static final Vec2d ORIGIN = new Vec2d(0D, 0D);

private final double x;
private final double y;

/**
 * Creates a new {@code Vec2d} with the given coordinates.
 * @param x x value
 * @param y y value
 */
public Vec2d(double x, double y) {
	this.x = x;
	this.y = y;
}

/**
 * Returns this vector's x value.
 * @return this vector's x value
 */
public double getX() {
	return x;
}

/**
 * Returns this vector's y value.
 * @return this vector's y value
 */
public double getY() {
	return y;
}

@Override
public String toString() {
	return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString();
}

@Override
public boolean equals(Object obj) {
	if(obj instanceof Vec2d) {
		Vec2d other = (Vec2d) obj;
		return x == other.x && y == other.y;
	}
	return false;
}

@Override
public int hashCode() {
	long result = Double.doubleToRawLongBits(x) * 31 + Double.doubleToRawLongBits(y);
	return (int) (result >>> 32) ^ (int) result;
}

@Override
public int compareTo(Vec2d o) {
	double dx = x - o.x;
	if(dx != 0D) return dx > 0D ? 1 : -1;
	double dy = y - o.y;
	return dy == 0D ? 0 : dy > 0D ? 1 : -1;
}

}