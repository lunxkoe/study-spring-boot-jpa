package hellojpa;

import hellojpa.domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parent.addChild(child2);

            em.persist(parent);
            em.persist(child1);
            em.persist(child2);

            em.flush();
            em.clear();

            Parent foundParent = em.find(Parent.class, parent.getId());
            List<Child> childList = foundParent.getChildList();
            childList.remove(0);

            em.flush();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        emf.close();
    }

    private static void printMember(Member member) {
        System.out.println("member.getUsername() = " + member.getUsername());
    }

    private static void printMemberAndTeam(Member member) {
        String username = member.getUsername();
        System.out.println("username = " + username);

        Team team = member.getTeam();
        System.out.println("team.getName() = " + team.getName());
    }
}

// 프록시와 연관관계 관리

// 프록시
/*
> Member를 조회할 때 Team도 함께 조회해야할까?
```java
private static void printMember(Member member) {
    System.out.println("member.getUsername() = " + member.getUsername());
}

private static void printMemberAndTeam(Member member) {
    String username = member.getUsername();
    System.out.println("username = " + username);

    Team team = member.getTeam();
    System.out.println("team.getName() = " + team.getName());
}
```
- printMember: 멤버만 사용
- printMemberAndTeam: 멤버와 팀 모두 사용

```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

em.flush();
em.clear();

Member foundMember = em.find(Member.class, member.getId());
System.out.println("foundMember.getId() = " + foundMember.getId());
System.out.println("foundMember.getUsername() = " + foundMember.getUsername());

Hibernate:
    select
        m1_0.MEMBER_ID,
        m1_0.createdAt,
        m1_0.createdBy,
        m1_0.modifiedAt,
        m1_0.modifiedBy,
        t1_0.TEAM_ID,
        t1_0.name,
        m1_0.USERNAME
    from
        Member m1_0
    left join
        Team t1_0
            on t1_0.TEAM_ID=m1_0.team_id
    where
        m1_0.MEMBER_ID=?
```

```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

em.flush();
em.clear();

Member foundMember = em.getReference(Member.class, member.getId());
System.out.println("foundMember.getId() = " + foundMember.getId());
System.out.println("foundMember.getUsername() = " + foundMember.getUsername());
```
- em.find => em.getReference
    - 예전에는 쿼리가 나가다가 getReference 시점에는 쿼리가 나가지 않음
    - foundMember에 접근을 하니깐 쿼리가 실행됨
    - getReference는 가짜 객체를 가져옴

> em.getReference의 정체는?
- foundMember = class hellojpa.domain.Member$HibernateProxy$c0BdEFw4

> 프록시 기초
- em.find() vs em.getReference()
- em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
- em.getReference(): 데이터베이스 조회를 미루는 가짜 엔티티 객체 조회

- Proxy
    - Entity target = null
    - getId()
    - getName()
    - 껍대기에 ID 값만 가지고 있음(우리가 넘겨주기 때문)

> 프록시 특징
- 실제 클래스를 상속 받아서 만들어짐
- 실제 클래스와 겉 모양이 같음
- 사용하는 입장에서 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상)

- 프록시 객체는 실제 객체의 참조를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출

> 프록시 객체의 초기화 및 동작 흐름
```java
Member member = em.getReference(Member.class, "id1");
member.getName();
```
- getName()
- 영속성 컨텍스트에 초기화 요청
- DB를 조회
- 실제 Entity 생성
- target.getName() 호출

- 이때 호출하면서 필요한 값을 가져옴(로딩함)

> 프록시의 특징
- 처음 사용할 때 한 번만 초기화
- **프록시 객체가 실제 엔티티로 바뀌는 것이 아님**
    - 프록시를 통해서 실제 객체에 접근할 수 있는 것임
- 프록시 객체는 원본 엔티티를 상속받음
- 따라서 타입 체크 시 주의해야함(== 비교 실패, instance of 사용)
- 영속성 컨텍스트에 실제 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환
```java
Member foundMember = em.find(Member.class, 1L);
// foundMember = class hellojpa.domain.Member

Member reference = em.getReference(Member.class, 1L);
// reference = class hellojpa.domain.Member
```

```java
Member reference = em.getReference(Member.class, 1L);
System.out.println("reference = " + reference.getClass());
// - reference = class hellojpa.domain.Member$HibernateProxy$DzNQWTAU

Member foundMember = em.find(Member.class, 1L);
System.out.println("foundMember = " + foundMember.getClass());
// - foundMember = class hellojpa.domain.Member$HibernateProxy$DzNQWTAU
// 순서를 바꾸어도 proxy 객체로 초기화되어서 이후 조회할 때도 proxy객체를 반환함
```

```java
Member reference = em.getReference(Member.class, 1L);
System.out.println("reference = " + reference.getClass());
// - reference = class hellojpa.domain.Member$HibernateProxy$ZZi09GTa
System.out.println("reference.getUsername() = " + reference.getUsername());
System.out.println("reference.getClass() = " + reference.getClass());
// - reference = class hellojpa.domain.Member$HibernateProxy$ZZi09GTa

Member foundMember = em.find(Member.class, 1L);
System.out.println("foundMember = " + foundMember.getClass());
// - foundMember = class hellojpa.domain.Member$HibernateProxy$m6VW2yAQ
```
    - JPA에서 영속성 컨텍스트에서 꺼낸 거에 대해서 == 비교는 항상 참이여야함

- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시 초기화하면 문제 발생
    - LazyInitializationException 예외가 발생함
```java
Member reference = em.getReference(Member.class, 1L);
System.out.println("reference = " + reference.getClass());
System.out.println("reference.getUsername() = " + reference.getUsername());

//            em.detach(reference);
em.clear();

System.out.println("reference.getUsername() = " + reference.getUsername());

Member reference = em.getReference(Member.class, 1L);
System.out.println("reference = " + reference.getClass());
//            System.out.println("reference.getUsername() = " + reference.getUsername());

//            em.detach(reference);
em.clear();

System.out.println("reference.getUsername() = " + reference.getUsername());
```
    - 위 두 개의 차이
    - 프록시 초기화가 일어나기 전에 준영속 상태로 만들어버리면 프록시를 초기화할 수 없음
    - 근데 그러면 위에는 어떻게 되는걸까?
        - 프록시 초기화를 진행하고, 실제 엔티티를 로드했다면
        - 영속성 컨텍스트에서 사라지는 건 맞는데
        - 자바의 힙 메모리 자체에서 데이터가 사라지는 건 아님
        - 따라서 참조가 가능한 것

> 프록시 확인
- 프록시 인스턴스의 초기화 여부 확인
    - emf.getPersistenceUnitUtil().isLoaded(Object entity): true / false

- 프록시 클래스 확인 방법
    - entity.getClass().getName() 출력

- 프록시 강제 초기화
    - org.hibernate.Hibernate.initialize(entity)

- 참고: JPA 표준은 강제 초기화 없음
    - 강제 호출: member.getName();
*/

