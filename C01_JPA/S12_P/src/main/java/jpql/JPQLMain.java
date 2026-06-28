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
            Team team1 = new Team();
            team1.setName("teamA");
            em.persist(team1);

            Team team2 = new Team();
            team2.setName("teamB");
            em.persist(team2);

            Team team3 = new Team();
            team3.setName("teamC");
            em.persist(team3);

            Member member1 = new Member();
            member1.setUsername("memberA");
            member1.setAge(10);
            member1.setTeam(team1);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("memberB");
            member2.setAge(10);
            member2.setTeam(team1);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("memberC");
            member3.setAge(10);
            member3.setTeam(team2);
            em.persist(member3);

            Member member4 = new Member();
            member4.setUsername("memberD");
            member4.setAge(10);
            em.persist(member4);

            em.flush();
            em.clear();

            int resultCount = em.createQuery("update Member m set m.age = 20")
                    .executeUpdate();

            System.out.println("resultCount = " + resultCount);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 객체지향 쿼리 언어2 - 중급 문법

// 경로 표현식
/*
> 경로 표현식
- .(점)을 찍어 객체 그래프를 탐색하는 것
```sql
select m.username -- 상태 필드
    from Member m
        join m.team t -- 단일 값 연관 필드
        join m.orders o -- 컬렉션 값 연관 필드
where t.name = '팀A';
```

> 경로 표현식 용어 정리
- 상태 필드: 단순히 값을 저장하기 위한 필드
    - m.username
- 연관 필드: 연관관계를 위한 필드
    - 단일 값 연관 필드
        - @ManyToOne, @OneToOne, 대상이 엔티티
    - 컬렉션 값 연관 필드
        - @OneToMany, @ManyToMany, 대상이 컬렉션

> 경로 표현식 특징
- 상태 필드: 경로 탐색의 끝, 탐색 X
- 단일 값 연관 경로: **묵시적 내부 조인 발생**, 탐색 O
- 컬렉션 값 연관 경로: **묵시적 내부 조인 발생**, 탐색 X
    - 그래서 FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
    - **.size를 하면 개수를 반환함**

- 매우 주의: 묵시적 조인은 최대한 지양해라 - 가독성 및 쿼리 튜닝이 어려워짐

> 명시적 조인, 묵시적 조인
- 명시적 조인: 조인 키워드 직접 사용
- 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인이 발생(내부 조인만 가능)
    - select m.team from Member m

> 경로 표현식 예제
- select o.member.team from Order o -- 성공(조인 2번)
- select t.members from Team -- 성공
- select t.members.useranme from Team t -- 실패
- select m.username from Team t join t.members m -- 성공

> 주의사항
- 항상 내부 조인
- 컬렌션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
    - **select m.username from Team t join t.members m -- 성공**
- 경로 탐색은 주로 select, where 절에서 사용하지만 묵시적 조인으로 인해 SQL의 from(join)절에 영향을 줌

> 실무 조언
- 가급적 묵시적 조인 대신에 명시적 조인 사용
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 한 눈에 파악하기 쉽지 않음
*/

