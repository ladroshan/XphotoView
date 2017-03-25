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

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixAngle;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixScaleX;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixScaleY;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixTranslateX;
import static io.github.xyzxqs.libs.xphotoview.MatrixUtils.getMatrixTranslateY;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

class ImageMatrixAnimator {
    public interface AnimatorListener {
        void onAnimationStart(ImageMatrixAnimator animation);

        void onAnimationEnd(ImageMatrixAnimator animation);

        void onAnimationCancel(ImageMatrixAnimator animation);

        void onAnimationRepeat(ImageMatrixAnimator animation);
    }

    public abstract static class SimpleAnimatorListener implements AnimatorListener {
        public void onAnimationStart(ImageMatrixAnimator animation) {
        }

        public void onAnimationEnd(ImageMatrixAnimator animation) {
        }

        public void onAnimationCancel(ImageMatrixAnimator animation) {
        }

        public void onAnimationRepeat(ImageMatrixAnimator animation) {
        }
    }

    private ImageMatrixAnimator() {
    }

    public void start() {
        //impl by subclass
    }

    public void startDelayed(long ms) {
        //impl by subclass
    }

    public boolean isRunning() {
        //impl by subclass
        throw new IllegalStateException("do not call super in subclass");
    }

    public void cancel() {
        //impl by subclass
    }

    public ImageMatrixAnimator addAnimatorListener(AnimatorListener listener) {
        //impl by subclass
        throw new IllegalStateException("do not call super in subclass");
    }

    public void removeAnimatorListener(AnimatorListener listener) {
        //impl by subclass
    }

    public void removeAllListeners() {
        //impl by subclass
    }

    public static class Builder {

        private static Interpolator defaultInterpolator = new AccelerateDecelerateInterpolator();

        private WeakReference<ImageView> viewRef;

        private float fromDegrees;
        private float toDegrees;
        private Interpolator angleInterpolator;

        private float animCenterX;
        private float animCenterY;

        private float fromScaleX;
        private float fromScaleY;
        private float toScaleX;
        private float toScaleY;

        private Interpolator scaleInterpolator;

        private float fromTranslateX;
        private float fromTranslateY;
        private float toTranslateX;
        private float toTranslateY;

        private boolean ftxIsSet = false;
        private boolean ftyIsSet = false;
        private boolean ttxIsSet = false;
        private boolean ttyIsSet = false;

        private Interpolator transXInterpolator;
        private Interpolator transYInterpolator;

        private int repeatCount = 1;

        private long durationMs = 200;

        public Builder(ImageView imageView) {
            viewRef = new WeakReference<>(imageView);
            if (imageView.getScaleType() != ImageView.ScaleType.MATRIX) {
                throw new IllegalStateException("the image scaleType must be ScaleType.MATRIX");
            }

            Drawable d = imageView.getDrawable();
            if (d != null) {
                animCenterX = d.getIntrinsicWidth() / 2;
                animCenterY = d.getIntrinsicHeight() / 2;
            }

            Matrix matrix = new Matrix(imageView.getImageMatrix());
            fromDegrees = toDegrees = MatrixUtils.getMatrixAngle(matrix);
            fromScaleX = toScaleX = getMatrixScaleX(matrix);
            fromScaleY = toScaleY = getMatrixScaleY(matrix);

            float[] txty = calculateTxTy();
            fromTranslateX = toTranslateX = txty[0];
            fromTranslateY = toTranslateY = txty[1];

        }

        /**
         * Set the animation center point, the fixed point of image view with image matrix values:
         * from rotate, from scaleX/Y and from translateX/Y.
         *
         * @param x the (fixed point) center x.
         * @param y the (fixed point) center y.
         * @return this {@link ImageMatrixAnimator.Builder}.
         */
        public Builder setAnimCenter(float x, float y) {
            Matrix matrix = new Matrix(viewRef.get().getImageMatrix());

            float angle = getMatrixAngle(matrix);

            matrix.postRotate(-angle, x, y);

            float tx = getMatrixTranslateX(matrix);
            float ty = getMatrixTranslateY(matrix);

            animCenterX = (x - tx) / fromScaleX;
            animCenterY = (y - ty) / fromScaleY;
            float[] txty = calculateTxTy();
            if (!ftxIsSet) {
                fromTranslateX = txty[0];
            }

            if (!ftyIsSet) {
                fromTranslateY = txty[1];
            }

            if (!ttxIsSet) {
                toTranslateX = txty[0];
            }

            if (!ttyIsSet) {
                toTranslateY = txty[1];
            }
            return this;
        }

