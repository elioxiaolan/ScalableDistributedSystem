import com.google.gson.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private final static String EXCHANGE_NAME = "liferide";
    private final static String QUEUE_NAME = "LIFTRIDE";
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JedisPool pool = new JedisPool("35.92.254.29", 6379);;
    private final static Integer NUM_THREAD = 256;

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("172.31.3.88");
        factory.setUsername("consumer");
        factory.setPassword("password");
        Connection connection = factory.newConnection();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxWait(Duration.ofMillis(2000));
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);
        pool.setConfig(config);

        Runnable runnable = () -> {
            Channel channel;
            try {
                channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    JsonObject liftRide = gson.fromJson(message, JsonObject.class);
                    System.out.println(" [x] Received '" + liftRide.toString() + "'");
                    JsonObject values = new JsonObject();
                    String key = String.valueOf(liftRide.get("resortID").getAsString());

                    try {
                        values.add("resortID", liftRide.get("resortID"));
                        values.add("seasonID", liftRide.get("seasonID"));
                        values.add("dayID", liftRide.get("dayID"));
                        values.add("skierID", liftRide.get("skierID"));
                        values.add("time", liftRide.get("time"));
                        values.add("liftID", liftRide.get("liftID"));
                        JsonElement vertical = gson.toJsonTree(liftRide.get("liftID").getAsInt() * 10);
                        values.add("vertical", vertical);
                    } finally {
                        try (Jedis jedis = pool.getResource()) {
                            jedis.sadd(key, gson.toJson(values));
                        }
                        System.out.println(" [x] Done Saving");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                };
                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREAD);

        for (int i = 0; i < NUM_THREAD; i++) {
            executorService.execute(new Thread(runnable));
        }
    }
}
