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
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class AnimationActivity extends AppCompatActivity {
    private AnimationView view;
    static SeekBar sb;
    private TextView tv17;
    private int proportion = 50;
    private String tarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        setTitle("動態軌跡");

        init();
        Intent intent = getIntent();
        tarName = intent.getStringExtra("tarName");

        ReadData mReadData = new ReadData(tarName);
        List<Float> height = mReadData.getHeight();
        List<Float> center = mReadData.getCenter();
        List<Float> radX = mReadData.getAngleX();
        List<Float> radY = mReadData.getAngleY();
        PretreatmentData mPretreatment_data = new PretreatmentData(radX, radY, center, height.get(0).floatValue());
        List<PointF> data = mPretreatment_data.getData();
        PointF centerPoint = mPretreatment_data.getCenterXY();

        sb.setMax(data.size()-1);
        sb.setOnSeekBarChangeListener(seekBar_listener);

        //加入 CustomView
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity=Gravity.CENTER;
        view = new AnimationView(this, data, centerPoint);
        addContentView(view, params);
        view.startDrawing();
    }

    private void init(){
        sb = (SeekBar)findViewById(R.id.seekBar);
        tv17 = (TextView)findViewById(R.id.textView17);
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_animation, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                Intent intent = new Intent(AnimationActivity.this, ChartActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.menu2:
                intent = new Intent(AnimationActivity.this, CompleteActivity.class);
                intent.putExtra("tarName", tarName);
                startActivity(intent);
                break;
            case R.id.home:
                intent = new Intent(AnimationActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private SeekBar.OnSeekBarChangeListener seekBar_listener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            view.setIndex_now(seekBar.getProgress());
        }
    };

    public void add_onClick(View v){
        proportion = proportion+10 > 100 ? 100 : proportion+10;
        tv17.setText(proportion + "%");
        view.setDelay_proportion(proportion);
    }

    public void minus_onClick(View v){
        proportion = proportion-10 < 0 ? 0 : proportion-10;
        tv17.setText(proportion + "%");
        view.setDelay_proportion(proportion);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
