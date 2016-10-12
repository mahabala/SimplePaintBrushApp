package com.example.padma.mahaspaintbrushapp;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * A view for letting users to draw. The view provides users a paint brush which
 * can be used to draw any shape.
 */

public class DrawableAreaView extends View implements View.OnClickListener {

    /**
     * A pair to represent a path and its paint brush color
     */

    private class PathColorPair {
        private Path path;
        int color;

        PathColorPair(Path thisPath, int thisColor) {
            path = thisPath;
            color = thisColor;
        }

        Path getPath() {
            return path;
        }

        int getColor() {
            return color;
        }
    }
    /**
     * A brush object
     */
    private Paint brush = new Paint();

    final static private int DEFAULT_COLOR = Color.BLUE;

    /**
     * A stack to maintain all traversed paths and their corresponding paint color
     */

    private Stack<PathColorPair> pathStack = new Stack<>();

    public DrawableAreaView(Context context) {
        super(context);
        init();
    }

    public DrawableAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawableAreaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onClick(View view) {
        /**
         * Reset all paths on clear view
         */
        Iterator<PathColorPair> iterator = pathStack.iterator();
        while(iterator.hasNext()) {
            PathColorPair pair = iterator.next();
            pair.getPath().reset();
        }

        /**
         * Clear the stack and reset the paint color to the default color
         * of blue and start a new path.
         */
        pathStack.clear();
        pathStack.push(new PathColorPair(new Path(), new Integer(DEFAULT_COLOR)));
        postInvalidate();
    }

    /**
     * Set paint color
     * @param color to be set
     */
    public void setPaintColor(int color) {
        /**
         * If there is a untraversed path at the top of the stack remove it.
         */
        if (!pathStack.isEmpty()) {
            PathColorPair pair = pathStack.peek();
            if (pair.getPath().isEmpty()) {
                pathStack.pop();
            }
        }

        /**
         * Set the paint color and push a new path for traversal.
         */
        brush.setColor(color);
        pathStack.push(new PathColorPair(new Path(), color));
    }

    /**
     * Undo the previously traversed path. The previously traversed path
     * will be at one position below the top of the stack since the top of the
     * stack will contain a new path that is yet to be traversed.
     */
    public void undo() {
        PathColorPair pair = null;

        if (!pathStack.isEmpty()) {
            pair = pathStack.pop();
        }

        if (!pathStack.isEmpty()) {
            PathColorPair pairToRemove = pathStack.pop();
            pairToRemove.getPath().reset();
        }

        if (pair != null) {
            pathStack.push(pair);
        }
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float pointX = event.getX();
        final float pointY = event.getY();

        boolean handled = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                PathColorPair pair = pathStack.peek();
                pair.getPath().moveTo(pointX, pointY);
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                pair = pathStack.peek();
                pair.getPath().lineTo(pointX, pointY);
                break;

            case MotionEvent.ACTION_UP:
                pathStack.push(new PathColorPair(new Path(), brush.getColor()));
                break;

            default:
                break;
        }
        postInvalidate();
        return handled;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        /**
         * Draw all paths with their corresponding color
         */
        Iterator<PathColorPair> iterator = pathStack.iterator();
        while(iterator.hasNext()) {
            PathColorPair pair = iterator.next();
            brush.setColor(pair.getColor());
            canvas.drawPath(pair.getPath(), brush);
        }
    }

    private void init() {
        /**
         * Set some brush characteristics
         */
        brush.setAntiAlias(true);
        brush.setColor(DEFAULT_COLOR);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(15f);
        pathStack.push(new PathColorPair(new Path(), DEFAULT_COLOR));
    }
}