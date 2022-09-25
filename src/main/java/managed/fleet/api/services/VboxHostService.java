package managed.fleet.api.services;

import common.models.Host;
import managed.fleet.api.interfaces.IHostService;
import managed.fleet.api.interfaces.IWebserverSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.*;

import java.util.ArrayList;
import java.util.List;

public class VboxHostService implements IHostService {
    private IVirtualBox vbox;
    private final VirtualBoxManager hostManager;
    private final IWebserverSession webserverSession;
    private static Logger logger = LoggerFactory.getLogger(VboxHostService.class);

    public VboxHostService() {
        hostManager = VirtualBoxManager.createInstance(null);
        webserverSession = new VboxWebserverSession();
    }

    public List<Host> scanHosts() {
        webserverSession.connect(hostManager);

        vbox = hostManager.getVBox();

        try {
            var hosts = new ArrayList<Host>();

            for (IMachine machine : vbox.getMachines()) {

                var host = new Host(
                        machine.getName(),
                        getMachineIPv4(machine.getName()),
                        machine.getNetworkAdapter(0L).getMACAddress(),
                        machine.getState()
                );

                hosts.add(host);
            }

            return hosts;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public MachineState GetHostState(String machineName) {
        webserverSession.connect(hostManager);

        vbox = hostManager.getVBox();

        var machineState = vbox.findMachine(machineName).getState();

        webserverSession.disconnect(hostManager);

        return machineState;
    }

    public String getMachineIPv4(String machineName) {
        webserverSession.connect(hostManager);

        vbox = hostManager.getVBox();

        if (!machineExists(machineName))
            return null;

        var machine = vbox.findMachine(machineName);

        var ip = machine.getGuestPropertyValue("/VirtualBox/GuestInfo/Net/0/V4/IP");

        webserverSession.disconnect(hostManager);

        return !ip.equals("") ? ip : "0.0.0.0";
    }

    public boolean machineExists(String machineName) {
        webserverSession.connect(hostManager);

        vbox = hostManager.getVBox();

        if (machineName == null)
            return false;

        List<IMachine> machines = vbox.getMachines();

        for (IMachine machine : machines) {
            if (machine.getName().equals(machineName))
                return true;
        }

        webserverSession.disconnect(hostManager);

        return false;
    }
}
