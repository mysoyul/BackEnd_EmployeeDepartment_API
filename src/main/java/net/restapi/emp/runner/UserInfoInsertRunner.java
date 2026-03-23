package net.restapi.emp.runner;

import lombok.extern.slf4j.Slf4j;
import net.restapi.emp.security.userinfo.UserInfo;
import net.restapi.emp.security.userinfo.UserInfoRepository;
import net.restapi.emp.security.userinfo.UserInfoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Profile("local")
@Slf4j
public class UserInfoInsertRunner implements ApplicationRunner {

    @Autowired
    private UserInfoUserDetailsService userInfoUserDetailsService;

    @Autowired
    private UserInfoRepository userInfoRepository;

    // ┌──────────────────────────┬───────────────┬────────────────────┐
    // │ email                    │ 평문 비밀번호  │ roles              │
    // ├──────────────────────────┼───────────────┼────────────────────┤
    // │ admin@company.com        │ password      │ ROLE_ADMIN         │
    // │ user1@company.com        │ password      │ ROLE_USER          │
    // │ user2@company.com        │ password      │ ROLE_USER          │
    // │ manager@company.com      │ password      │ ROLE_ADMIN,ROLE_USER│
    // └──────────────────────────┴───────────────┴────────────────────┘
    @Override
    public void run(ApplicationArguments args) throws Exception {
        insertIfAbsent("Admin",    "admin@company.com",   "password", "ROLE_ADMIN");
        insertIfAbsent("User One", "user1@company.com",   "password", "ROLE_USER");
        insertIfAbsent("User Two", "user2@company.com",   "password", "ROLE_USER");
        insertIfAbsent("Manager",  "manager@company.com", "password", "ROLE_ADMIN,ROLE_USER");
    }

    private void insertIfAbsent(String name, String email, String rawPassword, String roles) {
        if (userInfoRepository.findByEmail(email).isPresent()) {
            log.info("UserInfo 이미 존재, 건너뜀: {}", email);
            return;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setName(name);
        userInfo.setEmail(email);
        userInfo.setPassword(rawPassword);   // addUser() 내부에서 BCrypt 인코딩
        userInfo.setRoles(roles);
        userInfoUserDetailsService.addUser(userInfo);
        log.info("UserInfo 삽입 완료: {} [{}]", email, roles);
    }
}
