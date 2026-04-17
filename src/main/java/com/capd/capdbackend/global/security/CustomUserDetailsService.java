package com.capd.capdbackend.global.security;

import com.capd.capdbackend.domain.doctor.entity.DoctorEntity;
import com.capd.capdbackend.domain.doctor.repository.DoctorRepository;
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
    private final DoctorRepository doctorRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserEntity user;
        // 넘어온 값이 이메일인지(환자), 면허번호인지(의사) 구분
        if (identifier.contains("@")) {
            // 환자 로그인 (이메일로 UserEntity 바로 찾기)
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("환자를 찾을 수 없습니다: " + identifier));
        } else {
            // 의사 로그인 (면허번호로 DoctorEntity를 찾고, 그 안의 UserEntity를 꺼내기)
            DoctorEntity doctor = doctorRepository.findByLicenseId(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("의사를 찾을 수 없습니다: " + identifier));

            user = doctor.getUser(); // 연결된 UserEntity 추출
        }

        // 찾아낸 통일된 UserEntity와 로그인 식별자를 넘겨서 인증 객체 생성
        return new CustomUserDetails(user, identifier);
    }

}
