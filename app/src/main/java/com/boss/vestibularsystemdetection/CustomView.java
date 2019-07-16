package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class CustomView extends View {
    private List<PointF> data, quadrant_1st, quadrant_2st, quadrant_3st, quadrant_4st, quadrant_5st, quadrant_6st, quadrant_7st, quadrant_8st;;
    private PointF center;
    private float centerX, centerY, distanceX, distanceY;
    private int viewWidth, viewHeight;
    private boolean isAreaClick = false;
    private final int DEFAULT_SIZE = 450;
    private boolean isPointHide = false;

    public CustomView(Context context){
        super(context);
    }

    public CustomView(Context context, List<PointF> data, PointF center) {
        super(context);
        this.data = data;
        this.center = center;
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(List<PointF> data, PointF center){
        this.data = data;
        this.center = center;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getMySize(widthMeasureSpec);
        final int height = getMySize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    private int getMySize(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;//确切大小,所以将得到的尺寸给view
        } else if (specMode == MeasureSpec.AT_MOST) {
            //默认值为450px,此处要结合父控件给子控件的最多大小(要不然会填充父控件),所以采用最小值
            System.err.println("AT_MOST");
            result = Math.min(DEFAULT_SIZE, specSize);
        } else {
            System.err.println("else");
            result = DEFAULT_SIZE;
        }
        return result;
    }

    public void showArea(){
        isAreaClick = true;
        invalidate();
    }

    public void hideArea(){
        isAreaClick = false;
        invalidate();
    }

    public void hidePoint(boolean isPointHide){
        this.isPointHide = isPointHide;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if(data != null){
            //取得view 大小
            viewWidth = getWidth();
            viewHeight = getHeight();
            //View 中央
            centerX = (float) (getWidth()/2);
            centerY = (float) (getHeight()/2);

            //取得中心到中央位移量
            distanceX = center.x;
            distanceY = center.y;

            //ＸＸ ＆ 面積
            if(isAreaClick) {
                Paint paint = new Paint();

                if(!isPointHide) {
                    paint.setStrokeWidth(2);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.GRAY);
                    drawData(canvas, paint);
                }

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.rgb(255, 120, 120));
                drawArea(canvas, paint);

                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.GREEN);
                drawAxis(canvas, paint);
            }else {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);

                paint.setColor(Color.GRAY);
                drawData(canvas, paint);

                //View 的中央
                paint.setColor(Color.BLUE); //顏色
                paint.setStrokeWidth(20);
                canvas.drawPoint(centerX - distanceX, centerY - distanceY, paint);

                //人的中心
                paint.setColor(Color.RED);
                canvas.drawPoint(centerX + center.x - distanceX, centerY + center.y - distanceY, paint);

                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(6);
                drawScale(canvas, paint);
            }
        }

    }

    private void drawAxis(Canvas canvas, Paint paint){

        //X軸
        Path path = new Path();
        path.moveTo(0, viewHeight/2);
        path.lineTo(viewWidth, viewHeight/2);
        canvas.drawPath(path, paint);

        //Y軸
        path.reset();
        path.moveTo(viewWidth/2, centerY-viewWidth/2);
        path.lineTo(viewWidth/2, centerY+viewWidth/2);
        canvas.drawPath(path, paint);

        //y = X + C
        path.reset();
        float distance_temp = (float) (viewWidth/2/Math.sqrt(2));
        path.moveTo(centerX+distance_temp, centerY-distance_temp);
        path.lineTo(centerX-distance_temp, centerY+distance_temp);
        canvas.drawPath(path, paint);

        //y = -X + C
        path.reset();
        path.moveTo(centerX-distance_temp, centerY-distance_temp);
        path.lineTo(centerX+distance_temp, centerY+distance_temp);
        canvas.drawPath(path, paint);
    }

    private void drawData(Canvas canvas, Paint paint){
        Path path = new Path();
        path.moveTo(centerX + data.get(0).x - distanceX, centerY + data.get(0).y - distanceY);
        for (PointF point : data){
            float pointX = centerX + point.x - distanceX;
            float pointY = centerY + point.y - distanceY;
            path.lineTo(pointX, pointY);
        }
        canvas.drawPath(path, paint);
    }

    private void drawArea(Canvas canvas, Paint paint){
        float bottom = viewWidth * (float)Math.cos(67.5);

        Path path = new Path();
        if(quadrant_1st.size() > 0) {
            path.reset();
            float proportoinOfside = (float) quadrant_1st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX + sideLength, centerY);
            path.lineTo(centerX + sideLength2, centerY - sideLength2);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_2st.size() > 0) {
            path.reset();
            float proportoinOfside = (float) quadrant_2st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX + sideLength2, centerY - sideLength2);
            path.lineTo(centerX, centerY - sideLength);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_3st.size() > 0) {
            path.reset();
            float proportoinOfside = (float) quadrant_3st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX, centerY - sideLength);
            path.lineTo(centerX - sideLength2, centerY - sideLength2);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
            System.err.println(centerX+"");
        }

        if(quadrant_4st.size() > 0){
            path.reset();
            float proportoinOfside = (float) quadrant_4st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX - sideLength2, centerY - sideLength2);
            path.lineTo(centerX - sideLength, centerY);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_5st.size() > 0){
            path.reset();
            float proportoinOfside = (float) quadrant_5st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX - sideLength, centerY);
            path.lineTo(centerX - sideLength2, centerY + sideLength2);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_6st.size() > 0){
            path.reset();
            float proportoinOfside = (float) quadrant_6st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX - sideLength2, centerY + sideLength2);
            path.lineTo(centerX, centerY + sideLength);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_7st.size() > 0){
            path.reset();
            float proportoinOfside = (float) quadrant_7st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX, centerY + sideLength);
            path.lineTo(centerX + sideLength2, centerY + sideLength2);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }

        if(quadrant_8st.size() > 0){
            path.reset();
            float proportoinOfside = (float) quadrant_8st.size() / data.size();
            float sideLength = proportoinOfside * bottom / (float) Math.cos(67.5) / 2;
            float sideLength2 = sideLength / (float) Math.sqrt(2);
            path.moveTo(centerX + sideLength2, centerY + sideLength2);
            path.lineTo(centerX + sideLength, centerY);
            path.lineTo(centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }
    }

    private float sec(double a){
        return (float) (1 / Math.cos(a));
    }

    private void drawScale(Canvas canvas, Paint paint){
        Path path = new Path();
        path.moveTo(850, 1400);
        path.lineTo(850, 1425);
        path.lineTo(950, 1425);
        path.lineTo(950, 1400);
        canvas.drawPath(path, paint);

        path.reset();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth (3);
        paint.setTextSize(25);
        paint.setTextAlign(Paint.Align.CENTER);
        path.moveTo(850, 1380);
        path.lineTo(950, 1380);
        canvas.drawTextOnPath("10 cm", path, 0, 0, paint);
    }

    public void setQuadrant(List<PointF> q1, List<PointF> q2, List<PointF> q3, List<PointF> q4, List<PointF> q5, List<PointF> q6, List<PointF> q7, List<PointF> q8){
        quadrant_1st = q1;
        quadrant_2st = q2;
        quadrant_3st = q3;
        quadrant_4st = q4;
        quadrant_5st = q5;
        quadrant_6st = q6;
        quadrant_7st = q7;
        quadrant_8st = q8;
    }
}
