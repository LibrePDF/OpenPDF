package org.openpdf.css.style.derived;

import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.InlineMe;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.style.BorderRadiusCorner;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.newtable.CollapsedBorderValue;

import java.awt.*;

import static java.lang.Math.max;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_COLOR;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_LEFT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_RIGHT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_STYLE;
import static org.openpdf.css.constants.CSSName.BORDER_BOTTOM_WIDTH;
import static org.openpdf.css.constants.CSSName.BORDER_LEFT_COLOR;
import static org.openpdf.css.constants.CSSName.BORDER_LEFT_STYLE;
import static org.openpdf.css.constants.CSSName.BORDER_LEFT_WIDTH;
import static org.openpdf.css.constants.CSSName.BORDER_RIGHT_COLOR;
import static org.openpdf.css.constants.CSSName.BORDER_RIGHT_STYLE;
import static org.openpdf.css.constants.CSSName.BORDER_RIGHT_WIDTH;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_COLOR;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_LEFT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_RIGHT_RADIUS;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_STYLE;
import static org.openpdf.css.constants.CSSName.BORDER_TOP_WIDTH;
import static org.openpdf.css.constants.IdentValue.HIDDEN;
import static org.openpdf.css.constants.IdentValue.NONE;
import static org.openpdf.css.parser.FSRGBColor.TRANSPARENT;
import static org.openpdf.css.style.BorderRadiusCorner.UNDEFINED;

/**
 * User: patrick
 * Date: Oct 21, 2005
 */
