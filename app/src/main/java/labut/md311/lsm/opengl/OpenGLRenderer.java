package labut.md311.lsm.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import labut.md311.lsm.R;
import labut.md311.lsm.interp.LSM;
import labut.md311.lsm.interp.Lagrange;
import labut.md311.lsm.interp.LSMData;
import labut.md311.lsm.opengl.text.GLText;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_LINE_STRIP;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private int programId;
    private FloatBuffer vertexData;
    private int uColorLocation;
    private int aPositionLocation;

    private GLText glText;
    private int width;
    private int height;
    private float[] mProjMatrixText = new float[16];
    private float[] mVMatrixText = new float[16];
    private float[] mVPMatrixText = new float[16];
    private float[] text_positions = new float[26];
    private float[] dots_positions = {-0.54f, -0f,
            -0.34f, -0.18f,
            -0.14f, -0.25f,
            0.06f, -0.25f,
            0.26f, -0.03f};
    //0 and 6 x coordinates
    private final float X0 = -0.74f;
    private final float X6 = 0.46f;
    private String equation = new String();

    OpenGLRenderer(Context context) {
        this.context = context;

    }

    float[] getDots_positions() {
        return dots_positions;
    }

    void setDots_positions(float[] dots_positions) {
        this.dots_positions = dots_positions;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int vertexShaderId = Shaders.getShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShaderId = Shaders.getShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        programId = Shaders.createProgram(vertexShaderId, fragmentShaderId);
        glUseProgram(programId);
        glText = new GLText(context.getAssets());
        glText.load("robotoregular.ttf", 20, 2, 2);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        glViewport(0, 0, i, i1);
        width = i;
        height = i1;
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrixText, 0, -ratio, ratio, -1, 1, 1, 10);
        int useForOrtho = Math.min(width, height);
        Matrix.orthoM(mVMatrixText, 0,
                -useForOrtho / 2,
                useForOrtho / 2,
                -useForOrtho / 2,
                useForOrtho / 2, 0.1f, 100f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        prepareData();
        calcTextLinesDotsPos();
        bindData();
        glClear(GL_COLOR_BUFFER_BIT);

        //draw text
        Matrix.multiplyMM(mVPMatrixText, 0, mProjMatrixText, 0, mVMatrixText, 0);
        glText.begin(0.0f, 0.0f, 0.0f, 1.0f, mVPMatrixText);
        int c = 0;
        for (int i = 0; i < 14; i += 2) {
            if (i > 0) {
                glText.draw(String.valueOf(++c), text_positions[i], text_positions[i + 1]);
            } else {
                glText.draw(String.valueOf(c), text_positions[i], text_positions[i + 1]);
            }
        }
        glText.draw("X", text_positions[14], text_positions[15]);
        glText.draw("Y", text_positions[16], text_positions[17]);
        glText.draw("10", text_positions[18], text_positions[19]);
        glText.end();
        glText.begin(1.0f, 0.0f, 0.0f, 1.0f, mVPMatrixText);
        glText.draw(equation, text_positions[24], text_positions[25], 90);
        glText.draw("LSM", text_positions[20], text_positions[21]);
        glText.end();
        glText.begin(0.9f, 0.9f, 0.0f, 1.0f, mVPMatrixText);
        glText.draw("LAGRANGE", text_positions[22], text_positions[23]);
        glText.end();
        //draw other objects
        glUseProgram(programId);
        glLineWidth(5);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 0, 4);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 4, 6);
        glLineWidth(2);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 10, 12);
        glLineWidth(5);
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 22, 2);
        glUniform4f(uColorLocation, 0.9f, 0.9f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 24, 2);
        glUniform4f(uColorLocation, 0.6f, 0.196f, 0.8f, 1.0f);
        glDrawArrays(GL_POINTS, 26, 5);
        glUniform4f(uColorLocation, 0.9f, 0.9f, 0.0f, 1.0f);
        glDrawArrays(GL_LINE_STRIP, 31, 7);
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINE_STRIP, 38, 7);
    }

    private void prepareData() {
        float[][] xyTable_lagr = new float[2][5];
        float[] lsm_x_vals = new float[7];
        float[] x_vals = new float[5];
        float[] y_vals = new float[5];
        int count = 0;
        for (int i = 0; i < dots_positions.length; i += 2) {
            xyTable_lagr[0][count] = dots_positions[i];
            xyTable_lagr[1][count] = dots_positions[i + 1];
            lsm_x_vals[count + 1] = dots_positions[i];
            x_vals[count] = dots_positions[i];
            y_vals[count] = dots_positions[i + 1];
            count++;
        }
        float[] lagr_points = new float[7];
        lagr_points[0] = Lagrange.lagrPolynom(-0.74f, x_vals, y_vals);
        lagr_points[6] = Lagrange.lagrPolynom(0.46f, x_vals, y_vals);
        for (int i = 1; i < lagr_points.length - 1; i++) {
            lagr_points[i] = Lagrange.lagrPolynom(x_vals[i - 1], x_vals, y_vals);
        }
        lsm_x_vals[0] = -0.74f;
        lsm_x_vals[6] = 0.46f;
        LSMData lsmData = LSM.lsmPolynom(xyTable_lagr, 3, lsm_x_vals);
        float[] lsm_points = lsmData.getF_vals();
        float[] lsm_eq_coefs = lsmData.getEq_coefs();
        StringBuilder eq = new StringBuilder();
        eq.append("y = ");
        float round = 0f;
        for (int i = 0; i < lsm_eq_coefs.length; i++) {
            if (i > 0) {
                if (lsm_eq_coefs[i] > 0) {
                    round = Math.round(lsm_eq_coefs[i] * 100.0f) / 100.0f;
                    eq.append("+");
                    eq.append(round);
                } else {
                    round = Math.abs(Math.round(lsm_eq_coefs[i] * 100.0f) / 100.0f);
                    eq.append("-");
                    eq.append(round);
                }
                eq.append("*x^");
                eq.append(i);
            } else {
                round = Math.round(lsm_eq_coefs[i] * 100.0f) / 100.0f;
                eq.append(round);
            }
        }
        equation = eq.toString();
        float[] vertices = {
//        axis y line
                -0.8f, -0.95f, -0.8f, 0.95f,
                //axis x line
                -0.9f, -0.8f, 0.9f, -0.8f,
                //y arrow
                -0.813f, 0.875f, -0.787f, 0.875f, -0.8f, 0.965f,
                //x arrow
                0.9f, -0.825f, 0.945f, -0.8f, 0.9f, -0.775f,
                //axis x mark lines
                -0.54f, -0.825f, -0.54f, -0.775f,
                -0.34f, -0.825f, -0.34f, -0.775f,
                -0.14f, -0.825f, -0.14f, -0.775f,
                0.06f, -0.825f, 0.06f, -0.775f,
                0.26f, -0.825f, 0.26f, -0.775f,
                0.46f, -0.825f, 0.46f, -0.775f,
                //legend lines
                0.63f, 0.85f, 0.73f, 0.85f,
                0.63f, 0.75f, 0.73f, 0.75f,
                //dots
                dots_positions[0], dots_positions[1],
                dots_positions[2], dots_positions[3],
                dots_positions[4], dots_positions[5],
                dots_positions[6], dots_positions[7],
                dots_positions[8], dots_positions[9],
                //Lagrange points
                X0, lagr_points[0],
                x_vals[0], lagr_points[1],
                x_vals[1], lagr_points[2],
                x_vals[2], lagr_points[3],
                x_vals[3], lagr_points[4],
                x_vals[4], lagr_points[5],
                X6, lagr_points[6],
                //LSM points
                X0, lsm_points[0],
                x_vals[0], lsm_points[1],
                x_vals[1], lsm_points[2],
                x_vals[2], lsm_points[3],
                x_vals[3], lsm_points[4],
                x_vals[4], lsm_points[5],
                X6, lsm_points[6]
        };
//        float[] cps = {-0.54f, -0.2f,
//                -0.4f, 0.1f,
//                0.1f, -0.2f};
//        for (int i = 0; i < cps.length; i++) {
//            cps[i] += 1;
//        }
//        float[] tc = new float[400];
//        float[] lagr1 = {X0, lagr_points[0],
//                x_vals[0], lagr_points[1],
//                x_vals[1], lagr_points[2]};
//        int count2 = vertices.length + 2;
//        float[] new_v = Arrays.copyOf(vertices, vertices.length + 32);
//        for (float t = 0; t <= 1; t += 0.1) {
//            new_v[count2] = ((1 - t) * (1 - t) * lagr1[0] + 2 * (1 - t) * t * lagr1[2] + t * t * lagr1[4]);
//            new_v[count2 + 1] = ((1 - t) * (1 - t) * lagr1[1] + 2 * (1 - t) * t * lagr1[3] + t * t * lagr1[5]);
//            count2 += 2;
//        }
//        new_v[vertices.length] = X0;
//        new_v[vertices.length + 1] = lagr_points[0];
//
//        new_v[new_v.length - 10] = x_vals[1];
//        new_v[new_v.length - 9] = lagr_points[2];
//        new_v[new_v.length - 8] = x_vals[2];
//        new_v[new_v.length - 7] = lagr_points[3];
//        new_v[new_v.length - 6] = x_vals[3];
//        new_v[new_v.length - 5] = lagr_points[4];
//        new_v[new_v.length - 4] = x_vals[4];
//        new_v[new_v.length - 3] = lagr_points[5];
//        new_v[new_v.length - 2] = X6;
//        new_v[new_v.length - 1] = lagr_points[6];


        vertexData = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(vertices);
    }

    private void calcTextLinesDotsPos() {
        //axis x texts
        for (int i = 0; i < 14; i += 2) {
            text_positions[i] = (-0.75f + i * 0.1f) * width / 2;
            text_positions[i + 1] = -0.95f * height / 2;
        }
        text_positions[14] = 0.9f * width / 2;
        text_positions[15] = -0.95f * height / 2;
        //axis y texts
        text_positions[16] = -0.85f * width / 2;
        text_positions[17] = 0.83f * height / 2;
        text_positions[18] = -0.76f * width / 2;
        text_positions[19] = 0.75f * height / 2;
        //legend texts
        text_positions[20] = 0.8f * width / 2;
        text_positions[21] = 0.8f * height / 2;
        text_positions[22] = 0.76f * width / 2;
        text_positions[23] = 0.7f * height / 2;
        //LSM equation text
        text_positions[24] = -0.9f * width / 2;
        text_positions[25] = -0.75f * height / 2;
    }

    private void bindData() {
        glClearColor(1f, 1f, 1f, 1f);
        uColorLocation = glGetUniformLocation(programId, "u_Color");
        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);
    }
}
