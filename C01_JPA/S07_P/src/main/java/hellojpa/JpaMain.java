package hellojpa;

import hellojpa.domain.Member;
import hellojpa.domain.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);

            Team team = new Team();
            team.setName("teamA");
            team.getMembers().add(member);
            em.persist(team);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}

// 연관관계 종류

// 다대일 [N:1]
/*
> 연관관계 매핑 시 고려사항 3가지
- 다중성
- 단방향, 양방향
- 연관관계의 주인

> 다중성
- 다대일: @ManyToOne
- 일대다: @OneToMay
- 일대일: @OneToOne
- 다대다: @ManyToMany

> 단방향, 양방향
- 테이블
    - 외래 키 하나로 양쪽으로 조인 가능
    - 사실 방향이라는 개념이 없음

- 객체
    - 참조용 필드가 있는 쪽으로만 참조 가능
    - 한쪽만 참조하면 단방향
    - 양쪽이 서로 참조하면 양방향

> 연관관계의 주인
- 양방향의 경우, 둘 중 테이블의 외래 키를 관리할 곳을 정해주어야함
- 연관관계의 주인: **외래 키를 관리하는 참조**
- 주인의 반대편: 외래 키에 영향을 주지 않음. **읽기 전용**
    - 값을 수정해도 DB에 반영되지 않음

> 다대일 단방향
- member(다) - team(일)

> 다대일 단방향 정리
- 가장 많이 사용하는 연관관계
- 다대일의 반대는 일대다

> 다대일 양방향
- member(다) - team(일): 연관관계의 주인
- team(일) - member(다): 테이블의 영향을 주지 않음
*/

// 일대다 [1:N] - 여기서는 1이 연관관계의 주인으로 설정한 것을 말함(이렇게 하면 좋지 않은 설계)
/*
> 일대다 단방향
- 객체
    - Team
        - id
        - name
        - List<member>

    - Member
        - id
        - username

- 테이블
    - Team
        - id
        - name

    - Member
        - id
        - team_id
        - username

- 다대일과 테이블에서의 차이는 없음(DB는 어쩔 수 없이 다에 외래키가 들어갈 수 밖에 없음)
- 연관관계의 주인이 일(1)로 바뀌었다는 것만 다름
```java
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany
    @JoinColumn(name = "team_id") // 연관관계의 주인을 설정: 실제 DB에는 Member에 외래 키를 가지도록 구성됨
    private List<Member> members = new ArrayList<>();
```

```
Hibernate:
    create table Member (
        MEMBER_ID bigint not null,
        team_id bigint,
        USERNAME varchar(255),
        primary key (MEMBER_ID)
    )
Hibernate:
    create table Team (
        TEAM_ID bigint not null,
        name varchar(255),
        primary key (TEAM_ID)
    )
Hibernate:
    alter table if exists Member
       add constraint FK5nt1mnqvskefwe0nj9yjm4eav
       foreign key (team_id)
       references Team
```

> 문제? 상황
```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

Team team = new Team();
team.setName("teamA");
team.getMembers().add(member);
em.persist(team);
```
- team.getMembers().add(member);를 할 때 team의 테이블이 변경되는 것이 아님
- member의 외래키를 수정해주어야함(update 쿼리가 추가적으로 발생)

- 실행 결과
```
Hibernate:
    insert for
        hellojpa.domain.Member insert
        into
Member (USERNAME, MEMBER_ID)
values
        (?, ?)
Hibernate:
insert for
    hellojpa.domain.Team insert
        into
Team (name, TEAM_ID)
values
        (?, ?)
Hibernate:
update
        Member
set
team_id=?
where
MEMBER_ID=?
```

- 쿼리를 예측하는 것이 굉장히 어려움
- team만 손댔는데 member 테이블이 업데이트가 됨
- 따라서 이것을 사용하는 것을 권장하지 않음
    - 차라리 다대일 양방향 관계를 하는 것이 좋음

> 일대다 단방향 정리
- 1이 연관관계의 주인
- 근데 테이블은 항상 다 쪽에 외래키가 있음
- 반대편 테이블의 외래키를 관리하는 특이한 구조
- @JoinColumn을 꼭 사용해야함.
    - 그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블을 하나 추가함)
    ```
    Hibernate:
    create table Member (
        MEMBER_ID bigint not null,
        USERNAME varchar(255),
        primary key (MEMBER_ID)
    )
    Hibernate:
        create table Team (
            TEAM_ID bigint not null,
            name varchar(255),
            primary key (TEAM_ID)
        )
    Hibernate: -- 중간 테이블이 생김
        create table Team_Member (
            Team_TEAM_ID bigint not null,
            members_MEMBER_ID bigint not null unique
        )
    ```

> 일대다 단방향 정리
- 단점
    - 엔티티가 관리하는 외래키가 다른 테이블에 있음
    - 연관관계 관리를 위해 추가로 UPDATE SQL 실행
- 일대다 단방향 매핑보다는 다대일 양방향 매핑을 사용하자!!

> 일대다 양방향
- 억지성이 있음(스펙상 되는 것이 아니라 야매로 됨)
```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team

@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany
    @JoinColumn(name = "team_id")
    private List<Member> members = new ArrayList<>();
```
- insertable = false, updatable = false의 이유
    - 지금 JoinColumn이 양쪽에 다 있음(연관관계의 주인이 두 개)
    - 이를 무효화시키기 위해서 사용함
    - 읽기 전용이 되어버림(사실상 양방향 매핑처럼 보임)

> 첨언
- **다대일 양방향을 사용하자!!**
*/