// 즉시 로딩과 지연 로딩
/*
> Member를 조회할 때 Team도 함게 조회해야할까?
- Member의 정보만 필요하다면 Team도 조인해서 가져오면 손해를 볼 수 있음
- **지연 로딩이라는 것을 제공**

> 지연 로딩 적용
```java
@Entity
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
```

```sql
Hibernate:
    select
        m1_0.MEMBER_ID,
        m1_0.createdAt,
        m1_0.createdBy,
        m1_0.modifiedAt,
        m1_0.modifiedBy,
        m1_0.TEAM_ID,
        m1_0.USERNAME
    from
        Member m1_0
    where
        m1_0.MEMBER_ID=?
```
- Team을 한 번에 가져오지 않음
- Member만 가져옴
- Team은 Proxy로 가져옴
    - Team에 손을 대는 순간 Team을 가져오는 쿼리를 실행 시킴(프록시 초기화)

> 지연 로딩
- 로딩
- 지연로딩 LAZY
- team 프록시 엔티티
    - 실제 team의 값에 접근 시 team 객체를 초기화

> 즉시 로딩 EAGER를 사용해서 함께 조회
- 조회를 할 때 한 번에 연관된 객체를 전부 다 조회함

> 프록시와 즉시로딩 주의
- 가급적 지연 로딩만 사용(특히 실무에서)
- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
- 즉시 로딩은 JPAL에서 N+1 문제를 발생시킴
- @xToMany는 기본이 즉시 로딩 => LAZY로 설정
- @xToMany는 기본이 지연 로딩

> 지연 로딩 활용
- 함께 조회를 많이 함 => 즉시 로딩
- 따로 조회를 많이 함 => 지연 로딩
- 주의: 실무에서는 지연 로딩을 깔고 JPQL의 fetch 조인이나 엔티티 그래프 기능을 활용!!
*/

