package com.iriskopecky.agon.game.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtils {

    public static boolean recursiveDelete(Path deletionDir) {
        //don't bother do deletion if the file doesn't exist.
        if (deletionDir == null || Files.notExists(deletionDir)) {
            return false;
        }

        //do the deletion
        try {
            Files.walkFileTree(deletionDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    if (exc != null) {
                        throw exc;
                    }

                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException _) {
            return false;
        }

        return true;
    }
}
