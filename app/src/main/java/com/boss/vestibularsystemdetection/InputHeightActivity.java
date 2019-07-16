package com.boss.vestibularsystemdetection;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class InputHeightActivity extends AppCompatActivity {
    private double height = 0.0;
    private Handler handler;
    private MyMediaTool mp_centerStart, mp_inputHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_height);
        getSupportActionBar().hide();

        handler = new Handler();
        setDialog_inputHeight();
    }

    private void setDialog_inputHeight(){
        mp_inputHeight = new MyMediaTool(this, R.raw.input_height);
        mp_inputHeight.startPlayer();
        // get dialog.xml view
        LayoutInflater mlayoutInflater = LayoutInflater.from(this);
        View dialogView = mlayoutInflater.inflate(R.layout.dialog, null);

        TextView tv1 = (TextView)dialogView.findViewById(R.id.textView1);
        tv1.setText("輸入手機在眼鏡的高度(cm)");
        final EditText userInput = (EditText) dialogView.findViewById(R.id.edtDialogInput);

        final AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if(userInput.getText().toString().matches("")) {
                            mp_inputHeight.stopPlayer();
                            mp_inputHeight.resetPlayer();
                            dialog.cancel();
                            setDialog_inputHeight();
                        } else {
                            mp_inputHeight.stopPlayer();
                            height = Double.parseDouble(userInput.getText().toString());
                            InputData mInputData = new InputData(InputHeightActivity.this, "height", InputData.NEW);
                            mInputData.record((float) height);
                            handler.postDelayed(delayToCenter, 16000);
                            mp_centerStart.startPlayer();
                            dialog.cancel();
                        }
                    }
                })
                .create();

        mAlertDialog.show();
    }

    private Runnable delayToCenter = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(InputHeightActivity.this, CenterMeasureActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mp_centerStart = new MyMediaTool(this, R.raw.center_for_manual);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp_centerStart.releasePlayer();
        mp_inputHeight.releasePlayer();
    }
}
