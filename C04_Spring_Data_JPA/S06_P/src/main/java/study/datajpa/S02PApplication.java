package study.datajpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@EnableJpaAuditing
public class S02PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S02PApplication.class, args);
    }

    // 생성자 / 수정자 넣어주기 위한 코드
    @Bean
    public AuditorAware<String> auditorProvider() {
        // 실제로는 Security에서 꺼내서 넣어야함
        return () -> Optional.of(UUID.randomUUID().toString());
    }
}
