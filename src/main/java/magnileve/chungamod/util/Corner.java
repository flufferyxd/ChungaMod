package magnileve.chungamod.util;

/**
 * A corner of a rectangle.
 * @author Magnileve
 */
public enum Corner {

TOP_LEFT(true, true),
TOP_RIGHT(true, false),
BOTTOM_LEFT(false, true),
BOTTOM_RIGHT(false, false);

private final boolean top;
private final boolean left;

Corner(boolean top, boolean left) {
	this.top = top;
	this.left = left;
}

/**
 * Indicates if this corner is of the top half.
 * @return {@code true} if this corner is of the top half; {@code false} if this corner is of the bottom half
 */
public boolean isTop() {
	return top;
}

/**
 * Indicates if this corner is of the left half.
 * @return {@code true} if this corner is of the left half; {@code false} if this corner is of the right half
 */
public boolean isLeft() {
	return left;
}

}