package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장
    }

    @Test
    void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 더티 체킹으로 수정
        member1.setUsername("User!!!!");

        // count 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndGreaterThen() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getAge()).isEqualTo(m2.getAge());
    }

    @Test
    public void findByUsernameNamedQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername(m2.getUsername());

        assertThat(result.get(0).getUsername()).isEqualTo("BBB");
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser(m2.getUsername(), m2.getAge());

        assertThat(result.get(0).getUsername()).isEqualTo("BBB");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void testFindUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        for (String s : memberRepository.findUsernameList()) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void testFindMemberDto() {
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member member = new Member("AAA", 10);
        member.setTeam(teamA);
        memberRepository.save(member);

        List<MemberDto> usernameList = memberRepository.findMemberDto();
        for (MemberDto memberDto : usernameList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void testFindByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member byName : byNames) {
            System.out.println("byName = " + byName);
        }
    }

    @Test
    void testFindReturnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // 컬렉션
        List<Member> aaa1 = memberRepository.findListByUsername("AAA");
        // - 없으면 EmptyCollection을 반환해줌

        // 단건
        Member aaa2 = memberRepository.findMemberByUsername("AAA");
        // - 없으면 null을 반환함
        // - 순수한 JPA 입장에서 Exception이 터지는데 Data JPA는 null을 반환해줌

        // 단건 Optional
        Optional<Member> aaa3 = memberRepository.findOptionByUsername("AAA");
        // - 없으면 Optional.empty();
        // - 그냥 이거 써~~
        // - 단건인데 결과가 여러 개면 => Exception이 터짐
        // - NonUniqueException => Spring 예외로 변환해서 던져줌
    }

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        // - 카운트 쿼리를 같이 나감 (나름 최적화 되어있음 - 정렬 조건 없이)

        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        // - 이건 외부 DTO로 내보내도 됨

//        Slice<Member> page = memberRepository.findByAge(age, pageRequest);
        // - Slice는 limit + 1을 조회함
        // - 카운트 쿼리를 안 날림
        // - 참고로 Slice는 Page의 부모임

        // then
        List<Member> members = page.getContent();
        for (Member member : members) {
            System.out.println("member = " + member);
        }
        long totalCount = page.getTotalElements();// totalcount;
        System.out.println("totalCount = " + totalCount);

        assertThat(members.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); // 총 페이지 개수
        assertThat(page.isFirst()).isTrue(); // 첫 페이지인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지 여부

        // 주의사항
        // - Page를 하려고 하지 않는 이유
        // - 전체 개수를 세는 것이 성능에 치명적임
        // - 조인을 할 필요가 없음
//        @Query(value = "select m from Member m left join m.team t",
//                countQuery = "select count(m.username) from Member m")
    }

    @Test
    public void bulkTest() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int count = memberRepository.bulkAgePlus(20);

        assertThat(count).isEqualTo(3);

        // 벌크 연산의 아주 중요한 주의사항
        // - 영속성 컨텍스트를 무시하고 DB에 빵 때려버림
        // - 영속성 컨텍스트의 값은 업데이트가 안됨
        // - 따라서 항상 하고 영속성 컨텍스트를 날려야함
        // - 참고로 JPQL을 적으면 그전까지를 flush를 하고 실행함

        Member member5 = memberRepository.findMemberByUsername("member5");
        assertThat(member5.getAge()).isEqualTo(41);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member memberA = new Member("memberA");
        memberA.setTeam(teamA);
        Member memberB = new Member("memberA");
        memberB.setTeam(teamB);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        em.flush();
        em.clear();

        memberRepository.findEntityGraphByUsername("memberA");

//        List<Member> members = memberRepository.findAll();
//        for (Member member : members) {
//            System.out.println("member.getUsername() = " + member.getUsername());
//            // - 이때 추가 쿼리가 나감
//            System.out.println("member.getUsername() = " + member.getTeam().getName());
//        }
    }

    @Test
    public void queryHint() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

//        Member findMember = memberRepository.findById(member1.getId()).get();
//        findMember.setUsername("member2");

//        em.flush(); // 업데이트 쿼리가 이때 나감
        // - 문제가 있음 (원래가 뭐였는지 따로 가지고 있음(원본))
        // - 추가적인 메모리를 들고 있음(원본을 저장할)
        // - 100% 조회만 할거라면 최적화 할 수 있음
        // - JPA가 제공하는 것이 아니라 Hibernate가 제공하는 기능

        Member findMember = memberRepository.findReadOnlyByUsername("member1");
        findMember.setUsername("member2");

        em.flush(); // 업데이트 쿼리가 안 나감
        // - 원본을 저장하지 않음(변경 감지를 하지 않음)
    }

    @Test
    public void lock() {
        Member member1 = memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        List<Member> members= memberRepository.findLockByUsername("member1");

        em.flush();
    }
}