// 페치 조인 1 - 기본 (실무에서 정말 중요함)
/*
> 페치 조인(fetch join)
- SQL 조인 종류 X
- JPQL에서 성능 최적화를 위해 제공하는 기능
- 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능
- join fetch 명령어 사용
- [left | inner] join fetch 조인경로

> 엔티티 페치 조인
- 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 번에)
- SQL을 보면 회원 뿐만 아니라 팀도 함께 select
- JPQL
    - select m from Member m join fetch m.team
- SQL
    - select m.*, t.* from member m inner join team t on m.team_id = t.id

> 문제 상황
```java
Team team1 = new Team();
team1.setName("teamA");
em.persist(team1);

Team team2 = new Team();
team2.setName("teamB");
em.persist(team2);

Team team3 = new Team();
team3.setName("teamC");
em.persist(team3);

Member member1 = new Member();
member1.setUsername("memberA");
member1.setAge(10);
member1.setTeam(team1);
em.persist(member1);

Member member2 = new Member();
member2.setUsername("memberB");
member2.setAge(10);
member2.setTeam(team1);
em.persist(member2);

Member member3 = new Member();
member3.setUsername("memberC");
member3.setAge(10);
member3.setTeam(team2);
em.persist(member3);

Member member4 = new Member();
member4.setUsername("memberD");
member4.setAge(10);
em.persist(member4);

em.flush();
em.clear();

String query = "select m from Member m";

List<Member> result = em.createQuery(query, Member.class)
        .getResultList();

for (Member member : result) {
    System.out.println("member = " + member.getUsername());
    System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
    // 회원 조회 쿼리 1개(4명 가져옴) / 팀 조회 쿼리 2개(teamA, teamB)
}
```
- N+1 문제 발생

> 해결 방법
```java
String query = "select m from Member m join fetch m.team";
```
- 결과
    - inner join으로 한 번의 SQL로 가져옴
    - team도 Proxy가 아님(진짜 Team)

> 컬렉션 페치 조인
- 일대다 관계, 컬렉션 페치 조인
- JPQL
    - select t from Team t join fetch t.members where t.name = 'teamA'
- SQL
    - select t.* m.* from team t inner join member m on t.id = m.team_id where t.name = 'teamA'

```java
String query = "select t from Team t join fetch t.members";

List<Team> result = em.createQuery(query, Team.class)
        .getResultList();

for (Team team : result) {
    System.out.println("team.getName() + \" \" + team.getMembers().size() = " + team.getName() + " " + team.getMembers().size());
}
```
- 조인해서 한 번에 데이터를 가져옴(SQL 한 번만 발생)

> 주의사항
- 일대다 조인은 데이터가 뻥튀기 될 수 있음
- 근데 현재 JPA 버전에서는 뻥튀기 시키지 않고 중복을 제거해서 가져옴(강의에서는 뻥튀기되서 가져옴)
- 줄일 수 있는 방법이 있음
- **다대일은 괜찮음**

> 페치 조인과 DISTINCT
- SQL의 DISTINCT는 중복된 결과를 제거하는 명령
    - 하지만 행의 모든 값이 완전히 동일해야 제거함
- JPQL의 DISTINCT 2가지 기능 제공
    - SQL에 DISTINCT를 추가
    - 애플리케이션에서 엔티티 중복 제거
```java
String query = "select distinct t from Team t join fetch t.members";
```

> 페치 조인과 일반 조인의 차이
- 일반 조인 실행 시 연관된 엔티티를 함께 조회하지 않음
- JPQL
    - select t from Team t join t.members m where t.name = 'teamA'
- SQL
    - select t.* from team t inner join member m on t.id = m.team_id where t.name = 'teamA'

- JPQL은 결과를 반환할 때, 연관관계 고려 X
- 단지 select 절에 지정한 엔티티만 조회할 뿐
- 여기서 팀 엔티티만 조회하고 회원 엔티티는 조회 X

- 페치 조인을 사용할 때만 연관된 엔티티도 함께 즉시 조회
- 페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념
*/

// 페치 조인 2 - 한계
/*
> 페치 조인의 특징과 한계
- 페치 조인 대상에는 별칭을 줄 수 없음
    - 하이버네이트는 가능, 가급적 사용 X
    - 연속된 조인이 필요할 경우 사용
    - 별도의 쿼리를 사용하는 것을 추천
- 둘 이상의 컬렉션은 페치 조인할 수 없음
    - 데이터 뻥튀기가 될 수도 있음
- 컬렉션을 페치 조인하면 페이징 API를 사용할 수 없음
    - 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
    - N대다는 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험!!!)
        - 행이 뻥튀기 때문에 가져와야함
    - 해결안
        - 일대다 -> 다대일로 바꿈
        - team만 일단 조회(페이징 가능) + 안에 연관된 객체 접근 시(Lazy 설정으로 인한 N+1 문제를 막기 위해)@BatchSize(size = 100)

> @BatchSize(size = 100)
- 상황
```java
@BatchSize(size = 100)
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();

String query = "select t from Team t";

List<Team> result = em.createQuery(query, Team.class)
        .setFirstResult(0)
        .setMaxResult(2)
        .getResultList();

for (Team team : result) {
    System.out.println("team.getName() + \" \" + team.getMembers().size() = " + team.getName() + " " + team.getMembers().size());
    for (Member member : team.getMembers()) {
        System.out.println("member.getUsername() = " + member.getUsername());
    }
}
```

```sql
select
    t1_0.id,
    t1_0.name
from
    Team t1_0
-- team을 가져옴

Hibernate:
    select
        m1_0.team_id,
        m1_0.id,
        m1_0.age,
        m1_0.username
    from
        Member m1_0
    where
        m1_0.team_id in (?, ?, ?, ?, ?, ?, ?, ?, ?, ...)
-- 위에서 가져옴 team의 id를 size(100)개 만큼 넘김
```

- Global Setting으로 할 수 있음
```yaml
hibernate.default_batch_fetch_size = 1000
```

> 페치 조인의 특징과 한계
- 연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
    - @OneToMany(fetch = FetchType.LAZY)
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용

> 정리
- 모든 것을 페치 조인으로 해결할 수 없음
- 객체 그래프를 유지할 때 사용하면 효과적
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야하면
    - 페치 조인보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 일반적
*/

