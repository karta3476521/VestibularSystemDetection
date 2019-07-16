package com.boss.vestibularsystemdetection;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CompleteActivity extends AppCompatActivity {
    private String tarName;
    private List<Float> height, center, radX, radY;
    private List<PointF> data, quadrant_1st, quadrant_2st, quadrant_3st, quadrant_4st, quadrant_5st, quadrant_6st, quadrant_7st, quadrant_8st;
    private CustomView view;
    private boolean isAreaClick = false, isPointHide = false;
    private TextView tv8, tv9, tv10, tv11, tv12, tv13, tv14, tv15, tv16;
    private Button bt, bt2;
    private PointF centerPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);
        setTitle("數據分析");

        init();

        Intent intent = this.getIntent();
        tarName = intent.getStringExtra("tarName");

        ReadData mReadData = new ReadData(tarName);
        height = mReadData.getHeight();
        center = mReadData.getCenter();
        radX = mReadData.getAngleX();
        radY = mReadData.getAngleY();

        PretreatmentData mPretreatment_data = new PretreatmentData(radX, radY, center, height.get(0).floatValue());
        data = mPretreatment_data.getData();
        centerPoint = mPretreatment_data.getCenterXY();

        //加入 CustomView
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity=Gravity.CENTER;
        view = new CustomView(this);
        view.setData(data, centerPoint);
        addContentView(view, params);

        DecimalFormat df = new DecimalFormat("##.00");
        tv16.setText("中央到中心距離：" + Double.parseDouble(df.format(Math.hypot(centerPoint.x/10, centerPoint.y/10))) + "cm");
    }

    private void init(){
        bt = (Button)findViewById(R.id.button7);
        bt2 = (Button)findViewById(R.id.button18);
        tv8 = (TextView)findViewById(R.id.textView8);
        tv9 = (TextView)findViewById(R.id.textView9);
        tv10 = (TextView)findViewById(R.id.textView10);
        tv11 = (TextView)findViewById(R.id.textView11);
        tv12 = (TextView)findViewById(R.id.textView12);
        tv13 = (TextView)findViewById(R.id.textView13);
        tv14 = (TextView)findViewById(R.id.textView14);
        tv15 = (TextView)findViewById(R.id.textView15);
        tv16 = (TextView)findViewById(R.id.textView16);
        tv8.bringToFront();
        tv9.bringToFront();
        tv10.bringToFront();
        tv11.bringToFront();
        tv12.bringToFront();
        tv13.bringToFront();
        tv14.bringToFront();
        tv15.bringToFront();
        quadrant_1st = new ArrayList<PointF>();
        quadrant_2st = new ArrayList<PointF>();
        quadrant_3st = new ArrayList<PointF>();
        quadrant_4st = new ArrayList<PointF>();
        quadrant_5st = new ArrayList<PointF>();
        quadrant_6st = new ArrayList<PointF>();
        quadrant_7st = new ArrayList<PointF>();
        quadrant_8st = new ArrayList<PointF>();
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                Intent intent = new Intent(CompleteActivity.this, ChartActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.menu2:
                intent = new Intent(CompleteActivity.this, AnimationActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.home:
                intent = new Intent(CompleteActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void area_onClick(View v){
        isAreaClick = !isAreaClick;

        if(isAreaClick) {
            bt.setText("軌跡");
            bt2.setVisibility(View.VISIBLE);
            setQuadrantToCustomView();
            setText();
            view.showArea();
        }else {
            bt.setText("面積");
            bt2.setVisibility(View.INVISIBLE);
            view.hideArea();
            clearText();
            quadrant_1st = new ArrayList<PointF>();
            quadrant_2st = new ArrayList<PointF>();
            quadrant_3st = new ArrayList<PointF>();
            quadrant_4st = new ArrayList<PointF>();
            quadrant_5st = new ArrayList<PointF>();
            quadrant_6st = new ArrayList<PointF>();
            quadrant_7st = new ArrayList<PointF>();
            quadrant_8st = new ArrayList<PointF>();
        }
    }

    public void point_onClick(View v){
        isPointHide = !isPointHide;
        if(isPointHide) {
            bt2.setText("保留");
            view.hidePoint(true);
        }else {
            bt2.setText("去除");
            view.hidePoint(false);
        }
    }

    private void setQuadrantToCustomView(){
        for (PointF point : data)
            judgePointToQuadrant(point);
        view.setQuadrant(quadrant_1st, quadrant_2st, quadrant_3st, quadrant_4st, quadrant_5st, quadrant_6st, quadrant_7st, quadrant_8st);
    }

    private void setText(){
        DecimalFormat nf = new DecimalFormat("0.0");
        tv8.setText(nf.format((double)quadrant_3st.size() / data.size()*100) + "%");
        tv9.setText(nf.format((double)quadrant_4st.size() / data.size()*100) + "%");
        tv10.setText(nf.format((double)quadrant_5st.size() / data.size()*100) + "%");
        tv11.setText(nf.format((double)quadrant_6st.size() / data.size()*100) + "%");
        tv12.setText(nf.format((double)quadrant_7st.size() / data.size()*100) + "%");
        tv13.setText(nf.format((double)quadrant_8st.size() / data.size()*100) + "%");
        tv14.setText(nf.format((double)quadrant_1st.size() / data.size()*100) + "%");
        tv15.setText(nf.format((double)quadrant_2st.size() / data.size()*100) + "%");
    }

    private void clearText(){
        tv8.setText("");
        tv9.setText("");
        tv10.setText("");
        tv11.setText("");
        tv12.setText("");
        tv13.setText("");
        tv14.setText("");
        tv15.setText("");
    }

    private void judgePointToQuadrant(PointF point){
        double x = point.x - centerPoint.x;
        double y = point.y - centerPoint.y;

        if(x>=0 && -y>=0) {
            if (x >= Math.abs(y))
                quadrant_1st.add(point);
            else
                quadrant_2st.add(point);
        }else if(x<=0 && -y>=0) {
            if (Math.abs(x) >= Math.abs(y))
                quadrant_4st.add(point);
            else
                quadrant_3st.add(point);
        }else if(x<=0 && -y<=0) {
            if (Math.abs(x) >= Math.abs(y))
                quadrant_5st.add(point);
            else
                quadrant_6st.add(point);
        }else if(x>=0 && -y<=0) {
            if (Math.abs(x) >= Math.abs(y))
                quadrant_8st.add(point);
            else
                quadrant_7st.add(point);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
