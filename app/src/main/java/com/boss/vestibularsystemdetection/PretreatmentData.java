package com.boss.vestibularsystemdetection;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class PretreatmentData {
    private List<Float> radince, radince2;
    private List<PointF> data;
    private float height, centerX, centerY, centerZ;
    private int scale = 10;
    private boolean isCorrectDirection;

    public PretreatmentData(List<Float> rad, List<Float> rad2, List<Float> center, float height){
        this.height = height;

        judgeDirection(center);
        standardCenter(center);
        standardRad(rad, rad2);
        List<Float> distance = radToDistance(radince);
        List<Float> distance2 = radToDistance(radince2);
        List<Float> distance_magnify = magnifyData(distance);
        List<Float> distance2_magnify = magnifyData(distance2);
        combineToPoint(distance_magnify, distance2_magnify);
    }

    public PretreatmentData(List<Float> rad, List<Float> rad2, float height){
        this.height = height;

        standardRad(rad, rad2);
        List<Float> distance = radToDistance(radince);
        List<Float> distance2 = radToDistance(radince2);
//        List<Float> distance_magnify = magnifyData(distance);
//        List<Float> distance2_magnify = magnifyData(distance2);
        combineToPoint(distance, distance2);
    }

    private void judgeDirection(List<Float> list){
        if(list.get(1) > 0)
            isCorrectDirection = true;
        else
            isCorrectDirection = false;
    }

    private void standardCenter(List<Float> list){

        if(isCorrectDirection){
            //turn right
            centerX = list.get(0);
            centerY = list.get(1);
        }else{
            //turn left
            centerX = -list.get(0);
            centerY = -list.get(1);
        }

        centerX = centerX * height * scale;
        centerY = (centerY - (float) Math.toRadians(90)) * height * scale;
        centerZ = (list.get(2)) * height * scale;
    }

    private void standardRad(List<Float> list, List<Float> list2){
        radince2 = new ArrayList<Float>();

        if(isCorrectDirection) {
            radince = list;
            for(float value : list2)
                radince2.add(value - (float) Math.toRadians(90));
        }else {
            radince = new ArrayList<Float>();
            for (float value : list)
                radince.add(-value);
            for(float value : list2)
                radince2.add((-value) - (float) Math.toRadians(90));
        }
    }

    private void combineToPoint(List<Float> list, List<Float> list2){
        data = new ArrayList<PointF>();

        for(int i=1; i<list.size()-1; i++)
            data.add(new PointF(list.get(i).floatValue(), list2.get(i).floatValue()));

    }

    private List<Float> radToDistance(List<Float> list){
        List<Float> distance = new ArrayList<Float>();
        for(int i=0; i<list.size()-1; i++)
            distance.add(list.get(i).floatValue() * height);
        return distance;
    }

    private List<Float> magnifyData(List<Float> list){
        List<Float> magnify_data = new ArrayList<Float>();
        for(int i=0; i<list.size()-1; i++)
            magnify_data.add(list.get(i)*scale);
        return magnify_data;
    }

    public List<PointF> getData(){
        return data;
    }

    public PointF getCenterXY(){
        return new PointF(centerX, centerY);
    }

//    public PointF getCenterXZ(){
//        return new PointF(centerX, centerZ);
//    }

//    public PointF getCenterYZ(){
//        return new PointF(centerY, centerZ);
//    }
}
