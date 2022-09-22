package managed.fleet.api.services;

import common.models.Host;
import managed.fleet.api.interfaces.IHostService;
import org.virtualbox_6_1.*;

import java.util.ArrayList;
import java.util.List;

public class HostService implements IHostService {
    private VirtualBoxManager hostManager;
    private IVirtualBox vbox;

    public HostService() {
        connect();
    }

    private void connect() {
        try {
            hostManager = VirtualBoxManager.createInstance(null);

            hostManager.connect("http://192.168.0.111:18083", null, null);

            vbox = hostManager.getVBox();
        } catch (Exception e) {
            System.out.println("Web server is unavailable");
        }
    }

    public List<Host> scanHosts() {
        try {
            var hosts = new ArrayList<Host>();

            for (IMachine machine : vbox.getMachines()) {

                var host = new Host();

                host.setName(machine.getName());
                host.setIp(getMachineIPv4(machine.getName()));
                host.setMAC(machine.getNetworkAdapter(0L).getMACAddress());
                host.setState(machine.getState());

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

        IMachine machine = vbox.findMachine(machineName);

        var ip = machine.getGuestPropertyValue("/VirtualBox/GuestInfo/Net/0/V4/IP");

        return !ip.equals("") ? ip : "0.0.0.0";
    }

    public boolean machineExists(String machineName) {
        if (machineName == null) {
            return false;
        }

        List<IMachine> machines = vbox.getMachines();

        for (IMachine machine : machines) {
            if (machine.getName().equals(machineName)) {
                return true;
            }
        }

        return false;
    }
}
