package com.boss.vestibularsystemdetection;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadData {
    private String name;

    public ReadData(String str){
        this.name = str;
    }

    public ReadData(){}

    private File getFile(String file_name){
        File dirName, sdPath;

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            System.err.println("沒有外部儲存設備");
            return null;
        }else {
            //建立資料夾路徑
            sdPath = Environment.getExternalStorageDirectory();
            dirName = new File(sdPath.getAbsoluteFile() + "/CaseFile/"+name);

            return new File(dirName, "/" + file_name +".xml");
        }
    }

    public double getTargetHeight(String name){
        File dirName, sdPath, fileName = null;
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            System.err.println("沒有外部儲存設備");
        }else {
            //建立資料夾路徑
            sdPath = Environment.getExternalStorageDirectory();
            dirName = new File(sdPath.getAbsoluteFile() + "/CaseFile/");
            fileName = new File(dirName, "/" + name + ".xml");
        }

        if(fileName != null)
            return getValue(fileName);
        else
            return -1.0;
    }

    public ArrayList<Float> getAngleX(){
        File file_name = getFile("angleX");
        return getValues(file_name);
    }

    public ArrayList<Float> getAngleY(){
        File file_name = getFile("angleY");
        return getValues(file_name);
    }

    public ArrayList<Float> getAngleZ(){
        File file_name = getFile("angleZ");
        return getValues(file_name);
    }

    public ArrayList<Float> getHeight(){
        File file_name = getFile("height");
        return getValues(file_name);
    }

    public ArrayList<Float> getCenter(){
        File file_name = getFile("center");
        return getValues(file_name);
    }

    //取得數據
    private ArrayList<Float> getValues(File fileName){
        FileReader fr = null;
        BufferedReader br = null;
        ArrayList<Float> ft = new ArrayList<Float>();
        try{
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            for(String line=br.readLine(); line != null; line=br.readLine()){
                ft.add(Float.parseFloat(line));
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
        return ft;
    }

    //取得數據
    private double getValue(File file){
        FileReader fr = null;
        BufferedReader br = null;
        double height = -1.0;
        try{
            fr = new FileReader(file);
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
