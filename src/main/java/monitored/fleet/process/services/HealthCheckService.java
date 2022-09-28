package monitored.fleet.process.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.utlis.ConfigurationManger;
import monitored.fleet.process.models.HealthCheckConfiguration;
import common.models.Host;
import common.models.RetryPolicy;
import common.utlis.HttpClientFactory;
import common.utlis.RetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.MachineState;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealthCheckService implements Runnable {
    private final HealthCheckConfiguration configuration;
    private final HttpClientFactory httpClient;
    private static Logger logger;

    public HealthCheckService() {
        logger = LoggerFactory.getLogger(this.getClass());
        configuration = new HealthCheckConfiguration();
        httpClient = new HttpClientFactory();
    }

    @Override
    public void run() {
        var policy = new RetryPolicy<>(() -> checkHealth(ConfigurationManger.getSection("Url:FleetApi") + "/health"), r -> true, 3, true);

        var success = RetryStrategy.execute(policy);

        if (!success) {
            logger.warn("Host Manager Server is currently unavailable");
            return;
        }


        var cancellationToken = false;

        while (!cancellationToken) {
            try {
                List<Host> hosts = scanHosts();

                if (hosts.isEmpty())
                    throw new RuntimeException("There are no hosts");

                for (var host : hosts) {
                    host.setState(updateState(host.getName()));

                    if (host.getState().equals(MachineState.Running)) {
                        logger.info("Checking health for Host::" + host.getName());

                        if (host.getIp() == null || host.getIp().equals("0.0.0.0"))
                            host.setIp(getIp(host.getName()));

                        var isReachable = pingIp(host.getIp());

                        logger.info(isReachable ? "Host is reachable" : "Host is not reachable");

                        host.setConcurrentFailure(!isReachable ? host.getConcurrentFailure() + 1 : 0);

                        if (host.getConcurrentFailure() == configuration.getConcurrentFailureThreshold()) {
                            logger.warn("Terminating host::" + host.getName());

                            terminateHost(host.getName());
                        }
                    } else if (host.getState().equals(MachineState.PoweredOff)) {
                        logger.info("Starting host::" + host.getName());

                        startHost(host.getName());
                    }
                }

                Thread.sleep(configuration.getWait());
            } catch (IOException | InterruptedException e) {
                logger.error(e.toString());
            }
        }
    }

    private boolean checkHealth(String uri) {
        try {
            var response = httpClient.GetRequest(uri);

            if (response.statusCode() == 200)
                return true;
        } catch (Exception e) {
            logger.error("Health Check failed::" + uri);
        }

        return false;
    }

    private boolean pingIp(String ip) throws IOException {
        InetAddress inet = InetAddress.getByName(ip);

        logger.info("Sending Ping Request to " + ip);

        return inet.isReachable(configuration.getTimeoutThreshold());
    }

    private String getIp(String hostName) {
        var response = httpClient.GetRequest(ConfigurationManger.getSection("Url:FleetApi") + "/Host/GetIp?hostName=" + hostName);

        return response.body().toString();
    }

    private MachineState updateState(String hostName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var response = httpClient.GetRequest(ConfigurationManger.getSection("Url:FleetApi") + "/Host/GetState?hostName=" + hostName);

            return objectMapper.readValue(response.body().toString(), MachineState.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void startHost(String hostName) {
        var response = httpClient.GetRequest(ConfigurationManger.getSection("Url:FleetApi") + "/hostmanager/start?hostName=" + hostName);

        if (response.statusCode() == 200)
            logger.info("Host successfully started");
        else
            logger.warn("Host failed to start");
    }

    private void terminateHost(String hostName) {
        var response = httpClient.GetRequest("http://localhost:8080/hostmanager/terminate?hostName=" + hostName);

        if (response.statusCode() == 200)
            logger.info("Host successfully terminated");
        else
            logger.warn("Host failed to start");
    }

    private List<Host> scanHosts() {
        List<Host> hosts = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            var response = httpClient.GetRequest(ConfigurationManger.getSection("Url:FleetApi") + "/Host/GetHosts");

            Host[] hostsArr = objectMapper.readValue(response.body().toString(), Host[].class);

            hosts = Arrays.asList(hostsArr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return hosts;
    }
}


