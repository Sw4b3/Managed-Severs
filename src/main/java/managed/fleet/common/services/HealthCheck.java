package managed.fleet.common.services;

import managed.fleet.common.models.HealthCheckConfiguration;
import managed.fleet.common.models.Host;

import java.io.IOException;
import java.net.InetAddress;

public class HealthCheck {
    HealthCheckConfiguration configuration;
    int CurrentFailureConcurrency = 0;
    public HealthCheck(){
        configuration = new HealthCheckConfiguration();
    }

    public boolean run(Host host){
        int iterator = 0;

        try {
            while (true){
                var isReachable = pingIp(host.getIp());

                System.out.println(isReachable ? "Host is reachable" : "Host is not reachable");

                CurrentFailureConcurrency = !isReachable ? CurrentFailureConcurrency+1 :  0 ;

                if(CurrentFailureConcurrency == configuration.getConcurrentFailureThreshold())
                {
                    return false;
                }

                Thread.sleep(configuration.getWait());

                iterator++;
            }
        } catch (IOException e) {
            System.out.println(e);

            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println(e);

            throw new RuntimeException(e);
        }
    }

    private boolean pingIp(String ip) throws IOException {
        InetAddress inet = InetAddress.getByName(ip);

        System.out.println("Sending Ping Request to " + ip);

        return inet.isReachable(configuration.getTimeoutThreshold());
    }
}
