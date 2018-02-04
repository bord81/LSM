package labut.md311.lsm.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MainSurfaceView extends GLSurfaceView {
    private final OpenGLRenderer renderer;
    private float[] dots_positions = new float[10];
    float[] dots_from_renderer = new float[10];

    private int height = 0;
    private int width = 0;

    public MainSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new OpenGLRenderer(context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            dots_from_renderer = renderer.getDots_positions();
            height = getHeight();
            width = getWidth();
            for (int i = 0; i < dots_from_renderer.length; i++) {
                if (i % 2 == 0) {
                    dots_positions[i] = ((dots_from_renderer[i] + 1) / 2) * width;
                } else {
                    dots_positions[i] = height - ((dots_from_renderer[i] + 1) / 2) * height;
                }
            }
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < dots_positions.length; i += 2) {
                int radius = (int) (((float) width / 1000) * 20);
                if (x > dots_positions[i] - radius && x < dots_positions[i] + radius && y > dots_positions[i + 1] - radius && y < dots_positions[i + 1] + radius) {
                    dots_positions[i + 1] = y;
                    float[] dots_for_render = new float[10];
                    for (int j = 0; j < dots_for_render.length; j++) {
                        if (j % 2 == 0) {
                            dots_for_render[j] = dots_from_renderer[j];
                        } else {
                            dots_for_render[j] = 1 - 2 * dots_positions[j] / height;
                        }
                    }
                    renderer.setDots_positions(dots_for_render);
                    requestRender();
                    break;
                }
            }
        }
        return true;
    }
}
