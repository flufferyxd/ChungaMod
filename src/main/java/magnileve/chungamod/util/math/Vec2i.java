package magnileve.chungamod.util.math;

import com.google.common.base.MoreObjects;

/**
 * A two-dimensional vector containing {@code int} values.
 * @author Magnileve
 */
public final class Vec2i implements Comparable<Vec2i> {

/**
 * 0, 0
 */
public static final Vec2i ORIGIN = new Vec2i(0, 0);

private final int x;
private final int y;

/**
 * Creates a new {@code Vec2i} with the given coordinates.
 * @param x x value
 * @param y y value
 */
public Vec2i(int x, int y) {
	this.x = x;
	this.y = y;
}

/**
 * Returns this vector's x value.
 * @return this vector's x value
 */
public int getX() {
	return x;
}

/**
 * Returns this vector's y value.
 * @return this vector's y value
 */
public int getY() {
	return y;
}

@Override
public String toString() {
	return MoreObjects.toStringHelper(this).add("x", x).add("y", y).toString();
}

@Override
public boolean equals(Object obj) {
	if(obj instanceof Vec2i) {
		Vec2i other = (Vec2i) obj;
		return x == other.x && y == other.y;
	}
	return false;
}

@Override
public int hashCode() {
	return y * 31 + x;
}

@Override
public int compareTo(Vec2i o) {
	int result = x - o.x;
	return result == 0 ? y - o.y : result;
}

}