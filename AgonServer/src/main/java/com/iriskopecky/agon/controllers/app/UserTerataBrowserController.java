package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Controller
public class UserTerataBrowserController {

    private AccountRepository accountRepository;
    public UserTerataBrowserController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/user/terata")
    public String userTerata(Model model, Principal user) {
        //pass currentpage for nav highlighting
        model.addAttribute("currentpage", "userterata");

        //get the user and add their info
        Account account = accountRepository.findAccountByUsername(user.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Account not available."));
        model.addAttribute("username", account.getUsername());

        List<Teras> ownedTerata = account.getTerata();
        model.addAttribute("terata", ownedTerata);

        return "userterata";
    }

}
