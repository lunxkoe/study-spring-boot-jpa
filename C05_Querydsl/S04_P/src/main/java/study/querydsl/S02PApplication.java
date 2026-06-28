package study.querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S02PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S02PApplication.class, args);
    }

}

// 시작 - JPQL vs Querydsl
/*
> 파라미터 바인딩
- Querydsl은 파라미터 바인딩(pstmt)를 사용하여 SQL Injection으로부터 안전함

> Querydsl vs JPQL
- JPQL에 있는 문법 오류는 컴파일이 아닌 실행 시점에 오류를 잡을 수 있음
- Querydsl에 있는 문법 오류는 컴파일 시점에 오류를 잡을 수 있음

> 추가내용
- JPAQueryFactory를 필드로 빼놔도 동시성 문제가 없게 설계되어있음
- em도 멀티스레드 자체에 문제가 없게 설계가 되어있음
*/

// 기본 - Q-Type 활용
/*
> 사용 방법
- new QMember("m"): m == 별칭
- QMember.member
- member (static import): 권장(깔끔함) / default 별칭이 있음(member1 같은)

> 오해하면 안되는 것
- Querydsl은 결국 JPQL의 빌더 역할을 하는 것
- 따라서 실제 실행은 JPQL로 됨
- use_sql_comments: true # Querydsl로 작성된 JPQL 쿼리 확인용

> 수동 별칭 사용 상황
- 같은 테이블을 조인하거나 같은 동일한 테이블을 구분해야하는 상황에서는 별칭을 사용
- 그 외에는 별칭은 크게 상관없음(default 별칭 사용)
*/

// 검색 조건 쿼리
/*
> selectFrom
- .select(member).from(member) -> .selectFrom(member)

> and
- .where(member.username.eq("member1").and(member.age.eq(10)))
- .where(member.username.eq("member1"),member.age.eq(10)): ,로 하면 and로 연결됨(선호)
    - 선호 이유: 중간에 null이 들어가면 조건을 무시함(깔끔하게 코드를 작성할 수 있음)

> 무수하게 많은 검색 조건 쿼리가 존재함
- eq()
- ne()
- eq().not()

- isNotNull()

- in(10, 20)
- notIn(10, 20)
- between(10, 30)

- goe(): >=
- gt(): >
- loe(): <=
- lt(): <

- like()
- contains(): like '%string%'
- startsWith(): like 'string%'
*/

// 결과 조회
/*
> 결과 조회
- fetch(): 리스트 조회, 데이터 없으면 **빈 리스트 반환**
- fetchOne(): 단 건 조회
    - 결과가 없으면 **null**
    - 결과가 둘 이상이면 **com.querydsl.core.NonUniqueResultException**
- fetchFirst(): limit(1).fetchOne()
- fetchResults(): 페이징 정보 포함, total count 쿼리 추가 실행
    - 카운트 쿼리(getTotal())
        - 카운트 쿼리를 가져올 때, count(member0_.member_id)로 가져옴
    - 전체 조회 쿼리(getResult())
- fetchCount(): count 쿼리로 변경해서 count 수만 조회

> 페이징 주의사항
- 복잡한 상황에서는 카운트 쿼리를 같이 날려서 페이징을 처리하도록 하면 안됨
- 따로 카운트 쿼리를 날리도록 구성해야함
- **fetchResults()는 현재 deprecated된 상태. 따라서 어차피 따로 구현해야함**
*/

// 정렬
/*
> 기본 사용
- .orderBy(정렬조건1, 정렬조건2, ...)
- username.asc(): 오름차순
- username.desc(): 내림차순
- nullsLast(): null인 경우 가장 마지막에 출력

> 정렬 쿼리와 실제 쿼리 비교
- Querydsl 정렬 쿼리
```java
List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(100))
        .orderBy(member.age.desc(), member.username.asc().nullsLast())
        .fetch();
```

- 실제 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username
from
    member m1_0
where
    m1_0.age=?
order by
    m1_0.age desc,
    m1_0.username asc nulls last
```
*/

// 페이징
/*
> 기본 사용 - 페이징은 기본적으로 항상 orderBy와 함께 사용해야함
- .offset(num): 0부터 시작, num+1번째 데이터 조회를 의미
- .limit(num): 가져올 개수를 제한

> 페이징 쿼리와 실제 쿼리 비교
- Querydsl 페이징 쿼리
```java
List<Member> result = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)
        .limit(2)
        .fetch();
```

- JPQL이 작성해준 쿼리(JPQL은 직접 페이징 쿼리를 넣지 않음, 외부에서 넣어줌)
```sql
select
    member1
from
    Member member1
order by
    member1.username desc
```

- 실제 쿼리(페이징은 디비 방언에 따라서 다른 값이 들어갈 수 있음)
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username
from
    member m1_0
order by
    m1_0.username desc
offset
    ? rows
fetch
    first ? rows only -- 현재 최신 H2는 fetch를 사용해서 limit을 대체함
```

> 참고사항
- 페이징은 조회용 쿼리 하나 / 카운트 쿼리 하나 분리해서 많이 짬
- 조건이 붙기 시작하면 조인하고 그러면 굉장히 복잡하고, 성능이슈 발생 -> 따로 짜자!!
- 그래서 fetchResults()도 deprecated됨
*/

