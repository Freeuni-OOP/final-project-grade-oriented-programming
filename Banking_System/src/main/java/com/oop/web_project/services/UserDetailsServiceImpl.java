package com.oop.web_project.services;

import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Role;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerService customerService;

    public UserDetailsServiceImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerService.getCustomerByEmail(email);

        return new User(
                customer.getEmail(),
                customer.getHashedPassword(),
                getGrantedAuthority(customer.getRole())
        );
    }

    /**
     * Method creates Collection<GrantedAuthority> instance
     * to pass onto UserDetails implementation
     * @param role role of the user
     * @return list of granted roles for the user
     */
    private Collection<GrantedAuthority> getGrantedAuthority(Role role) {

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.name());

        System.out.println("granted authority: " + grantedAuthority.getAuthority());

        return List.of(grantedAuthority);
    }

}
