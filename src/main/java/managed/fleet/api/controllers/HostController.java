package managed.fleet.api.controllers;

import common.models.Host;
import managed.fleet.api.interfaces.IHostService;
import managed.fleet.api.services.HostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.virtualbox_6_1.MachineState;

import java.util.List;

@RestController
@RequestMapping(path = "/Host")
public class HostController {
    private final IHostService manager;

    public HostController() {
        this.manager = new HostService();
    }

    @GetMapping(path = "/GetHosts", produces = "application/json")
    public List<Host> GetHosts() {
        return manager.scanHosts();
    }

    @GetMapping(path = "/GetState", produces = "application/json")
    public MachineState GetHostState(@RequestParam String hostName) {
        return manager.GetHostState(hostName);
    }

    @GetMapping(path = "/GetIp", produces = "application/json")
    public String GetHostIp(@RequestParam String hostName) {
        return manager.getMachineIPv4(hostName);
    }
}
