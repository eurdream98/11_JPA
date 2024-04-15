package com.ohgriaffers.section03.entity;

import jakarta.persistence.EntityManager;

public class EntityLifeCycle {
    private EntityManager entityManager;
    public Menu findMenuByMenuCode(int menuCode){
        entityManager = EntityManagerGenerator.getInstance();
        return entityManager.find(Menu.class,menuCode);
    }

    public EntityManager getManagerInstance(){
        return entityManager;
    }
}
