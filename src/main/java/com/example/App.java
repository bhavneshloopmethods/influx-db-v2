package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class App {

    public static void main(final String[] args) throws Exception {

        Dotenv dotenv = Dotenv.load();

        String hostUrl = dotenv.get("INFLUX_HOST_URL");
        char[] authToken = dotenv.get("INFLUX_AUTH_TOKEN").toCharArray();
        String org = dotenv.get("org");
        String orgID = dotenv.get("orgID");

        InputStream inputStream = App.class.getClassLoader().getResourceAsStream("json-format.json");
        Object obj = new JSONParser().parse(new InputStreamReader(inputStream));
        JSONArray configArray = (JSONArray) obj;

        for (Object o : configArray) {

            if (o instanceof JSONObject) {
                JSONObject config = (JSONObject) o;
                String bucket = (String) config.get("bucket");
                try {
                    HttpClient client = HttpClient.newHttpClient();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(hostUrl + "/api/v2/buckets?name=" + bucket))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Authorization",
                                    "Token " + new String(authToken))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    ObjectMapper mapper = new ObjectMapper();
                    ApiResponse apiResponse = mapper.readValue(response.body(), ApiResponse.class);
                    int bucketCount = apiResponse.getBuckets().size();

                    System.out.println("GET request status code: " + response.statusCode());
                    System.out.println("GET request response body: " + response.body());

                    if (bucketCount < 1) {
                        ObjectMapper postmapper = new ObjectMapper();
                        Map<String, String> jsonBodyMap = new HashMap<>();
                        jsonBodyMap.put("name", bucket);
                        jsonBodyMap.put("description", bucket);
                        jsonBodyMap.put("orgID", orgID);

                        String jsonBody = postmapper.writeValueAsString(jsonBodyMap);

                        HttpRequest postRequest = HttpRequest.newBuilder()
                                .uri(URI.create(hostUrl + "/api/v2/buckets"))
                                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", "Token " + new String(authToken))
                                .build();

                        HttpResponse<String> createResponse = client.send(postRequest,
                                HttpResponse.BodyHandlers.ofString());

                        if (createResponse.statusCode() != 201) {
                            System.out.println("Failed to create bucket. Status code: " + createResponse.statusCode());
                            System.out.println("Response body: " + createResponse.body());
                        } else {
                            System.out.println("Bucket created successfully: " + createResponse.body());
                        }
                    }

                } catch (Exception e) {
                    System.out.println("-------------printing error stack trace ----------------");
                    e.printStackTrace();

                }

                InfluxDBClient influxDBClient = InfluxDBClientFactory.create(hostUrl, authToken, org, bucket);

                String measurement = (String) config.get("measurement");

                // Check if tags are JSON or String
                Object tagsObj = config.get("tags");
                Map<String, String> tags = new HashMap<>();
                if (tagsObj instanceof JSONObject) {
                    tags = parseMap((JSONObject) tagsObj);
                } else if (tagsObj instanceof String) {

                    // Assuming tags are comma-separated key=value pairs if it's a string
                    tags = parseTagsFromString((String) tagsObj);
                }

                Map<String, Double> fields = parseDoubleMap((JSONObject) config.get("fields"));
                Map<String, Double> startValues = parseDoubleMap((JSONObject) config.get("startValues"));
                Map<String, Double> minValues = parseDoubleMap((JSONObject) config.get("minValues"));
                Map<String, Double> maxValues = parseDoubleMap((JSONObject) config.get("maxValues"));
                Map<String, Double> variations = parseDoubleMap((JSONObject) config.get("variations"));
                List<Long> range = (List<Long>) config.get("range");
                String action = (String) config.get("action");
                long counts = (long) config.get("counts");
                long timeGaps = (long) config.get("timeGaps");

                // Copy startValues to mutable currentValues
                Map<String, Double> currentValues = new HashMap<>(startValues);

                for (long i = 0; i < counts; i++) {

                    for (String key : fields.keySet()) {
                        Double variation = variations.get(key);
                        long minRange = range.get(0);
                        long maxRange = range.get(1);
                        if (i >= minRange && i <= maxRange) {
                            if (action.equals("increase")) {
                                currentValues.put(key,
                                        Math.min(currentValues.get(key) + variation, maxValues.get(key)));
                            } else if (action.equals("decrease")) {
                                currentValues.put(key,
                                        Math.max(currentValues.get(key) - variation, minValues.get(key)));
                            }
                        } else {
                            currentValues.put(key,
                                    currentValues.get(key) + (Math.random() * variation * 2 - variation));
                        }
                    }

                    Point point = Point.measurement(measurement);
                    tags.forEach(point::addTag);
                    currentValues.forEach((key, value) -> point.addField(key, value));
                    WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
                    writeApi.writePoint(point);

                    System.out.println("Data written: " + point.toLineProtocol());

                    // Sleep between writes
                    Thread.sleep(timeGaps * 100);
                }
            } else {
                System.err.println("Unexpected non-JSONObject entry in configArray.");
            }

        }

        System.out.println("Completed!!");
    }

    private static Map<String, String> parseMap(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        if (jsonObject != null) {
            for (Object key : jsonObject.keySet()) {
                map.put(key.toString(), jsonObject.get(key).toString());
            }
        }
        return map;
    }

    // method to parse tags from a string
    private static Map<String, String> parseTagsFromString(String tagsString) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = tagsString.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }

    private static Map<String, Double> parseDoubleMap(JSONObject jsonObject) {
        Map<String, Double> map = new HashMap<>();
        if (jsonObject != null) {
            for (Object key : jsonObject.keySet()) {
                map.put(key.toString(), Double.parseDouble(jsonObject.get(key).toString()));
            }
        }
        return map;
    }

}
