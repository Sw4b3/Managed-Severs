package managed.fleet.api.services;

import common.models.Host;
import managed.fleet.api.interfaces.IHostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.*;

import java.util.ArrayList;
import java.util.List;

public class HostService implements IHostService {
    private IVirtualBox vbox;
    private static Logger logger;

    public HostService() {
        logger = LoggerFactory.getLogger(this.getClass());

        connect();
    }

    private void connect() {
        try {
            var hostManager = VirtualBoxManager.createInstance(null);

            hostManager.connect("http://192.168.0.111:18083", null, null);

            vbox = hostManager.getVBox();
        } catch (Exception e) {
            logger.error("Web server is unavailable");
        }
    }

    public List<Host> scanHosts() {
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
        return vbox.findMachine(machineName).getState();
    }

    public String getMachineIPv4(String machineName) {
        if (!machineExists(machineName))
            return null;

        var machine = vbox.findMachine(machineName);

        var ip = machine.getGuestPropertyValue("/VirtualBox/GuestInfo/Net/0/V4/IP");

        return !ip.equals("") ? ip : "0.0.0.0";
    }

    public boolean machineExists(String machineName) {
        if (machineName == null)
            return false;

        List<IMachine> machines = vbox.getMachines();

        for (IMachine machine : machines) {
            if (machine.getName().equals(machineName))
                return true;
        }

        return false;
    }
}
