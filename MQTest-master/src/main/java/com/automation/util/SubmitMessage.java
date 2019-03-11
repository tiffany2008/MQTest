package com.automation.util;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Created by Tiffany Wang
 */
public class SubmitMessage implements Runnable {


    private int min ;
    private int max ;
    private AmazonSQS sqs ;
    private String url;

    public SubmitMessage (int min, int max , AmazonSQS sqs, String url){
       this.min = min;
       this.max = max ;
       this.sqs = sqs ;
       this.url = url ;
    }

    @Override
    public void run() {
        while (min < max) {
            SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                    .withQueueUrl(url)
                    .withMessageBody("message" + min++);
            sqs.sendMessage(sendMessageStandardQueue);
        }
    }
}
