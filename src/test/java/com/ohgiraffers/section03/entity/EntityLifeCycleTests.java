package com.ohgiraffers.section03.entity;

import com.ohgriaffers.section03.entity.EntityLifeCycle;
import com.ohgriaffers.section03.entity.EntityManagerGenerator;
import com.ohgriaffers.section03.entity.Menu;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class EntityLifeCycleTests {
    private EntityLifeCycle lifeCycle;

    @BeforeEach
    void setUp(){
        this.lifeCycle = new EntityLifeCycle();
    }

    @DisplayName("비영속 테스트")
    @ParameterizedTest
    @ValueSource(ints = {1,2})
    void testTransient(int menuCode){
        Menu foundMenu = lifeCycle.findMenuByMenuCode(menuCode);

        Menu newMenu = new Menu(
                foundMenu.getMenuCode(),
                foundMenu.getMenuName(),
                foundMenu.getMenuPrice(),
                foundMenu.getCategoryCode(),
                foundMenu.getOrderableStatus()
        );

        EntityManager entityManager = lifeCycle.getManagerInstance();

        //then
        assertNotEquals(foundMenu,newMenu);
        assertTrue(entityManager.contains(foundMenu)); //영속성 컨텍스트에서 관리되는 객체이다.
        assertFalse(entityManager.contains(newMenu)); //newMenu는 영속성 컨텍스트에서 관리 되지 않는 객체이다.
    }

    @DisplayName("다른 엔티티 매니저가 관리하는 엔티티의 영속성 테스트")
    @ParameterizedTest
    @ValueSource(ints = {1,2})
    void testManagedOtherEntityManager(int menuCode){
        //when
        Menu menu1 = lifeCycle.findMenuByMenuCode(menuCode);
        Menu menu2 = lifeCycle.findMenuByMenuCode(menuCode);

        //then
        assertNotEquals(menu1,menu2);
    }

    @DisplayName("같은 엔티티 매니저가 관리하는 엔티티의 영속성 테스트")
    @ParameterizedTest
    @ValueSource(ints = {1,2})
    void testManagedSameEntityManager(int menuCode){
        //given
        EntityManager manager = EntityManagerGenerator.getInstance();
        //when
        Menu menu1 = manager.find(Menu.class,menuCode);
        Menu menu2 = manager.find(Menu.class,menuCode);
        //then
        assertEquals(menu1,menu2);
    }

    @DisplayName("준영속화 detach 테스트")
    @ParameterizedTest
    @CsvSource({"11,1000","12,1000"})
    void testDetachEntity(int menuCode,int menuPrice){
        //given
        EntityManager entityManager = EntityManagerGenerator.getInstance();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        //when
        entityTransaction.begin();
        Menu foundMenu = entityManager.find(Menu.class,menuCode);
        // detach : 특정 엔티티만 준영속 상태(영속성 컨텍스트가 관리하던 객체를 관리하지 않는 상태)를 만든다.
        entityManager.detach(foundMenu);
        foundMenu.setMenuPrice(menuPrice);
        //db에 commit하지 않는다. 반영하지 않는다.
        //flush : 영속성 컨텍스트의 상태를 DB로 내보낸다. commit하지 않은 상태이므로 rollback 가능하다.
        entityManager.flush();

        //then
        assertNotEquals(menuPrice,entityManager.find(Menu.class,menuCode).getMenuPrice());
        entityTransaction.rollback();
    }

    @DisplayName("준영속화 detach 후 다시 영속화 테스트")
    @ParameterizedTest
    @CsvSource({"11,1000","12,1000"})
    void testDetachAndMerge(int menuCode,int menuPrice){
        //given
        EntityManager entityManager = EntityManagerGenerator.getInstance();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        //when
        entityTransaction.begin();
        Menu foundMenu = entityManager.find(Menu.class,menuCode);
        entityManager.detach(foundMenu);
        foundMenu.setMenuPrice(menuPrice);
        //merge:파라미터로 넘어온 준영속 엔티티 객체의 식별자 값으로 1차 캐시에서 엔티티 객체를 조회한다.(없으면 DB에서 조회하여 1차 캐시에 저장한다.)
        //조회한 영속 엔티티 객체에 준영속 상태의 엔티티 객체의 값을 병합 한 뒤 영속 엔티티 객체를 반환한다.
        //혹은 조회 할 수 없는 데이터라면 새로 생성해서 병합한다.
        entityManager.merge(foundMenu);
        entityManager.flush();

        //then
        assertEquals(menuPrice,entityManager.find(Menu.class,menuCode).getMenuPrice());
        entityTransaction.rollback();
    }
    @DisplayName("detach후 merge한 데이터 update 테스트")
    @ParameterizedTest
    @CsvSource({"11,하양 민트초코 죽","12,까만 딸기탕후루"})
    void testMergeUpdate(int menuCode,String menuName){
        //given
        EntityManager entityManager = EntityManagerGenerator.getInstance();
        Menu foundMenu = entityManager.find(Menu.class,menuCode);
        entityManager.detach(foundMenu);

        //when
        foundMenu.setMenuName(menuName);
        Menu refoundMenu = entityManager.find(Menu.class,menuCode);

        entityManager.merge(foundMenu);
        //then
        assertEquals(menuName,refoundMenu.getMenuName());
    }

    @DisplayName("detach 후 merge한 데이터 save 테스트")
    @Test
    void testMergeSave(){
        //given
        EntityManager entityManager = EntityManagerGenerator.getInstance();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        Menu foundMenu =  entityManager.find(Menu.class,20);
        entityManager.detach(foundMenu);

        entityTransaction.begin();
        foundMenu.setMenuName("치약맛 초코 아이스크림");
        foundMenu.setMenuCode(999);
        entityManager.merge(foundMenu);
        entityTransaction.commit();

        //then
        assertEquals("치약맛 초코 아이스크림",entityManager.find(Menu.class,999).getMenuName());

    }
}
