package com.ohgiraffers.section02.crud;

import com.ohgriaffers.section02.crud.EntityManagerCRUD;
import com.ohgriaffers.section02.crud.Menu;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityManagerCRUDTests {

    private EntityManagerCRUD crud;

    @BeforeEach
    void initManager(){
        this.crud = new EntityManagerCRUD();
    }

    @DisplayName("메뉴 코드로 메뉴 조회 테스트")
    @ParameterizedTest //하나의 테스트 메소드로 여러 개의 파라미터에 대한 테스트가 가능함
    @CsvSource({"1,1","2,2","3,3"})
    void testFindMethodByMenuCode(int menuCode,int expected){
        // when
        Menu foundMenu = crud.findMenuByMenuCode(menuCode);

        // then
        assertEquals(expected,foundMenu.getMenuCode());
        System.out.println("foundMenu = " + foundMenu);
    }

    private static Stream<Arguments> newMenu(){
        return Stream.of(
                Arguments.of(
                        "신메뉴",
                        35000,
                        4,
                        "Y"
                ),
                Arguments.of(
                        "신메뉴1",
                        25000,
                        3,
                        "N"
                )
        );
    }

    @DisplayName("새로운 메뉴 추가 테스트")
    @ParameterizedTest
    @Transactional
    @MethodSource("newMenu")
    void testRegist(String menuName,int menuPrice,int categoryCode, String orderableStatus){
        Menu menu = new Menu(menuName,menuPrice,categoryCode,orderableStatus);
        Long count = crud.saveAndReturnAllCount(menu);

        assertEquals(53,count);
    }

    @DisplayName("메뉴 이름 수정 테스트")
    @ParameterizedTest
    @CsvSource("1, 변경 된 이름")
    void testModifyMenuName(int menuCode,String menuName){
        //when
        Menu modifiedMenu = crud.modifyMenuName(menuCode,menuName);
        //then
        assertEquals(menuName,modifiedMenu.getMenuName());
    }

    @DisplayName("메뉴 삭제 테스트")
    @ParameterizedTest
    @ValueSource(ints = {95})
    void testRemoveMenu(int menuCode){
        //when
        Long count = crud.removeAndReturnAllCount(menuCode);

        //then
        assertEquals(54,count);
    }

}
