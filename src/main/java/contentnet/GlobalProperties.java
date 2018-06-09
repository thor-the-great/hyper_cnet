package contentnet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GlobalProperties {
    static Properties props = new Properties();

    static {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(InputStream resourceStream = loader.getResourceAsStream("conf.properties")) {
            props.load(resourceStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getSetting(String keyName) {
        return props.getProperty(keyName);
    }

    public static final String _SERVER_PORT = GlobalProperties.getSetting("host.name")
            + ((GlobalProperties.getSetting("host.port") == null || "".equalsIgnoreCase(GlobalProperties.getSetting("host.port").trim())) ? "" : ":" + GlobalProperties.getSetting("host.port"));

}
