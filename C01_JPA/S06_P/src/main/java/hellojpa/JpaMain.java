package hellojpa;

import hellojpa.domain.Member;
import hellojpa.domain.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.changeTeam(team);
            em.persist(member);


            Member member2 = new Member();
            member2.setUsername("member2");
            member2.changeTeam(team);
            em.persist(member2);

            em.flush();
            em.clear();

            Team foundTeam = em.find(Team.class, team.getId());
            List<Member> members = foundTeam.getMembers();
            for (Member m : members) {
                System.out.println("m.getUsername() = " + m.getUsername());
            }

            System.out.println("members.size() = " + members.size());

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}

// 연관관계 매핑 기초

// 단방향 연관관계
/*
> 목표
- 객체와 테이블 연관관계의 차이를 이해
- 객체의 참조와 테이블의 외래 키를 매핑
- 용어 이해
    - 방향: 단방향, 양방향
    - 다중성: 다대일, 일대다, 일대일, 다대다
    - 연관관계의 주인: 객체 양방향 연관관계는 관리 주인이 필요

> 객체를 테이블에 맞추어 모델링
- 외래키를 가지고 있는 쪽이 N
- 외래키를 제공하는 쪽이 1

> 문제 상황
```java
Team team = new Team();
team.setName("teamA");
em.persist(team);

Member member = new Member();
member.setUsername("memberA");
member.setTeamId(team.getId()); // setTeam()이 더 객체지향 같음
em.persist(member);

Member foundMember = em.find(Member.class, member.getId());
Long foundTeamId = foundMember.getTeamId();
Team foundTeam = em.find(Team.class, foundTeamId);
```
- 테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾음
- 객체는 참조를 사용해서 연관된 객체를 찾음
- 외래 키 vs 참조

> 단방향 연관관계
```java
@ManyToOne
@JoinColumn(name = "TEAM_ID")
private Team team;
```
- 이제 단방향 연관관계를 매핑해줌
- name은 Member의 외래키 이름을 저걸로 할 것이다라는 의미
- 자동으로 Team이라는 객체(테이블)의 PK를 DB에 넣음

```java
Team team = new Team();
team.setName("teamA");
em.persist(team);

Member member = new Member();
member.setUsername("memberA");
member.setTeam(team);
em.persist(member);

em.flush();
em.clear();

// 조회
Member foundMember = em.find(Member.class, member.getId());
Team foundTeam = foundMember.getTeam();

// 수정
Team newTeam = em.find(Team.class, teamB.getId());
foundMember.setTeam(newTeam); // 나중에 update query가 나감
```
*/

// 양방향 연관관계와 연관관계의 주인
/*
> 양방향 매핑
- 단방향 매핑의 경우
    - member -> team으로는 이동 가능
    - team -> member로는 이동 불가능

- 양방향 매핑의 경우
    - 객체
        - Member (id, team, username)
        - Team (id, name, List<member>)
        - 이전에는 한 쪽에서만 참조 가능(단방향)
        - 현재는 List<member>로 양쪽에서 참조 가능
    - 테이블
        - Member (id, team_id, username)
        - Team (id, name)
        - 조인해서 찾으면 됨(양방향, 단방향의 구분이 없음)

```java
@OneToMany(mappedBy = "team") // 반대쪽 변수명을 적어야함!
private List<Member> members = new ArrayList<>();
```

```java
Member foundMember = em.find(Member.class, member.getId());
List<Member> members = foundMember.getTeam().getMembers();
for (Member m : members) {
    System.out.println("m.getUsername() = " + m.getUsername());
}
```
- 이제 team에 참가하고 있는 member들을 참조를 할 수 있게 됨

> mappedBy의 정체
- 객체와 테이블간에 연관관계를 맺는 차이를 이해해야함

> 객체와 테이블이 관계를 맺는 차이
- 객체 연관관계: 2개
    - 회원 -> 팀
    - 팀 -> 회원
    - 사실상 단방향이 2개

- 테이블 연관관계: 1개
    - 회원 <-> 팀의 연관관계(Join - team_id(FK, PK))

> 객체의 양방향 관계
- 사실 서로 다른 단방향 관계 2개

> 테이블의 양방향 연관관계
- 외래키 값 하나로 양쪽으로 조인할 수 있음

> 둘 중 하나로 외래 키를 관리해야함
- 이게 연관관계의 주인

> 연관관계의 주인
- 양방향 매핑 규칙
    - 객체의 두 관계 중 하나를 연관관계의 주인으로 지정
    - 연관관계의 주인만이 외래 키를 관리(등록, 수정)
    - **주인이 아닌 쪽은 읽기만 가능**
    - 주인은 mappedBy 속성 사용 X
    - 주인이 아니면 mappedBy 속성으로 주인 지정

> 누구를 주인으로?
- **외래 키가 있는 곳을 주인으로 정하자**(보통 다(N)쪽)
- 여기서는 Member.team이 연관관계의 주인
*/

