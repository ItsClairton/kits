package dev.clairton.bukkit.kits.util;

import dev.clairton.bukkit.kits.Kits;
import lombok.val;
import org.apache.commons.io.Charsets;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;

/*
    Considering this is a test, it can be run on many operating systems to test compatibility (Windows, Linux, e.g.)
    So we have this class to enforce UTF-8 encoding, and avoid encoding issues on Windows for example.
 */
public class EncodingUtil {

    private static File file;

    public static YamlConfiguration saveDefaultConfig() throws IOException, InvalidConfigurationException {
        file = new File(Kits.getInstance().getDataFolder(), "config.yml");

        if(file.exists()) return loadConfig();

        val instance = Kits.getInstance();
        val resource = instance.getResource("config.yml");

        val defaultConfig = new YamlConfiguration();
        defaultConfig.load(new InputStreamReader(resource, Charsets.UTF_8));

        if(!Kits.getInstance().getDataFolder().mkdirs() || !file.createNewFile()) {
            Kits.getInstance().log(ChatColor.RED, "Não foi possível carregar a configuração inicial (Permissão do sistema de arquivos?).");
            return null;
        }

        val data = defaultConfig.saveToString();
        try(val writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), Charsets.UTF_8)) {
            writer.write(data);
        }

        return loadConfig();
    }

    public static YamlConfiguration loadConfig() {
        val config = new YamlConfiguration();

        try(val stream = new FileInputStream(file)) {
            config.load(new InputStreamReader(stream, Charsets.UTF_8));
            return config;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
