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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

class GooglePhotosGestureDetector {

    public interface GooglePhotosGestureListener {

        /**
         * Notified when an event within a double-tap gesture occurs, including
         * the down, move, and up events.
         *
         * @param event The motion event that occurred during the double-tap gesture.
         * @return true if the event is consumed, else false
         */
        boolean onDoubleTapEvent(MotionEvent event);

        boolean onSingleFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        boolean onMultiFingerScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

        boolean onScale(float scaleFactor, float focusX, float focusY);

        boolean onRotation(float deltaDegree, float cx, float cy);

        void onActionDown(MotionEvent event);

        boolean onActionUp(MotionEvent event);
    }

    private static class GestureHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TAP:
                case MSG_SCROLL:
                    //do nothing
                    break;
            }
        }
    }

    private static final String TAG = "GestureDebug";
    private static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static final long DOUBLE_TAP_MIN_TIME = 40;
    private static final int INVALID_POINTER_INDEX = -1;
    private static final int MSG_TAP = 1;
    private static final int MSG_SCROLL = 2;
    private static final long SCROLL_STATE_CHANGE_WINDOW = 40;

    private final float DOUBLE_TAP_SLOP;
    private final float SCALE_SPAN_SLOP;
    private final float MIN_FLING_VELOCITY;
    private final float MAX_FLING_VELOCITY;


    private GooglePhotosGestureListener gestureListener;
    private VelocityTracker velocityTracker;
    private GestureHandler gestureHandler;

    private MotionEvent currentDownEvent;
    private MotionEvent previousUpEvent;

    private float currFocusX;
    private float currFocusY;
    private float lastFocusX;
    private float lastFocusY;

    private float initSpan;
    private float currSpan;
    private float prevSpan;

    private float angle;
    private int pointerIndex1 = INVALID_POINTER_INDEX;
    private int pointerIndex2 = INVALID_POINTER_INDEX;
    private float fX, fY, sX, sY;
    private boolean isNewState4Rotation = true;

    //or false for multi finger scroll
    private boolean inSingleFingerScroll;
    private boolean isNewState4Scroll = true;

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events). Can only be true if there is a double tap listener attached.
     */
    private boolean isDoubleTapping = false;


    public GooglePhotosGestureDetector(@Nullable Context context, @NonNull GooglePhotosGestureListener listener) {
        this.gestureListener = listener;
        gestureHandler = new GestureHandler();
        if (context == null) {
            MIN_FLING_VELOCITY = ViewConfiguration.getMinimumFlingVelocity();
            MAX_FLING_VELOCITY = ViewConfiguration.getMaximumFlingVelocity();
            DOUBLE_TAP_SLOP = 100;
            SCALE_SPAN_SLOP = 16;
        } else {
            ViewConfiguration config = ViewConfiguration.get(context);
            MIN_FLING_VELOCITY = config.getScaledMinimumFlingVelocity();
            MAX_FLING_VELOCITY = config.getScaledMaximumFlingVelocity();
            DOUBLE_TAP_SLOP = config.getScaledDoubleTapSlop();
            SCALE_SPAN_SLOP = config.getScaledTouchSlop() * 2;
        }
    }

    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
        calculateCurrFocusAndSpan(ev);

        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                gestureListener.onActionDown(ev);
                updateLastFocus();
                spanConfigChanged();
                checkDoubleTapGesture(currentDownEvent, previousUpEvent, ev);
                if (isDoubleTapping) {
                    handled |= gestureListener.onDoubleTapEvent(ev);
                }
                updateCurrDownEvent(ev);
                pointerIndex1 = ev.findPointerIndex(ev.getPointerId(0));
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                updateLastFocus();
                spanConfigChanged();
                pointerIndex2 = ev.findPointerIndex(ev.getPointerId(1));
                break;
            case MotionEvent.ACTION_MOVE:
                handled |= checkOnScroll(ev);
                updateLastFocus();
                if (isDoubleTapping) {
                    handled |= gestureListener.onDoubleTapEvent(ev);
                }
                if (!inSingleFingerScroll) {
                    handled |= checkOnScale(ev);
                    handled |= checkOnRotation(ev);
                } else {
                    spanConfigChanged();
                }
                prevSpan = currSpan;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isNewState4Rotation = true;
                updateLastFocus();
                spanConfigChanged();
                break;
            case MotionEvent.ACTION_UP:
                isNewState4Scroll = true;
                isNewState4Rotation = true;
                if (isDoubleTapping) {
                    handled |= gestureListener.onDoubleTapEvent(ev);
                    isDoubleTapping = false;
                } else {
                    handled |= checkOnFlingGesture(ev);
                }
                handled |= gestureListener.onActionUp(ev);
                updatePrevUpEvent(ev);
                pointerIndex1 = INVALID_POINTER_INDEX;
                pointerIndex2 = INVALID_POINTER_INDEX;
                break;
            case MotionEvent.ACTION_CANCEL:
                cancel();
                break;
        }
        return handled;
    }

    private boolean checkOnRotation(MotionEvent event) {
        if (pointerIndex1 != INVALID_POINTER_INDEX && pointerIndex2 != INVALID_POINTER_INDEX && event.getPointerCount() > pointerIndex2) {
            float nfX, nfY, nsX, nsY;

            nsX = event.getX(pointerIndex1);
            nsY = event.getY(pointerIndex1);
            nfX = event.getX(pointerIndex2);
            nfY = event.getY(pointerIndex2);
            if (isNewState4Rotation) {
                angle = 0;
                isNewState4Rotation = false;
            } else {
                calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY);
            }
            fX = nfX;
            fY = nfY;
            sX = nsX;
            sY = nsY;
            return gestureListener.onRotation(angle, currFocusX, currFocusY);
        }
        return false;
    }

    private float calculateAngleBetweenLines(float fx1, float fy1, float fx2, float fy2,
                                             float sx1, float sy1, float sx2, float sy2) {
        return calculateAngleDelta(
                (float) Math.toDegrees((float) Math.atan2((fy1 - fy2), (fx1 - fx2))),
                (float) Math.toDegrees((float) Math.atan2((sy1 - sy2), (sx1 - sx2))));
    }

    private float calculateAngleDelta(float angleFrom, float angleTo) {
        angle = angleTo % 360.0f - angleFrom % 360.0f;

        if (angle < -180.0f) {
            angle += 360.0f;
        } else if (angle > 180.0f) {
            angle -= 360.0f;
        }

        return angle;
    }

    private boolean checkOnScale(MotionEvent ev) {
        if (Math.abs(currSpan - initSpan) > SCALE_SPAN_SLOP) {
            float scaleFactor = prevSpan > 0 ? currSpan / prevSpan : 1;
            return gestureListener.onScale(scaleFactor, currFocusX, currFocusY);
        } else {
            return false;
        }
    }

    private boolean checkOnScroll(MotionEvent ev) {
        final float scrollX = lastFocusX - currFocusX;
        final float scrollY = lastFocusY - currFocusY;

        float absDx = Math.abs(scrollX);
        float absDy = Math.abs(scrollY);
        if ((absDx >= 1 || absDy >= 1)) {

            if (isNewState4Scroll || gestureHandler.hasMessages(MSG_SCROLL)) {
                isNewState4Scroll = false;
                int count = ev.getPointerCount();
                if (count > 1) {
                    inSingleFingerScroll = false;
                    gestureHandler.removeMessages(MSG_SCROLL);
                } else {
                    inSingleFingerScroll = true;
                    gestureHandler.sendEmptyMessageDelayed(MSG_SCROLL, SCROLL_STATE_CHANGE_WINDOW);
                }
            }

            if (inSingleFingerScroll) {
                return gestureListener.onSingleFingerScroll(currentDownEvent, ev, scrollX, scrollY);
            } else {
                return gestureListener.onMultiFingerScroll(currentDownEvent, ev, scrollX, scrollY);
            }
        } else {
            return false;
        }
    }

    private void spanConfigChanged() {
        initSpan = prevSpan = currSpan;
    }

    private void updateLastFocus() {
        lastFocusX = currFocusX;
        lastFocusY = currFocusY;
    }

    private void checkDoubleTapGesture(@Nullable MotionEvent prevDown,
                                       @Nullable MotionEvent prevUp,
                                       @NonNull MotionEvent currDown) {
        boolean hasTabMsg = gestureHandler.hasMessages(MSG_TAP);
        if (prevDown != null && prevUp != null
                && hasTabMsg
                && isConsideredDoubleTap(prevDown, prevUp, currDown)) {
            // This is a second tap
            isDoubleTapping = true;
        } else {
            // This is a first tap
            gestureHandler.sendEmptyMessageDelayed(MSG_TAP, DOUBLE_TAP_TIMEOUT);
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown,
                                          MotionEvent firstUp,
                                          MotionEvent secondDown) {
        final long deltaTime = secondDown.getEventTime() - firstUp.getEventTime();
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        return Math.hypot(deltaX, deltaY) < DOUBLE_TAP_SLOP;
    }

    private void updatePrevUpEvent(MotionEvent ev) {
        if (previousUpEvent != null) {
            previousUpEvent.recycle();
        }
        previousUpEvent = MotionEvent.obtain(ev);
    }

    private void cancel() {
        velocityTracker.recycle();
        velocityTracker = null;
        gestureHandler.removeMessages(MSG_TAP);
        isNewState4Scroll = true;
        isDoubleTapping = false;
        pointerIndex1 = INVALID_POINTER_INDEX;
        pointerIndex2 = INVALID_POINTER_INDEX;
    }

    private void updateCurrDownEvent(MotionEvent ev) {
        if (currentDownEvent != null) {
            currentDownEvent.recycle();
        }
        currentDownEvent = MotionEvent.obtain(ev);
    }

    private void calculateCurrFocusAndSpan(MotionEvent ev) {
        final float action = ev.getActionMasked();
        final boolean pointerUp = (action == MotionEvent.ACTION_POINTER_UP);
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;
        currFocusX = sumX / div;
        currFocusY = sumY / div;

        // Determine average deviation from focal point
        float devSumX = 0, devSumY = 0;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;

            // Convert the resulting diameter into a radius.
            devSumX += Math.abs(ev.getX(i) - focusX);
            devSumY += Math.abs(ev.getY(i) - focusY);
        }
        final float devX = devSumX / div;
        final float devY = devSumY / div;

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        final float spanX = devX * 2;
        final float spanY = devY * 2;
        currSpan = (float) Math.hypot(spanX, spanY);
    }

    private boolean checkOnFlingGesture(MotionEvent ev) {
        final VelocityTracker velocityTracker = this.velocityTracker;
        final int pointerId = ev.getPointerId(0);
        velocityTracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY);
        final float velocityY = velocityTracker.getYVelocity(pointerId);
        final float velocityX = velocityTracker.getXVelocity(pointerId);
        if ((Math.abs(velocityY) >= MIN_FLING_VELOCITY) || (Math.abs(velocityX) >= MIN_FLING_VELOCITY)) {
            return gestureListener.onFling(currentDownEvent, ev, velocityX, velocityY);
        } else {
            return false;
        }
    }
}
