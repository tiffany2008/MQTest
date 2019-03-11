package com.automation.test;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.automation.client.ClientBuilder;
import com.automation.client.QueueBuilder;
import com.automation.config.ConfigManager;
import com.automation.dt.Trie;
import com.automation.util.Comparison;
import com.automation.util.SubmitMessage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by Tiffany Wang
 */
public class Unit {


    @Test
    public void testTrie(){
        String mockName = "test";
        String mockUrl = ConfigManager.instance().getByKey("endpoint");
        Trie trie = new Trie();
        HashMap<String,String> names = new HashMap<>() ;
        int max = 1000;
        for (int i = 0 ; i < max ; ++i) {
           String curName = mockName + i ;
           String curUrl = mockUrl + i ;
           names.put(curName,curUrl);
           trie.addUrl(curUrl,curName);
        }
        for (String key : names.keySet()) {
            String curName = trie.qNameLookup(names.get(key));
            Assert.assertTrue(names.containsKey(curName));
        }
    }

}
