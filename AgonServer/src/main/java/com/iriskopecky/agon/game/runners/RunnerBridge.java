package com.iriskopecky.agon.game.runners;

import com.iriskopecky.util.Message;
import com.iriskopecky.util.Response;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.services.TerataService;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class RunnerBridge {

    private static final Path RUNNER_JAR = Path.of("AgonRunner/target/AgonRunner-0.0.1-SNAPSHOT.jar");
    private Teras currentTeras;
    private Path terasDir;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Process process;
    private JsonMapper mapper;

    /**
     * Construct a runner bridge
     * @param teras The teras this runner bridge is for.
     */
    public RunnerBridge(Teras teras) {
        this.currentTeras = teras;
        this.terasDir = TerataService.SAVE_DIRECTORY.resolve(teras.getFileKey());
        mapper = JsonMapper.builder().build();
    }

    /**
     * Starts the runner bridge.
     * @return True if successfully started, false otherwise.
     */
    public boolean start() {
        //prep to start
        Path compiledClasses = terasDir.resolve("compiled");

        String classpath = String.join(
                File.pathSeparator,
                RUNNER_JAR.toAbsolutePath().toString(),
                compiledClasses.toAbsolutePath().toString()
        );

        ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-cp",
                classpath,
                "com.iriskopecky.RunnerMain",
                currentTeras.getFileName()
        );

        //start the process
        try {
            process = builder.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }

        //establish IO
        writer = new BufferedWriter(
                new OutputStreamWriter(
                        process.getOutputStream(),
                        StandardCharsets.UTF_8
                )
        );

        reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        return true;
    }

    /**
     * Stop the process.
     */
    public void stop() {
        process.destroyForcibly();
    }

    /**
     * Send a message from the server to the teras runner.
     * @return The response from the runner.
     */
    public synchronized Response sendMessage(Message message) {
        try {
            //send the request
            writer.write(mapper.writeValueAsString(message));
            writer.newLine();
            writer.flush();

            //get the response
            String response = reader.readLine();
            return mapper.readValue(response, Response.class);
        } catch (Exception e) {
            return null;
        }
    }
}
