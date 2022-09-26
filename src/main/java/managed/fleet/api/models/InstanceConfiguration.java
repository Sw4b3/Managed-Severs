package managed.fleet.api.models;

public class InstanceConfiguration {
    private ImageTypeConfiguration ImageTypeConfiguration;
    private InstanceTypeConfiguration InstanceTypeConfiguration;
    private int StorageCapacity;

    public InstanceConfiguration(ImageTypeConfiguration imageTypeConfiguration, InstanceTypeConfiguration instanceTypeConfiguration, int storageCapacity) {
        ImageTypeConfiguration = imageTypeConfiguration;
        InstanceTypeConfiguration = instanceTypeConfiguration;
        StorageCapacity = storageCapacity;
    }

    public String getOSImage() {
        return ImageTypeConfiguration.getOSImageName();
    }

    public int getCpuCount() {
        return InstanceTypeConfiguration.getCpuCount();
    }

    public Long getStorageCapacity() {
        return StorageCapacity * 1073741824L;
    }

    public long getMemoryConfiguration() {
        return InstanceTypeConfiguration.getMemoryConfiguration();
    }

    public void setMemoryConfiguration(int memoryConfiguration) {
        InstanceTypeConfiguration.setMemoryConfiguration(memoryConfiguration);
    }
}
