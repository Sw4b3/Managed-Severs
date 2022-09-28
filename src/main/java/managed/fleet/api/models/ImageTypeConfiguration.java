package managed.fleet.api.models;

import managed.fleet.api.enums.OSType;

public class ImageTypeConfiguration {
    private String Name;
    private OSType OsType;
    private String ImageName;


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public OSType getOsType() {
        return OsType;
    }

    public void setOsType(OSType osType) {
        OsType = osType;
    }

    public String getOSImageName() {
        return ImageName;
    }

    public void setImageName(String imageName) {
        ImageName = imageName;
    }

}
