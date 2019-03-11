package com.automation.test;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.automation.client.ClientBuilder;
import com.automation.client.QueueBuilder;
import com.automation.util.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Created by Tiffany Wang
 */
public class SQSTest {

    private AmazonSQS sqs ;

    private String url ;

    private List<Message> sqsMessages;


    @BeforeClass
    public void setup (){
        sqs = ClientBuilder.build().client() ;
        url = QueueBuilder.factory().buildSQS("testSQS");
        //avoid null pointer
        sqsMessages = new ArrayList<>();
    }


    /**
     * this test is to test sending a message and receiving message
     */
    @Test
    public void testSingleMessage_send_receive (){
        String body = "A test message." ;
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageBody(body);
        sqs.sendMessage(sendMessageStandardQueue);

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url);
        receiveMessageRequest.setMaxNumberOfMessages(10);

        sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        Assert.assertTrue(sqsMessages.size() == 1);
        Message cur = sqsMessages.get(0);
        //verify body
        Assert.assertEquals(cur.getBody(),body);
    }

    /**
     * this test is to test sending a delay message
     */
    @Test
    public void testSingleMessage_send_receive_delay(){
        String body = "delay test message." ;
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageBody(body)
                .withDelaySeconds(6);
        sqs.sendMessage(sendMessageStandardQueue);
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url)
                    .withWaitTimeSeconds(20)
                    .withMaxNumberOfMessages(1);

        sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        Assert.assertTrue(sqsMessages.size() == 1);
        Message cur = sqsMessages.get(0);
        //verify body
        Assert.assertEquals(cur.getBody(),body);
    }


    /**
     * this test is to test sending a delay message and without set wait time
     */
    @Test
    public void testSingleMessage_send_receive_delay_without_wait(){
        String body = "delay test message." ;
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageBody(body)
                .withDelaySeconds(6);
        sqs.sendMessage(sendMessageStandardQueue);
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url)
                .withWaitTimeSeconds(0)
                .withMaxNumberOfMessages(1);

        sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        //no message should be received
        Assert.assertTrue(sqsMessages.size() == 0);
    }


    /**
    *this test is to send a batch message
    *SQS will not guarantee the order of message ,so only compare the content
     */
    @Test
    public void testSendBatchMessages(){
        List <SendMessageBatchRequestEntry> messageEntries = new ArrayList<>();
        String [] items = {"batch-1", "batch-2", "batch-3"} ;
        IntStream.range(0, items.length).forEach(index -> {
            messageEntries.add(new SendMessageBatchRequestEntry()
                    .withId("id" + (index + 1))
                    .withMessageBody(items[index]));
        });
        SendMessageBatchRequest sendMessageBatchRequest
                = new SendMessageBatchRequest(url, messageEntries);
        sqs.sendMessageBatch(sendMessageBatchRequest);
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url);
        receiveMessageRequest.setMaxNumberOfMessages(items.length);
        sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        String [] expected = new String [items.length] ;
        IntStream.range(0,sqsMessages.size()).forEach(index -> {
            expected[index] = sqsMessages.get(index).getBody();
        });

        Assert.assertTrue(Comparison.listEqualsIgnoreOrder(items,expected));
    }

    /*
    *send heaps of message for scalability testing
    */
    @Test
    public void testSendMultipleMessages(){
        String [] items = new String[1 << 6];
        IntStream.range(0, items.length).forEach(index -> {
            items[index] = "message" + index;

        });
        int cnt = 0 ;
        while (cnt < items.length) {
            SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                    .withQueueUrl(url)
                    .withMessageBody(items[cnt++]);
            sqs.sendMessage(sendMessageStandardQueue);
        }
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url);
        receiveMessageRequest.setMaxNumberOfMessages(10);
        receiveMessageRequest.setWaitTimeSeconds(10);
        cnt = 0 ;
        String [] expected = new String [items.length] ;

        /*
         polling all messages
         */
        while(cnt < items.length) {
            sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (int i = 0 ; cnt < items.length && i < sqsMessages.size() ; ++i) {
                expected[cnt++] = sqsMessages.get(i).getBody();
                sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(url).withReceiptHandle(
                        sqsMessages.get(i).getReceiptHandle()
                ));
            }
        }
        Assert.assertTrue(Comparison.listEqualsIgnoreOrder(items,expected));
    }

    /**
     * this test is emulate multiple clients connect to one server. still scalability test
     *
     */
    @Test
    public void multi_thread_connection_sending (){
        /*
        Emulate multiple clients
        get available cpu core first then for each core attach a thread to it
         */
        ExecutorService executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int max = 100 ;
        for (int i = 0 ; i < max ; i+=10) {
            executor.execute(new SubmitMessage(i, i + 10,sqs,url));
        }
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(url);
        receiveMessageRequest.setMaxNumberOfMessages(10);
        receiveMessageRequest.setWaitTimeSeconds(10);
        int cnt = 0 ;
         /*
         polling all messages
         */
        while(cnt < max) {
            sqsMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            for (int i = 0 ; cnt < max && i < sqsMessages.size() ; ++i) {
                sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(url).withReceiptHandle(
                        sqsMessages.get(i).getReceiptHandle()
                ));
                cnt++;
            }
        }
        Assert.assertTrue(cnt == max);
    }


    @AfterMethod
    public void clearq(){
         for (Message message : sqsMessages) {
             sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(url).withReceiptHandle(
                     message.getReceiptHandle()
             ));
         }
        sqs.deleteMessageBatch(new DeleteMessageBatchRequest(url));

    }

    @AfterClass
    public void cleanup(){
        QueueBuilder.factory().deletQueue(url);
    }

}
