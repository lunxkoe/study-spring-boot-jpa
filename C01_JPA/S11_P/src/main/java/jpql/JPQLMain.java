package jpql;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class JPQLMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("memberA");
            member.setAge(10);
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();

            List<Member> result = em.createQuery("select m, t from Member m left join Team t on m.username = t.name", Member.class)
                    .getResultList();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 객체지향 쿼리 언어1 - 기본 문법

// 소개
/*
> JPQL 소개
- 가장 단순한 조회 방법
    - EntityManager.find()
    - 객체 그래프 탐색 a.getB().getC()

> JPQL
- JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색 쿼리
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색

> JPA와 다른 걸(SpringJdbcTemplate, MyBatis 등) 함께 사용할 때 주의점
- 영속성 컨텍스트를 적절하게 플러시 해주어야함
- 영속성 컨텍스트랑 관련이 없기 때문에 강제로 DB에 데이터를 반영해주어야함!!
*/

// 기본 문법과 쿼리 API
/*
> JPQL 특징
- 엔티티 객체를 대상으로 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않음
- 결국 SQL로 변환됨

> JPQL 문법
```sql
select
    select
    from
    where
    group by
    having
    order by

update_문
delete_문
```
- 엔티티와 속성은 대소문자 구분 O (Member, age)
- JPQL 키워드는 대소문자 구분 X
- 엔티티 이름 사용, 테이블 이름이 아님 (Order O / Orders X)
- 별칭은 필수(m), as 생략 가능

> 집합과 정렬
```sql
select
    count(m),
    sum(m.age),
    avg(m.age),
    max(m.age),
    min(m.age),
from Member m;
```

> TypeQuery, Query
- TypeQuery: 반환 타입이 명확할 때 사용
- Query: 반환 타입이 명확하지 않을 때 사용
```java
TypeQuery<Member> query =
    em.createQuery("select m from Member m", Member.class)

Query query =
    em.createQuery("select m.username, m.age from Member m);
```

> 결과 조회
- query.getResultList(): 결과가 하나 이상일 때 리스트 반환
    - 결과가 없으면 빈 리스트 반환(NullPointException 주의 X)

- query.getSingleResult(): 결과가 정확히 하나, 단일 객체 반환
    - 결과가 없으면 NoResultException
    - 둘 이상이면 NonUniqueResultException
    - 진짜 결과가 하나일 때 사용해야함!!

> 파라미터 바인딩 - 이름 기준, 위치 기준 (이름으로 사용하자!!!!)
```java
em.createQuery("select m from Member m where m.username = :username");
query.setParameter("username", usernameParam);

em.createQuery("select m from Member m where m.username = ?1");
query.setParameter(1, usernameParam);
```
*/

// 프로젝션
/*
> 프로젝션
- select 절에 조회할 대상을 지정하는 것
- 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)
```sql
select m from Member m -- 엔티티 프로젝션
select m.team from Member m -- 엔티티 프로젝션
select m.address from Member m -- 임베디드 타입 프로젝션
select m.username, m.age from Member m -- 스칼라 타입 프로젝션
```
- distinct로 중복 제거 가능

> 엔티티 프로젝션
- 조회되는 모든 엔티티는 영속성 컨텍스트에 올라옴
- team 조회 시 조인
```java
List<Team> result = em.createQuery("select m.team from Member m", Team.class)
        .getResultList();
// - 이렇게 사용하는 것은 좋지 않음(묵시적 조인)
// - 명시적으로 조인을 명시하는 것이 좋음(가독성, 유지보수성)
```

> 임베디드 타입
```java
List<Address> result = em.createQuery("select o.address from Order o", Address.class)
        .getResultList();
```

> 스칼라 타입 프로젝션
```java
em.createQuery("select distinct m.username, m.age from Member m")
        .getResultList();
```

> 프로젝션 - 여러 값 조회
- select m.username, m.age from Member m
    - Query 타입으로 조회
    - Object[] 타입으로 조회
    - new 명령어로 조회
        - 단순 값을 DTO로 바로 조회
        - select new jpabook.jpql.UserDto(m.username, m.age) from Member m
        - 패키지 명을 포함하여 전체 클래스 명 입력
        - 순서와 타입이 일칳는 생성자 필요

```java
List result = em.createQuery("select distinct m.username, m.age from Member m")
        .getResultList();

List<Object[]> result = em.createQuery("select distinct m.username, m.age from Member m")
        .getResultList();

for (Object o : result) {
    System.out.println("o = " + (Object[])o[0]); // username
    System.out.println("o = " + (Object[])o[1]); // age
}
```

- DTO로 바로 조회
```java
List<MemberDto> resultList = em.createQuery("select new jpql.MemberDto(m.username, m.age) from Member m", MemberDto.class)
        .getResultList();
```
*/

