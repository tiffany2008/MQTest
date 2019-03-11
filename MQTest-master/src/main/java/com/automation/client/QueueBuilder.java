package com.automation.client;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.automation.dt.Trie;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Tiffany Wang
 */
public class QueueBuilder {

    private HashMap<String,String> urls ;

    private AmazonSQS client ;

    private HashMap<String,QueueRule> cmd ;

    private static QueueBuilder inst;

    private Trie trie ;


    private QueueBuilder (){
        urls = new HashMap<>();
        client = ClientBuilder.build().client() ;
        cmd = new HashMap<>() ;
        trie = new Trie() ;
        //loading cmd to a map
        cmd.put("SQS",(sqs,name,config)  -> {
            CreateQueueRequest createStandardQueueRequest = new CreateQueueRequest(name);
            return sqs.createQueue(createStandardQueueRequest).getQueueUrl();
        });
        cmd.put("FIFO",(sqs,name,attribute)  -> {
            CreateQueueRequest createFifoQueueRequest = new CreateQueueRequest(
                    name).withAttributes(attribute);
            return sqs.createQueue(createFifoQueueRequest)
                    .getQueueUrl();
        });
    }

   public static synchronized QueueBuilder factory (){
       if (inst == null) inst = new QueueBuilder();
       return inst ;
   }

   public synchronized String buildSQS(String name){
       String url = urls.getOrDefault(name,cmd.get("SQS").build(client, name, new HashMap<>()));
       urls.put(name,url);
       trie.addUrl(url,name);
       return url ;
   }

   public synchronized String buildFIFO(String name, Map<String,String> config){
       String url = urls.getOrDefault(name, cmd.get("FIFO").build(client, name, config));
       urls.put(name, url);
       trie.addUrl(url,name);
       return url;
   }

   public String buildDefaultFIFO(String name){
       Map<String, String> attributes = new HashMap<>();
       attributes.put("FifoQueue", "true");
       attributes.put("ContentBasedDeduplication", "true");
       return buildFIFO(name,attributes);
   }

   public synchronized void deletQueue(String url){
      client.deleteQueue(new DeleteQueueRequest(url));
       //lookup qName
      urls.remove(trie.qNameLookup(url));
   }


}
