package com.chutneytesting.security.infra.memory;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.domain.AuthenticationService;
import com.chutneytesting.security.domain.Authorization;
import com.chutneytesting.security.domain.Role;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserDto> users;
    private final AuthenticationService authenticationService;

    public InMemoryUserDetailsService(InMemoryUsersProperties inMemoryUsersProperties, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        users = inMemoryUsersProperties.getUsers().stream()
            .collect(toUnmodifiableMap(UserDto::getUsername, identity()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDto> user = Optional.ofNullable(users.get(username));
        return user
            .map(this::readRole)
            .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
    }

    private UserDto readRole(UserDto userDto) {
        UserDto dto = new UserDto(userDto);

        if (dto.getRoles().contains("ADMIN")) {
            dto.grantAuthority(Authorization.ADMIN_ACCESS.name());
        }

        Role role = authenticationService.userRoleById(dto.getId());
        dto.addRole(role.name);
        role.authorizations.stream().map(Enum::name).forEach(dto::grantAuthority);

        return dto;
    }
}
