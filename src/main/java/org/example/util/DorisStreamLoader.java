package org.example.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * This example mainly demonstrates how to use stream load to import data
 * Including file type (CSV) and data in JSON format
 */
public class DorisStreamLoader {
    // FE IP Address
    private final static String HOST = "node";
    // FE port
    private final static int PORT = 8030;
    //user name
    private final static String USER = "root";
    //user password
    private final static String PASSWD = "";

    public DorisStreamLoader(){
    }

    //Build http client builder
    private final static HttpClientBuilder httpClientBuilder = HttpClients
            .custom()
            .setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                protected boolean isRedirectable(String method) {
                    // If the connection target is FE, you need to deal with 307 redirect。
                    return true;
                }
            });

    /**
     * JSON import
     *
     */
    public void loadJson(String jsonData, String db, String table) throws Exception {
        try (CloseableHttpClient client = httpClientBuilder.build()) {
            //http path of stream load task submission
            String loadUrl = String.format("http://%s:%s/api/%s/%s/_stream_load", HOST, PORT, db, table);

            HttpPut put = new HttpPut(loadUrl);
            put.removeHeaders(HttpHeaders.CONTENT_LENGTH);
            put.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
            put.setHeader(HttpHeaders.EXPECT, "100-continue");
            put.setHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader());

            // You can set stream load related properties in the Header, here we set label and column_separator.
            put.setHeader("label", UUID.randomUUID().toString());
            put.setHeader("column_separator", ",");
            put.setHeader("format", "json");

            // Set up the import file. Here you can also use StringEntity to transfer arbitrary data.
            StringEntity entity = new StringEntity(jsonData, "UTF-8");
            put.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(put)) {
                String loadResult = "";
                if (response.getEntity() != null) {
                    loadResult = EntityUtils.toString(response.getEntity());
                }

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException(String.format("Stream load failed. status: %s load result: %s", statusCode, loadResult));
                }
//                System.out.println("Get load result: " + loadResult);
            }
        }
    }

    /**
     * Construct authentication information, the authentication method used by doris here is Basic Auth
     *
     */
    private String basicAuthHeader() {
        final String tobeEncode = DorisStreamLoader.USER + ":" + DorisStreamLoader.PASSWD;
        byte[] encoded = Base64.encodeBase64(tobeEncode.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encoded);
    }

    public static void main(String[] args) throws Exception {
        DorisStreamLoader loader = new DorisStreamLoader();
        //json load
        String jsonData = "{\"id\":556393577,\"number\":\"123344\",\"price\":\"23.5\",\"skuname\":\"test\",\"skudesc\":\"叶红鱼\"}";
        loader.loadJson(jsonData,"example_db", "doris_test_sink" );
    }

}
