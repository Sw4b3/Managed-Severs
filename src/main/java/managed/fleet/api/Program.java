package managed.fleet.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Program {

    public static void main(String[] args) {
		try {
            SpringApplication.run(Program.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
