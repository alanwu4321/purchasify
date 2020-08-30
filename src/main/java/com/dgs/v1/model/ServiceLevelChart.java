package com.dgs.v1.model;

import java.util.NavigableMap;
import java.util.TreeMap;

public class ServiceLevelChart {

    public static NavigableMap<Double, Double> treeMap = new TreeMap<Double, Double>() {{
        put(0.0, 0.00);
        put(50.0, 0.00);
        put(75.0, 0.84);
        put(79.0, 1.00);
        put(80.0, 1.05);
        put(84.13, 1.25);
        put(85.0, 1.30);
        put(89.44, 1.56);
        put(90.0, 1.60);
        put(93.32, 1.88);
        put(94.0, 1.95);
        put(94.52, 2.00);
        put(95.0, 2.06);
        put(96.0, 2.19);
        put(97.0, 2.35);
        put(97.72, 2.50);
        put(98.0, 2.56);
        put(99.0, 2.91);
        put(99.18, 3.00);
        put(99.50, 3.20);
        put(99.70, 3.44);
        put(99.86, 3.75);
        put(99.90, 3.85);
        put(99.93, 4.00);
        put(99.99, 5.00);
    }};

    public static Double get(double lvl) {
        try {
            return ServiceLevelChart.treeMap.getOrDefault(lvl, ServiceLevelChart.treeMap.lowerEntry(lvl).getValue());
        }catch(Exception e){
            //couldn't get lower level than 0
            return 0.0;
        }
    }

}
