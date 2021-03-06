package org.ripreal.textclassifier2;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Parser for properties needed to connect to jira.  Properties read from resources/config.properties. Root directory
 * may be a working or classpath dir.
 */
public class PropertiesClient {
    public static final String LOGIN = "LOGIN";
    public static final String PASSWORD = "PASSWORD";
    public static final String JIRA_HOME = "JIRA_HOME";
    public static final String PROXY_SERVER = "PROXY_SERVER";
    public static final String PROXY_PORT = "PROXY_PORT";

    private Logger log = LoggerFactory.getLogger(PropertiesClient.class);

    public final static Map<String, String> DEFAULT_PROPERTY_VALUES = ImmutableMap.<String, String>builder()
            .put(JIRA_HOME, "localhost")
            .put(LOGIN, "")
            .put(PASSWORD, "")
            .put(PROXY_SERVER, "") //http://username:password@localhost:8888
            .put(PROXY_PORT, "")
            .build();

    private final String fileUrl;
    private final String propFileName = "configJira.properties";

    public PropertiesClient() throws Exception {
        fileUrl = "resources/" + propFileName;
    }


    public Map<String, String> getPropertiesOrDefaults() {
        try {
            Map<String, String> map = toMap(tryGetProperties());
            map.putAll(Maps.difference(map, DEFAULT_PROPERTY_VALUES).entriesOnlyOnRight());
            return map;
        } catch (FileNotFoundException e) {
            tryCreateDefaultFile();
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        } catch (IOException e) {
            return new HashMap<>(DEFAULT_PROPERTY_VALUES);
        }
    }

    private Map<String, String> toMap(Properties properties) {
        return properties.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(o -> o.getKey().toString(), t -> t.getValue().toString()));
    }

    private Properties toProperties(Map<String, String> propertiesMap) {
        Properties properties = new Properties();
        propertiesMap.entrySet()
                .stream()
                .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        return properties;
    }

    private Properties tryGetProperties() throws IOException {
        InputStream inputStream = new FileInputStream(new File(fileUrl));
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop;
    }

    public void savePropertiesToFile(Map<String, String> properties) {
        OutputStream outputStream = null;
        File file = new File(fileUrl);
        try {
            outputStream = new FileOutputStream(file);
            Properties p = toProperties(properties);
            p.store(outputStream, null);
        } catch (Exception e) {
            log.info("Exception: " + e);
        } finally {
            closeQuietly(outputStream);
        }
    }

    public void tryCreateDefaultFile() {
        log.info("Creating default properties file: " + propFileName);
        tryCreateFile().ifPresent(file -> savePropertiesToFile(DEFAULT_PROPERTY_VALUES));
    }

    private Optional<File> tryCreateFile() {
        File file = new File(fileUrl);
        try {
            file.createNewFile();
            return Optional.of(file);
        } catch (IOException e) {
            log.error("error when writing default config file: " + file.getAbsolutePath(), e);
            throw new RuntimeException(String.format("cannot create config file %s", file.getAbsolutePath()));
        }
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignored
        }
    }

    public static String loadData(String resourcePath, String defaultValue) {
        try (BufferedInputStream inputStream = new BufferedInputStream(PropertiesClient.class.getResourceAsStream(resourcePath));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int result = inputStream.read(); result != -1; result = inputStream.read()) {
                outputStream.write(result);
            }

            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

}
