package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {

    @Value("#{target.username + ' ' + target.age}")
    // - 퍼올리고 해당 username만 반환함
    String getUsername();
    // - 반드시 get...여야함
}
