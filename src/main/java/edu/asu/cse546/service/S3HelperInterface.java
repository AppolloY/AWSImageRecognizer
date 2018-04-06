package edu.asu.cse546.service;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

public interface S3HelperInterface {
    // Create a bucket by name
    Bucket createBucket(String bucketName);

    // Get a bucket by name
    Bucket getBucket(String bucketName);

    // List all your buckets
    List<Bucket> listBuckets();

    // Put(upload) an object to a specific bucket
    void putObject(String bucketName, String imageUrl, String recognitionResult);

    // Get all objects in a specific bucket by bucketName
    List<S3ObjectSummary> listObjects(String bucketName);

    // Get(Download) object by bucket name and object key
    S3Object getObject(String bucketName, String keyName);

}
