package com.iriskopecky.agon.controllers.app;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.services.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@Controller
public class CreateAccountController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public CreateAccountController(AccountService accountService, AuthenticationManager authenticationManager) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Provide the registration page.
     * @param user The user signed in, null if nobody is signed in.
     * @return The template/page to provide.
     */
    @GetMapping("/register")
    public String registerPage(Principal user) {
        //ensure user is not already logged in
        if (user != null) {
            return "redirect:/browse";
        }

        return "register";
    }

    /**
     * Handle the account creation post request
     * @param user The user currently logged in, null if none.
     * @param requestBody The post request body.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @return The template to return to.
     */
    @PostMapping("/register")
    @ResponseBody
    public Map<String, String> registerAttempt(Principal user, @Valid @RequestBody RegisterRequest requestBody,
                                        HttpServletRequest request, HttpServletResponse response) {
        //ensure user is not already logged in
        if (user != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Signed in users cannot register new accounts.");
        }

        //create the account
        Account account = accountService.createAccount(requestBody.username(), requestBody.password());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Failed to create account. The username likely already exists.");
        }

        //authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        requestBody.username(), requestBody.password()
                )
        );
        request.getSession(true);
        request.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        return Map.of("redirect", "/browse");
    }


    public record RegisterRequest(
            @NotBlank
            @Size(min = 2, max = 32)
            String username,
            @NotBlank
            @Size(min = 8, max = 128)
            String password) {};

}
