package hellojpa;

import hellojpa.domain.Member;
import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member foundMember = em.find(Member.class, 1L);
            foundMember.setName("HelloJPA");

            tx.commit();;
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}


// Hello JPA - 프로젝트 생성
/*
> H2 데이터베이스 생성
- jdbc:h2:~/test
- jdbc:h2:tcp://localhost/~/test
*/

// 애플리케이션 개발
/*
> JPA 구동 방식
1. Persistence에서 설정 정보 조회
2. Persistence에서 EntityManagerFactory 생성
3. EntityManagerFactory에서 EntityManager 생성
```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
EntityManager em = emf.createEntityManager();

// Code

em.close();
emf.close();
```

> 객체와 테이블을 생성하고 매핑하기
```java
package hellojpa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    private Long id;
    private String name;
}
```

```sql
create table Member (
 id bigint not null,
 name varchar(255),
 primary key (id)
);
```

> 데이터 저장
```java
EntityTransaction tx = em.getTransaction();
tx.begin();

try {
    Member member = new Member();
    member.setId(1L);
    member.setName("HelloA");
    em.persist(member);

    tx.commit();;
} catch (Exception e) {
    tx.rollback();
} finally {
    em.close();
}

emf.close();
```
- 항상 Transaction을 시작하고 commit해서 실제 DB에 반영
- persist(member)로 영속성 컨텍스트에 member 저장

> 데이터 찾기
```java
Member foundMember = em.find(Member.class, 1L);
System.out.println("foundMember.getId() = " + foundMember.getId());
System.out.println("foundMember.getName() = " + foundMember.getName());
```

> 데이터 삭제
```java
em.remove(foundMember);
```

> 데이터 수정
```java
Member foundMember = em.find(Member.class, 1L);
foundMember.setName("HelloJPA");
```
- persist, remove 같이 무언가를 하지 않았는데 update 쿼리가 나감
- JPA를 통해서 엔티티를 가져오면 JPA가 관리함
- 커밋 직전에 원본에 변경된 사항이 있는지 체크함
    - 있으면 update query O
    - 없으면 update query X

> 주의
- EntityManagerFactory는 애플리케이션이 생성되는 시점 단 하나만 생성
- EntityManager는 쓰레드 간에 공유 X(사용하고 버려야함)
- JPA의 모든 데이터 변경은 트랜잭션 안에서 실행

> JPQL(객체 지향 쿼리 언어)
```java
List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
```
- 객체를 대상으로 가져옴
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 SQL이 필요함
- JPQL: 객체를 대상으로 쿼리
- SQL: 테이블을 대상으로 쿼리
*/