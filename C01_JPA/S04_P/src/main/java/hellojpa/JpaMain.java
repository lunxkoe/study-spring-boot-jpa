package hellojpa;

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



            tx.commit();;
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}

// 영속성 컨텍스트 1
/*
> JPA에서 가장 중요한 2가지
- 객체와 관계형 데이터베이스 매핑하기(Object Relational Mapping)
- 영속성 컨텍스트

> 엔티티 매니저 팩토리와 엔티티 매니저
- EntityManagerFactory
    - 고객의 요청이 올때마다 EntityManager를 생성
    - 생성된 EntityManager는 DB 커넥션을 사용해서 DB와 통신

> 영속성 컨텍스트
- 엔티티를 영구 저장하는 환경
- EntityManager.persist(entity);
    - **DB에 저장한다는 의미가 아님!!**
    - **영속성 컨텍스트에 저장한다는 의미!!**
- 논리적인 개념
- 엔티티 매니저를 통해서 영속성 컨텍스트에 접근

> 엔티티의 생명주기
- 비영속(new/transient)
    - 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
- 영속(managed)
    - 영속성 컨텍스트에 관리되는 상태
- 준영속(detached)
    - 영속성 컨텍스트에 저장되었다가 분리된 상태
- 삭제(removed)
    - 삭제된 상태

> 비영속
```java
Member member = new Member();
member.setId("member1");
member.setUsername("member1");
```

> 영속
```java
// 객체를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("member1");

// 객체를 저장한 상태(영속)
EntityManager em = emf.createEntityManager();
em.persist(member);
```
- 주의: DB에 저장되는 것이 아님 / 영속성 컨텍스트에 저장되는 것!!

> 준영속, 삭제
```java
// 회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
em.detach(member);

// 객체를 삭제한 상태(삭제)
// - 주의 삭제를 위해서 영속성 컨텍스트에서 분리(재사용하면 안됨)
// - 커밋 시점에 실제 DB에서 삭제
em.remove(member);
```

> 영속성 컨텍스트 이점
- 1차 캐시
- 동일성(identity) 보장
- 트랜잭션을 지원하는 쓰기 지연
- 변경 감지
- 지연 로딩
*/

// 영속성 컨텍스트 2
/*
> 엔티티 조회, 1차 캐시
```java
// 객체를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("member1");

// 객체를 저장한 상태(영속)
EntityManager em = emf.createEntityManager();
em.persist(member);
```
- 1차 캐시를 가지고 있음
- 1차 캐시 안
    - (@Id, Entity)

> 1차 캐시에서 조회
- em.find(Member.class, "member1");
- 1차 캐시에 있으면 실제 DB에서 조회하는 것이 아님(쿼리가 나가지 않음을 볼 수 있음)
- 1차 캐시에 없으면 실제 DB에서 조회(쿼리가 나가게 됨)
    - 1차 캐시에 저장 후 반환

> 영속 엔티티의 동일성 보장
```java
Member a = em.find(Member.class, "member1");
Member b = em.find(Member.class, "member1");
System.out.println(a == b); // true
```
- 1차 캐시로 반복 가능한 읽기 등급(Repeatable Read)의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공

> 엔티티 등록 트랜잭션을 지원하는 쓰기 지연
- em.persist(member)를 한다고 insert SQL을 데이터베이스에 보내지 않음
- 트랜잭션을 커밋하는 순간 모아서 한번에 데이터베이스에 쿼리를 날림(flush)
- 쓰기 지연 SQL 저장소라는 곳에 쌓아두는 것
- 또한 1차 캐시에 Entity들을 넣어둠(영속 상태)
- **쓰기 지연은 등록 / 수정 / 삭제에만 적용됨** - 조회는 X

> 엔티티 수정 변경 감지
```java
Member foundMember = em.find(Member.class, 1L);
member.setName("NNN");
// em.persist(member); // 이것을 하지 않아도 update 쿼리가 나감
```
- JPA의 목표: 컬렉션 다루듯이 사용하고 싶은 것
- 컬렉션을 잘 생각해보면, 데이터를 참조형으로 가져오면 해당 엔티티를 수정하면 컬렉션에 다시 저장해주고 그런 걸 하지 않아도 됨
- 동작 원리
    - 1차 캐시(@Id | Entity | **스냅샷**)
    - 스냅샷: 값을 딱 읽은 시점
    - flush()가 호출되는 시점(DB에 작업을 수행하려는 시점)
        - 스냅샷과 Entity를 비교함
        - 바뀐 내용이 있으면 Update 쿼리를 쓰기 지연 SQL 저장소에 만들어 둠
        - 스냅샷 최신화
    - 이후 DB에 실제 쿼리를 날림
*/

// 플러시(flush)
/*
> 플러시란
- 영속성 컨텍스트의 변경 내용을 데이터베이스에 반영

> 플러시 발생
- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 참고로 스냅샷도 갱신함
- 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송(등록, 수정, 삭제)
- 이후 commit()

> 영속성 컨텍스트를 플러시하는 방법
- em.flush() - 직접 호출
- 트랜잭션 커밋 - 플러시 자동 호출
- JPQL 쿼리 실행 - 플러시 자동 호출
```java
Member member = new Member(1L, "memberA");
em.persist(member);
em.flush();
// - 이게 없었을 때는 ==== 이후에 쿼리가 나감
// - 이게 있을 때는 여기서 쿼리가 나감
System.out.println("====");
em.commit();
```
- **flush를 한다고 영속성 컨텍스트를 비우는 것이 아님**
    - 쓰기 지연 SQL 저장소의 쿼리를 실행하는 것

> JPQL 쿼리 실행시 플러시가 자동으로 호출되는 이유
```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);

query = em.createQuery("select m from Member m", Member.class);
```
- JPQL은 영속성 컨텍스트를 거치지 않고 데이터베이스에 쿼리를 날리는 작업
    - 참고: 이때 가져온 데이터를 다시 영속성 컨텍스트에 넣음
    - 가져올 때, 영속성 컨텍스트를 참조해서 값을 가져옴
        - 만약 해당 ID의 엔티티가 영속성 컨텍스트에 이미 있다면:
            - DB에서 방금 읽어온 따끈따끈한 데이터는 과감히 버리고,
            - 영속성 컨텍스트에 이미 존재하는 기존 엔티티를 반환. (영속 엔티티의 동일성 보장 원칙 때문)
        - 만약 영속성 컨텍스트에 없다면:
            - DB에서 가져온 데이터를 영속성 컨텍스트에 새로 저장(영속화)하고 반환
- 최신 정보를 가져와야하므로 JPQL 실행 전 flush를 자동으로 호출함

> 결론: 플러시
- 영속성 컨텍스트를 비우지 않음
- 영속성 컨텍스트의 변경 내용을 데이터베이스에 동기화
- 트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화하면 됨
*/

// 준영속 상태
/*
> 준영속 상태란
- 영속 -> 준영속
- 영속 상태의 엔티티가 영속성 컨텍스트에서 분리(detached)
- 영속성 컨텍스트가 제공하는 기능을 사용 못함

> 준영속 상태로 만드는 방법
- em.detach(foundMember): 특정 엔티티만 준영속 상태로 만듦
- em.clear(): 영속성 컨텍스트를 완전히 초기화
- em.close(): 영속성 컨텍스트를 종료
*/