// 집합
/*
> 기본 사용법
- select 절에 사용
    - member.count()
    - member.age.sum()
    - member.age.avg()
    - member.age.max()
    - member.age.min()
- 반환 타입: Tuple(Querydsl의)

> Tuple
- 반환 타입이 여러 개 들어올 때 사용
- 사용 방법
```java
List<Tuple> result = queryFactory
        .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
        )
        .from(member)
        .fetch();

Tuple tuple = result.get(0);
assertThat(tuple.get(member.count())).isEqualTo(4);
assertThat(tuple.get(member.age.sum())).isEqualTo(100);
assertThat(tuple.get(member.age.avg())).isEqualTo(25);
assertThat(tuple.get(member.age.max())).isEqualTo(40);
assertThat(tuple.get(member.age.min())).isEqualTo(10);
```

> Group By
- 기본적인 사용법
    - .groupBy(item.price)
    - .having(item.price.gt(1000))
```java
List<Tuple> result = queryFactory
        .select(
                team.name,
                member.age.avg()
        )
        .from(member)
        .join(member.team, team)
        .groupBy(team.name)
        .fetch();
```

- 실제 쿼리
```sql
select
    t1_0.name,
    avg(cast(m1_0.age as float(53)))
from
    member m1_0
join
    team t1_0
        on t1_0.team_id=m1_0.team_id
group by
    t1_0.name
```
*/

// 조인 - 기본 조인
/*
> 기본 조인
- 첫 번째 파라미터에 조인 대상을 지정
- 두 번째 파라미터에 별칭으로 사용할 Q타입을 지정

> 기본 조인 쿼리와 실제 쿼리 비교
- Querydsl 기본 조인 쿼리
```java
List<Member> result = queryFactory
        .selectFrom(member)
        .join(member.team, team)
        .where(team.name.eq("teamA"))
        .fetch();
```

- JPQL 기본 조인 쿼리
```sql
select
    member1
from
    Member member1
inner join
    member1.team as team
where
    team.name = ?1
```

- 실제 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username
from
    member m1_0
join
    team t1_0
        on t1_0.team_id=m1_0.team_id
where
    t1_0.name=?
```

> 조인 종류
- join(innerJoin)
- outerJoin
- leftJoin
- rightJoin

> 연관관계가 없어도 조인할 수 있음(theta_join)
- 코드
```java
List<Member> result = queryFactory
        .select(member)
        .from(member, team)
        .where(member.username.eq(team.name))
        .fetch();
```
- JPQL
```sql
select
    member1
from
    Member member1,
    Team team
where
    member1.username = team.name
```

- 실제 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username
from
    member m1_0,
    team t1_0
where
    m1_0.username=t1_0.name
```

> theta_join 주의사항
- from 절에 여러 엔티티를 선택해서 세타 조인
- 외부 조인 불가능!! -> 다음에 설명할 조인 on을 사용하면 외부 조인 가능
*/

// 조인 - on절
/*
> ON 절을 활용한 조인(JPA 2.1부터 지원)
- 조인 대상 필터링
- 연관관계 없는 엔티티 외부 조인

> 조인 대상 필터링
- Querydsl 조인 대상 필터링 쿼리
```java
List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(member.team, team)
        .on(team.name.eq("teamA"))
        .fetch();
```

- JPQL
```sql
select
    member1,
    team
from
    Member member1
left join
    member1.team as team with team.name = ?1 -- 실제로 on 대신 with으로 들어감
```

- 실제 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username,
    t1_0.team_id,
    t1_0.name
from
    member m1_0
left join
    team t1_0
        on t1_0.team_id=m1_0.team_id
        and t1_0.name=?
```

- 간략한 내부 조인 vs 외부 조인
    - 내부 조인: 조인 대상이 없으면 가져오지 않음
    - 외부 조인: 조인 대상이 없으면 null로 가져옴

> 내부 조인을 사용한다면?
- join(member.team, team).where(team.name.eq("teamA")) == join(member.team, team).on(team.name.eq("teamA"))
- on절을 활용한 조인 대상 필터링을 사용할 때
- 내부 조인일 경우, 익숙한 where절로 해결하고
- 외부 조인일 경우, 필요한 경우에 on절을 활용하자!!

> 연관관게가 없는 엔티티 외부 조인
- Querydsl
```java
List<Tuple> result = queryFactory
        .select(member, team)
        .from(member)
        .leftJoin(team)
        .on(member.username.eq(team.name))
        .where(member.username.eq(team.name))
        .fetch();
```
- **주의사항**: leftJoin 시(member.team, team)이 아닌 (team)
    - 아무런 연관관계를 고려하지 않는 것이지 때문!!
    - 세부 동작 원리
        - 기본적으로는 (member.team, team) => id로 조인
        - (team)으로 하면 on절의 조건으로 조인!!

- JPQL
```sql
select
    member1,
    team
from
    Member member1
left join
    Team team with member1.username = team.name
```

- 실제 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    m1_0.team_id,
    m1_0.username,
    t1_0.team_id,
    t1_0.name
from
    member m1_0
left join
    team t1_0
        on m1_0.username=t1_0.name -- id가 아닌 on절의 조건으로 조인!!
```

- 결과
    - 외부 조인이므로 조건이 만족하지 않는 조인 대상의 경우 null로 가져옴

> 일반 조인 vs on 조인
- 일반 조인: leftJoin(member.team, team): 연관관계가 있는 조인
- on 조인: leftJoin(team): 연관관계가 없는 조인

> LEFT JOIN 사용 시, ON절에 조건을 주는 것과 WHERE절에 조건을 주는 것의 차이
- on절은 조인 대상을 먼저 필터링하고, where절은 조인 완료 후 결과를 필터링함
*/

