package edu.asu.cse546.helper;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import edu.asu.cse546.service.EC2HelperInterface;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class EC2Helper implements EC2HelperInterface {

    private final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion(Regions.US_WEST_1).build();

    private final String KEY_NAME = "546";
    private final String SECURITY_GROUP_NAME = "546-cc-sg";
    private final String APP_INSTANCE_IMAGE_ID = "ami-ed42538d";

    public void createInstance(String imageId) {
        System.out.println("Create an instance");

        int minInstanceCount = 1;
        int maxInstanceCount = 1;

        RunInstancesRequest runRequest = new RunInstancesRequest()
                .withImageId(imageId)
                .withInstanceType(InstanceType.T2Micro)
                .withMinCount(minInstanceCount)
                .withMaxCount(maxInstanceCount)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SECURITY_GROUP_NAME);

        RunInstancesResult runResponce = ec2.runInstances(runRequest);

        List<Instance> resultInstances = runResponce.getReservation().getInstances();
        for (Instance ins: resultInstances) {
            System.out.println("New instance has been created: " + ins.getInstanceId());
        }
    }

    public void createAppInstance() {
        createInstance(APP_INSTANCE_IMAGE_ID);
    }

    public void startInstance(String instanceId) {
        StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
        ec2.startInstances(request);
    }

    public void stopInstance(String instanceId) {
        StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
        ec2.stopInstances(request);
        System.out.println("The instance has been stoped.");
    }

    public void rebootInstance(String instanceId) {
        RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instanceId);
        ec2.rebootInstances(request);
    }

    public void terminateInstance(String instanceId) {
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);
        ec2.terminateInstances(request);
    }

    public void describeInstances() {
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for (Reservation reservation : response.getReservations()) {
                System.out.println("one reservation");
                for (Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "Found instance with id %s, " +
                                    "AMI %s, " +
                                    "type %s, " +
                                    "state %s " +
                                    "and monitoring state %s\n",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
            }

            request.setNextToken(response.getNextToken());
            if (response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public List<Instance> getAllInstances() {
        boolean done = false;

        List<Instance> instances = new ArrayList<>();

        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);
            for (Reservation reservation : response.getReservations()) {
                instances.addAll(reservation.getInstances());
            }

            request.setNextToken(response.getNextToken());
            if (response.getNextToken() == null) {
                done = true;
            }
        }

        return instances;
    }

    public Instance getInstanceByInstanceId(String instanceId) {
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult response = ec2.describeInstances(request);

        if (response.getReservations().size() == 0 || response.getReservations().get(0).getInstances().size() == 0) {
            return null;  // instanceId not valid
        }

        return response.getReservations().get(0).getInstances().get(0);
    }

    public String getInstanceStateName(String instanceId) {
        Instance instance = getInstanceByInstanceId(instanceId);
        if (instance == null) {
            return null;
        } else {
            return instance.getState().getName().toLowerCase();
        }
    }

    public long getInstanceRunningTime(String instanceId) {
        Instance instance = getInstanceByInstanceId(instanceId);
        if (instance == null || !getInstanceStateName(instanceId).equals("running")) {
            return 0L;
        }

        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime instanceLaunchTime = LocalDateTime.ofInstant(instance.getLaunchTime().toInstant(), ZoneId.systemDefault());

        Duration duration = Duration.between(instanceLaunchTime, currentTime);
        return duration.getSeconds();
    }

    public String getAppInstanceImageId() {
        return APP_INSTANCE_IMAGE_ID;
    }

    public static void main(String[] args) {
        EC2Helper example = new EC2Helper();

//        example.stopInstance("i-075542805fd9d9ee4");
//        example.createInstance();
//        EC2Example.createInstance();
//        example.getInstanceByInstanceId("i-075542805fd9d9ee4");
//        System.out.println(example.getInstanceRunningTime("i-03c0bd5775edb3622"));

        List<Instance> instances = example.getAllInstances();
        for (Instance instance: instances) {
            System.out.println(instance.getInstanceId());
        }
    }
}
