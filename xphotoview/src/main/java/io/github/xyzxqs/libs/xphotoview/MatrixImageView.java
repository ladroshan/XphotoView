/*
  Copyright 2017 xyzxqs (xyzxqs@gmail.com)
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package io.github.xyzxqs.libs.xphotoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixAngle;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixScaleX;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixScaleY;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixTranslateX;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixTranslateY;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

class MatrixImageView extends AppCompatImageView {

    protected final float[] imageCorners = new float[8];
    protected final float[] imageCenter = new float[2];

    protected float drawableIntrinsicWidth;
    protected float drawableIntrinsicHeight;

    protected float imageViewWidth;
    protected float imageViewHeight;

    private static final String TAG = MatrixImageView.class.getSimpleName();

    private final Matrix imageMatrix = new Matrix();

    private final RectF imageBound = new RectF();

    private float[] initImageCorners;
    private float[] initImageCenter;

    private float imageAngle;
    private float imageScaleX;
    private float imageScaleY;
    private float imageTranslateX;
    private float imageTranslateY;

    protected boolean hasLaidOut = false;

    public MatrixImageView(Context context) {
        this(context, null);
    }

    public MatrixImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatrixImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
    }

    @CallSuper
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        Drawable d = getDrawable();
        if (d != null && !hasLaidOut) {

            imageViewWidth = right - left;
            imageViewHeight = bottom - top;

            drawableIntrinsicWidth = d.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
            drawableIntrinsicHeight = d.getIntrinsicHeight() + getPaddingTop() + getPaddingBottom();

            imageBound.set(0, 0, drawableIntrinsicWidth, drawableIntrinsicHeight);
            initImageCorners = new float[]{
                    imageBound.left, imageBound.top,
                    imageBound.right, imageBound.top,
                    imageBound.right, imageBound.bottom,
                    imageBound.left, imageBound.bottom
            };
            initImageCenter = new float[]{
                    imageBound.centerX(), imageBound.centerY()
            };
            hasLaidOut = true;
            onImageLaidOut();
        }
    }

    @CallSuper
    protected void onImageLaidOut() {
    }

    @CallSuper
    protected void onImageRotate(float angle) {
    }

    @CallSuper
    protected void onImageScale(float scaleX, float scaleY) {
    }

    @CallSuper
    protected void onImageTranslate(float translateX, float translateY) {
    }


    @CallSuper
    @Override
    public void setImageMatrix(Matrix matrix) {
        super.setImageMatrix(matrix);
        imageMatrix.set(matrix);
        onImageMatrixUpdated(imageMatrix);
        updateImagePoints();
    }

    @CallSuper
    protected void onImageMatrixUpdated(Matrix imageMatrix) {
        float angle = getImageAngle();
        if (angle != imageAngle) {
            imageAngle = angle;
            onImageRotate(angle);
        }

        float scaleX = getImageScaleX();
        float scaleY = getImageScaleY();
        if (imageScaleX != scaleX || imageScaleY != scaleY) {
            imageScaleX = scaleX;
            imageScaleY = scaleY;
            onImageScale(scaleX, scaleY);
        }

        float transX = getImageTranslateX();
        float transY = getImageTranslateY();
        if (imageTranslateX != transX || imageTranslateY != transY) {
            imageTranslateX = transX;
            imageTranslateY = transY;
            onImageTranslate(transX, transY);
        }
    }

    /**
     * Only {@link ScaleType#MATRIX} can be used
     *
     * @param scaleType
     */
    @CallSuper
    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType);
        } else {
            Log.w(TAG, "Invalid ScaleType. Only ScaleType.MATRIX can be used");
        }
    }

    /**
     * Translate ImageView's drawable (not this view itself) to given position(x,y), relative to the drawable's left top
     * corner.
     *
     * @param x the ImageView drawable's left
     * @param y the ImageView drawable's top
     */
    public void translateImageToPosition(float x, float y) {
        postTranslate(x - getImageTranslateX(), y - getImageTranslateY());
    }

    /**
     * Scale ImageView's drawable (not this view itself) at given position.
     *
     * @param scale   scale the drawable to
     * @param centerX the x of fixed point
     * @param centerY the y of fixed point
     */
    public void scaleImageAtPosition(float scale, float centerX, float centerY) {
        postScale(scale / getImageScaleX(), centerX, centerY);
    }

    /**
     * Rotate ImageView's drawable (not this view itself) at given position
     *
     * @param degrees degrees value the ImageView rotate
     * @param centerX the x of fixed point
     * @param centerY the y of fixed point
     */
    public void rotateImageAtPositon(float degrees, float centerX, float centerY) {
        postRotate(degrees - getImageAngle(), centerX, centerY);
    }

    @CallSuper
    protected void postTranslate(float deltaX, float deltaY) {
        if (deltaX != 0 || deltaY != 0) {
            imageMatrix.postTranslate(deltaX, deltaY);
            setImageMatrix(imageMatrix);
        }

    }

    @CallSuper
    protected void postScale(float deltaScale, float px, float py) {
        if (deltaScale != 0) {
            imageMatrix.postScale(deltaScale, deltaScale, px, py);
            setImageMatrix(imageMatrix);
        }
    }

    @CallSuper
    protected void postScale(float deltaScaleX, float deltaScaleY, float px, float py) {
        if (deltaScaleX != 0 && deltaScaleY != 0) {
            imageMatrix.postScale(deltaScaleX, deltaScaleY, px, py);
            setImageMatrix(imageMatrix);
        }
    }

    @CallSuper
    protected void postRotate(float deltaDegrees, float px, float py) {
        if (deltaDegrees != 0) {
            imageMatrix.postRotate(deltaDegrees, px, py);
            setImageMatrix(imageMatrix);
        }
    }

    /**
     * Get the image drawable's (not this view itself) scaleX
     *
     * @return scaleX
     */
    public final float getImageScaleX() {
        return getMatrixScaleX(imageMatrix);
    }

    /**
     * Get the image drawable's (not this view itself) scaleY
     *
     * @return scaleY
     */
    public final float getImageScaleY() {
        return getMatrixScaleY(imageMatrix);
    }

    /**
     * Get the image drawable's (not this view itself) rotate angle
     *
     * @return rotate angle
     */
    public final float getImageAngle() {
        return getMatrixAngle(imageMatrix);
    }

    /**
     * Get the image drawable's (not this view itself) translateX
     *
     * @return translateX
     */
    public final float getImageTranslateX() {
        return getMatrixTranslateX(imageMatrix);
    }

    /**
     * Get the image drawable's (not this view itself) translateY
     *
     * @return translateY
     */
    public final float getImageTranslateY() {
        return getMatrixTranslateY(imageMatrix);
    }

    private void updateImagePoints() {
        if (hasLaidOut) {
            imageMatrix.mapPoints(imageCorners, initImageCorners);
            imageMatrix.mapPoints(imageCenter, initImageCenter);
        }
    }
}
