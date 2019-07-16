package com.boss.vestibularsystemdetection;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InputData {
    private File sdPath, dirName, fileName, tarDirName;
    private String name;
    static boolean NEW = false, LAST = true;

    public InputData(Context context, String name, boolean select){
        this.name = name;

        createFilePath(select);
    }

    private void createFilePath(boolean select){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//            Toast.makeText(context, "沒有外部儲存設備", Toast.LENGTH_SHORT).show();
        }else {
//            Toast.makeText(context, "有外部儲存設備", Toast.LENGTH_SHORT).show();
            //建立資料夾路徑
            sdPath = Environment.getExternalStorageDirectory();
            dirName = new File(sdPath.getAbsoluteFile() + "/caseFile");

            if(select) {
                File[] listFile = dirName.listFiles();
                for(File file : listFile){
                    if(file.isDirectory()){
                        if (file.list().length < 5) {
                            tarDirName = file;
                            break;
                        }
                    }
                }
            }else {
                String userName = getValues("Name");
                String userYear = getValues("Year");
                Date dNow = new Date();
                SimpleDateFormat ft = new SimpleDateFormat("MM月dd號 ahh:mm:ss");
                tarDirName = new File(dirName, userName + " " + userYear + "歲 " + ft.format(dNow));
                if(!tarDirName.exists())
                    tarDirName.mkdir();
            }

            fileName = new File(tarDirName, "/" + name + ".xml");
        }
    }

    public void record(float value){
        FileWriter fw = null;
        BufferedWriter bw = null;

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

    public void record(float[] values){
        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(fileName, false);
            bw = new BufferedWriter(fw);

            for(float value : values) {
                String str = value + "\n";
                bw.write(str);
            }
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

    public void record(List<Float> values){
        FileWriter fw = null;
        BufferedWriter bw = null;

        try{
            fw = new FileWriter(fileName, false);
            bw = new BufferedWriter(fw);

            for(float value : values) {
                String str = value + "\n";
                bw.write(str);
            }
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
    private String getValues(String name){
        FileReader fr = null;
        BufferedReader br = null;
        File fileName = new File(dirName, "/" + name + ".xml");
        String str = "";
        try{
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            for(String line=br.readLine(); line != null; line=br.readLine()){
                str = line;
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
        return str;
    }

}
