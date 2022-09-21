package managed.fleet.common.models;

public class Host {
    private String Name = null;
    private String Ip = null;

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
}
