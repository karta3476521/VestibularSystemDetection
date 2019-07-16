package com.boss.vestibularsystemdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class NewListOfViewActivity extends AppCompatActivity {
    private LinearLayout linear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list_of_view);
        setTitle("數據分析");

        //刪除不完整的資料夾
        DeletData mDeletData = new DeletData();
        mDeletData.deleteFile();
        setScrollView();
    }

    private void setScrollView(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "沒有外部儲存設備", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            File sdPath = Environment.getExternalStorageDirectory();
            File dirName = new File(sdPath.getAbsoluteFile() + "/CaseFile");
            File[] files = dirName.listFiles();
            MyComparator mComparator = new MyComparator();
            Arrays.sort(files, mComparator);
            final ArrayList<String> names = new ArrayList<String>();

            for (int i = 0; i < files.length; i++) {
                if(files[i].isDirectory())
                    if(files[i].list().length == 5)
                        names.add(files[i].getName());
            }

            linear = (LinearLayout)findViewById(R.id.myLinear);//取得组件
            for (int i = 0; i < names.size(); i++){
                final Button bt = new Button(this);
                final String name = names.get(i);
                bt.setText(name);
                bt.setTextSize(18);
                bt.setOnClickListener(new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewListOfViewActivity.this, CompleteActivity.class);
                        intent.putExtra("tarName", name);
                        startActivity(intent);
                    }
                });

                bt.setOnLongClickListener(new Button.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        setDialog_checkDeletFile(name, bt);
                        //true : longClick
                        //false : longClick + onClick
                        return true;
                    }
                });

                linear.addView(bt);
            }
        }
    }

    private void setDialog_checkDeletFile(final String name, final Button bt){
        // get dialog.xml view
        LayoutInflater mlayoutInflater = LayoutInflater.from(this);
        View dialogView = mlayoutInflater.inflate(R.layout.delet_dialog, null);
        Button bt13 = (Button)dialogView.findViewById(R.id.button13);
        Button bt14 = (Button)dialogView.findViewById(R.id.button14);

        final AlertDialog mAlertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        bt13.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

        bt14.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                deletFile(name);
                linear.removeView(bt);
                mAlertDialog.cancel();
            }
        });

        mAlertDialog.show();
    }

    private void deletFile(String name){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        }else {
            //建立資料夾路徑
            File sdPath = Environment.getExternalStorageDirectory();
            File dirName = new File(sdPath.getAbsoluteFile() + "/caseFile/" + name);
            File[] listFiles = dirName.listFiles();
            for(File file : listFiles)
                file.delete();

            dirName.delete();
        }
    }
}
