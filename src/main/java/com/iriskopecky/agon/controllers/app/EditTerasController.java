package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import com.iriskopecky.agon.services.AccountService;
import com.iriskopecky.agon.services.TerataService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class EditTerasController {

    private final TerataService terataService;
    private final TerasRepository terasRepository;
    private final AccountRepository accountRepository;

    public EditTerasController(TerataService terataService, AccountRepository accountRepository, TerasRepository terasRepository) {
        this.terataService = terataService;
        this.accountRepository = accountRepository;
        this.terasRepository = terasRepository;
    }

    @GetMapping("/user/teras/{id}/edit")
    public String editTerasController(Model model, Principal user, @PathVariable long id) {
        //pass currentpage for nav highlighting
        model.addAttribute("currentpage", "userterata");

        //get the user and add their info
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));
        model.addAttribute("username", account.getUsername());

        //get the teras
        Teras teras = terataService.getTerasAuthenticated(id, user.getName());

        //disallow access if not permitted to view
        if (teras == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        //add relevant info on the teras
        model.addAttribute("terasId", teras.getId());
        model.addAttribute("terasCreation", teras.getCreationDate());
        model.addAttribute("terasName", teras.getName());
        model.addAttribute("terasIsPublic", teras.isPublicFacing());
        model.addAttribute("terasDesc", teras.getDescription());

        return "editteras";
    }

    /**
     * Allow edits of a teras.
     * @param user The owner of the teras to be created.
     * @param name The name of the teras.
     * @param description A description for the teras.
     * @param isPublic Whether the teras should be publicly displayed.
     * @return JSON response.
     */
    @PostMapping("/user/teras/{id}/edit")
    public String createTeras(Principal user, @RequestParam String name, @RequestParam(defaultValue = "") String description,
                              @RequestParam(defaultValue = "false", name="public") boolean isPublic, @PathVariable long id) {
        //ensure the user is authenticated
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));

        //ensure the name is unique
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is empty");
        }

        //get existing teras with the name, if any.
        List<Teras> matchingNamedTeras = terasRepository.findAllByName(name);
        if (!matchingNamedTeras.isEmpty()) {
            if (matchingNamedTeras.size() > 1) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Name requested is occupied by more than one teras: this state should not happen.");
            }
            Teras match = matchingNamedTeras.getFirst();
            //if the user is editing a teras but not changing the name, allow it. Else, disallow.
            if (match.getId() != id) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The name requested already exists.");
            }
        }

        //find the teras
        Optional<Teras> terasOpt = terasRepository.findById(id);
        if (terasOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested Teras does not exist.");
        }
        Teras teras = terasOpt.get();

        //apply edits
        teras.setDescription(description);
        teras.setName(name);
        teras.setPublicFacing(isPublic);
        terasRepository.save(teras);

        return "redirect:/user/terata";
    }
}
