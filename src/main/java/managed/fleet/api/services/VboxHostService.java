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
    private final IWebserverSession webserverSession;
    private static Logger logger = LoggerFactory.getLogger(VboxHostService.class);

    public VboxHostService() {
        webserverSession = new VboxWebserverSession();
    }

    public List<Host> scanHosts() {
        logger.info("Scanning Hosts");

        return webserverSession.execute(() -> scanMachine());
    }

    public MachineState GetHostState(String machineName) {
        logger.info("Getting Hosts State::" + machineName);

        return webserverSession.execute(() -> GetMachineState(machineName));
    }

    public String getHostIPv4(String machineName) {
        logger.info("Getting Hosts Ip::" + machineName);

        return webserverSession.execute(() -> getMachineIPv4(machineName));
    }

    public boolean hostExists(String machineName) {
        logger.info("Checking if Hosts exist::" + machineName);

        return webserverSession.execute(() -> machineExists(machineName));
    }

    private List<Host> scanMachine() {
        try {
            var hosts = new ArrayList<Host>();

            for (IMachine machine : webserverSession.getVbox().getMachines()) {

                var host = new Host(
                        machine.getName(),
                        getHostIPv4(machine.getName()),
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

    public MachineState GetMachineState(String machineName) {
        if (!machineExists(machineName))
            return null;

        return webserverSession.getVbox().findMachine(machineName).getState();
    }

    private String getMachineIPv4(String machineName) {
        if (!machineExists(machineName))
            return null;

        var ip = webserverSession.getVbox().findMachine(machineName).getGuestPropertyValue("/VirtualBox/GuestInfo/Net/0/V4/IP");

        return !ip.equals("") ? ip : "0.0.0.0";
    }

    private boolean machineExists(String machineName) {
        if (machineName == null)
            return false;

        List<IMachine> machines = webserverSession.getVbox().getMachines();

        for (IMachine machine : machines) {
            if (machine.getName().equals(machineName))
                return true;
        }

        return false;
    }
}
