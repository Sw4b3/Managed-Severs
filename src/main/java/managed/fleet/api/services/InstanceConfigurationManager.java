package managed.fleet.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.utlis.ConfigurationManger;
import managed.fleet.api.interfaces.IInstanceConfigurationManager;
import managed.fleet.api.models.HostConfiguration;
import managed.fleet.api.models.InstanceConfiguration;
import managed.fleet.api.models.InstanceTypeConfiguration;
import managed.fleet.api.models.ImageTypeConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InstanceConfigurationManager implements IInstanceConfigurationManager {
    List<InstanceTypeConfiguration> instanceTypeConfigurations;
    List<ImageTypeConfiguration> imageTypesConfigurations;

    public InstanceConfigurationManager() {
        instanceTypeConfigurations = loadInstanceTypeConfiguration();
        imageTypesConfigurations = loadOsImagesConfiguration();
    }

    public InstanceConfiguration getInstanceConfiguration(HostConfiguration hostConfiguration) {
        return new InstanceConfiguration(
                getImagesConfiguration(hostConfiguration),
                getInstanceTypeConfiguration(hostConfiguration),
                hostConfiguration.getStorageCapacity()
        );
    }

    private InstanceTypeConfiguration getInstanceTypeConfiguration(HostConfiguration hostConfiguration) {
        for (var instanceTypeConfiguration : instanceTypeConfigurations) {
            if (hostConfiguration.getInstanceType().equals(instanceTypeConfiguration.getName()))
                return instanceTypeConfiguration;
        }

        throw new RuntimeException("No Instance Configuration found");
    }

    private ImageTypeConfiguration getImagesConfiguration(HostConfiguration hostConfiguration) {
        for (var imageTypesConfiguration : imageTypesConfigurations) {
            if (hostConfiguration.getOsType().equals(imageTypesConfiguration.getOsType()))
                return imageTypesConfiguration;
        }

        throw new RuntimeException("No Instance Configuration found");
    }

    private List<InstanceTypeConfiguration> loadInstanceTypeConfiguration() {
        File directoryPath = new File(ConfigurationManger.getSection("Path:InstanceTypeConfiguration").toString());

        try {
            InstanceTypeConfiguration[] hostsArr = new ObjectMapper().readValue(directoryPath, InstanceTypeConfiguration[].class);

            return Arrays.asList(hostsArr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ImageTypeConfiguration> loadOsImagesConfiguration() {
        File directoryPath = new File(ConfigurationManger.getSection("Path:ImagesConfiguration").toString());

        try {
            ImageTypeConfiguration[] hostsArr = new ObjectMapper().readValue(directoryPath, ImageTypeConfiguration[].class);

            return Arrays.asList(hostsArr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
