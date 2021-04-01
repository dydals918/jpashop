package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderSerivceTest {

    @Autowired EntityManager em;
    @Autowired OrderSerivce orderSerivce;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderSerivce.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 수", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격이 맞는가", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("재고 정정", 8, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_상품제고초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;
        //when
        orderSerivce.order(member.getId(), item.getId(), orderCount);

        //then
        fail("재고 부족 예외가 발생해야 합니다.");

    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderSerivce.order(member.getId(), item.getId(), orderCount);
        //when
        orderSerivce.orderCancel(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals("주문 취소 시 상태는 CANCEL이여야 한다.", OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals("주문 취소 시 수량이 증가해야 한다.", 10, item.getStockQuantity());

    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("대구", "111", "123-123"));
        em.persist(member);
        return member;
    }

}