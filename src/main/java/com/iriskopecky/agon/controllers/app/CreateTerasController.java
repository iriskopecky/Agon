package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import com.iriskopecky.agon.services.TerataService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.security.Principal;
import java.util.Map;

@Controller
public class CreateTerasController {

    private final TerasRepository terasRepository;
    private final TerataService terataService;
    private AccountRepository accountRepository;
    public CreateTerasController(AccountRepository accountRepository, TerasRepository terasRepository, TerataService terataService) {
        this.accountRepository = accountRepository;
        this.terasRepository = terasRepository;
        this.terataService = terataService;
    }

    @GetMapping("/user/create")
    public String userTerata(Model model, Principal user) {
        //pass currentpage for nav highlighting
        model.addAttribute("currentpage", "create");

        //get the user and add their info
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));
        model.addAttribute("username", account.getUsername());

        return "create";
    }

    /**
     * Allow creation of a teras.
     * @param user The owner of the teras to be created.
     * @param name The name of the teras.
     * @param description A description for the teras.
     * @param isPublic Whether the teras should be publicly displayed.
     * @param file The file containing code for the teras.
     * @return JSON response.
     */
    @PostMapping("/user/create")
    public String createTeras(Principal user, @RequestParam String name, @RequestParam(defaultValue = "") String description,
                                           @RequestParam(defaultValue = "false", name="public") boolean isPublic, @RequestParam("source") MultipartFile file) {
        //ensure the user is authenticated
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));

        //ensure the name is unique
        if (name == null || name.isEmpty() || !terasRepository.findAllByName(name).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is not valid or is already in use.");
        }

        //create the teras
        Teras teras = terataService.createTeras(name, description, isPublic, file, account);
        if (teras == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Teras"); //TODO replace with richer error handling/reporting.
        }

        return "redirect:/user/terata";
    }
}