        private float[] calculateTxTy() {
            Matrix matrix = new Matrix(viewRef.get().getImageMatrix());
            float[] animCenter = {animCenterX, animCenterY};

            float[] center = new float[2];

            Matrix m = new Matrix();

            float sx = getMatrixScaleX(m);
            float sy = getMatrixScaleY(m);
            m.mapPoints(center, animCenter);
            m.postScale(fromScaleX / sx, fromScaleY / sy, center[0], center[1]);

            float tx0 = getMatrixTranslateX(m);
            float ty0 = getMatrixTranslateY(m);

            m.mapPoints(center, animCenter);
            m.postRotate(fromDegrees, center[0], center[1]);

            float tx1 = getMatrixTranslateX(m);
            float ty1 = getMatrixTranslateY(m);

            float dx = tx1 - tx0;
            float dy = ty1 - ty0;

            float transX = getMatrixTranslateX(matrix) + animCenterX * fromScaleX - dx;
            float transY = getMatrixTranslateY(matrix) + animCenterY * fromScaleY - dy;
            return new float[]{transX, transY};
        }

        public Builder fromScaleX(float scaleX) {
            fromScaleX = scaleX;
            return this;
        }

        public Builder fromScaleY(float scaleY) {
            fromScaleY = scaleY;
            return this;
        }

        public Builder fromTranslateX(float transX) {
            fromTranslateX = transX;
            ftxIsSet = true;
            return this;
        }

        public Builder fromTranslateY(float transY) {
            fromTranslateY = transY;
            ftyIsSet = true;
            return this;
        }

        public Builder fromRotate(float degrees) {
            fromDegrees = degrees;
            return this;
        }

        public Builder toScaleX(float scaleX) {
            toScaleX = scaleX;
            return this;
        }

        public Builder toScaleY(float scaleY) {
            toScaleY = scaleY;
            return this;
        }

        public Builder toTranslateX(float transX) {
            toTranslateX = transX;
            ttxIsSet = true;
            return this;
        }

        public Builder toTranslateY(float transY) {
            toTranslateY = transY;
            ttyIsSet = true;
            return this;
        }

        public Builder toRotate(float degrees) {
            toDegrees = degrees;
            return this;
        }

        public Builder duration(long ms) {
            durationMs = ms;
            return this;
        }

        public Builder repeat(int count) {
            repeatCount = count;
            return this;
        }

        public Builder setAngleInterpolator(Interpolator angleInterpolator) {
            this.angleInterpolator = angleInterpolator;
            return this;
        }

        public Builder setScaleInterpolator(Interpolator scaleInterpolator) {
            this.scaleInterpolator = scaleInterpolator;
            return this;
        }

        public Builder setTransXInterpolator(Interpolator transXInterpolator) {
            this.transXInterpolator = transXInterpolator;
            return this;
        }

        public Builder setTransYInterpolator(Interpolator transYInterpolator) {
            this.transYInterpolator = transYInterpolator;
            return this;
        }

        public ImageMatrixAnimator build() {

            if (angleInterpolator == null) {
                angleInterpolator = defaultInterpolator;
            }

            if (scaleInterpolator == null) {
                scaleInterpolator = defaultInterpolator;
            }

            if (transXInterpolator == null) {
                transXInterpolator = defaultInterpolator;
            }

            if (transYInterpolator == null) {
                transYInterpolator = defaultInterpolator;
            }

            return new ImageMatrixAnimatorReal(this);
        }
    }


    private static class ImageMatrixAnimatorReal extends ImageMatrixAnimator implements Runnable {
        private static final String TAG = ImageMatrixAnimator.class.getSimpleName();

        private final List<AnimatorListener> listenerList = new ArrayList<>();

        private final Matrix matrix = new Matrix();

        private final WeakReference<ImageView> viewRef;

        private final float fromDegrees;
        private final float toDegrees;

        private final Interpolator angleInterpolator;

        private final float fromScaleX;
        private final float fromScaleY;
        private final float toScaleX;
        private final float toScaleY;

        private final Interpolator scaleInterpolator;

        private final float fromTranslateX;
        private final float fromTranslateY;
        private final float toTranslateX;
        private final float toTranslateY;

        private final Interpolator transXInterpolator;
        private final Interpolator transYInterpolator;

        private long startTime;

        private float durationMs;

        private int repeat;

        private boolean isStarted = false;

        private final float[] animCenter = new float[2];
        private final float[] center = new float[2];

        ImageMatrixAnimatorReal(Builder builder) {
            super();
            this.viewRef = builder.viewRef;

            this.fromDegrees = builder.fromDegrees;
            this.toDegrees = builder.toDegrees;
            this.animCenter[0] = builder.animCenterX;
            this.animCenter[1] = builder.animCenterY;

            this.angleInterpolator = builder.angleInterpolator;

            this.fromScaleX = builder.fromScaleX;
            this.fromScaleY = builder.fromScaleY;
            this.toScaleX = builder.toScaleX;
            this.toScaleY = builder.toScaleY;

            this.scaleInterpolator = builder.scaleInterpolator;

            this.fromTranslateX = builder.fromTranslateX;
            this.fromTranslateY = builder.fromTranslateY;
            this.toTranslateX = builder.toTranslateX;
            this.toTranslateY = builder.toTranslateY;

            this.transXInterpolator = builder.transXInterpolator;
            this.transYInterpolator = builder.transYInterpolator;

            this.repeat = builder.repeatCount;

            this.durationMs = builder.durationMs;
        }

