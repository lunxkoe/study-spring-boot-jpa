package hellojpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            tx.commit();;
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}

// 객체와 테이블 매핑
/*
> 엔티티 매핑 소개
- 객체와 테이블 매핑: @Entity, @Table
- 필드와 컬럼 매핑: @Column
- 기본 키 매핑: @Id
- 연관관계 매핑: @ManyToOne, @JoinColumn

> @Entity
- 붙은 클래스는 JPA가 관리
- JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
- 주의
    - 기본 생성자 필수(protected, public)
    - final 클래스, enum, interface, inner 클래스 사용 X
    - 저장할 필드에 final 사용 X

> @Entity 속성 정리
- 속성: name
    - JPA에서 사용할 엔티티 이름을 지정
    - 기본값: 클래스 이름을 그대로 사용
    - 같은 클래스 이름이 없으면 기본값 사용

> @Table
- 엔티티와 매핑할 테이블 지정
- @Table(name = "MBR"): MBR이라는 이름의 테이블로 생성됨
- 속성
    - name: 매핑할 테이블 이름 | 엔티티 이름을 사용
    - catalog: 데이터베이스 catalog 매핑
    - schema: 데이터베이스 schema 매핑
    - uniqueConstraints(DDL): DDL 생성 시에 유니크 제약 조건 생성
*/

// 데이터베이스 스키마 자동 생성
/*
> 데이터베이스 스키마 자동 생성
- DDL을 애플리케이션 실행 시점에 자동 생성
- 테이블 중심 -> 객체 중심
- 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
- 개발에서만 사용할 것!!
- 운영 서버에서는 사용하지 않음!!

> 데이터베이스 스키마 자동 생성 - 속성(ddl.auto)
- create: 기본 테이블 삭제 후 다시 생성(drop + create)
- create-drop: create와 같으나 종료시점에 테이블 drop
- update: 변경분만 반영(운영 DB에는 사용하면 안됨)
- validate: 엔티티와 테이블이 정상 매핑되었는지만 확인
- none: 사용하지 않음

> 데이터베이스 스키마 자동 생성 - 주의
- 운영 장비에는 절대 create, create-drop, update 사용하면 안됨
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- 스테이징과 운영 서버는 validate 또는 none

> DDL 생성 기능
- 제약 조건 추가
    - @Column(unique = true, length = 10)
- 유니크 제약 조건 추가
    - @Table(uniqueConstraints = {...})
- DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실제 동작에 영향을 주지 않음
*/

// 필드와 컬럼 매핑
/*
> 객체 -> 테이블 변환
```java
@Entity
public class Member {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    private Integer age;

    @Enumerated(EnumType.STRING)
    // - Enum 타입
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    // - 보통 DB는 Date(날짜) / Time(시간) / Timestamp(날짜와 시간)을 구분함
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    // - 큰 문자열(BLOB / CLOB)
    private String description;

    @Transient
    // - 특정 필드를 컬럼에서 제외
    private int temp;
}
```

```
Hibernate:
    create table Member (
        age integer,
        createdDate timestamp(6),
        id bigint not null,
        lastModifiedDate timestamp(6),
        name varchar(255),
        roleType varchar(255) check (roleType in ('USER','ADMIN')),
        description clob,
        primary key (id)
    )
```

> @Column
- name
    - 필드와 매핑할 테이블의 컬럼 이름
    - 기본값: 객체의 필드 이름

- insertable, updatable
    - 등록, 변경 가능 여부
    - 기본값: True

- nullable
    - null값의 허용 여부를 설정
    - false로 설정하면 DDL 생성 시에 not null 제약 조건을 붙임

- unique
    - 테이블에 unique 조건을 검
    - @Table에 uniqueConstraints와 일치
    - 실무에서 잘 사용 안함(유니크 제약 조건 이름 명시를 할 수 없어서)

- columnDefinition
    - 데이터베이스 컬럼 정보를 직접 줄 수 있음
    - @Column(name = "name", columnDefinition = "varchar(100) default 'EMPTY')
    - 기본값: 필드의 자바 타입과 방언 정보를 사용

- length
    - 문자 길이 제약 조건
    - String 타입에만 사용함
    - 기본값: 255

- precision, scale
    - BigDecimal 타입에서 사용
    - precision은 소수점을 포함한 전체 자릿수
    - scale은 소수의 자릿수
    - 참고로 double, float 타입에는 적용되지 않음

> @Enumerated
- Enum 타입을 매핑할 때 사용
- 속성
    - EnumType.ORDINAL: enum 순서를 데이터베이스에 저장(기본값 - Integer 타입)
    - EnumType.STRING: enum 이름을 데이터베이스에 저장(String 타입)

- 매우 주의: ORDINAL은 절대 사용하지 말 것!!

> @Temporal
- 날짜 타입을 매핑할 때 사용
- 참고: LocalDate, LocalDateTime을 사용할 때는 생략 가능
- 속성
    - TemporalType.DATE: 날짜, 데이터베이스 date 타입과 매핑
    - TemporalType.TIME: 시간, 데이터베이스 time 타입과 매핑
    - TemporalType.TIMESTAMP: 날짜와 시간, 데이터베이스 timestamp 타입과 매핑

> @Lob
- BLOB, CLOB
- 지정할 수 있는 속성이 없음
- 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑

> @Transient
- 필드 매핑 X
- 데이터베이스에 저장 X, 조회 X
- 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용
*/