// 다형성 쿼리
/*
> ITEM (Album / Book / Movie)

> Type
- 조회 대상을 특정 자식으로 한정
- Item 중에 Book, Movie를 조회해라
- JPQL
    - select i from Item i where type(i) in (Book, Movie)
- SQL
    - select i from item i where i.DTYPE in ('B', 'M')

- 부모인 Item과 자식 Book이 있다 - 자식 타입에만 접근할 수 있음
- JPQL
    - select i from Item i where treat(i as Book).author = 'kim'
- SQL
    - select i.* from Item i where i.DTYPE = 'B' and i.author = 'kim'

- 위는 싱글 테이블 전략일 때
    - 전략에 따라 다를 수 있음
*/

// 엔티티 직접 사용
/*
> 엔티티 직접 사용 - 기본 키 값
- JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용
- JPQL
    - select count(m.id) from Member m  // 엔티티의 아이디를 사용
    - select count(m) from Member m     // 엔티티 직접 사용
- SQL (둘 다 같은 SQL을 실행)
    - select count(m.id) as cnt from Member m

> 구체적인 예시
```java
String jpql = "select m from Member m where m = :member";

String jpql = "select m from Member m where m.id = :memberId";
```
- 둘 다 실제 SQL에서는 id를 사용함

> 엔티티 직접 사용 - 외래 키 값
```java
String jpql = "select m from Member m where m.team = :team"

String jpal = "select m from Member m where m.team.id = :teamId"
```
- 이것도 select m.* from Member m where m.team_id = ?를 사용함
*/

// Named 쿼리
/*
> Name 쿼리 - 정적 쿼리
- 미리 정의해서 이름을 부여해두고 사용하는 JPQL
- 정적 쿼리
- 어노테이션, XML에 정의
- 애플리케이션 로딩 시점에 초기화 후 재사용
- 애플리케이션 로딩 시점에 쿼리를 검증(컴파일에서 문법 오류를 잡을 수 있음)!!!

> Name 쿼리 - 어노테이션
```java
-- 선언
@Entity
@NameQuery(
    name = "Member.findByUsername",
    query = "select m from Member m where m.username = :username")
)
public class Member {}

-- 사용
em.createQuery("Member.findByUsername", Member.class)
    .setParameter("username", "회원1")
    .getResultList();
```
- Spring Data JPA의 @Query
    - Named 쿼리로 만드는 것 (컴파일 시점에 쿼리를 검증할 수 있음!!)
    - 이름없는 네임드 쿼리라고 함
*/

// 벌크 연산
/*
> 벌크 연산
- 재고가 10개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행
    - 재고가 10개 미만인 상품 리스트로 조회
    - 상품 엔티티의 가격을 10% 증가
    - 트랜잭션 커밋 시점에 변경 감지가 동작
- 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행

> 예제
- 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)
- executeUpdate()의 결과는 영향받은 엔티티 수 반환
- UPDATE / DELETE 지원
- INSERT(insert into .. select, 하이버네이트 지원, JPA 표준 스펙에는 없음)

> 벌크 연산 주의
- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
- 해결 방안
    - 벌크 연산을 먼저 실행
    - 벌크 연산 수행 후 영속성 컨텍스트 초기화

> 참고: Spring Data JPA
- @Modifying(clearAutomatically=true, flushAutomatically=true)
    - clearAutomatically: 벌크 연산 후 영속성 컨텍스트 초기화
    - flushAutomatically: 벌크 연산 전 영속성 컨텍스트 SQL 쓰기 저장소 DB에 flush
        - 참고: JPA는 기본적으로 JPQL을 실행하기 전에 flush를 함
        - 하이버네이트의 FlushMode 설정을 수동으로 바꾼 특수한 상황에서 안전장치로 쓰임
*/