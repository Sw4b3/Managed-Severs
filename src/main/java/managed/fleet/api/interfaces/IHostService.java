package managed.fleet.api.interfaces;

import common.models.Host;
import org.virtualbox_6_1.MachineState;

import java.util.List;

public interface IHostService {
    List<Host> scanHosts();

    MachineState GetHostState(String machineName);

    String getHostIPv4(String machineName);

    boolean hostExists(String machineName);
}
