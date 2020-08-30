package com.dgs.v1.util;

public class MathUtils {

    public static Double round(double num) {
        return Math.round(num * 100.0) / 100.0;
    }

    public static Double round(Double num) {
        return Math.round(num * 100.0) / 100.0;
    }
    // Function to find mean
    // of the array elements.
    public static double Sum(Double[] arr)
    {
        // Calculate sum of all elements.
        double sum = 0;

        for (int i = 0; i < arr.length; i++)
            sum = sum + arr[i];

        return sum;
    }

    // Function to find mean
    // of the array elements.
    public static Double Mean(Double[] arr, int n)
    {
        // Calculate sum of all elements.
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            sum += arr[i];
        }

        return sum / n;
    }

    // Function to find mean absolute
    // deviation of given elements.
    public static double meanAbsDevtion(Double[] arr,
                                        int n)
    {
        // Calculate the sum of absolute
        // deviation about mean.
        double absSum = 0;

        for (int i = 0; i < n; i++)
            absSum = absSum + Math.abs(arr[i]
                    - Mean(arr, n));

        // Return mean absolute
        // deviation about mean.
        return absSum / n;
    }

}
