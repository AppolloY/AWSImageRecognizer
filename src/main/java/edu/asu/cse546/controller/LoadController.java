package edu.asu.cse546.controller;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.amazonaws.services.ec2.model.Instance;
import edu.asu.cse546.helper.EC2Helper;

import java.util.ArrayList;
import java.util.List;

public class LoadController {
    private EC2Helper ec2Helper;
    private List<Instance> ec2List;
    private SQSController sqsController;
    private String appImageId;
    private int lastCreatedMachineCount = 0 ;
    /*"pending", "running", "shuttingdown", "stopped", "stopping", "terminated"*/
    private static final String PENDING = "pending";
    private static final String RUNNING = "running";
    private static final String SHUTTINGDOWN = "shuttingdown";
    private static final String STOPPED = "stopped";
    private static final String STOPPING = "stopping";
    private static final String TERMINATED = "terminated";
    private static final int SLEEP_TIME = 10; // seconds
    private static final int ONE_JOB_TIME_COST = 5; // In second
    private static final int INSTANCE_LIMITATION = 20;
    private static final int RESERVED_INSTANCE = 3;

    private ArrayList<String> stoppedAppInstanceIds;
    private ArrayList<String> runningAppInstanceIds;
    private ArrayList<String> pendingAppInstanceIds;

    public LoadController(){
        this.ec2Helper = new EC2Helper();
        this.sqsController = SQSController.getInstance();
        this.appImageId = this.ec2Helper.getAppInstanceImageId();
        // Boot up a stopped instance at the beginning
//        System.out.println("Booting Up one stopped machine at the beginning.");
//        initializeOneInstance();
    }


//    private void initializeOneInstance(){
//        ArrayList<String> stoppedInstanceList = getAppInstanceIdListWithStatus(STOPPED);
//        if (stoppedInstanceList.size() == 0){
//            System.out.println("There is no stopped App Instance, Create a new one!");
//            this.ec2Helper.createAppInstance();
//        }else{
//            System.out.println("Stopped App Instance Not Null! Start it.");
//            this.ec2Helper.startInstance(stoppedInstanceList.get(0));
//        }
//    }

    private int getOtherStateInstanceNum(){
        int result = 0 ;
        this.ec2List = this.ec2Helper.getAllInstances();
        for (Instance instance : this.ec2List){
            if (!instance.getImageId().equals(this.ec2Helper.getAppInstanceImageId())) continue;
            String stateName = this.ec2Helper.getInstanceStateName(instance.getInstanceId());
            if (stateName.equals(STOPPING) || stateName.equals(PENDING) || stateName.equals(SHUTTINGDOWN)){
                result ++;
            }
        }
        return result;
    }

//    private ArrayList<String> getAppInstanceIdListWithStatus(List<Instance> instancesList,String status){
//        ArrayList<String> instanceIdList = new ArrayList<>();
//        for(Instance instance:instancesList){
//            if (instance.getImageId().equals(this.appImageId) &&
//                    this.ec2Helper.getInstanceStateName(instance.getInstanceId()).equals(status)
//                     )
//                instanceIdList.add(instance.getInstanceId());
//        }
//        return instanceIdList;
//    }

    private int getRequireMachineNum(int queueSize){
        return Math.min(queueSize, INSTANCE_LIMITATION);
    }

    private void setAppInstanceListWithDifferentStatus(List<Instance> instancesList) {
        System.out.println("Instance List Size: " + instancesList.size());

        stoppedAppInstanceIds = new ArrayList<>();
        runningAppInstanceIds = new ArrayList<>();
        pendingAppInstanceIds = new ArrayList<>();

        for (Instance instance: instancesList){
            if (!instance.getImageId().equals(this.appImageId)) {
                continue;
            }

            String currentAppInstanceId = instance.getInstanceId();
            String currentAppInstanceStatus = this.ec2Helper.getInstanceStateName(currentAppInstanceId);

            if (currentAppInstanceStatus.equals(STOPPED)) {
                stoppedAppInstanceIds.add(currentAppInstanceId);
            } else if (currentAppInstanceStatus.equals(RUNNING)) {
                runningAppInstanceIds.add(currentAppInstanceId);
            } else if (currentAppInstanceStatus.equals(PENDING)) {
                pendingAppInstanceIds.add(currentAppInstanceId);
            }
        }

        System.out.println("STOPPED: " + stoppedAppInstanceIds.size());
        System.out.println("RUNNING: " + runningAppInstanceIds.size());
        System.out.println("PENDING: " + pendingAppInstanceIds.size());
    }

    public void autoScaling(){
        while(true){
            System.out.println("----------------------------");
            try{
                this.ec2List = this.ec2Helper.getAllInstances();

//                ArrayList<String> stoppedAppInstanceList = getAppInstanceIdListWithStatus(this.ec2List,STOPPED);
//                ArrayList<String> runningAppInstanceList = getAppInstanceIdListWithStatus(this.ec2List,RUNNING);

                setAppInstanceListWithDifferentStatus(this.ec2List);

                int requestQueueSize = this.sqsController.getRequestQueueSize();
                int requiredMachineNum = getRequireMachineNum(requestQueueSize);
                int pendingMachineNum = pendingAppInstanceIds.size();
                System.out.println("The Request Queue Size."+requestQueueSize);
                System.out.println("The Required Machine Size."+requiredMachineNum);
                if (runningAppInstanceIds.size() < requiredMachineNum){
                    System.out.println("Enter instance creation or starting process.");
                    int numInstanceToCreate = requiredMachineNum - runningAppInstanceIds.size() - pendingMachineNum;
                    System.out.println("Number of instance to create."+numInstanceToCreate);
                    System.out.println("Number of stopped instance :"+ stoppedAppInstanceIds.size());
                    // Try to start the stopped instance
                    for (String instanceId : stoppedAppInstanceIds){
                        if(numInstanceToCreate > 0){
                            System.out.println("Start a stopped machine. " + instanceId);
                            this.ec2Helper.startInstance(instanceId);
                            numInstanceToCreate --;
                        }
                    }
                    // Try to create a new instance
                    int actualAvailableMachine = INSTANCE_LIMITATION - RESERVED_INSTANCE - runningAppInstanceIds.size() - pendingMachineNum;
                    int actualCreateInstanceNum = Math.min(numInstanceToCreate, actualAvailableMachine - getOtherStateInstanceNum());

                    for(int i = 0; i < actualCreateInstanceNum; i++){
                        System.out.println("Create a new Machine!"+i);
                        this.ec2Helper.createAppInstance();
                    }
                }else {
                    System.out.println("Enter terminate instance process");
                    // Clear the stopped instances and keep at lease one stopped machine.
                    for(String instanceId : stoppedAppInstanceIds){
                        // Terminate all stopped the instance
                        System.out.println("Terminate a stopped machine!");
                        this.ec2Helper.terminateInstance(instanceId);
                    }
                }
                Thread.sleep( SLEEP_TIME * 1000); // SLEEP for s
            } catch (Exception e) {
                System.out.println("Encounter an exception!");
                e.printStackTrace();
                continue;
            }
        }
    }


    public static void main(String[] args){
        LoadController loadController = new LoadController();
        loadController.autoScaling();
    }

}
