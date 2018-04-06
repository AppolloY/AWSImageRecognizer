package edu.asu.cse546.util;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.asu.cse546.controller.SQSController;
import edu.asu.cse546.helper.S3Helper;
import edu.asu.cse546.helper.SQSHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class ImageRecognizer {
    private S3Helper s3Helper;
    private final static int IDLE_TIME = 3;

    // Constructor
    private ImageRecognizer() {
        this.s3Helper = new S3Helper();
    }

    // Recognize image by its url
    private String getImageRecognitionResult(String url) {
        try {
            // Run the commands
            Runtime rt = Runtime.getRuntime();
            String[] sourceCmd = {
                    "/bin/bash",
                    "-c",
                    "cd /home/ubuntu/tensorflow/bin/ && source activate && python /home/ubuntu/tensorflow/models/tutorials/image/imagenet/classify_image.py --image_file " + url + " --num_top_predictions 1"
            };
            Process proc = rt.exec(sourceCmd);

            // Read the output from the command to get the recognition of the image
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
            }

            return sb.toString().split("\\(")[0].trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Parse a message from request queue to get Job Id
    private String getJobIdFromMessage(Message message) throws Exception {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> myMap = new Gson().fromJson(message.getBody(), type);

        return myMap.get("job_id");
    }

    // Parse a message from request queue to get image URL
    private String getImageUrlFromMessage(Message message) throws Exception {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> myMap = new Gson().fromJson(message.getBody(), type);

        return myMap.get("image_url");
    }

    // Write a recognition result to our bucket in S3
    private void writeRecognitionResultToBucket(String imageUrl, String recognitionResult) {
        s3Helper.putObject(S3Helper.getRecognitionResultBucket(), imageUrl, recognitionResult);
    }

    // Shutdown current instance
    private void shutdownCurrentInstance() {
        System.out.println("=== Shutting down! ");
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("sudo shutdown -h now");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ImageRecognizer ip = new ImageRecognizer();

        SQSHelper sqsHelper = new SQSHelper();

//        helper.send(REQUEST_QUEUE_URL, "{\"job_id\":\"12345678\",\"image_url\":\"https://www.pets4homes.co.uk/images/articles/771/large/cat-lifespan-the-life-expectancy-of-cats-568e40723c336.jpg\"}", 0);
//        helper.send(REQUEST_QUEUE_URL, "{\"job_id\":\"12345679\",\"image_url\":\"https://cdn0.woolworths.media/content/wowproductimages/large/105919.jpg\"}", 0);
//        helper.send(REQUEST_QUEUE_URL, "{\"job_id\":\"12345680\",\"image_url\":\"http://mforum.biz/wp-content/uploads/cherry-blossom-flower-home-design-one-of-most-beautiful-myanmar-flowers-macro-is-the-several-1.jpg\"}", 0);
//        helper.send(REQUEST_QUEUE_URL, "{\"job_id\":\"12345681\",\"image_url\":\"https://www.what-dog.net/Images/faces2/scroll001.jpg\"}", 0);

        SQSController sqsController = SQSController.getInstance();
        String receiptHandle = "";
        LocalDateTime lastRecognitionTime = LocalDateTime.now();
        boolean processingFlag = false;

        while (true) {
            try {
                LocalDateTime currentTime = LocalDateTime.now();
                Duration duration = Duration.between(lastRecognitionTime, currentTime);
                if (duration.getSeconds() > IDLE_TIME && !processingFlag) {
                    ip.shutdownCurrentInstance();
                }

                Message currentMessage = sqsController.getOneRequest();
                if (currentMessage == null) {
                    continue;
                }
                processingFlag = true;
                receiptHandle = currentMessage.getReceiptHandle();

                String imageUrl = ip.getImageUrlFromMessage(currentMessage);
                String jobId = ip.getJobIdFromMessage(currentMessage);

                String result = ip.getImageRecognitionResult(imageUrl);

                sqsController.setProcessingFinished();

                String responseJson = "{\"job_id\":\"" + jobId + "\",\"result\":\"" + result + "\"}";
                sqsHelper.send(sqsController.getResponseQueueUrl(), responseJson, 0);

                if (result != null && !result.equals("")) {
                    ip.writeRecognitionResultToBucket(imageUrl, result);
                }

                lastRecognitionTime = LocalDateTime.now();
                processingFlag = false;
            } catch (Exception e) {
                sqsHelper.delete(sqsController.getRequestQueueUrl(), receiptHandle);
                processingFlag = false;
                continue;
            }
        }
    }
}