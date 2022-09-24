package common.utlis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class HttpClientFactory {
    private final HttpClient client;
    private static Logger logger;

    public HttpClientFactory() {
        logger = LoggerFactory.getLogger(this.getClass());
        client = HttpClient.newHttpClient();
    }

    public HttpResponse GetRequest(String uri) {
        var request = HttpRequest.newBuilder(URI.create(uri))
                .header("accept", "application/json")
                .build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new HTTPException(response.statusCode());

            return response;
        } catch (ConnectException e) {
            logger.error("Unable to connect::" + uri);
            throw new RuntimeException(e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse PostRequest(String uri, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
