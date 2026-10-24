package com.packaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class PackagingApplication {
    public static void main(String[] args) throws Exception {
        String configuredPath = System.getenv("PACKAGING_DB_PATH");
        Path databasePath = configuredPath == null || configuredPath.isBlank()
                ? Path.of(System.getProperty("user.home"), ".packaging-management", "packaging")
                : Path.of(configuredPath);
        Path parent = databasePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        System.setProperty("packaging.database.path", databasePath.toAbsolutePath().toString());
        SpringApplication.run(PackagingApplication.class, args);
    }
}
