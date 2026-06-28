package study.datajpa.entity;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;

import java.util.List;

@SpringBootTest
@Transactional
@Rollback(false)
@Slf4j
class MemberTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamA);
        Member member3 = new Member("member3", 10, teamB);
        Member member4 = new Member("member4", 10, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            log.info("member = {}", member);
            log.info("-> member.team = {}", member.getTeam());
        }

        log.info("=====");
        em.flush();
        em.clear();

        Team foundTeam1 = em.find(Team.class, 1L);
        Member member = new Member("member5", 10, foundTeam1);
        em.persist(member);
        log.info("현재 팀의 멤버 수: {}", foundTeam1.getMembers().size());

        log.info("=====");
        em.flush();
        em.clear();

        Team foundTeam2 = em.find(Team.class, 2L);
        Member member5 = em.find(Member.class, 5L);
        member5.changeTeam(foundTeam2);
    }
}