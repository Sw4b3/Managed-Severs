package monitored.fleet.process;

import common.utlis.ConfigurationManger;
import monitored.fleet.process.services.HealthCheckService;

public class Main {

    public static void main(String[] args) {
        ConfigurationManger.loadConfigurations(System.getProperty("user.dir") + "/src/main/java/monitored/fleet/process/appsettings.json");
        ConfigurationManger.loadEnvironmentVariable();

        while (true) {
            try {
                new HealthCheckService().run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
