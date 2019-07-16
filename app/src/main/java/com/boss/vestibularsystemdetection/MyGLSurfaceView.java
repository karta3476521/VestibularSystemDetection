package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    MyGLRenderer renderer;
    static final int MY_GLRENDERER_FOR_VR = 0;
    static final int MY_GLRENDERER_FOR_CENTER = 1;

    public MyGLSurfaceView(Context context, int mode) {
        super(context);

        setEGLContextClientVersion(1);

        if(mode == 0)
            renderer = new MyGLRenderer((VrMeasurementActivity) context);
        else if(mode == 1) {
            renderer = new MyGLRenderer((CenterMeasureActivity) context);
            renderer.setResources(getResources());
        }
        setRenderer((GLSurfaceView.Renderer)renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }
    public MyGLRenderer getRenderer() {
        return renderer;
    }
}
