package study.datajpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Entity
//@Getter
@EntityListeners(AuditingEntityListener.class)
public class Item implements Persistable<String> {

    @Id
//    @GeneratedValue
    private String id;

    @CreatedDate // persist가 호출되기 전에 insert됨
    private LocalDateTime createdDate;

    protected Item() {

    }

    public Item(String id) {
        this.id = id;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        // 새것인지 아닌지에 대해서 로직을 작성해야함
        // - 생성일로 풀어낼 수도 있음
        // - 즉, 내가 ID를 주입해서 한다면 기본 적략이 merge로 동작함
        // - merge는 우선 DB를 호출해서 값을 확인하고 없으면 새로운 엔티티로 인지하므로 매우 비효율적임
        return createdDate == null; // 새로운 객체임
    }
}
