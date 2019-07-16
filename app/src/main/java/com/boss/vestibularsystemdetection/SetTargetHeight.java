package com.boss.vestibularsystemdetection;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SetTargetHeight extends AppCompatActivity {
    private File sdPath, dirName, fileName;
    private EditText edt1, edt2;
    private double chileHeight, adultHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_target_height);
        setTitle("設定目標物高度");

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

        edt1 = (EditText)findViewById(R.id.editText);
        edt2 = (EditText)findViewById(R.id.editText2);
        chileHeight = getValues("childHeight") != -1.0 ? getValues("childHeight") : 0.0;
        edt1.setHint(chileHeight+"");
        adultHeight = getValues("adultHeight") != -1.0 ? getValues("adultHeight") : 120.0;
        edt2.setHint(adultHeight+"");
    }

    public void bt_onClick(View v){
        if(!edt1.getText().toString().matches("")){
            chileHeight = Double.parseDouble(edt1.getText().toString());
            record(chileHeight, "childHeight");
        }

        if(!edt2.getText().toString().matches("")){
            adultHeight = Double.parseDouble(edt2.getText().toString());
            record(adultHeight, "adultHeight");
        }

        finish();
    }

    public void record(double value, String name){
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

    //取得數據
    private double getValues(String name){
        FileReader fr = null;
        BufferedReader br = null;
        File fileName = new File(dirName, "/" + name + ".xml");
        double height = -1.0;
        try{
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            for(String line=br.readLine(); line != null; line=br.readLine()){
                height = Double.parseDouble(line);
            }
        } catch (NumberFormatException ex1){
            System.err.println("沒有內容");
        }catch (FileNotFoundException ex2) {
            System.err.println("檔案不存在");
        }catch (IOException ex3){
            System.err.println("讀取失敗");
        }finally {
            try{
                br.close();
            } catch (IOException ex4) {
                System.err.println("關檔失敗");
            }
        }
        return height;
    }
}