// 양방향 연관관계와 연관관계의 주인 2 - 주의점, 정리
/*
> 양방향 매핑 시 가장 많이 하는 실수
- 연관관계의 주인에 값을 입력하지 않음
```java
Member member = new Member();
member.setUsername("member1");
em.persist(member);

Team team = new Team();
team.setName("TeamA");
team.getMembers().add(member); // 읽기 전용(주인이 아님)
em.persist(team);
```

```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.setTeam(team);
em.persist(member);

team.getMembers().add(member); // 가능하면 양방향으로 넣어주는 것이 매우 권장
// - DB에는 영향이 없는데, 메모리에 올라온 객체 상태 불일치 문제 발생
// - 양방향은 연관관계 편의 메소드 사용
```

> 양방향 매핑 시 항상 두 쪽에 다 값을 넣어줘야함

```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setUsername("member1");
member.changeTeam(team); // 양방향 연관관계 편의 메소드
em.persist(member);

// == 양방향 연관관계 편의 메서드 ==
public void changeTeam(Team team) {
    this.team = team;
    team.getMembers().add(this);
}
```

> 내가 공부한 것
```java
em.flush();
em.clear();

Team foundTeam = em.find(Team.class, team.getId());
List<Member> members = foundTeam.getMembers();
//            for (Member m : members) {
//                System.out.println("m.getUsername() = " + m.getUsername());
//            }
```
- em.flush() / em.clear()를 하지 않은 경우
    - 영속성 컨텍스트의 team 객체의 members는 진짜 new ArrayList<>()가 들어가있음
    - 따라서 조회가 되지 않음

- em.flush() / em.clear()를 한 경우
    - em.find()를 할 때, LazyLoading이므로 members에는 Proxy ArrayList<>()가 들어있음
    - 이후 Proxy ArrayList에서 값을 읽으려고 하면 그때 쿼리가 나감(한 번의 쿼리가 나감 처음에 데이터를 다 가져오기 때문에)
        - 프록시 초기화(여러 값이 있을 경우 첫 번째 값을 읽을 때만 쿼리가 나감)

> 양방향 연관관계 주의 - 실습
- 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정핮
- 연관관계 편의 메소드를 생성하자
    - 이거는 한쪽에만 비즈니스적으로 중심인 부분에서 하도록
- **양방향 매핑시에 무한 루프를 조심하자**
    - toString(), lombok, JSON 생성 라이브러리
    - DTO를 사용하자!!

> 양방향 매핑 정리
- 단방향 매핑만으로도 이미 연관관계 매핑은 완료(처음 설계 시 가장 우선)
- 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 많음
- **단방향 매핑을 잘하고, 양방향은 필요할 때 추가해도 됨(테이블에 영향 X)**

> 연관관계의 주인을 정하는 기준
- 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
- **연관관계의 주인은 외래 키의 위치를 기준으로 정해야함**
- "연관관계 편의 메소드"는 주인이랑 상관 없음(비즈니스 적으로 중심이 되는 쪽)
*/
