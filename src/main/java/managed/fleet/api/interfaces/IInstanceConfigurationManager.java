package managed.fleet.api.interfaces;

import managed.fleet.api.models.HostConfiguration;
import managed.fleet.api.models.InstanceConfiguration;

public interface IInstanceConfigurationManager {
    InstanceConfiguration getInstanceConfiguration(HostConfiguration hostConfiguration);
}