// 기본 키 매핑
/*
> 기본 키 어노테이션
- @Id
- @GenerateValue
```java
@Id @GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

> 기본 키 매핑 방법
- 직접 할당: @Id만 사용
- 자동 생성: @GeneratedValue
    - identity: 데이터베이스에 위임
    - sequence: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
        - @SequenceGenerator 필요
    - table: 키 생성용 테이블 사용, 모든 DB에서 사용
        - @TableGenerator 필요
    - auto: 방언에 따라 자동 지정(identity, sequence, table), 기본값

> IDENTITY
- 기본 키 생성을 데이터베이스에 위임
- MySQL의 auto_increment
- JPA는 보통 커밋 시점에 insert SQL 실행
- auto_increment는 데이터베이스 insert SQL을 실행 한 이후에 ID 값을 알 수 있음
- IDENTITY 전략은 em.persist() 시점에 즉시 insert SQL 실행하고 DB에서 식별자를 조회

> SEQUENCE
- 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트
- 오라클, PostgreSQL, DB2, H2에서 사용

> SEQUENCE 전략 - 매핑
```java
@Entity
@SequenceGEnerator(
    name = "MEMBER_SEQ_GENERATOR",
    sequenceName = "MEMBER_SEQ", // 매핑할 데이터베이스 시퀀스 이름
    initialValue = 1, allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
```

> SEQUENCE - @SequenceGenerator
- 주의: allocationSize 기본값 = 50
    - name: 식별자 생성기 이름
    - sequenceName: 데이터베이스에 등록되어있는 시퀀스 이름
    - initialValue: DDL 생성 시에만 사용됨, DDL을 생성할 때 처음 1 시작하는 수를 지정
    - allocationSize: 시퀀스 한 번 호출에 증가하는 수, 성능 최적화에 사용 | **기본값 50**
    - catalog, schema

> TABLE
- 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
- 장점: 모든 데이터베이스에 적용 가능
- 단점: 성능

> TABLE - @TableGenerator
- 속성
    - name
    - table
    - initialValue
    - allocationSize

> 권장하는 식별자 전략
- 기본 키 제약 조건: null 아님, 유일, 변하면 안됨
- 권장: Long형 + 대체키 + 키 생성전략 사용

> 기본키와 영속성 컨텍스트
- 영속성 컨텍스트에 들어갈 때, ID(PK)가 필요함

> SEQUENCE 전략의 특징
- start with 1 increment by 1
- em.persist() 시점에 call next value for MEMBER_SEQ로 시퀀스를 가져옴
- insert를 하지는 않음
- 근데 next value for 계속하면 네트워크 비용이 듦
- allocationSize: 50: 50개의 시퀀스를 메모리에 올려둠
    - 1 - 50이 다 차면 50개를 더 호출함(51 - 100)
    - 네트워크 비용을 줄일 수 있음
    - 엄밀히 말하면 1을 먼저 가져오고, 51로 50개를 한 번 더 가져옴
    - **TABLE 전략도 같음**
*/

// 실전 예제 1 - 요구사항 분석과 기본 매핑
/*
> 요구사항 분석
- 회원은 상품을 주문할 수 있음
- 주문 시 여러 상품을 선택할 수 있음

> 코드 참고
- 가급적 제약 조건을 여기 같이 해주는 것이 좋음(유지보수 및 가독성)
- spring boot를 사용하면 orderDate => order_date로 변경해줌(나도 이걸 선호)
    - 지금은 orderDate로 됨

> 데이터 중심 설계의 문제점 => 연관관계 매핑을 사용!!
- 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
- 테이블의 외래키를 객체에 그대로 가져옴
- 객체 그래프 탐색이 불가능
- 참조가 없으므로 UML도 잘못됨
*/
