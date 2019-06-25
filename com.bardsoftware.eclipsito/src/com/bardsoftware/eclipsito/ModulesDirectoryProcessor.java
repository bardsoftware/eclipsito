package com.bardsoftware.eclipsito;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModulesDirectoryProcessor {

    public static List<PluginDescriptor> process(File pluginDir, String descriptorPattern) throws IOException {
        return processDescriptors(findModuleDescriptors(pluginDir, descriptorPattern));
    }

    static List<File> findModuleDescriptors(File pluginDir, final String descriptorPattern) throws IOException {
        List<File> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(descriptorPattern);
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                File file = path.toFile();
                if (file.exists() && pattern.matcher(file.getName()).matches()) {
                    result.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(pluginDir.toPath(), Collections.singleton(FileVisitOption.FOLLOW_LINKS), 2, visitor);
        return result;
    }


    static List<PluginDescriptor> processDescriptors(List<File> descriptorFiles) {
        return descriptorFiles.stream().map(file -> {
            try {
                return DescriptorParser.parse(file);
            } catch (Exception e) {
                Launch.LOG.log(Level.SEVERE,
                    String.format("Failed to process descriptor=%s", file.getAbsolutePath()), e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
