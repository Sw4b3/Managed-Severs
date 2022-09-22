package monitored.fleet.process.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.models.HealthCheckConfiguration;
import common.models.Host;
import common.utlis.HttpClientFactory;
import org.virtualbox_6_1.MachineState;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCheckService {
    private final HealthCheckConfiguration configuration;
    private final HttpClientFactory httpClient;

    public HealthCheckService() {
        configuration = new HealthCheckConfiguration();
        httpClient = new HttpClientFactory();
    }

    public void run() {
        try {
            while (!checkHealth("http://localhost:8080/health/")) {
                System.out.println("Host Manager Server is currently unavailable");

                Thread.sleep(5000);
            }

            List<Host> hosts = scanHosts();

            while (true) {
                if (hosts.isEmpty())
                    throw new RuntimeException("There are no hosts");

                for (var host : hosts) {
                    host.setState(updateState(host.getName()));

                    if (host.getState().equals(MachineState.Running)) {
                        System.out.println("Checking health for Host::" + host.getName());

                        if (host.getIp() == null || host.getIp().equals("0.0.0.0"))
                            host.setIp(getIp(host.getName()));

                        var isReachable = pingIp(host.getIp());

                        System.out.println(isReachable ? "Host is reachable" : "Host is not reachable");

                        host.setConcurrentFailure(!isReachable ? host.getConcurrentFailure() + 1 : 0);

                        if (host.getConcurrentFailure() == configuration.getConcurrentFailureThreshold()) {
                            System.out.println("Terminating host::" + host.getName());

                            terminateHost(host.getName());
                        }

                        Thread.sleep(configuration.getWait());
                    }
                    if (host.getState().equals(MachineState.PoweredOff)) {
                        System.out.println("Starting host::" + host.getName());

                        startHost(host.getName());
                    }
                }

                Thread.sleep(10000);
            }
        } catch (IOException | InterruptedException e) {
           e.printStackTrace();
        }
    }

    private boolean checkHealth(String uri) {
        try {
            var response = httpClient.GetRequest(uri);

            if (response.statusCode() == 200)
                return true;
        } catch (Exception e) {
            System.out.println("Health Check failed::" + uri);
        }

        return false;
    }

    private boolean pingIp(String ip) throws IOException {
        InetAddress inet = InetAddress.getByName(ip);

        System.out.println("Sending Ping Request to " + ip);

        return inet.isReachable(configuration.getTimeoutThreshold());
    }

    private String getIp(String hostName) {
        var response = httpClient.GetRequest("http://localhost:8080/Host/GetIp?hostName=" + hostName);

        return response.body().toString();
    }

    private MachineState updateState(String hostName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var response = httpClient.GetRequest("http://localhost:8080/Host/GetState?hostName=" + hostName);

            return objectMapper.readValue(response.body().toString(), MachineState.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void startHost(String hostName) {
        var response = httpClient.PostRequest("http://localhost:8080/hostmanager/start", hostName);

        if (response.statusCode() == 200)
            System.out.println("Host successfully started");
        else
            System.out.println("Host failed to start");
    }

    private void terminateHost(String hostName) {
        var response = httpClient.PostRequest("http://localhost:8080/hostmanager/terminate", hostName);

        if (response.statusCode() == 200)
            System.out.println("Host successfully terminated");
        else
            System.out.println("Host failed to start");
    }

    private List<Host> scanHosts() {
        List<Host> hosts = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var response = httpClient.GetRequest("http://localhost:8080/Host/GetHosts");

            Host[] hostsArr = objectMapper.readValue(response.body().toString(), Host[].class);

            hosts = Arrays.asList(hostsArr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hosts;
    }
}


