package common.utlis;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class ConfigurationManger {
    private final static HashMap<Object, Object> configurations = new HashMap<>();

    public static void loadEnvironmentVariable() {
        try {
            var envVariables = new HashMap<>(System.getenv());

            configurations.put("environment_variables",envVariables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadConfigurations(String appsettingPath) {
        try {
            var objectMapper = new ObjectMapper();

            var fileName = Path.of(appsettingPath);

            var result = Files.readString(fileName);

            var appsettingsVariables = new HashMap<>();

            appsettingsVariables.put("appsettings", objectMapper.readValue(result, HashMap.class));

            configurations.putAll(appsettingsVariables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getSection(String key) {
        var keys = key.split(":");

        if (keys.length == 0)
            return null;

        var value = getSection((HashMap<Object, Object>) configurations.getOrDefault("appsettings", null), keys);

        if (value == null)
            return getSection((HashMap<Object, Object>) configurations.getOrDefault("environment_variables", null), keys);

        return value;
    }

    private static Object getSection(HashMap<Object, Object> map, String[] keys) {
        if (keys.length == 1)
            return map.getOrDefault(keys[0], null);

        var value = (HashMap<Object, Object>) map.getOrDefault(keys[0], null);

        for (int i = 1; i < keys.length; i++) {
            if (keys.length - 1 == i)
                return value.getOrDefault(keys[i], null);

            value = (HashMap<Object, Object>) value.get(keys[i]);
        }

        return value;
    }
}
