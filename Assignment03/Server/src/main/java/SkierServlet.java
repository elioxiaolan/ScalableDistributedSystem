import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import Model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;


@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkierServlet extends HttpServlet {
    private ChannelPool channelPool;
    private String resortID;
    private String seasonID;
    private String dayID;
    private String skierID;
    private final static String QUEUE_NAME = "LIFTRIDE";
    @Override
    public void init() throws ServletException {
        try {
            System.out.println("Start");
            super.init();
            System.out.println("end");
            this.channelPool = new ChannelPool();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();
        System.out.println(urlPath);
        Message message = new Message();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String resJson = null;

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            message.setMessage("Data Not Found");
            resJson = gson.toJson(message);
            PrintWriter out = res.getWriter();
            out.write(resJson);
            out.flush();
            return;
        }

        if (!valPostUrl(urlPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            message.setMessage("Invalid Inputs");
            resJson = gson.toJson(message);
        } else {
//            String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//            System.out.println(json);
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
                    sb.append(line).append('\n');
                }
            } finally {
                reader.close();
            }
            String json = sb.toString();
//            System.out.println(json);
            if (!valLiftRide(json)) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                message.setMessage("Invalid Inputs");
                resJson = gson.toJson(message);
            } else {
                processParameter(urlPath);
                LiftRide liftRide = gson.fromJson(json, LiftRide.class);
                liftRide.setResortID(this.resortID);
                liftRide.setSeasonID(this.seasonID);
                liftRide.setDayID(this.dayID);
                liftRide.setSkierID(this.skierID);
                String jsonMessage = gson.toJson(liftRide);
                try {
                    Channel channel = channelPool.takeChannel();
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    channel.basicPublish("", QUEUE_NAME, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
                    this.channelPool.add(channel);
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    message.setMessage(("Create Successfully"));
                    resJson = gson.toJson(message);
                    System.out.println(" [x] Sent '" + jsonMessage + "'");
                } catch (IOException e) {
                    e.printStackTrace();
                    message.setMessage("Unsuccessful!");
                    resJson = gson.toJson(message);
                } catch (InterruptedException e) {
                    message.setMessage("Unsuccessful!");
                    resJson = gson.toJson(message);
                }
            }
        }

        PrintWriter out = res.getWriter();
        out.write(resJson);
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    }

    protected void processParameter(String urlPath) {
        String[] urlArr = urlPath.split("/");

        this.resortID = urlArr[1];
        this.seasonID = urlArr[3];
        this.dayID = urlArr[5];
        this.skierID = urlArr[7];
    }

    protected boolean valPostUrl(String urlPath) {
        String[] urlArr = urlPath.split("/");

        if (urlArr.length == 8) {
            return valSkiersUrl(urlPath);
        }

        return false;
    }

    protected boolean valSkiersUrl(String urlPath) {
        String[] urlArr = urlPath.split("/");

        if (!urlArr[2].equals("seasons") || !urlArr[4].equals("days") || !urlArr[6].equals("skiers")) {
            return false;
        }

        try {
            for (int i = 1; i < 8; i += 2) {
                Integer.parseInt(urlArr[i]);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    protected boolean valLiftRide(String json) {
        Gson gson = new Gson();
        LiftRide liftRide = gson.fromJson(json, LiftRide.class);
//        System.out.println(liftRide.getTime());
//        System.out.println(liftRide.getLiftID());

        return liftRide.getTime() != null && liftRide.getLiftID() != null;
    }
}