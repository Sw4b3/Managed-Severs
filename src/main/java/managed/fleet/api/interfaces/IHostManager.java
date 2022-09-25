package managed.fleet.api.interfaces;

import managed.fleet.api.models.HostConfiguration;

public interface IHostManager {
      void startHost(String machineName);
      void terminateHost(String machineName);
      void registerClient(HostConfiguration hostConfiguration);
      void deregisterClient(String machineName);
}
