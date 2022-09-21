package monitored.fleet;

import monitored.fleet.services.HealthCheckService;

public class Main {

    public static void main(String[] args) {
        while (true) {
            try {
                new HealthCheckService().run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
