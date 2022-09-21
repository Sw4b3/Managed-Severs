package managed.fleet.common.Interfaces;

public interface IHostManager {
    public void run();
    public  void startHost(String machineName);
    public  void terminateHost(String machineName);
    public  void registerClient();
    public  void deregisterClient();
}
