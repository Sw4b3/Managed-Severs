package managed.fleet.api.models;

import managed.fleet.api.enums.OSType;

public class HostConfiguration {
    private int StorageCapacity;
    private int MemoryConfiguration;
    private OSType OsType;

    //TODO: to be move to web.xml
    private final static String Windows = "Windows_Server_2016_Datacenter_EVAL_en-us_14393_refresh.ISO";
    private final static String Ubuntu = "ubuntu-22.04.1-desktop-amd64.iso";
    private final static String Mac = "macOS Mojave 10.14.iso";

    public String getOSImage() throws Exception {
        switch (OsType) {
            case Windows:
                return Windows;
            case Ubuntu:
                return Ubuntu;
            case Mac:
                return Mac;
            default:
                throw new Exception("There are no image paths for this OS type::" + OsType);
        }
    }

    public void setOsType(OSType osType) {
        OsType = osType;
    }

    public Long getStorageCapacity() {
        return StorageCapacity * 1073741824L;
    }

    public void setStorageCapacity(int storageCapacity) {
        StorageCapacity = storageCapacity;
    }

    public long getMemoryConfiguration() {
        return MemoryConfiguration * 1024;
    }

    public void setMemoryConfiguration(int memoryConfiguration) {
        MemoryConfiguration = memoryConfiguration;
    }
}
