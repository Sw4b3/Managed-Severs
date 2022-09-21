package managed.fleet.api.controllers;

import managed.fleet.common.services.VBoxHostManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/host")
public class HostManagerController {
    @GetMapping(path = "/create", produces = "application/json")
    public String CreateHost() {
        new VBoxHostManager().createHost();

        return "Ok";
    }

    @GetMapping(path = "/delete", produces = "application/json")
    public String DeleteHost() throws Exception {
        throw new Exception("Endpoint not created yet");
    }

    @GetMapping(path = "/start", produces = "application/json")
    public String StartHost() {
        new VBoxHostManager().startHost("vm0");

        return "Ok";
    }

    @GetMapping(path = "/terminate", produces = "application/json")
    public String TerminateHost() {
        new VBoxHostManager().terminateHost("vm0");

        return "Ok";
    }
}