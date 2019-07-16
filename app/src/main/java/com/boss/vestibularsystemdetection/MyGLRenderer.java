package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.support.v7.app.AppCompatActivity;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    Square left, right;
    Rectangle left_rect, right_rect;
    OnePoint left_point, right_point;
    DrawText left_text, right_text;
    DrawBitmap left_bitmap, right_bitmap;
    int[] texture = new int[1];
    Context mContext;
    private SurfaceTexture surface;
    private Resources resources;

    public MyGLRenderer(AppCompatActivity context) {
        this.mContext = context;
        left = new Square();
        right = new Square();

        if(context.getClass().getName() == VrMeasurementActivity.class.getName()) {
            left_rect = new Rectangle();
            right_rect = new Rectangle();
        }else if(context.getClass().getName() == CenterMeasureActivity.class.getName()){
            left_point = new OnePoint();
            right_point = new OnePoint();
            left_text = new DrawText();
            right_text = new DrawText();
        }
    }

    public void setResources(Resources resources){
        this.resources = resources;
        left_bitmap = new DrawBitmap(resources);
        right_bitmap = new DrawBitmap(resources);
    }

    // Call back when the surface is first created or re-created
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Set color's clear-value to black
        gl.glClearDepthf(1.0f);            // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);   // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);    // The type of depth testing to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);   // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);      // Disable dithering for better performance

        createTexture(gl);
        //right.loadTexture(gl, delegate);
//        gl.glEnable(GL10.GL_TEXTURE_2D);  // Enable texture 不可跟glEnable(GL_TEXTURE_EXTERNAL_OES) 同時存在，會白屏
        gl.glEnable(GL_TEXTURE_EXTERNAL_OES);

        // You OpenGL|ES initialization code here
        // ......
    }

    private void  createTexture(GL10 gl) {
        gl.glGenTextures(1,texture, 0);
        if(mContext.getClass().getName() == VrMeasurementActivity.class.getName()) {
            VrMeasurementActivity delegate = (VrMeasurementActivity) mContext;
            delegate.startCamera(texture[0]);
        }else if(mContext.getClass().getName() == CenterMeasureActivity.class.getName()){
            CenterMeasureActivity delegate = (CenterMeasureActivity) mContext;
            delegate.startCamera(texture[0]);
        }

        gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);


//	    gl.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    }

     // Call back after onSurfaceCreated() or whenever the window's size changes
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) height = 1;   // To prevent divide by zero
        float aspect = (float)width / height;

        // Set the viewport (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
        gl.glLoadIdentity();                 // Reset projection matrix
        // Use perspective projection
        //原理：https://blog.csdn.net/shuaihj/article/details/7228575
        GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
        gl.glLoadIdentity();    // Reset


        // You OpenGL|ES display re-sizing code here
        // ......
    }


    // Call back to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear color and depth buffers using clear-value set earlier
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();    //初始化單元矩陣

        float[] mtx = new float[16];
        surface.updateTexImage();
        surface.getTransformMatrix(mtx);

//        gl.glTexImage2D(GL_TEXTURE_EXTERNAL_OES, 0, surface, 0);

        if(mContext.getClass().getName() == VrMeasurementActivity.class.getName()) {
            // Translate left and into the screen
            gl.glTranslatef(-1.25f, 0.0f, -4.0f);
            left.draw(gl);
            left_rect.draw(gl);

            // Translate right, relative to the previous translation
            gl.glTranslatef(2.7f, 0.0f, 0.0f);
            right.draw(gl);
            right_rect.draw(gl);

        }else if(mContext.getClass().getName() == CenterMeasureActivity.class.getName()){
            // Translate left and into the screen
            gl.glTranslatef(-1.25f, 0.0f, -4.0f);
            left.draw(gl);
//            left_point.draw(gl);
//            left_text.draw(gl);
//            left_bitmap.draw(gl);
            // Translate right, relative to the previous translation
            gl.glTranslatef(2.7f, 0.0f, 0.0f);
            right.draw(gl);
//            right_point.draw(gl);
//            right_text.draw(gl);
//            right_bitmap.draw(gl);
        }
    }

    public void setSurface(SurfaceTexture _surface)
    {
        surface = _surface;
    }

}