// 조인 - 페치 조인
/*
> 페치 조인
- SQL에서 제공하는 기능이 아님
- SQL 조인을 활용해서 연관된 엔티티를 SQL 한 번에 조회하는 기능
- 주로 성능 최적화에 사용하는 방법

> EntityManagerFactory
```java
@PersistenceUnit
EntityManagerFactory emf;

boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
```
- 로딩이 되었는지 안되었는지 boolean으로 반환

> 기본 사용법
```java

```

- JPQL
```sql
select
    member1
from
    Member member1
inner join

fetch
    member1.team as team
where
    member1.username = ?1
```

- 실제 SQL 쿼리
```sql
select
    m1_0.member_id,
    m1_0.age,
    t1_0.team_id,
    t1_0.name,
    m1_0.username
from
    member m1_0
join
    team t1_0
        on t1_0.team_id=m1_0.team_id
where
    m1_0.username=?
```

> 그냥 조인해서 가져오는 거랑 무슨 차이가 있을까?
- 기본적으로 그냥 조인은 member만 조회를 함
- select절에 team을 넣으면 tuple로 반환함
- fetchJoin()을 사용하면 Member 안에 넣어줌!!!
*/

// 서브 쿼리
/*
> 기본 사용법 - JPAExpressions (static import 가능)
```java
QMember memberSub = new QMember("memberSub");

List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
                JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
        ))
        .fetch();

assertThat(result).extracting("age")
        .containsExactly(40);
```

- 실제 SQL
```sql
QMember memberSub = new QMember("memberSub");

List<Member> result = queryFactory
        .selectFrom(member)
        .where(member.age.eq(
                JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
        ))
        .fetch();

assertThat(result).extracting("age")
        .containsExactly(40);
```

- 더 정확한 것은 Test 코드 참고

> from 절의 서브쿼리 한계
- JPA JPQL 서브쿼리의 한계점으로 from절의 서브쿼리(인라인 뷰)는 지원하지 않음
- 당연히 Querydsl도 지원하지 않음
- 하이버네이트 구현체를 사용하면 select 절의 서브 쿼리는 지원함
    - JPA 표준 스펙에서는 안됨
- Querydsl도 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원함

> from 절의 서브쿼리 해결방안 - 고급 내용(Hibernate 6 엔진 자체는 지원한다고는 함)
- 서브쿼리를 join으로 변경 (가능한 상황도 있고, 불가능한 상황도 있음)
- 애플리케이션에서 쿼리를 2번 분리해서 실행
- 최종: nativeSQL을 사용
*/

// Case 문
/*
> 기본적인 사용법
```java
@Test
void basicCase() {
    List<String> result = queryFactory
            .select(
                    member.age
                            .when(10).then("열살")
                            .when(20).then("스무살")
                            .otherwise("기타")
            )
            .from(member)
            .fetch();

    for (String s : result) {
        System.out.println("s = " + s);
    }
}

@Test
void complexCase() {
    List<String> result = queryFactory
            .select(new CaseBuilder()
                    .when(member.age.between(0, 20)).then("0~20살")
                    .when(member.age.between(21, 30)).then("21살~30살")
                    .otherwise("기타")
            )
            .from(member)
            .fetch();

    for (String s : result) {
        System.out.println("s = " + s);
    }
}
```
- 이것을 사용해야할까?
    - DB에서는 가능하면 뭔가 작업을 하는 것은 좋지 않음
    - 애플리케이션에서 처리하는 것을 지향
*/

// 상수, 문자 더하기
/*
> 고정된 상수 추가
- .select(member.username, Expressions.constant("A"))

> 문자 더하기
- .select(member.username.concat("_").concat(member.age.stringValue()))
    - member.age는 문자가 아니여서 concat이 안됨
    - stringValue()로 문자로 바꾸어서 처리(Casting)

> 핵심
- .stringValue()
- 문자가 아닌 다른 타입들을 문자로 변환할 수 있음
- 특히 나중에 enum 값을 다룰 때 자주 사용함
*/