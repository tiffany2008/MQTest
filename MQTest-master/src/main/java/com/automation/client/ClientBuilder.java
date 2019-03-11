package com.automation.client;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.automation.config.ConfigManager;

/**
 * Created by Tiffany Wang
 */
public class ClientBuilder {


    private  static ClientBuilder instance ;

    private AmazonSQS client ;

    //creating intance is not allowed
    private ClientBuilder () {
        ConfigManager configManager = ConfigManager.instance();
        client = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(configManager.getByKey("accessKey"),
                        configManager.getByKey("secretKey"))))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(configManager.getByKey("endpoint"), configManager.getByKey("region")))
                .build();
    }

    public static synchronized   ClientBuilder build (){
        if (instance == null) instance = new ClientBuilder() ;
        return instance ;
    }

    public AmazonSQS client (){
        return client ;
    }




}
