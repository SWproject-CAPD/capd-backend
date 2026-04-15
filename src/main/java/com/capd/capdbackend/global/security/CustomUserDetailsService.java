package com.capd.capdbackend.global.security;

import com.capd.capdbackend.domain.user.entity.UserEntity;
import com.capd.capdbackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserEntity user;

        // 넘어온 값이 이메일인지(환자), 면허번호인지(의사) 구분
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("환자를 찾을 수 없습니다: " + identifier));
        } else {
            user = userRepository.findByLicenseId(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("의사를 찾을 수 없습니다: " + identifier));
        }

        return new CustomUserDetails(user);
    }

}
