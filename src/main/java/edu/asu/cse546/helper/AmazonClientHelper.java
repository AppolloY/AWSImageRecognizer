package edu.asu.cse546.helper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class AmazonClientHelper {
    private static volatile AmazonClientHelper helper = null;

    private AmazonClientHelper() {
    }

    // Use volatile and synchronized to prevent race
    // conditions that could result in the creation of multiple instances
    public static AmazonClientHelper getHelper() {
        if (helper == null) {
            synchronized (AmazonClientHelper.class) {
                helper = new AmazonClientHelper();
            }
        }
        return helper;
    }

    public AmazonSQS getSQSClient(Regions region) {
        return AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    public AmazonEC2 getEC2Client() {
        return null;
    }
}
