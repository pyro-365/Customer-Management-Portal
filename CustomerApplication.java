package com.customer;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerApplication {
    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(CustomerApplication.class, args);
    }

    private static void loadEnvFile() {
        File env = new File(".env");
        if (!env.exists()) return;
        try {
            List<String> lines = Files.readAllLines(env.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String val = line.substring(eq + 1).trim();
                    if (System.getProperty(key) == null && System.getenv(key) == null) {
                        System.setProperty(key, val);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
