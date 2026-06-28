package hellojpa;

import hellojpa.domain.Member;
import hellojpa.domain.Movie;
import hellojpa.domain.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("user1");
            member.setCreatedAt(LocalDateTime.now());
            member.setCreatedBy("kim ");
            em.persist(member);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}

// 고급 매핑

// 상속관계 매핑
/*
> 상속관계 매핑
- 관계형 데이터메이스는 상속 관계 X
- 슈퍼 타입, 서브 타입 관계라는 모델링 기법이 객체 상속과 유사
- 상속관계 매핑: 객체의 상속 구조와 DB의 슈퍼타입, 서브타입 관계를 매핑
    - 조인 전략
    - 단일 테이블 전략
    - 구현 클래스마다 테이블 전략

```java
@Entity
public abstract class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int age;
}

@Entity
public class Album extends Item {
    private String artist;
}

@Entity
public class Movie extends Item {
    private String director;
    private String actor;
}

@Entity
public class Book extends Item {
    private String author;
    private String isbn;
}
```
- JPA의 기본 전략이 단일 테이블 전략

> 주요 어노테이션
- @Inheritance(stratgy=InheritanceType.XXX)
    - JOINED: 조인 전략
    - SINGLE_TABLE: 단일 테이블 전략
    - TABLE_PER_CLASS: 구현 클래스마다 테이블 전략
- @DiscriminatorColumn: DTYPE

> 조인 전략
- 장점
    - 테이블 정규화
    - 외래 키 참조 무결성 제약조건 활용 가능
    - 저장공간 효율화

- 단점
    - 조회 시 조인을 많이 사용, 성능 저하
    - 조회 쿼리가 복잡함
    - 데이터 저장시 INSERT SQL 2번 호출

> 단일 테이블 전략
- @DiscriminatorColumn이 없어도 DTYPE이 필수로 생성됨
- 장점
    - 조인이 필요 없으므로 조회 성능이 빠름
    - 조회 쿼리가 단순함

- 단점
    - 자식 엔티티가 매핑한 컬럼은 모두 null 허용
    - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있음
    - 오히려 조회 성능이 느려질 수도 있음

> 구현 클래스마다 테이블 전략 (쓰면 안되는 전략)
*/

// Mapped SuperClass - 매핑 정보 상속
/*
> @MappedSuperclass
- 공통 매핑 정보가 필요할 때 사용(id, name)
```java
@MappedSuperclass
public abstract class BaseEntity {

    private String createdBy;
    private LocalDateTime createdAt;
    private String modifiedBy;
    private LocalDateTime modifiedAt;

@Entity
public class Member extends BaseEntity {

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
```
- 상속관계 매핑 X
- 엔티티 X, 테이블과 매핑 X
- 부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공
- 직접 생성할 일이 없으므로 추상 클래스 권장
- 조회, 검색 불가

- 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- 참고: @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능
*/