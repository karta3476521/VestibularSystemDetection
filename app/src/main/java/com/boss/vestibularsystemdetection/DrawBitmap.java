package com.boss.vestibularsystemdetection;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class DrawBitmap {
    private FloatBuffer vertexBuffer; // 頂點陣列的緩衝
    private FloatBuffer coordBuffer; //紋理緩衝
    private Resources resources;

    private float[] vertices = {  // Vertices for the square
            -1.15f, -1.0f,  0.0f,  // 0. left-bottom
            1.15f, -1.0f,  0.0f,  // 1. right-bottom
            -1.15f,  1.0f,  0.0f,  // 2. left-top
            1.15f,  1.0f,  0.0f   // 3. right-top
    };

    //纹理坐标系
    private float[] coord = new float[]{
            0.0f, 1.0f,  // A. left-bottom (NEW)
            1.0f, 1.0f,  // B. right-bottom (NEW)
            0.0f, 0.0f,  // C. left-top (NEW)
            1.0f, 0.0f   // D. right-top (NEW)
    };

    // 建構子 – 建立資料陣列的緩衝
    public DrawBitmap(Resources resources) {
        this.resources = resources;

        // 建立頂點陣列緩衝. 頂點是浮點數要乘4個byte.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // 使用原生順序
        vertexBuffer = vbb.asFloatBuffer(); // 轉換位元組緩衝為浮點數
        vertexBuffer.put(vertices);     // 將資料複製到緩衝
        vertexBuffer.position(0);      // 倒轉歸零
        //准备纹理缓冲
        ByteBuffer coordbb = ByteBuffer.allocateDirect(coord.length * 4);
        coordbb.order(ByteOrder.nativeOrder());
        coordBuffer = coordbb.asFloatBuffer();
        coordBuffer.put(coord);
        coordBuffer.position(0);
    }

    // 渲染這個圖形
    public void draw(GL10 gl) {
        // Enable vertex-array and define the buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        // int size, int type, int stride, Buffer pointer
        // size: 頂點的座標數 (只有2,3,4).
        // type: 頂點座標的資料型態, GL_BYTE, GL_SHORT, GL_FIXED, or GL_FLOAT
        // stride: 連續頂點可以有幾個byte的offset. 0是緊密的包裝？
        //设置顶点和纹理的位置、类型
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordBuffer);

        Bitmap bm = BitmapFactory.decodeResource(resources, R.drawable.pencil);
        //绘图
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
//        .texImage2D(GL10.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(resources, R.drawable.pencil), 0);
        //取消缓冲
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }
}
