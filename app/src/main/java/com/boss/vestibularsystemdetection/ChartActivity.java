package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ChartActivity extends AppCompatActivity {
    private List<ArrayList> collection_length;
    private BarChart barChart;
    private YAxis leftAxis;             //左侧Y轴
    private YAxis rightAxis;            //右侧Y轴
    private XAxis xAxis;                //X轴
    private String tarName;
    private Intent intent;
    private int valueOfClassfication = 2;   //除幾倍
    private double stringOfClassfication = 0.5; //下方的數字
    private int scale = 1;
    private boolean isButtonClick = false;
    private RadioGroup rg;
    private EditText edt;
    private int angle = 135;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setTitle("震幅統計(123)");

        rg = (RadioGroup)findViewById(R.id.radioGroup);
        edt = (EditText)findViewById(R.id.editText3);
        //將radioGroup 移到最上層
        rg.bringToFront();
        edt.bringToFront();
        edt_OnTouch();
        Intent intent = this.getIntent();
        tarName = intent.getStringExtra("tarName");
        showChart();
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chart, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                intent = new Intent(ChartActivity.this, AnimationActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.menu2:
                intent = new Intent(ChartActivity.this, CompleteActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.home:
                intent = new Intent(ChartActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //radioGroup
    public void radioBotton(View v){

        switch (rg.getCheckedRadioButtonId()){
            case R.id.radioButton1:
                scale = 1;
                showChart();
                break;
            case R.id.radioButton2:
                scale = 10;
                showChart();
                break;
            case R.id.radioButton3:
                scale = 20;
                showChart();
                break;
        }
    }

    private void showChart(){
        ReadData rd = new ReadData(tarName);
        List<Float> radX = rd.getAngleX();
        List<Float> radY = rd.getAngleY();
        List<Float> height = rd.getHeight();
        PretreatmentData pd = new PretreatmentData(radX, radY, height.get(0).floatValue());
        List<PointF> data = pd.getData();
        List<PointF> Newdata = obscureValues(data);

        List<Double> length;
        if(!isButtonClick)
            length = getLength(Newdata);
        else
            length = getNewLength(Newdata);
        createCollection(length);
        sort(length);

        barChart = findViewById(R.id.bar_chart);
        initBarChart(barChart);
        showBarChart(collection_length, "XY軸（每" + stringOfClassfication + " cm為一單位）", Color.BLUE);
    }

    private List<PointF> obscureValues(List<PointF> points){
        List<PointF> newPoints = new ArrayList<PointF>();
        for(int i=0; i<points.size()-1; i+=scale)
            newPoints.add(points.get(i));
        return newPoints;
    }

    private ArrayList<Double> getLength(List<PointF> points){
        ArrayList<Double> len = new ArrayList<Double>();
        int old_index = 0;
        for(int i=0; i<points.size()-3; i++) {
            PointF u = new PointF(points.get(i + 1).x - points.get(i).x, points.get(i + 1).y - points.get(i).y);
            PointF v = new PointF(points.get(i + 2).x - points.get(i + 1).x, points.get(i + 2).y - points.get(i + 1).y);
            double aTob = Math.hypot(points.get(i + 1).x - points.get(i).x, points.get(i + 1).y - points.get(i).y);
            double bToc = Math.hypot(points.get(i + 2).x - points.get(i + 1).x, points.get(i + 2).y - points.get(i + 1).y);
            double cosX = (u.x * v.x + u.y * v.y) / (aTob * bToc);
            if(Math.toDegrees(Math.acos(cosX)) <= angle) {
                old_index = i;
                len.add(Math.hypot(points.get(i + 2).x - points.get(old_index).x, points.get(i + 2).y - points.get(old_index).y));
            }else if(i == points.size()-4){
                len.add(Math.hypot(points.get(i + 2).x - points.get(old_index).x, points.get(i + 2).y - points.get(old_index).y));
            }
        }
        return len;
    }

    private ArrayList<Double> getNewLength(List<PointF> points){
        ArrayList<Double> len = new ArrayList<Double>();
        int old_index = 0;
        for(int i=0; i<points.size()-3; i++) {
            PointF u = new PointF(points.get(i + 1).x - points.get(old_index).x, points.get(i + 1).y - points.get(old_index).y);
            PointF v = new PointF(points.get(i + 2).x - points.get(i + 1).x, points.get(i + 2).y - points.get(i + 1).y);
            double aTob = Math.hypot(points.get(i + 1).x - points.get(old_index).x, points.get(i + 1).y - points.get(old_index).y);
            double bToc = Math.hypot(points.get(i + 2).x - points.get(i + 1).x, points.get(i + 2).y - points.get(i + 1).y);
            double cosX = (u.x * v.x + u.y * v.y) / (aTob * bToc);
            if(Math.toDegrees(Math.acos(cosX)) <= angle) {
                old_index = i;
                len.add(Math.hypot(points.get(i + 2).x - points.get(old_index).x, points.get(i + 2).y - points.get(old_index).y));
            }else if(i == points.size()-4){
                len.add(Math.hypot(points.get(i + 2).x - points.get(old_index).x, points.get(i + 2).y - points.get(old_index).y));
            }
        }
        return len;
    }

    private void createCollection(List<Double> len){
        collection_length = new ArrayList<ArrayList>();
        double max_len = Collections.max(len);
        int max_num = (int) max_len/valueOfClassfication;
        for(int i=0; i<=max_num; i++)
            collection_length.add(new ArrayList<Double>());
    }

    private void sort(List<Double> len){
        for(double d : len){
            int num = (int)d/valueOfClassfication;
            collection_length.get(num).add(d);
        }
    }

    //初始化BarChart图表
    private void initBarChart(BarChart barChart) {
        //图表设置
        //背景颜色
        barChart.setBackgroundColor(Color.WHITE);
        //不显示图表网格
        barChart.setDrawGridBackground(false);
        //背景阴影
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        //不显示边框
        barChart.setDrawBorders(false);
        //设置动画效果
        barChart.animateY(1000,  Easing.EasingOption.Linear);
        barChart.animateX(1000,  Easing.EasingOption.Linear);
        //不顯示右下角描述
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        //禁用圖表觸摸事件
        barChart.setTouchEnabled(false);

        //XY轴的设置
        //X轴设置显示位置在底部
        xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        //不显示X轴网格线
        xAxis.setDrawGridLines(false);
        //X轴自定义值
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //1：表示每1 單位為一組
                return String.valueOf(((int) value) * stringOfClassfication);
            }
        });

        leftAxis = barChart.getAxisLeft();
        rightAxis = barChart.getAxisRight();
        //不顯示右側y軸
        rightAxis.setEnabled(false);
        //右侧Y轴网格线设置为虚线
        rightAxis.enableGridDashedLine(10f, 10f, 0f);

        //保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        rightAxis.setAxisMinimum(0f);

    }

    public void showBarChart(List<ArrayList> dateValueList, String name, int color) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dateValueList.size(); i++) {

            BarEntry barEntry = new BarEntry(i, (float) dateValueList.get(i).size());
            entries.add(barEntry);
        }
        // 每一个BarDataSet代表一类柱状图
        BarDataSet barDataSet = new BarDataSet(entries, name);
        initBarDataSet(barDataSet, color);

        BarData data = new BarData(barDataSet);
        barChart.setData(data);
    }

    private void initBarDataSet(BarDataSet barDataSet, int color) {
        barDataSet.setColor(color);
        barDataSet.setFormLineWidth(1f);
        barDataSet.setFormSize(15.f);
        //修改柱狀圖頂部值為整術
        barDataSet.setValueFormatter(
                new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        return String.valueOf((int)value);
                    }
                }
        );
        //頂部值大小
        barDataSet.setValueTextSize(10f);
    }

    public void bt_onClick(View v){
        Button bt2 = (Button)findViewById(R.id.button15);
        isButtonClick = !isButtonClick;
        if(isButtonClick) {
            bt2.setText("123");
            setTitle("震幅統計(023)");
        }else {
            bt2.setText("023");
            setTitle("震幅統計(123)");
        }

        showChart();
    }

    private void edt_OnTouch(){
        edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtGetFocus(edt);
                }else {
                    edtLostFocus(edt);
                }
            }
        });
    }

    // 重置edittext, 居中并失去焦点
    private void edtLostFocus(EditText mEditText) {
        System.err.println("字串："+mEditText.getText().toString());
        if(isNumber(mEditText.getText().toString()) && mEditText.getText().length() != 0) {
            System.err.println("in");
            angle = 180 - Integer.parseInt(mEditText.getText().toString());
            mEditText.setText(mEditText.getText().toString() + "度");
        }else {
            int str = 180 - angle;
            mEditText.setText(str + "度");
        }
        mEditText.setGravity(Gravity.CENTER);
        showChart();
        InputMethodManager imm = (InputMethodManager)ChartActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    //获取焦点
    private void edtGetFocus(final EditText mEditText) {
        mEditText.requestFocus();
        mEditText.setGravity(Gravity.START);
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager manager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.showSoftInput(mEditText, 0);
            }
        });
        //清除edt
        edt.setText("");
        edt.setHint("度");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获取当前焦点所在的控件；
            View view = getCurrentFocus();
            if (view != null && view instanceof EditText) {
                Rect r = new Rect();
                view.getGlobalVisibleRect(r);
                int rawX = (int) ev.getRawX();
                int rawY = (int) ev.getRawY();
                // 判断点击的点是否落在当前焦点所在的 view 上；
                if (!r.contains(rawX, rawY)) {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    //判斷是否為數字
    public static boolean isNumber(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
