package managed.fleet.api;

import common.utlis.ConfigurationManger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Program {

    public static void main(String[] args) {
        try {
            ConfigurationManger.loadConfigurations(System.getProperty("user.dir") + "/src/main/java/managed/fleet/api/appsettings.json");
            ConfigurationManger.loadEnvironmentVariable();

            SpringApplication.run(Program.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
