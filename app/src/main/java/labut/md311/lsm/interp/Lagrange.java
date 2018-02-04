package labut.md311.lsm.interp;

public class Lagrange {
    public static float lagrPolynom(float x, float[] x_vals, float[] y_vals) {
        float sum = 0f;
        int t_size = x_vals.length;
        for (int i = 0; i < t_size; i++) {
            float m = 1.0f;
            for (int j = 0; j < t_size; j++) {
                if (i != j)
                    m *= (x - x_vals[j]) / (x_vals[i] - x_vals[j]);
            }
            sum += y_vals[i] * m;
        }
        return sum;
    }
}
