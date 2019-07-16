package com.boss.vestibularsystemdetection;

import android.os.Environment;

import java.io.File;

public class DeletData {
    private File sdPath, dirName;

    public DeletData(){

    }

    public void deleteFile(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        }else {
            //建立資料夾路徑
            sdPath = Environment.getExternalStorageDirectory();
            dirName = new File(sdPath.getAbsoluteFile() + "/caseFile");

            File[] listFiles = dirName.listFiles();
            for(File file : listFiles){
                if(file.isDirectory())
                    if(file.list().length < 5)
                        delete(file);
            }
        }
    }

    private void delete(File files){
        for(File file : files.listFiles()){
            file.delete();
        }
        files.delete();
    }
}
