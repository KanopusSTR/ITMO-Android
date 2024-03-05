package hw.first.animations;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

public class MyCustomIndicator extends View {

    private final Drawable indicator;
    private Float deg = 0f;
    @ColorInt
    private final int color;

    public MyCustomIndicator(Context context) {
        this(context, null);
    }

    public MyCustomIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCustomIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyCustomIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        indicator = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_accessible_forward_24);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.MyCustomIndicator, defStyleAttr, defStyleRes);
        try {
            color = attributes.getColor(R.styleable.MyCustomIndicator_color, Color.BLUE);
        } finally {
            attributes.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int size = min(width, height);
        int c = size / 2;
        int xShift = -(size - width) / 2;
        int yShift = -(size - height) / 2;
        canvas.save();
        double rad = deg * Math.PI / 180;
        double scale = abs(2 * cos(rad));
        size = (int)(size*scale);
        canvas.rotate(deg, c + xShift, c + yShift);
        indicator.setBounds(xShift + c - size/10, yShift, xShift + c + size/10, yShift + size/5);
        indicator.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        indicator.draw(canvas);
        canvas.restore();
        nextFrame();
    }

    public void nextFrame() {
        deg += 1;
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, deg);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        deg = ((SavedState) state).deg;
        super.onRestoreInstanceState(savedState.getSuperState());
    }

    protected static class SavedState extends BaseSavedState {
        Float deg;

        public SavedState(Parcelable superState, float deg) {
            super(superState);
            this.deg = deg;
        }

        public SavedState(Parcel in) {
            super(in);
            deg = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(deg);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }



}
