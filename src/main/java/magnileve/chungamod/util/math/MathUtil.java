package magnileve.chungamod.util.math;

/**
 * Contains static utility methods to do mathematical processes.
 * @author Magnileve
 */
public class MathUtil {

private MathUtil() {}

/**
 * Ensures that a {@code double} is within the given range.
 * @param value a {@code double}
 * @param min minimum return value
 * @param max maximum return value
 * @return {@code min} if it is greater than {@code value}, {@code max} if it is smaller than {@code value}, or {@code value} otherwise
 */
public static double roundBetween(double value, double min, double max) {
	return value < min ? min : value > max ? max : value;
}

/**
 * Compares a point to a line.
 * As this method internally uses floating point variables,
 * extremely precise results, such as a return value of {@code 0}, cannot be relied on.
 * @param x x position of the point to compare
 * @param y y position of the point to compare
 * @param x1 x position of a point on the line
 * @param y1 y position of a point on the line
 * @param x2 x position of a another point on the line
 * @param y2 y position of a another point on the line
 * @return {@code 1} if the given point is above the given line,
 * {@code -1} if the given point is below the given line, or
 * {@code 0} if the given point is on the given line
 */
public static int compareToLine(int x, int y, int x1, int y1, int x2, int y2) {
	return compareToLine((double) x, y, x1, y1, x2, y2);
}

/**
 * Compares a point to a line.
 * As this method uses floating point variables,
 * extremely precise results, such as a return value of {@code 0}, cannot be relied on.
 * @param x x position of the point to compare
 * @param y y position of the point to compare
 * @param x1 x position of a point on the line
 * @param y1 y position of a point on the line
 * @param x2 x position of a another point on the line
 * @param y2 y position of a another point on the line
 * @return {@code 1} if the given point is above the given line,
 * {@code -1} if the given point is below the given line, or
 * {@code 0} if the given point is on the given line
 */
public static int compareToLine(double x, double y, double x1, double y1, double x2, double y2) {
	double d = (y2 - y1) / (x2 - x1);
	double result = y - x * d - (y1 - x1 * d);
	return result < 0D ? -1 : result > 0D ? 1 : 0;
}

/**
 * Compares a point to a line.
 * As this method uses floating point variables,
 * extremely precise results, such as a return value of {@code 0}, cannot be relied on.
 * @param x x position of the point to compare
 * @param y y position of the point to compare
 * @param line line to be compared, containing slope and y offset
 * @return {@code 1} if the given point is above the given line,
 * {@code -1} if the given point is below the given line, or
 * {@code 0} if the given point is on the given line
 */
public static int compareToLine(double x, double y, Vec2d line) {
	double d = line.getX();
	double result = y - x * d - line.getY();
	return result < 0D ? -1 : result > 0D ? 1 : 0;
}

/**
 * Creates a {@code Vec2d} containing the slope and y offset of a line intersecting the given points.
 * @param x1 x position of a point on the line
 * @param y1 y position of a point on the line
 * @param x2 x position of a another point on the line
 * @param y2 y position of a another point on the line
 * @return a {@code Vec2d} containing the slope and y offset of a line intersecting the given points
 */
public static Vec2d toLine(int x1, int y1, int x2, int y2) {
	return toLine((double) x1, y1, x2, y2);
}

/**
 * Creates a {@code Vec2d} containing the slope and y offset of a line intersecting the given points.
 * @param x1 x position of a point on the line
 * @param y1 y position of a point on the line
 * @param x2 x position of a another point on the line
 * @param y2 y position of a another point on the line
 * @return a {@code Vec2d} containing the slope and y offset of a line intersecting the given points
 */
public static Vec2d toLine(double x1, double y1, double x2, double y2) {
	double d = (y2 - y1) / (x2 - x1);
	return new Vec2d(d, y1 - x1 * d);
}

/**
 * Gets the position of a given point on a line parallel to the line from point 1 to point 2
 * where {@code 0} represents a point along the line from point 1 to point 3,
 * and {@code 1} represents a point along the line from point 2 to point 3.
 * @param x x position of a point
 * @param y y position of a point
 * @param relativeLine a line parallel to the line of the given point
 * @param line1 a line representing position {@code 0}
 * @param line2 a line representing position {@code 1}
 * @param point1x x position of triangle point 1
 * @param point1y y position of triangle point 1
 * @param point2x x position of triangle point 2
 * @param point2y y position of triangle point 2
 * @param point3x x position of triangle point 3
 * @param point3y y position of triangle point 3
 * @param insideTriangle if the given point must be inside the triangle made by points 1, 2, and 3
 * @return a {@code double} between {@code 0} and {@code 1} if the given point is
 * between the line from point 1 to point 3 and the line from point 2 to point 3,
 * a {@code double} below {@code 0} if the given point is before the line from point 1 to point 3, or
 * a {@code double} above {@code 1} if the given point is after the line from point 2 to point 3
 * @throws IllegalArgumentException if {@code insideTriangle} is {@code true},
 * and the given point is not inside the triangle made by points 1, 2, and 3
 */
public static double relativePointBetween(int x, int y, int point1x, int point1y,
		int point2x, int point2y, int point3x, int point3y, boolean insideTriangle) {
	Vec2d line12 = toLine(point1x, point1y, point2x, point2y),
			line13 = toLine(point1x, point1y, point3x, point3y),
			line23 = toLine(point2x, point2y, point3x, point3y);
	if(insideTriangle && !(sameSideOfLine(x, y, point3x, point3y, line12) &&
			sameSideOfLine(x, y, point2x, point2y, line13) && sameSideOfLine(x, y, point1x, point1y, line23)))
		throw new IllegalArgumentException("Coordinates provided are not inside triangle");
	return relativePointBetween(x, y, line12, line13, line23);
}

/**
 * Gets the position of a given point on a line parallel to the line from point 1 to point 2
 * where {@code 0} represents a point along the line from point 1 to point 3,
 * and {@code 1} represents a point along the line from point 2 to point 3.
 * @param x x position of a point
 * @param y y position of a point
 * @param relativeLine a line parallel to the line of the given point
 * @param line1 a line representing position {@code 0}
 * @param line2 a line representing position {@code 1}
 * @param point1x x position of triangle point 1
 * @param point1y y position of triangle point 1
 * @param point2x x position of triangle point 2
 * @param point2y y position of triangle point 2
 * @param point3x x position of triangle point 3
 * @param point3y y position of triangle point 3
 * @return a {@code double} between {@code 0} and {@code 1} if the given point is
 * between the line from point 1 to point 3 and the line from point 2 to point 3,
 * a {@code double} below {@code 0} if the given point is before the line from point 1 to point 3, or
 * a {@code double} above {@code 1} if the given point is after the line from point 2 to point 3
 */
public static double relativePointBetween(int x, int y, int point1x, int point1y,
		int point2x, int point2y, int point3x, int point3y) {
	return relativePointBetween(x, y, toLine(point1x, point1y, point2x, point2y),
			toLine(point1x, point1y, point3x, point3y), toLine(point2x, point2y, point3x, point3y));
}

/**
 * Gets the position of a given point on a line parallel to {@code relativeLine}
 * where {@code 0} represents a point along {@code line1}, and {@code 1} represents a point along {@code line2}.
 * @param x x position of a point
 * @param y y position of a point
 * @param relativeLine a line (containing slope and y offset) parallel to the line of the given point
 * @param line1 a line representing position {@code 0}
 * @param line2 a line representing position {@code 1}
 * @return a {@code double} between {@code 0} and {@code 1} if the given point is between {@code line1} and {@code line2},
 * a {@code double} below {@code 0} if the given point is before {@code line1}, or
 * a {@code double} above {@code 1} if the given point is after {@code line2}
 */
public static double relativePointBetween(double x, double y, Vec2d relativeLine, Vec2d line1, Vec2d line2) {
	double d = relativeLine.getX();
	Vec2d thisLine = new Vec2d(d, y - x * d);
	Vec2d start = intersection(thisLine, line1);
	Vec2d end = intersection(thisLine, line2);
	return distance(start.getX(), start.getY(), x, y) /
			distance(start.getX(), start.getY(), end.getX(), end.getY());
}

/**
 * Returns the distance between two points.
 * @param x1 x position of a point
 * @param y1 y position of a point
 * @param x2 x position of another point
 * @param y2 y position of another point
 * @return the distance between the two points
 */
public static double distance(double x1, double y1, double x2, double y2) {
	double dx = x2 - x1, dy = y2 - y1;
	return Math.sqrt(dx * dx + dy * dy);
}

/**
 * Returns the point where two lines intersect.
 * @param line1 a line (containing slope and y offset)
 * @param line2 another line
 * @return the point where two given lines intersect
 */
public static Vec2d intersection(Vec2d line1, Vec2d line2) {
	double d1 = line1.getX(), c1 = line1.getY(), d2 = line2.getX(), c2 = line2.getY();
	double x = (c2 - c1) / (d1 - d2);
	return new Vec2d(x, x * d1 + c1);
}

/**
 * Determines if two points are on the same side of a line.
 * If either of the points are on the line, this method normally returns {@code true},
 * but as this method uses floating point variables, this functionality cannot be guaranteed.
 * @param x1 x position of a point
 * @param y1 y position of a point
 * @param x2 x position of another point
 * @param y2 y position of another point
 * @param line a line (containing slope and y offset)
 * @return {@code true} if the given points are on the same side of the given line
 */
public static boolean sameSideOfLine(double x1, double y1, double x2, double y2, Vec2d line) {
	int i = compareToLine(x1, y1, line);
	int h = compareToLine(x2, y2, line);
	return i >= 0 && h >= 0 || i <= 0 && h <= 0;
}

/**
 * Returns the greatest given {@code float} value.  That is,
 * the result is the argument closest to positive infinity.
 * @param values floats to be compared
 * @return the greatest value in {@code values}
 */
public static float max(float... values) {
	float max = values[0];
	for(int i = 1; i < values.length; i++) {
		float check = values[i];
		if(check > max) max = check;
	}
	return max;
}

/**
 * Returns the smallest given {@code float} value.  That is,
 * the result is the argument closest to negative infinity.
 * @param values floats to be compared
 * @return the smallest value in {@code values}
 */
public static float min(float... values) {
	float min = values[0];
	for(int i = 1; i < values.length; i++) {
		float check = values[i];
		if(check < min) min = check;
	}
	return min;
}

}