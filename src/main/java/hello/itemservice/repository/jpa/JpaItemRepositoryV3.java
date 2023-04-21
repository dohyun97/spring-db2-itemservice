package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static hello.itemservice.domain.QItem.*;

@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
         Item item = findById(itemId).orElseThrow();
         item.setItemName(updateParam.getItemName());
         item.setQuantity(updateParam.getQuantity());
         item.setPrice(updateParam.getPrice());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }
    //아래 더 좋게 리팩토링한 방법이 있어
    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();
        if(StringUtils.hasText(itemName)){
            builder.and(item.itemName.like("%"+itemName+"%"));   //QItem.item.itemName
        }
        if(maxPrice != null){
            builder.and(item.price.loe(maxPrice));      //QItem.item.price
        }

        List<Item> result = query
                .select(item)       //QItem.item
                .from(item)         //QItem.item
                .where(builder)
                .fetch();
        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        List<Item> result = query
                .select(item)       //QItem.item
                .from(item)         //QItem.item
                .where(likeItemName(itemName),maxPrice(maxPrice))
                .fetch();
        return result;

    }

    private BooleanExpression likeItemName(String itemName){
        if(StringUtils.hasText(itemName)){
            return item.itemName.like("%"+itemName+"%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice){
        if(maxPrice != null){
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
