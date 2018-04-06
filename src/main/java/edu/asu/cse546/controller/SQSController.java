package edu.asu.cse546.controller;

import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import edu.asu.cse546.helper.SQSHelper;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class SQSController {
    private final static String ACCOUNT_ID = "234176573851/";
    private final static String REGION_URL = "https://sqs.us-west-1.amazonaws.com/";
    private final static String REQUEST_QUEUE = "546_request_queue";
    private final static String RESPONSE_QUEUE = "546_response_queue";
    private volatile boolean processingFlag = true;
    private String requestQueueUrl;
    private String responseQueueUrl;
    private SQSHelper sqsHelper;
    private static SQSController sqsController;
    private String currentMsgReceiptHandle;
    private volatile HashMap<String,String> resultsMap;

    class ControlThread implements Runnable{
        private Message message;
        public ControlThread(Message message){
            this.message = message;
        }
        @Override
        public void run() {
            while(processingFlag){
                try {
                    sqsHelper.setMessageInvisible(requestQueueUrl, message.getReceiptHandle(), 20);
                    // Sleep the thread to see if the task is finished.
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (AmazonSQSException e){
                    break;
                }
            }
            processingFlag = true;
        }
    }
    class ResponseFetchThread implements Runnable{
        private HashMap<String,String> resultsMap;
        public ResponseFetchThread(HashMap<String,String> resultsMap) {this.resultsMap = resultsMap;}
        @Override
        public void run() {
            while (true) {
                try {
                    for(Message message : sqsHelper.receive(responseQueueUrl)){
                        JSONObject json = new JSONObject(message.getBody());
                        this.resultsMap.put(json.getString("job_id"),json.getString("result"));
                        sqsHelper.delete(responseQueueUrl,message.getReceiptHandle());
                    }
                } catch (QueueDoesNotExistException e) {
                    continue;
                }

            }
        }
    }

    private SQSController() {
        this.sqsHelper = new SQSHelper();
        this.resultsMap = new HashMap<>();
        this.requestQueueUrl = REGION_URL + ACCOUNT_ID + REQUEST_QUEUE;
        this.responseQueueUrl = REGION_URL + ACCOUNT_ID + RESPONSE_QUEUE;
        // Start the thread to collect response from sqs
        ResponseFetchThread responseFetchThread = new ResponseFetchThread(this.resultsMap);
        new Thread(responseFetchThread).start();
        // Initialize the queue
        if (this.sqsHelper.getQueuesWithPrefix("546_").size() == 0) {
            this.sqsHelper.createQueue(REQUEST_QUEUE, "0", "86400", "20");
            this.sqsHelper.createQueue(RESPONSE_QUEUE, "0", "86400", "20");
        }
    }

    public static SQSController getInstance() {
        if (sqsController == null) {
            sqsController = new SQSController();
            return sqsController;
        } else
            return sqsController;
    }

    public void sendMsg(String message) {
        this.sqsHelper.send(this.requestQueueUrl, message, 0);
    }

    public String receiveResponseMsg(String requestId) {
        while (true) {
            if (this.resultsMap.containsKey(requestId))
                return resultsMap.get(requestId);
        }
    }

    public Message getOneRequest(){
        List<Message> requests =  this.sqsHelper.receive(this.requestQueueUrl, 1, 20);
        if (requests.size() > 0){
            Message message = requests.get(0);
            this.currentMsgReceiptHandle = message.getReceiptHandle();
            ControlThread controlThread = new ControlThread(message);
            new Thread(controlThread).start();
            return message;
        }else{
            // Return null when there is no request in the queue
            return null;
        }

    }

    public void setProcessingFinished(){
        this.processingFlag = false;
        if (this.currentMsgReceiptHandle != null){
            this.sqsHelper.delete(this.requestQueueUrl, this.currentMsgReceiptHandle);
        }else{
            System.out.println("ERROR at SETTING currentMsgReceiptHandle");
        }
    }

    public int getRequestQueueSize() {
        String attributeName = QueueAttributeName.ApproximateNumberOfMessages.toString();
        String result = this.sqsHelper.getQueueAttribute(getRequestQueueUrl(),attributeName).getAttributes().get(attributeName);

        return Integer.valueOf(result);
    }

    public String getRequestQueueUrl() {
        return requestQueueUrl;
    }

    public void setRequestQueueUrl(String requestQueueUrl) {
        this.requestQueueUrl = requestQueueUrl;
    }

    public String getResponseQueueUrl() {
        return responseQueueUrl;
    }

    public void setResponseQueueUrl(String responseQueueUrl) {
        this.responseQueueUrl = responseQueueUrl;
    }
}
