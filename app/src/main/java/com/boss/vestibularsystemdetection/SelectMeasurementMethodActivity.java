package com.boss.vestibularsystemdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class SelectMeasurementMethodActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private Intent intent;
    private Switch mSwitch;
    private TextView tv19;
    private File sdPath, dirName, fileName;
    private int mode;
    private boolean isSetName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_measurement_method);
        getSupportActionBar().hide();

//        makeHeightXml();
        getSD();
        mSwitch = (Switch)findViewById(R.id.switch1);
        tv19 = (TextView)findViewById(R.id.textView19);
        mSwitch.setOnCheckedChangeListener(this);

        File file = new File(dirName, "Name.xml");
        File file2 = new File(dirName, "Year.xml");
        if(file.exists() && file2.exists())
            isSetName = true;
    }

    private void getSD(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //沒有外部SD
        }else {
            //建立資料夾路徑
            sdPath = Environment.getExternalStorageDirectory();
            dirName = new File(sdPath.getAbsoluteFile() + "/caseFile");

            //建立資料夾
            if (!dirName.exists()) {
                dirName.mkdir();
            }
        }
    }

//    private void makeHeightXml(){
//        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//            //沒有外部SD
//        }else {
//            //建立資料夾路徑
//            sdPath = Environment.getExternalStorageDirectory();
//            dirName = new File(sdPath.getAbsoluteFile() + "/caseFile");
//
//            //建立資料夾
//            if (!dirName.exists()) {
//                dirName.mkdir();
//            }
//
//            fileName = new File(dirName, "/childHeight.xml");
//            if(!fileName.exists()){
//                record(-1.0+"", "childHeight");
//            }
//            fileName = new File(dirName, "/adultHeight.xml");
//            if(!fileName.exists()){
//                record(-1.0+"", "adultHeight");
//            }
//        }
//    }

    public void record(String value, String name){
        FileWriter fw = null;
        BufferedWriter bw = null;
        fileName = new File(dirName, "/" + name + ".xml");

        try{
            fw = new FileWriter(fileName, false);
            bw = new BufferedWriter(fw);

            bw.write(value+"");
        }catch (FileNotFoundException ex1){
            System.err.println("檔案不存在");
            System.err.println("路徑：" + fileName);
        }catch (IOException ex2) {
            System.err.println("寫入失敗");
        }finally {
            try{
                if(bw != null)
                    bw.close();
            } catch (IOException e) {
                System.err.println("關檔失敗");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if(isChecked) {
            tv19.setText("小孩模式");
            mode = 1;
        }else {
            tv19.setText("大人模式");
            mode = 0;
        }
    }

    public void vrMeasurement_onClick(View v){
        if(isSetName) {
            intent = new Intent(SelectMeasurementMethodActivity.this, VrMeasurementActivity.class);
            intent.putExtra("Mode", mode);
            startActivity(intent);
        }
    }

    public void manualMeasurement_onClick(View v){
        if(isSetName) {
            intent = new Intent(SelectMeasurementMethodActivity.this, ManualMeasurementActivity.class);
            intent.putExtra("Mode", mode);
            startActivity(intent);
        }
    }

    public void setHeight_onClick(View v){
        intent = new Intent(SelectMeasurementMethodActivity.this, SetTargetHeight.class);
        startActivity(intent);
    }

    public void setName(View v){
        setDialog();
    }

    private void setDialog() {
        // get dialog.xml view
        LayoutInflater mlayoutInflater = LayoutInflater.from(this);
        View dialogView = mlayoutInflater.inflate(R.layout.set_name, null);

        final EditText nameInput = (EditText) dialogView.findViewById(R.id.edtDialogInput);
        final EditText yearInput = (EditText)dialogView.findViewById(R.id.edtDialogInput2);
        Button bt16 = (Button)dialogView.findViewById(R.id.button16);
        Button bt17 = (Button)dialogView.findViewById(R.id.button17);

        final AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        bt16.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

        bt17.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (nameInput.getText().toString().matches("") || yearInput.getText().toString().matches("")) {
                    System.err.println("in");
                }else {
                    String name = nameInput.getText().toString();
                    String year = yearInput.getText().toString();
                    File file = new File(sdPath.getAbsoluteFile() + "/caseFile/",  "Name");
                    record(name, "Name");
                    File file2 = new File(sdPath.getAbsoluteFile() + "/caseFile/",  "Year");
                    record(year, "Year");
                    isSetName = true;
                    mAlertDialog.cancel();
                }
            }
        });

        mAlertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}