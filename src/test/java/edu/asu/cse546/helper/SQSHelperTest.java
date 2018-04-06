package edu.asu.cse546.helper;

import com.amazonaws.services.sqs.model.Message;
import org.junit.*;


import java.util.List;

public class SQSHelperTest {
    private SQSHelper sqsHelper;
    private String url;

    @Before
    public void initialze() {
        this.sqsHelper = new SQSHelper();
        this.url = "https://sqs.us-west-1.amazonaws.com/778294382379/testQueuse001";
    }

    @Test
    public void testCreateQueue() {
        String queueName = "testQueuse001";
        String delaySeconds = "0";
        String messageRetentionTime = "86400";
        String longPollingTimer = "20";
        boolean result = this.sqsHelper.createQueue(queueName, delaySeconds, messageRetentionTime, longPollingTimer);
        Assert.assertTrue(result);

    }

    @Test
    public void testgetQueuesWithPrefix() {
        String prefix = "";
        List<String> urls = this.sqsHelper.getQueuesWithPrefix(prefix);
        Assert.assertNotNull(urls);
        for (String url : urls) {
            System.out.println(url);
        }
    }

    @Test
    public void testDeleteQueue() {
        this.sqsHelper.deleteQueue(this.url);
        Assert.assertEquals(0, this.sqsHelper.getQueuesWithPrefix("").size());
    }

    @Test
    public void testSendAndReceive() {
        String message = "Hello world";
        this.sqsHelper.send(this.url, message, 0);
        List<Message> messages = this.sqsHelper.receive(this.url);
        Assert.assertEquals(message, messages.get(0).getBody());
    }

    @Test
    public void testReceiveMessgae() {
        // Assume there is something in the queue
        List<Message> result = this.sqsHelper.receive(this.url);
        Assert.assertNotEquals(0, result.size());
        System.out.println(result.get(0).getBody());
    }

    @Test
    public void testDeleteMsg() {
        // First get the receipt of the message which you want to delete (call message.getReceiptHandle())
        List<Message> result = this.sqsHelper.receive(this.url);
        String receiptHandle = result.get(0).getReceiptHandle();
        this.sqsHelper.delete(this.url, receiptHandle);
    }

    @Test
    public void testsetMessageInvisible() {
        this.sqsHelper.setMessageInvisible(this.url,
                this.sqsHelper.receive(this.url).get(0).getReceiptHandle(),
                20);
    }

    @Test
    public void testReceiveMessageWithParam(){
        List<Message> messages =  this.sqsHelper.receive(this.url, 2, 20);
        for (Message message : messages){
            System.out.println(message.getBody());
            while(true){
                this.sqsHelper.setMessageInvisible(this.url, message.getReceiptHandle(), 20);
            }
        }
    }

}
