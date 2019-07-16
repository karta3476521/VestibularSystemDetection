package com.boss.vestibularsystemdetection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class OnePoint {
    private FloatBuffer vertexBuffer; // 頂點陣列的緩衝
    private ByteBuffer indexBuffer;  // 索引緩衝
    private float[] vertices = {  // Vertices for the square
            0.0f, 0.0f,  0.0f,  //0. 原點
    };

    // 建構子 – 建立資料陣列的緩衝
    public OnePoint() {
        // 建立頂點陣列緩衝. 頂點是浮點數要乘4個byte.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // 使用原生順序
        vertexBuffer = vbb.asFloatBuffer(); // 轉換位元組緩衝為浮點數
        vertexBuffer.put(vertices);     // 將資料複製到緩衝
        vertexBuffer.position(0);      // 倒轉歸零
    }

    // 渲染這個圖形
    public void draw(GL10 gl) {
        // Enable vertex-array and define the buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // int size, int type, int stride, Buffer pointer
        // size: 頂點的座標數 (只有2,3,4).
        // type: 頂點座標的資料型態, GL_BYTE, GL_SHORT, GL_FIXED, or GL_FLOAT
        // stride: 連續頂點可以有幾個byte的offset. 0是緊密的包裝？
        // 透過index-array繪製元素
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glPointSize(50f);
        gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}

