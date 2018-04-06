package edu.asu.cse546.controller;

import edu.asu.cse546.helper.S3Helper;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;


public class HttpController extends AbstractHandler {
    private final static int PORT = 5460;
    private SQSController sqsController;

    public HttpController() {
        this.sqsController = SQSController.getInstance();

    }

    /**
     * @param s name
     * @param request request
     * @param httpServletRequest servlet request
     * @param httpServletResponse servlet response
     * @throws IOException IOE
     * @throws ServletException Servlet
     * Http request sample to server : http://serverip:5460?imageUrl=xxxxxxx
     * Request message format: {"job_id":"time","image_url":"http://xxx.jpg"}
     * Response message format: {"job_id":"time","result":"Hello Kitty"}
     */
    public void handle(String s, Request request, HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpServletResponse.setContentType("text/html;charset=utf-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        String imageUrl = request.getParameter("imageUrl");
        String jobId = generateJobId(imageUrl);
        JSONObject requestMsg = new JSONObject();
        requestMsg.put("job_id", jobId);
        requestMsg.put("image_url", imageUrl);
        System.out.println(requestMsg.toString());

        this.sqsController.sendMsg(requestMsg.toString());
        String processResult = this.sqsController.receiveResponseMsg(jobId);

        Writer out = httpServletResponse.getWriter();
        out.write(processResult);
        out.flush();
    }
    private String generateJobId(String imageUrl){
        Date datetime = new Date();
        String time = String.valueOf(datetime.getTime());
        String imageName = Paths.get(imageUrl).getFileName().toString();
        Random rn = new Random();
        return time+"_"+imageName+"_"+rn.nextInt(100000);

    }

    public void startServer() {
        try {
            Server server = new Server(PORT);
            server.setHandler(new HttpController());
            server.start();
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        S3Helper s3Helper = new S3Helper();
        s3Helper.createBucket(S3Helper.getRecognitionResultBucket());

        HttpController controller = new HttpController();
        controller.startServer();
    }
}
