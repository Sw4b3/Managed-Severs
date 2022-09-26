package managed.fleet.api.models;

public class InstanceTypeConfiguration {
    private String Name;
    private int MemoryConfiguration;
    private int CpuCount;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getCpuCount() {
        return CpuCount;
    }

    public void setCpuCount(int cpuCount) {
        CpuCount = cpuCount;
    }

    public long getMemoryConfiguration() {
        return MemoryConfiguration * 1024;
    }

    public void setMemoryConfiguration(int memoryConfiguration) {
        MemoryConfiguration = memoryConfiguration;
    }
}
