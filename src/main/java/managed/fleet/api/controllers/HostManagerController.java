package managed.fleet.api.controllers;

import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.services.VBoxHostManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/hostmanager")
public class HostManagerController {

    private final IHostManager manager;

    public  HostManagerController(){
        this.manager = new VBoxHostManager();
    }

    @GetMapping(path = "/create", produces = "application/json")
    public String CreateHost() {
        manager.registerClient();

        return "Ok";
    }

    @GetMapping(path = "/delete", produces = "application/json")
    public String DeleteHost() throws Exception {
        throw new Exception("Endpoint not created yet");
    }

    @PostMapping(path = "/start", produces = "application/json")
    public String StartHost(@RequestBody String hostName) {
        manager.startHost(hostName);

        return "Ok";
    }

    @GetMapping(path = "/terminate", produces = "application/json")
    public String TerminateHost() {
        manager.terminateHost("vm0");

        return "Ok";
    }
}