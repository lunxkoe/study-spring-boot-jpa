package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member foundMember = memberRepository.findById(id).get();
        return foundMember.getUsername();
    }

    // 도메인 클래스 컨버터
    // - 사실 이걸 사용하는 걸 권장하지 않음
    // - 그래고 조회용으로만 사용해야함 (트랜잭션이 없음)
    @GetMapping("/member2/{id}")
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    // 설정을 통해서 디폴트 값 변경도 가능
    // - http://localhost:8080/members?page=0&size=3
    @GetMapping("/members")
    public Page<MemberDto> list(Pageable pageable) {
        // @PageableDefault(size = 5, sort = "username") Pageable pageable
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member ->
                new MemberDto(member.getId(), member.getUsername(), null));
        return map;
    }

//    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
