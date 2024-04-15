package com.ohgriaffers.section02.crud;

import com.ohgriaffers.section01.entitymanager.EntityManagerGenerator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class EntityManagerCRUD {
    private EntityManager entityManager;
    /*1. 특정 코드로 조회하는 기능*/
    public Menu findMenuByMenuCode(int menuCode){
        entityManager = EntityManagerGenerator.getInstance();
        return entityManager.find(Menu.class,menuCode);
    }

    /*2. 메뉴 데이터 저장하는 기능*/
    public Long saveAndReturnAllCount(Menu newMenu){
        entityManager = EntityManagerGenerator.getInstance();

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        entityManager.persist(newMenu);

        entityTransaction.commit();
        return getCount(entityManager);
    }

    private Long getCount(EntityManager entityManager) {

        //JPQL 문법 -> 나중에 별도 챕터에서 다룸
        return entityManager.createQuery("SELECT COUNT(*) FROM Section02Menu",Long.class).getSingleResult();
    }

    /*3. 메뉴 데이터 수정하는 기능*/
    public Menu modifyMenuName(int menuCode,String menuName){
        /*수정하고 싶은 값을 우선 가져온ㄷ다. */
        entityManager = EntityManagerGenerator.getInstance();
        Menu foundMenu = entityManager.find(Menu.class,menuCode);

        EntityTransaction transaction = entityManager.getTransaction();

        /*begin과 commit사이에 db변화 로직을 작성하여 안전하게 db변화를 준다.*/
        transaction.begin();
        foundMenu.setMenuName(menuName);
        transaction.commit();
        return foundMenu;
    }

    /*4. 삭제하는 기능*/
    public Long removeAndReturnAllCount(int menuCode){
        entityManager = EntityManagerGenerator.getInstance();
        Menu foundMenu = entityManager.find(Menu.class,menuCode);

        EntityTransaction transaction = entityManager.getTransaction();

        /*begin과 commit사이에 db변화 로직을 작성하여 안전하게 db변화를 준다.*/
        transaction.begin();
        entityManager.remove(foundMenu);

        transaction.commit();

        return getCount(entityManager);
    }
}
