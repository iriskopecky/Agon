package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import com.iriskopecky.agon.data.Teras;
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
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class EditTerasController {

    private final TerataService terataService;
    private final AccountRepository accountRepository;

    public EditTerasController(TerataService terataService, AccountRepository accountRepository) {
        this.terataService = terataService;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/user/teras/{id}/edit")
    public String editTerasController(Model model, Principal user, @PathVariable long id) {
        //pass currentpage for nav highlighting
        model.addAttribute("currentpage", "create");

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

        return "editteras";
    }
}