        @Override
        public void run() {
            ImageView view = viewRef.get();
            if (view == null) {
                return;
            }

            if (!isStarted && repeat > 0) {
                isStarted = true;
                startTime = System.currentTimeMillis();
                repeat--;
                notifyListeners(new Callback() {
                    @Override
                    public void action(AnimatorListener listener) {
                        listener.onAnimationStart(ImageMatrixAnimatorReal.this);
                    }
                });
            }

            long currentTime = System.currentTimeMillis();

            float t = Math.min(durationMs, currentTime - startTime);

            float fraction = t / durationMs;

            matrix.reset();

            float transX = getInterpolation(transXInterpolator, fromTranslateX, toTranslateX, fraction);
            float transY = getInterpolation(transYInterpolator, fromTranslateY, toTranslateY, fraction);

            float scaleX = getInterpolation(scaleInterpolator, fromScaleX, toScaleX, fraction);
            float scaleY = getInterpolation(scaleInterpolator, fromScaleY, toScaleY, fraction);

            float angle = getInterpolation(angleInterpolator, fromDegrees, toDegrees, fraction);

            float sx = getMatrixScaleX(matrix);
            float sy = getMatrixScaleY(matrix);
            matrix.mapPoints(center, animCenter);
            matrix.postScale(scaleX / sx, scaleY / sy, center[0], center[1]);

            float tx0 = getMatrixTranslateX(matrix);
            float ty0 = getMatrixTranslateY(matrix);

            matrix.mapPoints(center, animCenter);

            matrix.postRotate(angle, center[0], center[1]);

            float tx1 = getMatrixTranslateX(matrix);
            float ty1 = getMatrixTranslateY(matrix);

            //offset x/y = laid out(0,0) to laid out center + to target center - to target(0, 0) + offset case by rotation
            float tx = (animCenter[0] - tx0) + (transX - (center[0] - tx0)) - (center[0] - tx0) + (tx1 - tx0);
            float ty = (animCenter[1] - ty0) + (transY - (center[1] - ty0)) - (center[1] - ty0) + (ty1 - ty0);

            matrix.postTranslate(tx - tx1, ty - ty1);

            view.setImageMatrix(matrix);

            if (t < durationMs) {
                ViewCompat.postOnAnimation(view, this);
            } else if (repeat > 0) {
                isStarted = false;
                ViewCompat.postOnAnimation(view, this);

                notifyListeners(new Callback() {
                    @Override
                    public void action(AnimatorListener listener) {
                        listener.onAnimationRepeat(ImageMatrixAnimatorReal.this);
                    }
                });
            } else {
                //make it can start again
                isStarted = false;
                repeat = 1;

                notifyListeners(new Callback() {
                    @Override
                    public void action(AnimatorListener listener) {
                        listener.onAnimationEnd(ImageMatrixAnimatorReal.this);
                    }
                });
            }
        }

        private float getInterpolation(Interpolator interpolator, float start, float end, float fraction) {
            if (start == end || fraction == 1) {
                return end;
            } else {
                return start + (end - start) * interpolator.getInterpolation(fraction);
            }
        }

        @Override
        public void start() {
            View v = viewRef.get();
            if (v != null) {
                ViewCompat.postOnAnimation(v, this);
            }
        }

        @Override
        public void startDelayed(long delayMillis) {
            View v = viewRef.get();
            if (v != null) {
                ViewCompat.postOnAnimationDelayed(v, this, delayMillis);
            }
        }

        @Override
        public boolean isRunning() {
            return System.currentTimeMillis() - startTime < durationMs;
        }

        @Override
        public void cancel() {
            View v = viewRef.get();
            if (v != null) {
                v.removeCallbacks(this);

                notifyListeners(new Callback() {
                    @Override
                    public void action(AnimatorListener listener) {
                        listener.onAnimationCancel(ImageMatrixAnimatorReal.this);
                    }
                });
            }
        }

        @Override
        public ImageMatrixAnimator addAnimatorListener(AnimatorListener listener) {
            listenerList.add(listener);
            return this;
        }

        @Override
        public void removeAnimatorListener(AnimatorListener listener) {
            listenerList.remove(listener);
        }

        @Override
        public void removeAllListeners() {
            listenerList.clear();
        }

        private void notifyListeners(Callback callback) {
            for (AnimatorListener listener : listenerList) {
                callback.action(listener);
            }
        }

        private interface Callback {
            void action(AnimatorListener listener);
        }
    }
}
