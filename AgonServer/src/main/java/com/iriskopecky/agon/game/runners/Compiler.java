package com.iriskopecky.agon.game.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class Compiler {

    private static final Path API_JAR = Path.of("AgonAPI/target/AgonAPI-0.0.1-SNAPSHOT.jar");
    private static final int MAX_WAIT_SECONDS = 10;

    /**
     * Compile the file at javaFile into the directory at compilationDir.
     * @param terasDir The root directory for the teras
     * @param javaFile The java file to compile.
     * @return A compilationResult describing what happened.
     */
    public static CompilationResult compile(Path terasDir, Path javaFile) throws IOException {
        //get the output directory
        Path compilationDir = terasDir.resolve("compiled");
        Files.createDirectories(compilationDir);

        //store log
        Path log = terasDir.resolve("compile.log");

        //Prep the compile command
        ProcessBuilder builder = new ProcessBuilder(
                "javac",
                "-proc:none",
                "-encoding", "UTF-8",
                "-cp", API_JAR.toAbsolutePath().toString(),
                "-d", compilationDir.toString(),
                javaFile.toString()
        );

        //make sure log goes to the right spot.
        builder.redirectErrorStream(true);
        builder.redirectOutput(log.toFile());

        //compile
        Process compiler = builder.start();

        //handle any errors that occur.
        try {
            //allow time to pass.
            boolean finished = compiler.waitFor(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            //if it took more than max compilation time, kill it.
            if (!finished) {
                compiler.destroyForcibly();
                return CompilationResult.TIMEOUT;
            }
            //If zero, compilation succeeded. Else it failed.
            if (compiler.exitValue() != 0) {
                //TODO read from log file and give that intel to the user.
                return CompilationResult.UNKNOWN_ERROR;
            }
        } catch (InterruptedException e) {
            compiler.destroyForcibly();
            Thread.currentThread().interrupt();
            return CompilationResult.COMPILATION_ERROR;
        }


        return CompilationResult.SUCCESS;
    }





    public enum CompilationResult {
        SUCCESS,
        TIMEOUT,
        COMPILATION_ERROR,
        UNKNOWN_ERROR
    }
}
