package edu.asu.cse546.service;

import com.amazonaws.services.ec2.model.Instance;

import java.util.List;

public interface EC2HelperInterface {

    /**
     * Create an instance by a specific image ID.
     * @param imageId The image ID for the instance you want to create.
     */
    void createInstance(String imageId);

    /**
     * Create an image recognition instance.
     */
    void createAppInstance();

    /**
     * Start an instance with instance ID.
     * @param instanceId The ID of the instance you want to start.
     */
    void startInstance(String instanceId);

    /**
     * Stop an instance with instance ID.
     * @param instanceId The ID of the instance you want to stop.
     */
    void stopInstance(String instanceId);

    /**
     * Reboot an instance with instance ID.
     * @param instanceId The ID of the instance you want to reboot.
     */
    void rebootInstance(String instanceId);

    /**
     * Terminate an instance with instance ID.
     * @param instanceId The ID of the instance you want to terminate.
     */
    void terminateInstance(String instanceId);

    /**
     * Get the basic information of all instances.
     *
     * Sample output:
     * "Found instance with id i-075542805fd9d9ee4, AMI ami-07303b67, type t2.micro, state running and monitoring state disabled".
     */
    void describeInstances();

    /**
     * Get all instance associated with current account in this region.
     * @return List of all instances in current account and region (Default region: US_WEST_1).
     */
    List<Instance> getAllInstances();

    /**
     * Get the specific instance by instance ID
     * @param instanceId The ID of the instance you want to get.
     * @return The instance associated with the specific instance ID. Will return null if the instance ID is invalid.
     */
    Instance getInstanceByInstanceId(String instanceId);

    /**
     * Get the state name of the instance
     * There are 6 state names (all in lowercase):
     * "pending", "running", "shuttingdown", "stopped", "stopping", "terminated"
     *
     * @param instanceId The ID of the instance state name you want to get.
     * @return The instance state name corresponding to the instance id. Will return null if the instance ID is invalid.
     */
    String getInstanceStateName(String instanceId);

    /**
     * Get the running time of a running instance.
     * @param instanceId The ID of the instance.
     * @return The running time (representing in seconds) of an running instance. Will return 0 if the instance ID is invalid or the instance is not running.
     */
    long getInstanceRunningTime(String instanceId);

    /**
     * Get the image ID of our app instance.
     * @return The image ID of the app instance.
     */
    String getAppInstanceImageId();
}


