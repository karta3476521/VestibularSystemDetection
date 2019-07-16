package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class AnimationView extends View {
    private List<PointF> data;
//    private int viewWidth, viewHeight;
    private float centerX, centerY;
    private boolean isDrawing = false, isEnd = false;
    private int index_now = 0;
    private int delay_proportion = 50;
    private Bitmap bm, bm2;
    private Handler handler = new Handler();
    private Context mContext;
    private final int DEFAULT_SIZE = 450;
    private PointF center;
    private int scale = 2;  //2倍
    private float distanceX, distanceY;
    private int[] alpha = {1, 25, 50, 75, 100, 125, 150, 175, 200, 225};

    public AnimationView(Context context, List<PointF> data, PointF center) {
        super(context);
        this.data = data;
        this.mContext = context;
        this.center = center;
    }

    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawing) {
            //設置筆刷
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            centerX = (float) (getWidth()/2);
            centerY = (float) (getHeight()/2);

            if(index_now>0) {
                paint.setColor(Color.GRAY);
                drawData(canvas, paint);
            }

            //人的中心
            paint.setColor(Color.RED);
            paint.setStrokeWidth(20);
            float centerOfHumanX = centerX + center.x*scale;
            float centerOfHumanY = centerY + center.y*scale;
            distanceX = centerX - centerOfHumanX;
            distanceY = centerY - centerOfHumanY;
            canvas.drawPoint(centerOfHumanX + distanceX, centerOfHumanY + distanceY, paint);

            //superBall
            bm = decodeBitmapFromResource(R.drawable.pencil);
            bm2 = Bitmap.createBitmap(bm);
            canvas.drawBitmap(bm2, centerX + data.get(index_now).x*scale + distanceX, centerY + data.get(index_now).y*scale + distanceY, null);
        }
    }

//    private void drawArea(Canvas canvas, Paint paint){
//        viewWidth = getWidth();
//        viewHeight = getHeight();
//
//        //y = X + C
//        Path path = new Path();
//        int c =  viewHeight/2 - viewWidth/2;
//        path.moveTo(0, c);
//        path.lineTo(viewWidth, viewWidth+c);
//        canvas.drawPath(path, paint);
//
//        //y = -X + C
//        path.reset();
//        int c2 = viewWidth/2 + viewHeight/2;
//        path.moveTo(0, c2);
//        path.lineTo(viewWidth, -viewWidth+c2);
//        canvas.drawPath(path, paint);
//
//        //y軸
//        path.reset();
//        path.moveTo(getWidth()/2, 0);
//        path.lineTo(getWidth()/2, getHeight());
//        canvas.drawPath(path, paint);
//
//        //x軸
//        path.reset();
//        path.moveTo(0, getHeight()/2);
//        path.lineTo(getWidth(), getHeight()/2);
//        canvas.drawPath(path, paint);
//    }

    private void drawData(Canvas canvas, Paint paint){
        Path path = new Path();
        path.moveTo(centerX + data.get(index_now).x*scale + distanceX, centerY + data.get(index_now).y*scale + distanceY);
        int index_least = index_now - data.size()*delay_proportion/100 < 0 ? 0 : index_now - data.size()*delay_proportion/100;
        for (int index = index_now-1; index >= index_least; index--){
            float pointX = centerX + data.get(index).x*scale + distanceX;
            float pointY = centerY + data.get(index).y*scale + distanceY;
            path.lineTo(pointX, pointY);
        }
        canvas.drawPath(path, paint);
    }

//    private void setLineAlpha(int index, Paint paint){
//        int index_Alpha = index/data.size()*100/10;
//        switch (index_Alpha){
//            case 0:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 1:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 2:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 3:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 4:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 5:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 6:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 7:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 8:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//            case 9:
//                paint.setAlpha(alpha[index_Alpha]);
//                break;
//        }
//    }

    private Runnable delayToStart = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(updateSec, 100);
        }
    };

    private Runnable updateSec = new Runnable() {
        @Override
        public void run() {
            index_now++;

            if(index_now < data.size()) {
                AnimationActivity.sb.setProgress(index_now);
                invalidate();
                handler.postDelayed(updateSec, 100);
            }else {
                index_now--;
                invalidate();
                Toast.makeText(mContext, "繪製完畢", Toast.LENGTH_SHORT).show();
                isEnd = true;
            }
        }
    };

    //建立一個Bitmap
    private Bitmap decodeBitmapFromResource(int resName){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resName, options);
        options.inSampleSize = 20;
        options.inJustDecodeBounds =false;
        return  BitmapFactory.decodeResource(getResources(), resName, options);
    }

    public void startDrawing(){
        isDrawing = true;
        handler.postDelayed(delayToStart, 1000);
    }

    public void setIndex_now(int index){
        index_now = index;

        if(isEnd)
            handler.postDelayed(updateSec, 100);
    }

    public void setDelay_proportion(int proportion){
        delay_proportion = proportion;
        invalidate();
    }

    public void dispatchWindowFocusChanged(boolean hasFocus) {
        try {
            super.dispatchWindowFocusChanged(hasFocus);
            if(!hasFocus){
                handler.removeCallbacks(updateSec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
