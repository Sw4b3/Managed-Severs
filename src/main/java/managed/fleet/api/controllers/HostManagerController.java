package managed.fleet.api.controllers;

import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.models.HostConfiguration;
import managed.fleet.api.services.VBoxHostManagerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/hostmanager")
public class HostManagerController {

    private final IHostManager manager;

    public HostManagerController() {
        this.manager = new VBoxHostManagerService();
    }

    @PostMapping(path = "/create", produces = "application/json")
    public String CreateHost(@RequestBody HostConfiguration hostConfiguration) {
        manager.registerClient(hostConfiguration);

        return "Ok";
    }

    @GetMapping(path = "/delete", produces = "application/json")
    public String DeleteHost(@RequestParam String hostName) {
        manager.deregisterClient(hostName);

        return "Ok";
    }

    @GetMapping(path = "/start", produces = "application/json")
    public String StartHost(@RequestParam String hostName) {
        manager.startHost(hostName);

        return "Ok";
    }

    @GetMapping(path = "/terminate", produces = "application/json")
    public String TerminateHost(@RequestParam String hostName) {
        manager.terminateHost(hostName);

        return "Ok";
    }
}