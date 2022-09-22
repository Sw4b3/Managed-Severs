package managed.fleet.api.interfaces;

public interface IHostManager {
      void startHost(String machineName);
      void terminateHost(String machineName);
      void registerClient();
      void deregisterClient();
}
