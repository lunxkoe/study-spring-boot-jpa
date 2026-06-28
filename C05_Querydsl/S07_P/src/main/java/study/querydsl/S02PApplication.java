package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class S02PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S02PApplication.class, args);
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}

// 사용자 정의 리포지토리
/*
> MemberRepository
    - MemberRepository + Impl은 무조건 맞춰줘야함

> 특화된 기능이라면?
- 모든 걸 다 Custom에 때려박을 필요가 없음
- 아예 따로 분리 후 주입받아서 사용하면 됨
*/

// 스프링 데이터 페이지 활용 1 - Querydsl 페이징 연동
/*
> 스프링 데이터 페이징 활용
- Page, Pageable 활용
- 전체 카운트를 한 번에 조회하는 단순한 방법
- 데이터 내용과 전체 카운트를 별도로 조회하는 방법

> 데이터 내용과 전체 카운트를 별도로 조회
```java
@Override
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageLoe(condition.getAgeLoe()),
                    ageGoe(condition.getAgeGoe())
            )
            .fetch();

    JPAQuery<Long> countQuery = queryFactory
            .select(member.count()) // count(member.id)와 동일
            .from(member)
            // 주의: 검색 조건에 team.name이 있으므로 leftJoin이 유지되어야 함
            // 만약 검색 조건에 team이 없다면 leftJoin을 지워서 성능을 높일 수 있음
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageLoe(condition.getAgeLoe()),
                    ageGoe(condition.getAgeGoe())
            );

    // PageableExecutionUtils를 사용해 조합
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
}
```
- 따로 하면 생기는 이점
    - 상황에 따라서 조인을 하지 않아도 카운트 수에 영향이 없을 수 있음 (최적화 가능)
*/

// CountQuery 최적화
/*
> 카운트쿼리가 생략 가능할 수도 있음
    - 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
    - 마지막 페이지일 때(offset + 컨텐츠 사이즈를 더해서 전체 사이즈를 구함)
    ```java
    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    ```

> PageableExecutionUtils가 위의 계산을 알아서 해줌
    - 여기서 countQuery를 날려야하면 날리고, 안 필요하면 안 날림
*/

// 컨트롤러 개발
/*
> 스프링 데이터 정렬(Sort)
- 스프링 데이터 JPA는 자신의 정렬을 Querydsl의 정렬(OrderSpecifier)로 편리하게 변경하는 기능을 제공함
- 참고
    - 정렬은 조건이 조금만 복잡해져도, Pageable의 Sort 기능을 사용하기 어려움
    - 루트 엔티티 범위를 넘어가는 동적 정렬 기능이 필요하면 스프링 데이터 페이징이 제공하는 Sort를 사용하기 보다는
    - 파라미터를 받아서 직접 처리하는 것을 권장함
*/