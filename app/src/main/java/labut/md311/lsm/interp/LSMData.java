package labut.md311.lsm.interp;

public class LSMData {
    private final float[] f_vals;
    private final float[] eq_coefs;

    public LSMData(float[] f_vals, float[] eq_coefs) {
        this.f_vals = f_vals;
        this.eq_coefs = eq_coefs;
    }

    public float[] getF_vals() {
        return f_vals;
    }

    public float[] getEq_coefs() {
        return eq_coefs;
    }
}
