package managed.fleet.api.models;

import managed.fleet.api.enums.OSType;

public class HostConfiguration {
    private OSType OsType;
    private int StorageCapacity;
    private String InstanceType;

    public OSType getOsType() {
        return OsType;
    }

    public void setOsType(OSType osType) {
        OsType = osType;
    }

    public int getStorageCapacity() {
        return StorageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        StorageCapacity = storageCapacity;
    }

    public String getInstanceType() {
        return InstanceType;
    }

    public void setInstanceType(String instanceType) {
        InstanceType = instanceType;
    }
}
