package com.bicycle.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class FileUtils {

    @SneakyThrows
    public Path createParentDirectoriesIfNotExist(Path path) {
        final Path parentPath = path.getParent();
        if(!Files.exists(parentPath)) Files.createDirectories(parentPath);
        return path;
    }

}
