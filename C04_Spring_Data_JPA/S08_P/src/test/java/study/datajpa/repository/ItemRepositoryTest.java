package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
        public void save() {
            Item item = new Item("A");
            // - ID가 들어있으면 persist가 아닌 merge로 동작함
            // - DB에 A라는 아이디를 가진 객체가 있는지 찾아옴 (select 수행)
            // - 이후에 insert를 수행함
            itemRepository.save(item);
        }
    //    // 새로운 엔티티를 판단하는 기본 전략
    //    // - 식별자가 객체일 때 null로 판단
    //    // - 식별자가 자바 기본 타입일 때 "0"으로 판단
}