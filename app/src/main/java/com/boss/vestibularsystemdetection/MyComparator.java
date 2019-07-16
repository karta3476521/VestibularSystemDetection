package com.boss.vestibularsystemdetection;

import java.io.File;
import java.util.Comparator;

public class MyComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        return o1.lastModified() == o2.lastModified() ? 0 : (o1.lastModified() < o2.lastModified() ? 1 : -1 ) ;
    }
}
