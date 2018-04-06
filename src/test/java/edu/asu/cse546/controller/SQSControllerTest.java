package edu.asu.cse546.controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author garden
 * @create 3/20/18
 */
public class SQSControllerTest {
    private final static String TESTURL =  "https://sqs.us-west-1.amazonaws.com/778294382379/request_queue";
    private SQSController controller;

    @Before
    public void initialize(){
        controller  = SQSController.getInstance();
        controller.setRequestQueueUrl(TESTURL);
    }

    @Test
    public void sendRequestMsg() {
    }

    @Test
    public void receiveResponseMsg() {
    }

    @Test
    public void getOneRequest() {
        controller.getOneRequest();
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        controller.setProcessingFinished();
    }
    @Test
    public void testRequestQueueSize(){
        System.out.println(this.controller.getRequestQueueSize());
    }
}