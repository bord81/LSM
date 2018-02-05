package labut.md311.lsm.interp;

public class LSM {

    public static LSMData lsmPolynom(float[][] xyTable, int basis, float[] vals) {
        basis++;
        float[][] matrix_intern = makeSystem(xyTable, basis);
        float[] result_intern = gauss(matrix_intern, basis, basis + 1);
        float[] f_vals = new float[vals.length];
        for (int i = 0; i < vals.length; i++) {
            float y = 0;
            for (int j = 0; j < basis; j++) {
                y += result_intern[j] * Math.pow(vals[i], j);
            }
            f_vals[i] = y;
        }
        return new LSMData(f_vals, result_intern);
    }


    private static float[][] makeSystem(float[][] xyTable, int basis) {
        float[][] matrix = new float[basis][basis + 1];
        for (int i = 0; i < basis; i++) {
            for (int j = 0; j < basis; j++) {
                matrix[i][j] = 0;
            }
        }
        for (int i = 0; i < basis; i++) {
            for (int j = 0; j < basis; j++) {
                float sumA = 0, sumB = 0;
                for (int k = 0; k < xyTable[0].length; k++) {
                    sumA += Math.pow(xyTable[0][k], i) * Math.pow(xyTable[0][k], j);
                    sumB += xyTable[1][k] * Math.pow(xyTable[0][k], i);
                }
                matrix[i][j] = sumA;
                matrix[i][basis] = sumB;
            }
        }
        return matrix;
    }

    private static float[] gauss(float[][] matrix, int rowCount, int colCount) {
        int i;
        int[] mask = new int[colCount - 1];
        for (i = 0; i < colCount - 1; i++) {
            mask[i] = i;
        }
        if (gaussDirectPass(matrix, mask, colCount, rowCount)) {
            return gaussReversePass(matrix, mask, colCount, rowCount);
        } else {
            return null;
        }
    }

    private static boolean gaussDirectPass(float[][] matrix, int[] mask, int colCount, int rowCount) {
        int i, j, k, maxId, tmpInt;
        float maxVal, tempfloat;
        for (i = 0; i < rowCount; i++) {
            maxId = i;
            maxVal = matrix[i][i];
            for (j = i + 1; j < colCount - 1; j++) {
                if (Math.abs(maxVal) < Math.abs(matrix[i][j])) {
                    maxVal = matrix[i][j];
                    maxId = j;
                }
            }
            if (maxVal == 0) {
                return false;
            }
            if (i != maxId) {
                for (j = 0; j < rowCount; j++) {
                    tempfloat = matrix[j][i];
                    matrix[j][i] = matrix[j][maxId];
                    matrix[j][maxId] = tempfloat;
                }
                tmpInt = mask[i];
                mask[i] = mask[maxId];
                mask[maxId] = tmpInt;
            }
            for (j = 0; j < colCount; j++) {
                matrix[i][j] /= maxVal;
            }
            for (j = i + 1; j < rowCount; j++) {
                float tempMn = matrix[j][i];
                for (k = 0; k < colCount; k++) {
                    matrix[j][k] -= matrix[i][k] * tempMn;
                }
            }

        }
        return true;
    }

    private static float[] gaussReversePass(float[][] matrix, int[] mask, int colCount, int rowCount) {
        int i, j, k;
        for (i = rowCount - 1; i >= 0; i--) {
            for (j = i - 1; j >= 0; j--) {
                float tempMn = matrix[j][i];
                for (k = 0; k < colCount; k++) {
                    matrix[j][k] -= matrix[i][k] * tempMn;
                }
            }
        }
        float[] answer = new float[rowCount];
        for (i = 0; i < rowCount; i++) {
            answer[mask[i]] = matrix[i][colCount - 1];
        }
        return answer;
    }
}
