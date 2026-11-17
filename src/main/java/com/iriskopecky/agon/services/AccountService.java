package com.iriskopecky.agon.services;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository repository;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    /**
     * Create an account.
     * @param username The username of the new account. Expected to already be validated.
     * @param password The password of the new account. Expected to already be validated.
     * @return The account entity created, or null if not created.
     */
    public Account createAccount(String username, String password) {
        //ensure we do not create a duplicate account
        if (repository.findAccountByUsername(username).isPresent()) {
            return null;
        }

        //hash the password
        String hash = encoder.encode(password);

        //create the account
        Account account = new Account(username, hash);

        //save it
        repository.save(account);

        return account;
    }
}
