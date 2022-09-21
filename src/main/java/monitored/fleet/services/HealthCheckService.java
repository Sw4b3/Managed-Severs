package monitored.fleet.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import managed.fleet.common.models.HealthCheckConfiguration;
import managed.fleet.common.models.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCheckService {
    HealthCheckConfiguration configuration;

    public HealthCheckService() {
        configuration = new HealthCheckConfiguration();
    }

    public void run() throws Exception {
        while (!checkHealth("http://localhost:8080/health/")) {
            System.out.println("Host Manager is currently unavailable");

            Thread.sleep(5000);
        }

        List<Host> hosts = scanHosts();

        if (hosts.isEmpty())
            throw new Exception("There are no hosts");

        while (true) {
            try {
                for (var host : hosts) {
                    System.out.println("Checking health for Host:: " + host.getName());

                    if (host.getIp() == null) {
                        host.setIp(getIp(host.getName()));

                        if (host.getIp() == null)
                            startHost(host.getName());
                    }

                    int CurrentFailureConcurrency = 0;

                    if (host == null) {
                        System.out.println("No Hosts");
                        break;
                    }

                    var isReachable = pingIp(host.getIp());

                    System.out.println(isReachable ? "Host is reachable" : "Host is not reachable");

                    CurrentFailureConcurrency = !isReachable ? CurrentFailureConcurrency + 1 : 0;

                    if (CurrentFailureConcurrency == configuration.getConcurrentFailureThreshold()) {
                        System.out.println("Terminate host");

                        break;
                    }

                    Thread.sleep(configuration.getWait());
                }
            } catch (IOException e) {
                System.out.println(e);

                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                System.out.println(e);

                throw new RuntimeException(e);
            }

            Thread.sleep(10000);
        }
    }

    private boolean checkHealth(String uri) {
        try {
            var client = HttpClient.newHttpClient();

            var request = HttpRequest.newBuilder(URI.create(uri))
                    .header("accept", "application/json")
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
                return true;
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    private boolean pingIp(String ip) throws IOException {
        InetAddress inet = InetAddress.getByName(ip);

        System.out.println("Sending Ping Request to " + ip);

        return inet.isReachable(configuration.getTimeoutThreshold());
    }

    private String getIp(String hostName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var client = HttpClient.newHttpClient();

            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/hostmanager/start"))
                    .POST(HttpRequest.BodyPublishers.ofString(hostName))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body().toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startHost(String hostName) {
        try {
            var client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/hostmanager/start"))
                    .POST(HttpRequest.BodyPublishers.ofString(hostName))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200)
                System.out.println("Host successfully started");
            else
                System.out.println("Host failed to start");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Host> scanHosts() {
        List<Host> hosts = new ArrayList<Host>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var client = HttpClient.newHttpClient();

            var request = HttpRequest.newBuilder(
                            URI.create("http://localhost:8080/Host/GetHosts"))
                    .header("accept", "application/json")
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Host[] hostsArr = objectMapper.readValue(response.body(), Host[].class);

            hosts = Arrays.asList(hostsArr);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return hosts;
    }
}


