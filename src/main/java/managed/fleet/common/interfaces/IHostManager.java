package managed.fleet.common.interfaces;

public interface IHostManager {
    public  void startHost(String machineName);
    public  void terminateHost(String machineName);
    public  void registerClient();
    public  void deregisterClient();
}
