package edu.asu.cse546.service;

import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;

import java.util.List;

public interface SQSHelperInterface {

    /**
     * Create a new Queue with name and basic configurations
     *
     * @param queueName              The queue name
     * @param delaySeconds           The seconds of message delayed to be deployed in the queue
     * @param messageRetentionPeriod The seconds that the message stay in the queue
     * @param longPollingTimer       The timer for improving message receive performance.
     * @return boolean
     */
    boolean createQueue(String queueName, String delaySeconds, String messageRetentionPeriod, String longPollingTimer);

    /**
     * Get the url list for the queue with specific prefix
     *
     * @param namePrefix The prefix of the queue name
     * @return The Url List of all Strings
     */
    List<String> getQueuesWithPrefix(String namePrefix);

    /**
     * Delete the queue with the url
     *
     * @param queue_url The url of the queue
     */
    void deleteQueue(String queue_url);

    /**
     * Send new message to a specific queue
     *
     * @param queueUrl  The url of the queue
     * @param message   The message body
     * @param delayTime The time delay of showing up in the queue
     */
    void send(String queueUrl, String message, int delayTime);

    /**
     * Receive all message from a specific queue
     *
     * @param queueUrl The url of the queue
     * @return A list of Message
     */
    List<Message> receive(String queueUrl);

    /**
     * Delete the specific message
     *
     * @param queueurl      The url of the queue
     * @param receiptHandle The receiptHandle bundled with the message
     */
    void delete(String queueurl, String receiptHandle);

    /**
     * Set message invisibility clock
     *
     * @param queueUrl The url of the queue
     * @param receiptHandle    The receiptHandle of message
     * @param timer    The timer of being invisible
     */
    void setMessageInvisible(String queueUrl, String receiptHandle, int timer);

    /**
     *
     * @param queueUrl The url of the queue
     * @param maxNum The maximum messages to receive
     * @param invisibleTimer The timer of being invisible
     * @return A list of messages
     */
    List<Message> receive(String queueUrl, int maxNum, int invisibleTimer);

    GetQueueAttributesResult getQueueAttribute(String queueUrl,String attributeName);
}
