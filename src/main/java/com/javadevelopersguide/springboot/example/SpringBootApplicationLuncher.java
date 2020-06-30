package com.javadevelopersguide.springboot.example;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.net.URISyntaxException;
import org.thethingsnetwork.data.common.Connection;
import org.thethingsnetwork.data.common.messages.ActivationMessage;
import org.thethingsnetwork.data.common.messages.DataMessage;
import org.thethingsnetwork.data.common.messages.UplinkMessage;
import org.thethingsnetwork.data.mqtt.Client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This Luncher for the spring boot application.
 *
 * @author manoj.bardhan
 *
 */
public class SpringBootApplicationLuncher {

   private static Logger log = LoggerFactory.getLogger(SpringBootApplicationLuncher.class);

    public static void main(String[] args) {
        String region = "eu";
        String appId = "co2_sensor_stenden";
        String accessKey = "ttn-account-v2.J5ws5KGhK9jVP5p56HfG1VyLka8PecrVTtIsam6MpWA"; // Top-Secret
        String apiUrl = "https://loradashboardapi.herokuapp.com/add_measurement";

        try
        {
            Client client = new Client(region, appId, accessKey);

            client.onMessage((String devId, DataMessage data) -> {

                try
                {
                    log.info("Got uplink message!");
                    UplinkMessage message = (UplinkMessage) data;
                    System.out.println(message);
                    int co2 = (int)message.getPayloadFields().get("co2");
                    double temperature = Double.valueOf(message.getPayloadFields().get("temperature").toString());
                    double humidity = Double.valueOf(message.getPayloadFields().get("humidity").toString());
                    int tvoc = (int)message.getPayloadFields().get("tvoc"); // hydrocarbons
                    String DateTime = java.time.LocalDateTime.now().toString();
                    JSONObject Obj = new JSONObject();
                    Obj.put("datetime", DateTime);
                    Obj.put("air_pressure", 0); // Node doesn't support air pressure
                    Obj.put("humidity", humidity);
                    Obj.put("temperature", temperature);
                    Obj.put("hydrocarbons", tvoc);
                    Obj.put("carbon_dioxide", co2);
                    int nodeId = Integer.parseInt(devId);
                    Obj.put("nodeID", nodeId);
                    String jsonMessage = Obj.toString();
                    log.info("Message json: " + (jsonMessage));

                    HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");// GET POST PUT DELETE
                    conn.setRequestProperty("Content-Type", "application/json");
                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonMessage.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                        log.info("Message sent to API");
                    }catch (Exception out) {
                        log.error("Something went wrong with sending the message!");
                    }
                    log.info("" +conn.getResponseCode()); // 200 - 299 (200)


                }catch (Exception ex) {
                    log.error("Cry in the bathroom: " + ex.getMessage());
                    log.error(Arrays.toString(ex.getStackTrace()));
                }
            });

            client.onActivation((String _devId, ActivationMessage _data) -> log.info("Activation: " + _devId + ", data: " + _data.getDevAddr()));

            client.onError((Throwable _error) -> log.error("error: " + _error.getMessage()));

            client.onConnected((Connection _client) -> log.info("connected!"));

            client.start();

        } catch (URISyntaxException ex)
        {
            log.error(Arrays.toString(ex.getStackTrace()));
            log.error(ex.getMessage());
        } catch (Exception ex)
        {
            log.error(Arrays.toString(ex.getStackTrace()));
        }
        SpringApplication.run(HelloWorldController.class, args);
    }
}