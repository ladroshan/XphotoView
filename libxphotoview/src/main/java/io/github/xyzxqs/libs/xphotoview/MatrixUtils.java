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

import android.graphics.Matrix;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

class MatrixUtils {

    private final static float[] matrixValues = new float[9];

    private final static Object lock = new Object();

    private MatrixUtils() {
    }

    public static float getMatrixTranslateX(@NonNull Matrix matrix) {
        return getMatrixValue(matrix, Matrix.MTRANS_X);
    }

    public static float getMatrixTranslateY(@NonNull Matrix matrix) {
        return getMatrixValue(matrix, Matrix.MTRANS_Y);
    }

    public static float getMatrixScaleX(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2));
    }

    public static float getMatrixScaleY(@NonNull Matrix matrix) {
        return (float) Math.sqrt(Math.pow(getMatrixValue(matrix, Matrix.MSCALE_Y), 2)
                + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_X), 2));
    }

    public static float getMatrixAngle(@NonNull Matrix matrix) {
        return (float) -(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X), getMatrixValue(matrix, Matrix.MSCALE_X))
                * (180 / Math.PI));
    }

    public static float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 8) int valueIndex) {

        float value;
        synchronized (lock) {
            matrix.getValues(matrixValues);
            value = matrixValues[valueIndex];
        }
        return value;
    }

    public static void setMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 8) int valueIndex, float value) {
        synchronized (lock) {
            matrix.getValues(matrixValues);
            matrixValues[valueIndex] = value;
            matrix.setValues(matrixValues);
        }
    }
}
