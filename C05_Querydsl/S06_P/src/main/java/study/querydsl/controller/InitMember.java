package study.querydsl.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile({"local"})
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    // 주의사항: @PostConstruct하는 부분과 @Transactional을 하는 부분을 분리해야함
    // - 스프링의 '트랜잭션(AOP 프록시) 적용 시점'과 '빈(Bean) 초기화 시점'이 엇갈리기 때문

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    @RequiredArgsConstructor
    static class InitMemberService {
        private final EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teanB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member"+i, i, selectedTeam));
            }
        }
    }
}
