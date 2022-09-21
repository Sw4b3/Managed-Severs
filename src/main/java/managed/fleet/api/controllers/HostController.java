package managed.fleet.api.controllers;

import managed.fleet.common.interfaces.IHostManager;
import managed.fleet.common.models.Host;
import managed.fleet.common.services.VBoxHostManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/Host")
public class HostController {

    private final VBoxHostManager manager;

    public HostController(){
        this.manager = new VBoxHostManager();
    }

    @GetMapping(path = "/GetHosts", produces = "application/json")
    public List<Host> GetHosts() {
        return manager.scanHosts();
    }

    @PostMapping(path = "/GetHostIp", produces = "application/json")
    public String GetHostIp(@RequestBody String hostName) {
        return manager.getMachineIPv4(hostName);
    }
}
