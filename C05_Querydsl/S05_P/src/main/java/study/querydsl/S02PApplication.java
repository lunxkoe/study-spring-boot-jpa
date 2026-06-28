package study.querydsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class S02PApplication {

    public static void main(String[] args) {
        SpringApplication.run(S02PApplication.class, args);
    }

}

// 프로젝션과 결과 반환 - 기본
/*
> 프로젝션 대상이 하나
```java
List<String> result = queryFactory
    .select(member.username)
    .from(member)
    .fetch();
```
- 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
- 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회

> 튜플 조회
```java
List<Tuple> fetch = queryFactory
        .select(member.username, member.age)
        .from(member)
        .fetch();

for (Tuple tuple : fetch) {
    String username = tuple.get(member.username);
    Integer age = tuple.get(member.age);
    System.out.println("username = " + username);
    System.out.println("age = " + age);
}
```
- 여기서 튜플은 Querydsl것
- 서비스, 컨트롤러 계층까지 넘어가는 것은 좋지 않음
*/

// 프로젝션 결과 반환 - DTO
/*
> 순수 JPA에서 DTO 조회
```java
List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
        .getResultList();
for (MemberDto memberDto : result) {
    System.out.println("memberDto = " + memberDto);
}
```

> Querydsl 빈 생성(Bean population)
- 결과를 DTO 반환할 때 사용
    - 프로퍼티 접근
    - 필드 직접 접근
    - 생성자 사용

> 프로퍼티 접근
```java
List<MemberDto> result = queryFactory
        .select(Projections.bean(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

for (MemberDto memberDto : result) {
    System.out.println("memberDto = " + memberDto);
}
```
- 주의: 기본 생성자가 반드시 필요함 / Setter

> 필드 직접 접근
```java
List<MemberDto> result = queryFactory
        .select(Projections.fields(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

for (MemberDto memberDto : result) {
    System.out.println("memberDto = " + memberDto);
}
```
- Getter/Setter 필요 없음
- 필드에 바로 주입

> 어떻게 값을 넣는 것일까?
- 필드명을 기준으로 넣음
- 필드명이 맞지 않다면?
    - member.username.as("name")처럼 맞춰서 넣어주면 됨

> 생성자 사용
```java
List<MemberDto> result = queryFactory
        .select(Projections.constructor(MemberDto.class, member.username, member.age))
        .from(member)
        .fetch();

for (MemberDto memberDto : result) {
    System.out.println("memberDto = " + memberDto);
}
```
- 생성자의 순서와 타입에 맞게 넣어주어야함

> 필드명이 맞지 않는 경우 + 서브쿼리
```java
QMember memberSub = new QMember("memberSub");
List<UserDto> result = queryFactory
        .select(Projections.fields(
                UserDto.class,
                member.username.as("name"),
                ExpressionUtils.as(JPAExpressions
                        .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
        .from(member)
        .fetch();

for (UserDto userDto : result) {
    System.out.println("userDto = " + userDto);

}
```
- .as()로 맞출 수 있음
- 서브쿼리의 경우 마지막에 "age"같이 맞출 수 있음(ExpressionsUtils로 감싸는 것이 별칭을 주는 것이 포인트)
*/

// Project 결과 반환 - @QueryProjection
/*
> @QueryProjection
```java
@Data
public class MemberDto {

    private String username;
    private int age;

    public MemberDto() {

    }

    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
```

```java
List<MemberDto> result = queryFactory
        .select(new QMemberDto(member.username, member.age))
        .from(member)
        .fetch();

for (MemberDto memberDto : result) {
    System.out.println("memberDto = " + memberDto);
}
```
- 컴파일 타입에 타입이 맞지 않으면 오류를 발생함
- 처음에 했던 생성자 방식은 컴파일 오류를 잡을 수 없음이 매우 큰 차이

- 고민거리
    - 가장 안전한 방법으로써의 장점
    - 기존의 DTO은 Querydsl에 대한 의존성이 없었는데, 생김
*/

