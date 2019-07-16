package com.boss.vestibularsystemdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ListOfViewActivity extends AppCompatActivity {
    private Intent intent;
    private Spinner spin;
    private String[] title;
    private ArrayAdapter<String> adp;
    private File[] files;
    private int page;
    private String tarName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_view);

        getStringTitle();
        setSpinner();
    }

    private void getStringTitle(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "沒有外部儲存設備", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            File sdPath = Environment.getExternalStorageDirectory();
            File dirName = new File(sdPath.getAbsoluteFile() + "/CaseFile");
            files = dirName.listFiles();
            MyComparator mComparator = new MyComparator();
            Arrays.sort(files, mComparator);
            ArrayList<String> ti = new ArrayList<String>();

            for (int i = 0; i < files.length; i++) {
                if(files[i].isDirectory())
                    ti.add(files[i].getName());
            }
            title = new String[ti.size()];
            ti.toArray(title);
        }
    }

    private void setSpinner(){
        spin = (Spinner)findViewById(R.id.spinner);
        //預設是第一個選項, 所以通常會留空字串
        adp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, title);
        spin.setAdapter(adp);

        spin.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        page = title.length - position;
                        tarName = title[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
    }

//    public void contrast_XY_onClick(View v){
//        intent = new Intent(ListOfViewActivity.this, MultiViewActivity.class);
//        startActivity(intent);
//    }

//    public void simple_onClick(View v){
//        intent = new Intent(SelectInViewActivity.this, SimpleActivity.class);
//        intent.putExtra("page", page);
//        startActivity(intent);
//    }

    public void complete_onClick(View v){
        if(files.length != 0) {
            intent = new Intent(ListOfViewActivity.this, CompleteActivity.class);
            intent.putExtra("tarName", tarName);
            startActivity(intent);
        }
    }

}
