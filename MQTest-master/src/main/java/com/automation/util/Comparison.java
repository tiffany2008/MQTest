package com.automation.util;

import java.util.HashMap;

/**
 * Created by Tiffany Wang
 */
public class Comparison {

    /*
     linear time complexity
     */
    public static boolean listEqualsIgnoreOrder(String [] expected , String [] actual){
        if (expected == null || actual == null || actual.length != expected.length) return false;
        HashMap<String,Integer> dict = new HashMap<> ();
        for (String e : expected) {
            dict.put(e, dict.getOrDefault(e, 0) + 1);
        }
        for (String a : actual) {
            int cnt = dict.getOrDefault(a, 0) - 1 ;
            if (cnt == -1) return false ;
            if (cnt == 0) {
                dict.remove(a) ;
            } else {
                dict.put(a, cnt) ;
            }
        }
        if(!dict.isEmpty()) System.err.println("ERROR : missing items { " + buildErrorMessage(dict) + "}");
        return dict.isEmpty();
    }

    private static String buildErrorMessage (HashMap<String,Integer> dict){
        StringBuilder error = new StringBuilder() ;
        for (String k : dict.keySet()) {
            if (error.length() != 0) {
                error.append(k);
                error.append(",");
            }
        }
        if(error.length() > 0) error.setLength(error.length() - 1);
        return error.toString();
    }


}
