package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class PublicTerataBrowserController {

    private AccountRepository accountRepository;
    private TerasRepository terasRepository;
    public PublicTerataBrowserController(AccountRepository accountRepository, TerasRepository terasRepository) {
        this.accountRepository = accountRepository;
        this.terasRepository = terasRepository;
    }

    @GetMapping("/browse")
    public String browseTerata(Model model, Principal user, @RequestParam(name="sortingmethod", required=false) String sortingMethod) {
        //pass currentpage for nav highlighting
        model.addAttribute("currentpage", "browse");

        //get the user and add their info
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));
        model.addAttribute("username", account.getUsername());

        //get all public-facing terata
        List<Teras> terata = terasRepository.findAllByPublicFacing(true);
        if (sortingMethod != null && sortingMethod.equals("creation")) {
            terata.sort(new Teras.TerasCreationComparator());
        } else if (sortingMethod != null && sortingMethod.equals("alphabetical")) {
            terata.sort(new Teras.TerasAlphabeticalComparator());
        } else if (sortingMethod != null && sortingMethod.equals("leaderboard-CAT1")) {
            //TODO implement, with regards to various leaderboard categories.
        }
        model.addAttribute("terata", terata);

        return "browse";
    }
}
