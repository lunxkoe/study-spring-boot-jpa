package study.datajpa.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import study.datajpa.entity.Member;

public class MemberSpec {

    public static Specification<Member> teamName(final String teamName) {
        return new Specification<Member>() {
            @Override
            public Predicate toPredicate(Root<Member> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder builder) {

                if (StringUtils.hasText(teamName)) {
                    return null;
                }

                Join<Object, Object> t = root.join("team", JoinType.INNER);// 회원과 팀을 조인
                return builder.equal(t.get("name"), teamName);
            }
        };
    }

    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, builder) ->
                builder.equal(root.get("username"), username);
    }
}
