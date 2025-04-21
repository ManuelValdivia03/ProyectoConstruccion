package dataaccess;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LogManager.getLogger(ConfigLoader.class);
    private static final Properties properties = new Properties();

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                logger.fatal("No se encontró el archivo config.properties en el classpath");
                throw new RuntimeException("No se encontró config.properties");
            }

            properties.load(input);
            logger.info("Archivo config.properties cargado correctamente");

        } catch (IOException e) {
            logger.error("Error al cargar config.properties", e);
            throw new RuntimeException("Error al cargar configuración", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
