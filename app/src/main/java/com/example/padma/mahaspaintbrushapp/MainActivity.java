package com.example.padma.mahaspaintbrushapp;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

/**
 * An activity that hosts certain UI elements to enable a user to
 * choose a color and provides a drawing area. There is also a control
 * to clear the contents of the drawing area.
 */

public class MainActivity extends Activity implements ColorMapView.OnColorChangedListener {
    private DrawableAreaView mDrawableArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawableArea = (DrawableAreaView)findViewById(R.id.drawableAreaView);
        Button clearButton = (Button)findViewById(R.id.clearButton);
        /* Set a listener on clearButton in order to clear the drawing area on user's click */
        clearButton.setOnClickListener(mDrawableArea);

        Button undoButton = (Button)findViewById(R.id.undoButton);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawableArea.undo();
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void colorChanged(int value) {
        mDrawableArea.setPaintColor(value);
    }
}
