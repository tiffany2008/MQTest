package com.automation.dt;

/**
 * Created by Tiffany Wang
 * an optimized prefix tree
 * which has O(1) performance in URL lookup
 * and better performance in memory usage comparing to hash structure
 *
 */
public class Trie {

    private Node root ;

    public Trie(){
        root  = new Node() ;
    }

    public void addUrl (String url, String qName){
        Node cur = root ;
        for (char c : url.toCharArray()) {
            if (cur.sub[c] == null) {
                cur.sub[c] = new Node() ;
            }
            cur = cur.sub[c] ;
        }
        cur.match  = true ;
        cur.qName = qName ;
    }

    public String qNameLookup (String url){
        Node cur = root ;
        for (char c : url.toCharArray()) {
            if (cur.sub[c] == null) return "" ;
            cur = cur.sub[c] ;
        }
        if (!cur.match) return "" ;
        return cur.qName ;
    }

    static private class Node {
        //extended ASCII
        Node [] sub = new Node[1 << 8];
        String qName ;
        boolean match ;
    }


}