// 영속성 전이(CASCADE)와 고아 객체
/*
> 영속성 전이: CASCADE
- 연관관계랑 관련이 없음
- 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때 사용
- 예: 부모 엔티티 저장 시, 자식 엔티티도 함게 저장

> 영속성 전이: 저장
```java
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);
em.persist(child1);
em.persist(child2);
```
- 이렇게 해야 각각의 엔티티가 다 저장됨
- 문제: parent를 저장하면 child까지 저장되도록 하고 싶음
```java
@Entity
public class Parent {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> childList = new ArrayList<>();

//
Child child1 = new Child();
Child child2 = new Child();

Parent parent = new Parent();
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);
//            em.persist(child1);
//            em.persist(child2);
```
- CascadeType.ALL로 설정을 하면 자동으로 자식 객체까지 모두 다 persist를 진행함

> 영속성 전이: CASCADE - 주의
- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없음
- 엔티티를 영속화할 때, 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐

> CASCADE의 종류
- ALL: 모두 적용
- PERSIST: 영속만
- REMOVE: 삭제만

> 언제 걸어야할까? (매우 주의)
- 단 하나의 부모가 자식을 다룰 때 사용함(소유자가 하나일 때)
- 사용하면 안되는 경우
    - 여러 곳에서 child를 관리할 경우 사용하면 안됨

> 고아 객체
- 고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
- orphanRemoval = true
```java
Parent parent = em.find(Parent.class, id);
parent.getChildren().remove(0);
// 자식 엔티티를 컬렉션에서 제거 - 이때 delete 쿼리가 나감 (부모와의 연관관계가 끊어진 자식을 대상으로)
```

- 아래를 실행
```java
Parent foundParent = em.find(Parent.class, 1L);
List<Child> childList = foundParent.getChildList();
childList.remove(0);

- 위의 실행 결과
```sql
Hibernate:
delete
        from
Child
        where
id=?
```

> 고아 객체 - 주의
- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능
- **참조하는 곳이 하나일 때 사용해야함!!!**
- 특정 엔티티가 개인 소유할 때 사용
- @OneToOne / @OneToMany만 가능
- CascadeType.REMOVE 처럼 동작함
    - 부모를 지우면 자식도 함게 지워짐
    - 이걸 설정하지 않아도 orphanRemoval = true만 설정되어있으면
    - 부모를 지우면 자식도 함게 전부 지워짐
- 주의: Cascade는 자식에 대한 설정이 없으면 자식을 리스트에서 제거한다고 DB에 아무런 영향을 주지 않음
- 단, 부모를 지워버리면 orphanRemoval이 동작함

> 영속성 전이 + 고아 객체, 생명주기
- CascadeType.ALL + orphanRemoval = true
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- 두 옵션을 모두 활성화하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있음
- DDD의 Aggregate Root 개념을 구현할 때 유용함
*/

// 실습 시 발견한 것
// - 연관관계의 주인이 아닌 OneToOne은 FetchType.Lazy로 설정을 해도, Eager로 동작함
//      - 있는지 확인해봐야하기 때문(Proxy의 한계)