// 동적 쿼리 - BooleanBuilder 활용
/*
> 동적 쿼리를 해결하는 두 가지 방식
- BooleanBuilder
- Where 다중 파라미터 사용
```java
@Test
void dynamicQuery_BooleanBuilder() {
    String usernameParam = "member1";
    Integer ageParam = 10;

    List<Member> result = searchMember1(usernameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);
}

private List<Member> searchMember1(String usernameParam, Integer ageParam) {

    BooleanBuilder builder = new BooleanBuilder();
    if (usernameParam != null) {
        builder.and(member.username.eq(usernameParam));
    }

    if (ageParam != null) {
        builder.and(member.age.eq(ageParam));
    }

    return queryFactory
            .selectFrom(member)
            .where(builder)
            .fetch();
}
```
*/

// 동적 쿼리 - Where 다중 파라미터 사용
/*
> 동적 쿼리 - Where
```java
@Test
void dynamicQuery_whereParam() {
    String usernameParam = "member1";
    Integer ageParam = 10;

    List<Member> result = searchMember2(usernameParam, ageParam);
    assertThat(result.size()).isEqualTo(1);
}

private List<Member> searchMember2(String usernameParam, Integer ageParam) {
    return queryFactory
            .selectFrom(member)
            .where(usernameEq(usernameParam), ageEq(ageParam))
            .fetch();
}

private BooleanExpression ageEq(Integer ageParam) {
    if (ageParam == null) {
        return null; // null이되면 무시를 함
    }
    return member.age.eq(ageParam);
}

private BooleanExpression usernameEq(String usernameParam) {
    if (usernameParam == null) {
        return null; // null이되면 무시를 함
    }
    return member.username.eq(usernameParam);
}

private Predicate allEq(String usernameParam, Integer ageParam) {
    return usernameEq(usernameParam).and(ageEq(ageParam));
}
```
- 예: 광고 상태: isServiceable, 날짜가 IN, ...
    - 각각의 메소드를 조합해서 사용할 수 있음(allEq처럼)
    - **재사용할 수 있다는 강점이 있음**

> 정리
- where 조건의 null 값은 무시가 됨
- 메서드를 다른 쿼리에서 재사용할 수 있음
- 쿼리 자체의 가독성이 높아짐
- 조합 가능
    - **null 체크는 주의해서 처리해야함!!!!**
*/

// 수정 삭제 벌크 연산
/*
> 수정, 삭제 배치 쿼리
- 쿼리 한 번으로 대량 데이터 수정
```java
@Test
//    @Commit
void bulkUpdate() {
    long count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.lt(28))
            .execute();
}
```
- 주의사항
    - 영속성 컨텍스트에 데이터가 올라가있음
    - 벌크 연산은 영속성 컨텍스트를 무시하고 바로 DB로 쿼리를 날림
    - bulkUpdate() 안에서 다시 member를 조회하면 영속성 컨텍스트에 올라와있는 데이터를 그냥 가져옴
    - 영속성 컨텍스트에 이미 데이터가 있으면 DB에서 데이터를 퍼올려도 무시를 함
    - 벌크 연산 후에는 em.flush() / em.clear()를 반드시 하자!!!! (습관)

> 예제
```java
@Test
//    @Commit
void bulkUpdate() {
    long count = queryFactory
            .update(member)
            .set(member.username, "비회원")
            .where(member.age.lt(28))
            .execute();
}

@Test
void bulkAdd() {
    queryFactory
            .update(member)
            .set(member.age, member.age.multiply(2))
            .execute();
}

@Test
void bulkDelete() {
    queryFactory
            .delete(member)
            .where(member.age.gt(10))
            .execute();
}
```
- execute()로 실행
*/

// SQL Function 호출하기
/*
> SQL Function 호출하기
- JPA와 같이 Dialect에 등록된 내용만 호출할 수 있음
- member -> M으로 변경하는 replace 함수 사용
```java
@Test
void sqlFunction() {
    List<String> result = queryFactory
            .select(Expressions.stringTemplate(
                    "function('replace', {0}, {1}, {2})",
                    member.username, "member", "M"))
            .from(member)
            .fetch();
    for (String s : result) {
        System.out.println("s = " + s);
    }
}
```
- 자주 쓰는 함수는 내장되어있음
*/