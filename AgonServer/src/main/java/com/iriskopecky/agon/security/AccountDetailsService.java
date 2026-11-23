package com.iriskopecky.agon.security;

import com.iriskopecky.agon.data.Account;
import com.iriskopecky.agon.data.AccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountDetailsService implements UserDetailsService {

    private final AccountRepository repository;

    public AccountDetailsService(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<Account> oAccount = repository.findAccountByUsername(username);
        if (oAccount.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }

        Account account = oAccount.get();

        return User.builder()
                .username(account.getUsername())
                .password(account.getHash())
                .roles("USER").build();
    }
}