public class BorderPropertySet extends RectPropertySet {
    private static final Corners NO_CORNERS = new Corners(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    private static final Styles NO_STYLES = new Styles(null, null, null, null);
    private static final Colors NO_COLORS = new Colors(TRANSPARENT, TRANSPARENT, TRANSPARENT, TRANSPARENT);
    public static final BorderPropertySet EMPTY_BORDER = new BorderPropertySet(0.0f, 0.0f, 0.0f, 0.0f, NO_STYLES, NO_CORNERS, NO_COLORS);

    private record Styles(@Nullable IdentValue top, @Nullable IdentValue right, @Nullable IdentValue bottom, @Nullable IdentValue left) {
        private boolean hasHidden() {
            return top == HIDDEN || right == HIDDEN || bottom == HIDDEN || left == HIDDEN;
        }
    }

    private record Colors(FSColor top, FSColor right, FSColor bottom, FSColor left) {
        public Colors lighten() {
            return new Colors(
                    top.lightenColor(),
                    right.lightenColor(),
                    bottom.lightenColor(),
                    left.lightenColor()
            );
        }
        public Colors darken() {
            return new Colors(
                    top.darkenColor(),
                    right.darkenColor(),
                    bottom.darkenColor(),
                    left.darkenColor()
            );
        }
    }

    private record Corners(BorderRadiusCorner topLeft, BorderRadiusCorner topRight,
                           BorderRadiusCorner bottomRight, BorderRadiusCorner bottomLeft) {
    }

    private final Styles styles;
    private final Colors colors;
    private final Corners corners;

    public BorderPropertySet(BorderPropertySet border) {
        this(border.top(), border.right(), border.bottom(), border.left(), border.styles, border.corners, border.colors);
    }

    private BorderPropertySet(
            float top,
            float right,
            float bottom,
            float left,
            Styles styles,
            Corners corners,
            Colors colors
    ) {
        super(top, right, bottom, left);
        this.styles = styles;
        this.colors = colors;
        this.corners = corners;
    }

    public BorderPropertySet(
            CollapsedBorderValue top,
            CollapsedBorderValue right,
            CollapsedBorderValue bottom,
            CollapsedBorderValue left
    ) {
        this(top.width(),
                right.width(),
                bottom.width(),
                left.width(),
                new Styles(top.style(), right.style(), bottom.style(), left.style()),
                NO_CORNERS,
                new Colors(top.color(), right.color(), bottom.color(), left.color())
        );
    }

    private BorderPropertySet(
            CalculatedStyle style,
            CssContext ctx
    ) {
        this(
                calculate(style, BORDER_TOP_STYLE, BORDER_TOP_WIDTH, ctx),
                calculate(style, BORDER_RIGHT_STYLE, BORDER_RIGHT_WIDTH, ctx),
                calculate(style, BORDER_BOTTOM_STYLE, BORDER_BOTTOM_WIDTH, ctx),
                calculate(style, BORDER_LEFT_STYLE, BORDER_LEFT_WIDTH, ctx),
                new Styles(
                        style.getIdent(BORDER_TOP_STYLE),
                        style.getIdent(BORDER_RIGHT_STYLE),
                        style.getIdent(BORDER_BOTTOM_STYLE),
                        style.getIdent(BORDER_LEFT_STYLE)
                ),
                new Corners(
                        new BorderRadiusCorner(BORDER_TOP_LEFT_RADIUS, style, ctx),
                        new BorderRadiusCorner(BORDER_TOP_RIGHT_RADIUS, style, ctx),
                        new BorderRadiusCorner(BORDER_BOTTOM_RIGHT_RADIUS, style, ctx),
                        new BorderRadiusCorner(BORDER_BOTTOM_LEFT_RADIUS, style, ctx)
                ),
                new Colors(
                        style.asColor(BORDER_TOP_COLOR),
                        style.asColor(BORDER_RIGHT_COLOR),
                        style.asColor(BORDER_BOTTOM_COLOR),
                        style.asColor(BORDER_LEFT_COLOR)
                )
        );
    }

    private static float calculate(CalculatedStyle style, CSSName borderStyle, CSSName borderWidth, CssContext ctx) {
        return style.isIdent(borderStyle, NONE) || style.isIdent(borderStyle, HIDDEN) ? 0 :
                style.getFloatPropertyProportionalHeight(borderWidth, 0, ctx);
    }

    @Deprecated
    @SuppressWarnings("unused")
    @InlineMe(replacement = "this.lighten()")
    public final BorderPropertySet lighten(IdentValue style) {
        return lighten();
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     */
    public BorderPropertySet lighten() {
        return new BorderPropertySet(
                top(), right(), bottom(), left(),
                styles,
                corners,
                colors.lighten()
        );
    }

    @Deprecated
    @SuppressWarnings("unused")
    @InlineMe(replacement = "this.darken()")
    public final BorderPropertySet darken(IdentValue style) {
        return darken();
    }

    /**
     * Returns the colors for brighter parts of each side for a particular decoration style
     */
    public BorderPropertySet darken() {
        return new BorderPropertySet(
                top(), right(), bottom(), left(),
                styles,
                corners,
                colors.darken()
        );
    }

    public static BorderPropertySet newInstance(
            CalculatedStyle style,
            @Nullable CssContext ctx
    ) {
        BorderPropertySet result = new BorderPropertySet(style, ctx);
        return result.isAllZeros() && !result.hasHidden() && !result.hasBorderRadius() ?
                BorderPropertySet.EMPTY_BORDER :
                result;

    }

    @Override
    public String toString() {
        return "BorderPropertySet[top=%s,right=%s,bottom=%s,left=%s]".formatted(top(), right(), bottom(), left());
    }

    public boolean noTop() {
        return styles.top() == NONE || (int) top() == 0;
    }

    public boolean noRight() {
        return styles.right() == NONE || (int) right() == 0;
    }

    public boolean noBottom() {
        return styles.bottom() == NONE || (int) bottom() == 0;
    }

    public boolean noLeft() {
        return styles.left() == NONE || (int) left() == 0;
    }

    @Nullable
    @CheckReturnValue
    public IdentValue topStyle() {
        return styles.top();
    }

    @Nullable
    @CheckReturnValue
    public IdentValue rightStyle() {
        return styles.right();
    }

    @Nullable
    @CheckReturnValue
    public IdentValue bottomStyle() {
        return styles.bottom();
    }

    @Nullable
    @CheckReturnValue
    public IdentValue leftStyle() {
        return styles.left();
    }

    public FSColor topColor() {
        return colors.top();
    }

    public FSColor rightColor() {
        return colors.right();
    }

    public FSColor bottomColor() {
        return colors.bottom();
    }

    public FSColor leftColor() {
        return colors.left();
    }

    public boolean hasHidden() {
        return styles.hasHidden();
    }

    public boolean hasBorderRadius() {
        return getTopLeft().hasRadius() || getTopRight().hasRadius() || getBottomLeft().hasRadius() || getBottomRight().hasRadius();
    }

    public BorderRadiusCorner getBottomRight() {
        return corners.bottomRight();
    }

    public BorderRadiusCorner getBottomLeft() {
        return corners.bottomLeft();
    }

    public BorderRadiusCorner getTopRight() {
        return corners.topRight();
    }

    public BorderRadiusCorner getTopLeft() {
        return corners.topLeft();
    }

    public BorderPropertySet normalizedInstance(Rectangle bounds) {
        float factor = 1;

        // top
        factor = Math.min(factor, bounds.width / getSideLength(corners.topLeft(), corners.topRight(), bounds.width));
        // bottom
        factor = Math.min(factor, bounds.width / getSideLength(corners.bottomRight(), corners.bottomLeft(), bounds.width));
        // right
        factor = Math.min(factor, bounds.height / getSideLength(corners.topRight(), corners.bottomRight(), bounds.height));
        // left
        factor = Math.min(factor, bounds.height / getSideLength(corners.bottomLeft(), corners.bottomRight(), bounds.height));

        Corners normalizedCorners = new Corners(
                new BorderRadiusCorner(factor * corners.topLeft().getMaxLeft(bounds.height), factor * corners.topLeft().getMaxRight(bounds.width)),
                new BorderRadiusCorner(factor * corners.topRight().getMaxLeft(bounds.width), factor * corners.topRight().getMaxRight(bounds.height)),
                new BorderRadiusCorner(factor * corners.bottomRight().getMaxLeft(bounds.height), factor * corners.bottomRight().getMaxRight(bounds.width)),
                new BorderRadiusCorner(factor * corners.bottomLeft().getMaxLeft(bounds.width), factor * corners.bottomLeft().getMaxRight(bounds.height))
        );
        return new BorderPropertySet(top(), right(), bottom(), left(),
                styles,
                normalizedCorners,
                colors
        );
    }

    /**
     * helper function for normalizeBorderRadius. Gets the max side width for each of the corners or the side width whichever is larger
     */
    private float getSideLength(BorderRadiusCorner left, BorderRadiusCorner right, float sideLength) {
        return max(sideLength, left.getMaxRight(sideLength) + right.getMaxLeft(sideLength));
    }

    @CheckReturnValue
    @Override
    public BorderPropertySet resetNegativeValues() {
        return new BorderPropertySet(max(0, top()), max(0, right()), max(0, bottom()), max(0, left()), styles, corners, colors);
    }
}

