package edu.asu.cse546.helper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import edu.asu.cse546.service.SQSHelperInterface;

import java.util.List;

public class SQSHelper implements SQSHelperInterface {
    private AmazonSQS sqs;

    // Initialize the sqs instance
    public SQSHelper() {
        this.sqs = AmazonClientHelper.getHelper().getSQSClient(Regions.US_WEST_1);
    }

    // Create a new Queue with name and basic configurations
    public boolean createQueue(String queueName, String delaySeconds, String messageRetentionPeriod, String longPollingTimer) {
        CreateQueueRequest request = new CreateQueueRequest(queueName)
                .addAttributesEntry("DelaySeconds", delaySeconds)
                .addAttributesEntry("VisibilityTimeout","0")
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", longPollingTimer)
                .addAttributesEntry("MessageRetentionPeriod", messageRetentionPeriod);
        try {
            this.sqs.createQueue(request);
            return true;
        } catch (AmazonSQSException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get the url list for the queue with specific prefix
    public List<String> getQueuesWithPrefix(String namePrefix) {
        ListQueuesResult lq_result = this.sqs.listQueues(new ListQueuesRequest(namePrefix));
        return lq_result.getQueueUrls();
    }

    // Delete the queue with the url
    public void deleteQueue(String queue_url) {
        this.sqs.deleteQueue(queue_url);
    }

    // Send new message to a specific queue
    public void send(String queueUrl, String message, int delayTime) {
        System.out.println(queueUrl);
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
                .withDelaySeconds(delayTime);
        sqs.sendMessage(request);
    }

    // Receive all messages from a specific queue
    public List<Message> receive(String queueUrl) {
        return sqs.receiveMessage(queueUrl).getMessages();
    }
    // Receive only two messages and set 20s invisible
    public List<Message> receive(String queueUrl, int maxNum, int invisibleTimer){
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(maxNum)
                .withVisibilityTimeout(invisibleTimer);
        return sqs.receiveMessage(request).getMessages();
    }

    @Override
    public GetQueueAttributesResult getQueueAttribute(String queueUrl, String attributeName) {
        GetQueueAttributesRequest request = new GetQueueAttributesRequest(queueUrl)
                .withAttributeNames(attributeName);
        return sqs.getQueueAttributes(request);
    }

    // Delete a specific message
    public void delete(String queueurl, String receiptHandle) {
        this.sqs.deleteMessage(queueurl, receiptHandle);
    }

    // Set a message with a receiptHandle to be invisible in timer seconds.
    public void setMessageInvisible(String queueUrl, String receiptHandle, int timer) {
        sqs.changeMessageVisibility(queueUrl, receiptHandle, timer);
    }

}
