package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends
        JpaRepository<Member, Long>,
        MemberRepositoryCustom,
        JpaSpecificationExecutor<Member>
{

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//    @Query(name = "Member.findByUsername")
    // - 없어도 동작함
    // - 관례가 있어서 되는 것임
    // - 먼저 NamedQuery를 찾아보고
    // - 메소드 이름으로 쿼리를 생성해줌
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    // - 이것도 애플리케이션 로딩 시점에 오류가 발생함
    // - 사실상 이름이 없는 네임드 쿼리
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // DTO 조회 쿼리
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 컬렉션 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 컬렉션 조회
    List<Member> findListByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionByUsername(String username); // 단건 Optional

    // 페이징 처리
    // - 인터페이스로 각종 디비에 적용할 수 있음 (data jpa만 사용할 수 있는게 아님)
    // - Page: 추가 count 쿼리 결과를 포함하는 페이징
    // - Slice: 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit+1 조회)
    // - List: 추가 count 쿼리없이 결과만 반환
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);
//    Slice<Member> findByAge(int age, Pageable pageable);

    // 벌크성 수정 쿼리
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    // - 이게 있어야지 executeUpdate를 실행함
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();
    // - 문제: fetch join을 사용하려면 JPQL을 적어야함
    // - 적지 않고 자동 생성 기능을 활용할 때 사용 => EntityGraph

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"}) // fetch join 사용
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

//    @EntityGraph(attributePaths = ("team"))
    @EntityGraph("Member.all")
    // - Entity의 NamedEntityGraph를 사용
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // JPA Hint
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    // select for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

    List<UsernameOnly> findProjectionsByUsername(@Param("username") String username);

    List<UsernameOnlyDto> findProjectionsDtoByUsername(@Param("username") String username);

    // 네이티브 쿼리
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);
}
