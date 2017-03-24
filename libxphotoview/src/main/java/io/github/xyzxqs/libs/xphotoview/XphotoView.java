/**
 * Copyright 2017 xyzxqs (xyzxqs@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xyzxqs.libs.xphotoview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import io.github.xyzxqs.libs.xphotoview.GooglePhotosGestureDetector.GooglePhotosGestureListener;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

public class XphotoView extends MatrixImageView implements GooglePhotosGestureListener {
    private static final String TAG = XphotoView.class.getSimpleName();

    public interface Callback {
        /**
         * notify that the photo preview has been dismissed. you may want to call {@link Activity#finish()}
         * and {@link Activity#overridePendingTransition(int, int)} whit (0,0) args on this callback.
         */
        void onPreviewDismissed();

        /**
         * request update the background alpha.
         *
         * @param alpha the background alpha value, from 0.0f to 1.0f
         */
        void onReqUpdateBgAlpha(float alpha);
    }

    private static final float H_SPACE_CLOSE_WINDOW = 40;
    private static final float H_SPACE_THRESHOLD = 200;

    private GooglePhotosGestureDetector gestureDetector;
    private ScrollerCompat scrollerCompat;

    private Callback callback;
    private int initLeft;
    private int initTop;
    private int initWidth = 20;
    private int initHeight;

    private float laidOutScaleX = 1;

    private static final float[] scaleStepValues = {1f, 2f};
    private int scaleStepsIndex = 0;

    private float backgroundAlpha = 1.0f;

    private float initMotionY4Scroll;

    //is new motion event for single finger scroll
    private boolean isNew4SFScroll = true;
    //is in single finger scroll to change scale state
    private boolean isInSFScrollChangeScale = false;
    private boolean isClosing = false;
    private boolean isNewEvent4Scale = true;
    private boolean firstScaleIsZoomOut = true;
    private boolean isLongPhoto = false;
    private boolean isFitXYUpdating = false;
    private boolean isOnFling = false;
    private boolean isDoubleTapping = false;

    public XphotoView(Context context) {
        this(context, null);
    }

    public XphotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XphotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        gestureDetector = new GooglePhotosGestureDetector(context, this);
        scrollerCompat = ScrollerCompat.create(context);
    }

    /**
     * Set the photo args for animation.
     * <p>
     * Note: should call this before the image laid out.
     *
     * @param left     the point x of image left
     * @param top      the point y of image top
     * @param width    the image width
     * @param height   the image height
     * @param callback to handle the update when animation in process
     */
    public void setInitArgs(int left, int top, int width, int height, @NonNull Callback callback) {
        initLeft = left;
        initTop = top;
        initWidth = width;
        initHeight = height;
        this.callback = callback;
    }

    private boolean initArgsHasSet() {
        //if callback == null, init args not set.
        return callback != null;
    }

    /**
     * Dismiss the photo preview. when the animation finished, the {@link Callback#onPreviewDismissed()}
     * will callback.
     * <p>
     * Note: for this work, please set the photo init args by call
     * {@link #setInitArgs(int, int, int, int, Callback)} before the image laid out.
     */
    public void dismissPreview() {
        if (initArgsHasSet()) {
            new ImageMatrixAnimator.Builder(this)
                    .toRotate(0)
                    .toTranslateX(initLeft + initWidth / 2)
                    .toTranslateY(initTop + initHeight / 2)
                    .toScaleX(initWidth / drawableIntrinsicWidth)
                    .toScaleY(initHeight / drawableIntrinsicHeight)
                    .duration(300)
                    .build()
                    .addAnimatorListener(new ImageMatrixAnimator.SimpleAnimatorListener() {
                        @Override
                        public void onAnimationStart(ImageMatrixAnimator animation) {
                            isClosing = true;
                        }

                        @Override
                        public void onAnimationEnd(ImageMatrixAnimator animation) {
                            if (callback != null) {
                                callback.onPreviewDismissed();
                            }
                            animation.removeAnimatorListener(this);
                        }
                    })
                    .start();
        } else {
            Log.w(TAG, "dismissPreview: no init args set");
        }
    }

    @Override
    protected void onImageLaidOut() {
        super.onImageLaidOut();
        laidOutScaleX = imageViewWidth / drawableIntrinsicWidth;
        isLongPhoto = laidOutScaleX * drawableIntrinsicHeight > imageViewHeight;
        if (initArgsHasSet()) {
            ViewCompat.setAlpha(this, 0.0f);
            animateRect2FitView(initLeft, initTop, initWidth, initHeight);
        } else {
            scaleImageAtPosition(imageViewWidth / drawableIntrinsicWidth, 0, 0);
            float dx = 0;
            float dy = isLongPhoto ?
                    0
                    : (imageViewHeight - laidOutScaleX * drawableIntrinsicHeight) / 2;
            translateImageToPosition(dx, dy);
        }
    }

    @Override
    protected void onImageMatrixUpdated(Matrix imageMatrix) {
        super.onImageMatrixUpdated(imageMatrix);
        float scale = getImageScaleX();
        if (isClosing) {
            updateAlpha(calculateClosingAlpha());
        } else if (hasLaidOut && scale <= laidOutScaleX) {
            backgroundAlpha = calculateBackgroundAlpha();
            updateAlpha(backgroundAlpha);
        }
    }

    private void updateAlpha(float alpha) {
        if (callback != null) {
            callback.onReqUpdateBgAlpha(alpha);
        }
    }

    private float calculateClosingAlpha() {
        float scale = getImageScaleX();
        float factor = (scale * drawableIntrinsicWidth - initWidth) / (imageViewWidth - initWidth);
        return backgroundAlpha * factor;
    }

    private float calculateBackgroundAlpha() {
        float scale = getImageScaleX();
        float offCenterDistance = getOffCenterDistance();

        float dx = 300 - offCenterDistance;
        float scaleAlpha = scale / laidOutScaleX;
        float distanceAlpha = (offCenterDistance > 30 ? (dx > 30 ? dx : 30) : 300) / 300;

        return isLongPhoto ? scaleAlpha : Math.min(scaleAlpha, distanceAlpha);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            isDoubleTapping = true;
            float scale = getNextStepScale();
            animate2ZoomImage(scale, e.getX(), e.getY());
            return true;
        }
        return false;
    }

    private float getNextStepScale() {
        return scaleStepValues[((++scaleStepsIndex) % scaleStepValues.length)]
                * imageViewWidth / drawableIntrinsicWidth;
    }

    @Override
    public boolean onSingleFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (isNew4SFScroll) {
            initMotionY4Scroll = e2.getY();
            isNew4SFScroll = false;
        }
        float currScale = getImageScaleX();
        float offset, cx, cy;
        if (!isLongPhoto) {
            offset = getOffCenterDistance();
            cx = imageCenter[0];
            cy = imageCenter[1];
        } else {
            offset = getOverDistance(e2.getY());
            cx = imageViewWidth / 2;
            cy = imageViewHeight / 2;
        }
        if (offset < H_SPACE_THRESHOLD && currScale <= laidOutScaleX * 1.05) {
            isInSFScrollChangeScale = true;
            final float scale = (imageViewWidth - offset) / drawableIntrinsicWidth;
            scaleImageAtPosition(scale, cx, cy);
        }
        postTranslate(-distanceX, -distanceY);
        return true;
    }

    private float getOffCenterDistance() {
        return (float) Math.hypot(Math.abs(imageCenter[0] - imageViewWidth / 2),
                Math.abs(imageCenter[1] - imageViewHeight / 2));
    }

    private float getOverDistance(float currY) {
        float top = getImageTranslateY();
        float bottom = top + getImageScaleY() * drawableIntrinsicHeight;
        float dy = currY - initMotionY4Scroll;
        float overTop = 300;
        float overBottom = 300;

        if (dy > 0) {
            if (top >= 0) {
                overTop = dy;
            } else {
                isInSFScrollChangeScale = false;
            }
        } else {
            if (bottom <= imageViewHeight) {
                overBottom = -dy;
            } else {
                isInSFScrollChangeScale = false;
            }
        }

        return Math.min(overTop, overBottom);
    }

    @Override
    public boolean onMultiFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        postTranslate(-distanceX, -distanceY);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        final float h = getImageScaleY() * drawableIntrinsicHeight;
        final float w = getImageScaleX() * drawableIntrinsicWidth;
        int hs = (int) (imageViewHeight - h);
        int ws = (int) (imageViewWidth - w);
        int cx = (int) ((imageViewWidth - w) / 2);
        int cy = (int) ((imageViewHeight - h) / 2);
        if ((hs < 0 || ws < 0)
                && !isDoubleTapping
                && !isRotated()
                && !(isInSFScrollChangeScale && isLongPhoto)) {
            scrollerCompat.fling((int) getImageTranslateX(), (int) getImageTranslateY(),
                    (int) velocityX,
                    (int) velocityY,
                    ws < 0 ? ws : cx,
                    ws < 0 ? 0 : cx,
                    hs < 0 ? hs : cy,
                    hs < 0 ? 0 : cy,
                    80,
                    80);
            isOnFling = true;
            invalidate();
        }
        return hs < 0 || ws < 0;
    }

    @Override
    public void computeScroll() {
        if (isOnFling && !isClosing && !isRotated()) {
            if (isDoubleTapping) {
                scrollerCompat.abortAnimation();
                isOnFling = false;
                return;
            }
            if (scrollerCompat.computeScrollOffset()) {
                float x = scrollerCompat.getCurrX();
                float y = scrollerCompat.getCurrY();
                translateImageToPosition(x, y);
                invalidate();
            } else {
                isOnFling = false;
                animate2FitXYIfNeed(isLongPhoto);
            }
        }
    }

    private boolean isRotated() {
        return Math.abs(getImageAngle()) > 1;
    }

    @Override
    public boolean onScale(float scaleFactor, float focusX, float focusY) {
        postScale(scaleFactor, focusX, focusY);
        if (isNewEvent4Scale) {
            firstScaleIsZoomOut = getImageScaleX() < laidOutScaleX;
            isNewEvent4Scale = false;
        }
        return true;
    }


    @Override
    public boolean onRotation(float deltaDegree, float cx, float cy) {
        boolean rotatable = firstScaleIsZoomOut && !isLongPhoto;
        if (rotatable) {
            postRotate(deltaDegree, cx, cy);
        }
        return rotatable;
    }

    @Override
    public void onActionDown(MotionEvent event) {
        scrollerCompat.abortAnimation();
    }

    @Override
    public boolean onActionUp(MotionEvent event) {
        isNewEvent4Scale = true;
        isNew4SFScroll = true;
        isInSFScrollChangeScale = false;
        if (!isDoubleTapping) {
            float scale = getImageScaleX();
            if (scale * drawableIntrinsicWidth <= imageViewWidth - (H_SPACE_THRESHOLD - H_SPACE_CLOSE_WINDOW)) {
                if (initArgsHasSet()) {
                    dismissPreview();
                } else if (!isOnFling) {
                    //if the init args not set, just animate to fit image view.
                    animate2FitView();
                }
            } else if (!isOnFling) {
                if ((Math.abs(getImageAngle()) > 10 || scale < laidOutScaleX) && !isLongPhoto) {
                    animate2FitView();
                } else {
                    animate2FitXYIfNeed(scale < laidOutScaleX);
                }
            }
            return true;
        }
        isDoubleTapping = false;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) | super.onTouchEvent(event);
    }

    /*
    private interface XYOverCheckerCallback {
        void onOverChecked(float left, float top, float right, float bottom);
    }

    private void xyOverCheck(float x, float y, XYOverCheckerCallback callback) {
        final float h = getImageScaleY() * drawableIntrinsicHeight;
        final float w = getImageScaleX() * drawableIntrinsicWidth;
        final float l = x - 0;
        final float t = y - 0;
        final float r = imageViewWidth - (x + w);
        final float b = imageViewHeight - (y + h);
        callback.onOverChecked(l, t, r, b);
    }
    */

    private interface XYCheckerCallback {
        void onNeedUpdate(boolean xNeed, float centerX, boolean yNeed, float centerY);
    }

    private void xyCheck(XYCheckerCallback callback) {
        final float h = getImageScaleY() * drawableIntrinsicHeight;
        final float w = getImageScaleX() * drawableIntrinsicWidth;
        boolean xNeed = true, yNeed = true;

        float cy;
        if (h > imageViewHeight) {
            if (imageCorners[1] > 0) {
                cy = h / 2;
            } else if (imageCorners[5] < imageViewHeight) {
                cy = imageViewHeight - h / 2;
            } else {
                cy = imageCenter[1];
                yNeed = false;
            }
        } else {
            cy = imageViewHeight / 2;
        }

        float cx;
        if (w > imageViewWidth) {
            if (imageCorners[0] > 0) {
                cx = w / 2;
            } else if (imageCorners[2] < imageViewWidth) {
                cx = imageViewWidth - w / 2;
            } else {
                cx = imageCenter[0];
                xNeed = false;
            }
        } else {
            cx = imageViewWidth / 2;
        }

        callback.onNeedUpdate(xNeed, cx, yNeed, cy);
    }

    private void animate2FitXYIfNeed(final boolean fitWidth) {
        if (!isFitXYUpdating) {
            xyCheck(new XYCheckerCallback() {
                @Override
                public void onNeedUpdate(boolean xNeed, float centerX, boolean yNeed, float centerY) {
                    if (xNeed || yNeed) {
                        isFitXYUpdating = true;
                        ImageMatrixAnimator.Builder builder = new ImageMatrixAnimator.Builder(XphotoView.this);
                        builder.toRotate(0);
                        if (xNeed) builder.toTranslateX(centerX);
                        if (yNeed) builder.toTranslateY(centerY);
                        if (fitWidth) {
                            float h = laidOutScaleX * drawableIntrinsicHeight;
                            builder.toScaleX(imageViewWidth / drawableIntrinsicWidth);
                            builder.toScaleY(h / drawableIntrinsicHeight);
                        }
                        builder.duration(200)
                                .build()
                                .addAnimatorListener(new ImageMatrixAnimator.SimpleAnimatorListener() {

                                    @Override
                                    public void onAnimationEnd(ImageMatrixAnimator animation) {
                                        isFitXYUpdating = false;
                                    }
                                })
                                .start();
                    }
                }
            });
        }
    }

    private void animateRect2FitView(int initLeft, int initTop, int initWidth, int initHeight) {
        float h = laidOutScaleX * drawableIntrinsicHeight;
        float endLeft = 0;
        float endTop = isLongPhoto ? 0 : (imageViewHeight - h) / 2;

        new ImageMatrixAnimator.Builder(this)
                .fromTranslateX(initLeft + initWidth / 2)
                .fromTranslateY(initTop + initHeight / 2)
                .fromScaleX(initWidth / drawableIntrinsicWidth)
                .fromScaleY(initHeight / drawableIntrinsicHeight)
                .toTranslateX(endLeft + imageViewWidth / 2)
                .toTranslateY(endTop + h / 2)
                .toScaleX(imageViewWidth / drawableIntrinsicWidth)
                .toScaleY(h / drawableIntrinsicHeight)
                .toRotate(0)
                .duration(300)
                .build()
                .addAnimatorListener(new ImageMatrixAnimator.SimpleAnimatorListener() {
                    @Override
                    public void onAnimationStart(ImageMatrixAnimator animation) {
                        ViewCompat.setAlpha(XphotoView.this, 1.0f);
                    }
                })
                .start();
    }

    private void animate2FitView() {
        float h = imageViewWidth / drawableIntrinsicWidth * drawableIntrinsicHeight;
        float endTop = isLongPhoto ? 0 : (imageViewHeight - h) / 2;
        new ImageMatrixAnimator.Builder(this)
                .toRotate(0)
                .toTranslateX(imageViewWidth / 2)
                .toTranslateY(endTop + h / 2)
                .toScaleX(imageViewWidth / drawableIntrinsicWidth)
                .toScaleY(imageViewWidth / drawableIntrinsicWidth)
                .duration(250)
                .build()
                .start();
    }

    private void animate2ZoomImage(float scale, float centerX, float centerY) {
        new ImageMatrixAnimator.Builder(this)
                .toScaleX(scale)
                .toScaleY(scale)
                .setScaleInterpolator(new EaseInOutInterpolator())
                .setAnimCenter(centerX, centerY)
                .duration(220)
                .build()
                .start();
    }
}
