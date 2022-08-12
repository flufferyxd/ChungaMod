package magnileve.chungamod.util.math;

import com.google.common.base.MoreObjects;

/**
 * A three-dimensional vector containing {@code float} values.
 * @author Magnileve
 */
public final class Vec3f implements Comparable<Vec3f> {

/**
 * 0, 0, 0
 */
public static final Vec3f ORIGIN = new Vec3f(0F, 0F, 0F);

private final float x;
private final float y;
private final float z;

/**
 * Creates a new {@code Vec3f} with the given coordinates.
 * @param x x value
 * @param y y value
 * @param z z value
 */
public Vec3f(float x, float y, float z) {
	this.x = x;
	this.y = y;
	this.z = z;
}

/**
 * Returns this vector's x value.
 * @return this vector's x value
 */
public float getX() {
	return x;
}

/**
 * Returns this vector's y value.
 * @return this vector's y value
 */
public float getY() {
	return y;
}

/**
 * Returns this vector's z value.
 * @return this vector's z value
 */
public float getZ() {
	return z;
}

@Override
public String toString() {
	return MoreObjects.toStringHelper(this).add("x", x).add("y", y).add("z", z).toString();
}

@Override
public boolean equals(Object obj) {
	if(obj instanceof Vec3f) {
		Vec3f other = (Vec3f) obj;
		return x == other.x && y == other.y && z == other.z;
	}
	return false;
}

@Override
public int hashCode() {
	return (Float.floatToRawIntBits(x) * 31 + Float.floatToRawIntBits(y)) * 31 + Float.floatToRawIntBits(z);
}

@Override
public int compareTo(Vec3f o) {
	float dx = x - o.x;
	if(dx != 0D) return dx > 0D ? 1 : -1;
	float dy = y - o.y;
	if(dy != 0D) return dy > 0D ? 1 : -1;
	float dz = z - o.z;
	return dz == 0D ? 0 : dz > 0D ? 1 : -1;
}

}