// 페이징
/*
> 페이징 API
- JPA는 페이징을 다음 두 API로 추상화
- setFirstResult(int startPosition): 조회 시작 위치(0부터 시작)
- setMaxResults(int maxResult): 조회할 데이터 수

> 예시
```java
for (int i = 0; i < 10; i++) {
    Member member = new Member();
    member.setUsername("member" + i);
    member.setAge(i);
    em.persist(member);
}

List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
        .setFirstResult(1) // 0부터 시작
        .setMaxResults(10) // 가져올 최대 개수
        .getResultList();

System.out.println("result.size() = " + result.size()); // 9개를 가져옴
for (Member member : result) {
    System.out.println("member = " + member);
}
```
*/

// 조인
/*
> 조인
- 내부 조인
    - select m from Member m join m.team t

- 외부 조인
    - select m from Member m left join m.team t

- 세타 조인(cross)
    - select count(m) Member m, Team t where m.username = t.name

> 내부 조인
```java
Team team = new Team();
team.setName("teamA");
em.persist(team);

Member member = new Member();
member.setUsername("memberA");
member.setAge(10);
member.setTeam(team);
em.persist(member);

em.flush();
em.clear();

List<Member> result = em.createQuery("select m from Member m join m.team", Member.class)
        .getResultList();
```
- 함정: Member의 Team에 FetchType.LAZY를 하지 않을 경우
    - 조인을 해서 Member만 가져오는 것이기 때문에 추가로 Team에 대한 select 쿼리가 나감
    - 착각할 수 있는게, 원래는 Member를 가져올 때, 조인해서 일반적으로 가져오지만
    - 현재는 조인을 해서 Member를 가져온 후
    - Eager에 의해서 Team을 초기화 시키기 위해서 select으로 가져오는 것(마치 Member를 조회한(LAZY로) 후 Team에 접근을 할 시 생기는 것과 동일한 효과)
    - 외부 조인도 동일, 세타 조인도 동일

> 조인 - on절
- 조인 대상 필터링
- 연관관계 없는 엔티티 외부 조인 가능(하이버네이트 5.1부터 가능)

> 조인 대상 필터링
- 회원과 팀을 조인하면서 팀 이름이 A인 팀만 조인
```sql
select m, t from Member m left join m.team t on t.name = 'A'
```

> 연관관게 없는 엔티티 외부 조인 가능
- 회원의 이름과 팀의 이름이 같은 대상 외부 조인
```sql
select m, t from Member m left join Team t on m.username = t.name
```

- 이거 다 Eager로 되어 있으면 추가 쿼리 발생함
- 그렇다고 fetch만 붙이면 안됨
    - fetch는 진짜 연관된 객체들을 가져오는 것
    - 필터링 + left 조인은 더욱이 null이 들어올 수 있음(Team에)
*/

// 서브 쿼리
/*
> 서브 쿼리
```sql
select m from Member m where m.age > (select avg(m2.age) from Member m2)
```

> 서브 쿼리 지원 함수
- [NOT] EXISTS: 서브쿼리에 결과가 존재하면 참
    - ALL / ANY / SOME
    - ALL 모두 만족하면 참
    - ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] in: 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

> JPA 서브 쿼리 한계
- JPQ는 where, having 절에서만 서브 쿼리 사용 가능
- select 절도 가능 (하이버네이트에서 지원)
- from 절의 서브 쿼리는 현재 JPQL에서 불가능
    - 조인으로 풀 수 있으면 풀어서 해결
    - 안되면 쿼리 두 번 날리기
    - 안되면 Native
*/

// JPQL 타입 표현과 기타식
/*
> 타입 표현
- 문자: 'HELLO'
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (패키지명 포함)
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)
    - select i from Item i where type(i) = Book
*/

// 조건식(CASE 등등)
/*
> 조건식 - CASE 식
- 기본 CASE 식
```sql
select
    case
        when m.age <= 10 then '학생요금'
        when m.age >= 60 the '경로 요금'
        else '일반 요금
    end
from Member m
```

- 단순 CASE 식
```sql
select
    case t.name
        when '팀A' then '인센티브100%'
        when '팀B' then '인센티브120%'
        else '인센티브105%'
    end
from Team t
```
- COALESCE: 하나씩 조회해서 null이 아니면 반환
```sql
select coalesce(m.username, '이름 없는 회원') from Member m
```
- NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 반환
```sql
select NULLIF(m.username, '관리자') from Member m
```
*/

// JPQL 함수
/*
> 기본 함수
- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE, INDEX(JPA 용도)

> 사용자 정의 함수 호출
- 하이버네이트는 사용전 방언에 추가해야함
    - 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록
    ```sql
    select function('group_concat', i.name) from Item i
    ```

```java
public class MyH2Dialect extends H2Dialect {

    public MyH2Dialect() {
        registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}

<!--            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>-->
            <property name="hibernate.dialect" value="dialect.MyH2Dialect"/>
```
    - 근데 하이버네이트 5에서만 됨 / 6은 안됨
*/
