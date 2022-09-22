package common.models;

import org.virtualbox_6_1.MachineState;

public class Host {
    private String Name = null;
    private String Ip = "0.0.0.0";
    private String MAC = null;
    private MachineState State = null;

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public MachineState getState() {
        return State;
    }

    public void setState(MachineState state) {
        State = state;
    }
}
