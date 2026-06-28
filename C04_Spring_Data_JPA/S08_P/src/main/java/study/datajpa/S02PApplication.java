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

// Specifications (명세)
/*
> 동적 쿼리 작성에 사용
- 쓰지 말고, Querydsl이라는 기술을 활용하자!!
*/

// Query By Example
/*
> 장점
- 동적 쿼리를 편하게 처리
- 도메인 객체를 그대로 사용
- 데이터 저장소를 RDB에서 NOSQL로 변경해도 코드 변경이 없게 추상화 되어있음
    - spring.data 쪽
- 스프링 데이터 JPA의 JpaRepository 인터페이스에 이미 포함

> 단점
- 조인은 가능하지만 내부 조인만 가능함 (외부 조인 안됨)
- 다음과 같은 중첩 제약 조건 안됨
- 매칭 조건이 매우 단순함
    - 문자는 regex
    - 다른 속성은 =만 가능

> 결론
- Querydsl이라는 기술을 사용하자!!
*/

// Projections
/*
> 개념
- 엔티티 대신에 DTO를 편리하게 조회할 때 사용
- 전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
- select 절에 들어갈 데이터

> 결론
- 이런 기능이 있다
- 단순할 때 사용하고, Querydsl를 사용하자!!
*/

// Native Query
/*
> 주의사항
- 가급적 네이티브 쿼리는 사용하지 않는게 좋음

> 한계
- 페이징 지원
- 반환 타입
    - Object[]
    - Tuple
    - DTO
- 제약
    - Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음
    - JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
    - 동적 쿼리 불가

> 권장
- JdbcTemplate or MyBatis 권장
- 또는 Projection 기능과 가지고 사용
*/
