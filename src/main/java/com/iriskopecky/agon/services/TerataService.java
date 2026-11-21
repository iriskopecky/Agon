package com.iriskopecky.agon.services;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import com.iriskopecky.agon.game.files.Compiler;
import com.iriskopecky.agon.game.files.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class TerataService {

    public static final int MAX_SIZE = 512 * 1024; //256KB
    //public static final String SAVE_DIRECTORY = "/var/lib/agon/terata/"; //prod config
    public static final Path SAVE_DIRECTORY = Path.of("uploads", "terata"); //dev config
    public static final String JAVA_FILE_REGEX = "[A-Za-z_$][A-Za-z0-9_$]*\\.java";

    private final TerasRepository terasRepository;

    public TerataService(TerasRepository terasRepository) {
        this.terasRepository = terasRepository;
    }

    /**
     * Ensure all instances of grabbing a Teras that need to be authenticated are.
     * @param terasId The id of the teras.
     * @param username The username of the account seeking to view the teras.
     * @return If terasId exists and username matches the owner of the teras, returns the teras instance. Else, null.
     */
    public Teras getTerasAuthenticated(long terasId, String username) {
        //if the user is not authenticated, return null
        if (username == null) return null;

        Optional<Teras> optionalTeras = terasRepository.findById(terasId);
        //if there is no teras matching the id, return null
        if (optionalTeras.isEmpty()) {
            return null;
        }

        Teras teras = optionalTeras.get();

        //if username owns the teras, allow the retrieval, else block it.
        if (teras.getOwner().getUsername().equals(username)) {
            return teras;
        } else {
            return null;
        }
    }

    /**
     * Validate that the provided file is good to go, then compile and save it.
     * @param input The multipartfile from the request param. Nothing is guaranteed about this input.
     * @return A path pointing to a directory freshly created that stores the .java input and the compiled version.
     */
    public Path validateAndCompileSource(MultipartFile input) {
        //ensure the file exists
        if (input == null || input.isEmpty()) {
            return null;
        }
        //ensure the file is of appropriate size
        if (input.getSize() > MAX_SIZE) {
            return null;
        }
        //ensure the file is of appropriate name
        if (input.getOriginalFilename() == null || !input.getOriginalFilename().matches(JAVA_FILE_REGEX)) {
            return null;
        }

        Path workingDir = null;
        Path outputDir = null;
        Path sourceFile = null;
        boolean delete = false;
        Compiler.CompilationResult result = Compiler.CompilationResult.UNKNOWN_ERROR;

        try {
            //get the working directory
            workingDir = SAVE_DIRECTORY.toAbsolutePath().normalize();
            Files.createDirectories(workingDir);

            //create the output dir
            String uuid = UUID.randomUUID().toString();
            outputDir = workingDir.resolve(uuid);
            Files.createDirectories(outputDir);
            sourceFile = outputDir.resolve(input.getOriginalFilename());
            input.transferTo(sourceFile);

            //compile the java file.
            result = Compiler.compile(outputDir, sourceFile);
        } catch (IOException _) {
            delete = true;
        }

        //handle errors that might occur.
        if (delete || result != Compiler.CompilationResult.SUCCESS) {
            //TODO richer error handling and reporting to the user.
            System.out.println("Error: " + result.toString());
            FileUtils.recursiveDelete(outputDir);
            return null;
        }

        //TODO validate that the compiled file is fine. Probably in Compiler tbh.

        return outputDir;
    }

    /**
     * Creates a Teras with the given information, compiling and saving the file.
     * @param name The name of the teras.
     * @param description The description of the teras.
     * @param isPublic Whether the teras starts as public or not.
     * @param source The input file with the request.
     * @return A Teras entity if creation succeeded, or null otherwise.
     */
    public Teras createTeras(String name, String description, boolean isPublic, MultipartFile source, Account owner) {
        //handle the file
        Path terasDir = validateAndCompileSource(source);
        if (terasDir == null) { //some part of the file management process failed.
            return null;
        }

        //get the file key
        String fileKey = terasDir.getFileName().toString();

        //create the teras
        Teras teras = new Teras(owner, name, description, isPublic, fileKey);
        terasRepository.save(teras);
        return teras;
    }
}
