package edu.asu.cse546.helper;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import edu.asu.cse546.service.S3HelperInterface;

import java.nio.file.Paths;
import java.util.List;

public class S3Helper implements S3HelperInterface {
    private final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_1).build();

    private final static String RECOGNITION_RESULT_BUCKET = "recognition-result-bucket";

    public Bucket createBucket(String bucketName) {
        Bucket b = null;
        if (s3.doesBucketExistV2(bucketName)) {
            System.out.format("Bucket %s already exists.\n", bucketName);
            b = getBucket(bucketName);
        } else {
            try {
                b = s3.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }

    public Bucket getBucket(String bucketName) {
        Bucket namedBucket = null;
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket b: buckets) {
            if (b.getName().equals(bucketName)) {
                namedBucket = b;
                break;
            }
        }
        return namedBucket;
    }

    public List<Bucket> listBuckets() {
        List<Bucket> buckets = s3.listBuckets();
        System.out.println("Your Amazon S3 buckets are:");
        for (Bucket b: buckets) {
            System.out.println("* " + b.getName());
        }
        return buckets;
    }

    public void putObject(String bucketName, String imageUrl, String recognitionResult) {
        String keyName = Paths.get(imageUrl).getFileName().toString();
        s3.putObject(bucketName, keyName, imageUrl);
        try {
            s3.putObject(bucketName, keyName, recognitionResult);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }

    public List<S3ObjectSummary> listObjects(String bucketName) {
        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        System.out.format("Objects in S3 bucket %s:\n", bucketName);
        for (S3ObjectSummary os: objects) {
            System.out.println("* " + os.getKey());
        }

        return objects;
    }

    public S3Object getObject(String bucketName, String keyName) {
        System.out.format("Downloading %s from S3 bucket %s...\n", keyName, bucketName);

        S3Object object = s3.getObject(bucketName, keyName);
        return object;

//        below is another version if you want to download the actual object.
//        try {
//            S3Object o = s3.getObject(bucketName, keyName);
//            S3ObjectInputStream s3is = o.getObjectContent();
//            FileOutputStream fos = new FileOutputStream(new File(keyName));
//            byte[] read_buf = new byte[1024];
//            int read_len = 0;
//            while ((read_len = s3is.read(read_buf)) > 0) {
//                fos.write(read_buf, 0, read_len);
//            }
//            s3is.close();
//            fos.close();
//        } catch (AmazonServiceException e) {
//            System.err.println(e.getErrorMessage());
//            System.exit(1);
//        } catch (FileNotFoundException e) {
//            System.err.println(e.getMessage());
//            System.exit(1);
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            System.exit(1);
//        }
//        System.out.println("Done!");

    }

    public static String getRecognitionResultBucket() {
        return RECOGNITION_RESULT_BUCKET;
    }

    public static void main(String[] args) {
        S3Helper example = new S3Helper();
//        example.createBucket(RECOGNITION_RESULT_BUCKET);
//        example.createBucket("testbucket-3-18");
//        example.putObject(RECOGNITION_RESULT_BUCKET, "/Users/longyue/Desktop/1.pic");
//        example.putObject(RECOGNITION_RESULT_BUCKET, "/Users/longyue/Desktop/2.pic");
//        example.putObject(RECOGNITION_RESULT_BUCKET, "/Users/longyue/Desktop/3.pic");

        List<S3ObjectSummary> objects = example.listObjects(RECOGNITION_RESULT_BUCKET);
    }
}