// 일대일 [1:1]
/*
> 일대일 관계
- 그 반대도 일대일
- 주 테이블이나 대상 테이블 중 외래 키 선택 가능
    - 주 테이블에 외래 키
    - 대상 테이블에 외래 키
- 외래 키에 데이터베이스 유니크 제약조건 추가

> 일대일: 주 테이블에 외래 키 단방향
- member - locker
    - 회원은 단 하나의 락커를 가질 수 있음
```java
@OneToOne
@JoinColumn(name = "LOCKER_ID")
private Locker locker;

// 양방향
@OneToOne(mappedBy = "locker")
private Member member;
```

> 일대일: 주 테이블에 외래 키 양방향 정리
- 다대일 양방향 매핑처럼 외래 키가 있는 곳이 연관관계의 주인
- 반대편은 mappedBy 사용

```
Hibernate:
    create table Locker (
        LOCKER_ID bigint not null,
        name varchar(255),
        primary key (LOCKER_ID)
    )
Hibernate:
    create table Member (
        LOCKER_ID bigint unique,
        MEMBER_ID bigint not null,
        team_id bigint,
        USERNAME varchar(255),
        primary key (MEMBER_ID)
    )
```

> 일대일: 대상 테이블에 외래 키 단방향
- Locker에 member_id가 있는 경우
- **지원 자체가 안됨**
- 단방향 관계는 JPA 지원 X
- 양방향 관계는 지원

> 일대일: 대상 테이블에 외래 키 양방향
- 객체
    - member(id, locker, username)
    - locker(id, name, member) => member를 연관관계의 주인으로 잡으면 됨

- 테이블
    - member(id, username)
    - locker(id, member_id, name)

> 고려 사항
- member가 locker_id를 들고 있는 것이 좋을까?
- locker가 member_id를 들고 있는 것이 좋을까?

- 정답은 없음
    - 다만, 하나의 회원이 여러 개의 locker를 가질 수 있게 된다면?
    - 대상 테이블에 외래 키에서 unique 제약 조건만 빼주면 됨
    - 주 테이블에 외래 키의 경우에는 문제가 발생함
    - 근데 개발자 관점에서 member가 locker를 가지고 있는 것이 좋을 수 있음
        - locker가 있어 없어 판단하기 좋음
    - 일단 주 테이블에 외래 키 단방향을 선호해보자
- 대상 테이블의 외래 키의 경우에는 양방향으로 잡으면 됨

> 일대일 정리
- 주 테이블에 외래 키(많이 접근하는 테이블)
    - 주 객체가 대상 객체의 참조를 가지는 것처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
    - 객체지향 개발자 선호
    - JPA 매핑 편리
    - 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
    - 단점: 값이 없으면 외래 키게 null 허용

- 대상 테이블에 외래 키
    - 대상 테이블에 외래 키가 존재
    - 전통적인 데이터베이스 개발자 선호
    - 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
    - 단점: 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨
        - 그니깐 member에 locker에 프록시를 넣어주려면 값이 null인지 아닌지를 판단해야함
        - 근데 이렇게 되어있으면 member를 조회한다고 알 수 없음. 대상 테이블을 반드시 조회해봐야함
*/

// 다대다 [N:M]
/*
> 다대다
- 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
- 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야함
- member - member_product - product
- **객체는 다대다 관계 가능**
    - 컬렉션을 사용하면 됨

> 다대다
- @ManyToMany
- @JoinTable로 연결 테이블 지정
- 다대다 매핑: 단방향, 양방향 가능

> 한계
- 실무에서 사용 X

- 다대다 단방향
```java
@ManyToMany
@JoinTable(name = "MEMBER_PRODUCT")
private List<Product> products = new ArrayList<>();
```

```
Hibernate:
    alter table if exists MEMBER_PRODUCT
       add constraint FKc6hsxwm11n18ahnh5yvbj62cf
       foreign key (products_id)
       references Product
```

- 다대다 양방향
```java
@ManyToMany(mappedBy = "products")
private List<Member> members = new ArrayList<>();
```

- 연결 테이블이 단순히 연결만하고 끝나지 않음!!!
- 주문 시간, 수량 같은 데이터가 들어올 수 있음(하지만 @ManyToMany는 추가 데이터를 넣을 수 없음)

> 한계 극복
- 연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)
- @ManyToMany -> @OneToMany, @ManyToOne
```java
package hellojpa.domain;

import jakarta.persistence.*;

@Entity
public class MemberProduct {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

@Entity
public class Product {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "product")
    private List<MemberProduct> memberProducts = new ArrayList<>();
}

@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;

    @OneToMany(mappedBy = "member")
    private List<MemberProduct> memberProducts = new ArrayList<>();
```

```java
@ManyToMany
@JoinTable(name = "CATEGORY_ITEM",
        joinColumns = @JoinColumn(name = "CATEGORY_ID"), // 내가 조인하는 거
        inverseJoinColumns = @JoinColumn(name = "ITEM_ID") // 상대방이 조인하는 거
)
```
*/