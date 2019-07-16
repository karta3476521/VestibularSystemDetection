package com.boss.vestibularsystemdetection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Rectangle {
    private FloatBuffer vertexBuffer; // 頂點陣列的緩衝
    private ByteBuffer indexBuffer;  // 索引緩衝
    private float[] vertices = {  // Vertices for the square
            -0.4f, -0.2f,  0.0f,  // 0. left-bottom
            0.4f, -0.2f,  0.0f,  // 1. right-bottom
            -0.4f,  0.2f,  0.0f,  // 2. left-top
            0.4f,  0.2f,  0.0f,   // 3. right-top
            0.4f, 0.0f, 0.0f,    //4. right-middle
            -0.4f, 0.0f, 0.0f    //5.left-middle
    };

    private byte[] indices = { 5, 0, 1, 4, 5, 2, 3, 4}; // 連結這些點的順序
    // 建構子 – 建立資料陣列的緩衝
    public Rectangle() {
        // 建立頂點陣列緩衝. 頂點是浮點數要乘4個byte.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // 使用原生順序
        vertexBuffer = vbb.asFloatBuffer(); // 轉換位元組緩衝為浮點數
        vertexBuffer.put(vertices);     // 將資料複製到緩衝
        vertexBuffer.position(0);      // 倒轉歸零
        //建立頂點索引陣列緩衝
        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    // 渲染這個圖形
    public void draw(GL10 gl) {
        gl.glLineWidth(3);
        // Enable vertex-array and define the buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // int size, int type, int stride, Buffer pointer
        // size: 頂點的座標數 (只有2,3,4).
        // type: 頂點座標的資料型態, GL_BYTE, GL_SHORT, GL_FIXED, or GL_FLOAT
        // stride: 連續頂點可以有幾個byte的offset. 0是緊密的包裝？
        // 透過index-array繪製元素
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        // int mode, int count, int type, Buffer indices)
        // mode: GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES, GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, or GL_TRIANGLES
        // count: 有幾個元素將要被渲染
        // type:頂點的資料型態 (must be GL_UNSIGNED_BYTE or GL_UNSIGNED_SHORT).
        // indices:index array的指標
        gl.glDrawElements(GL10.GL_LINE_STRIP, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
