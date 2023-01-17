package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void testMember() throws Exception{
        //given
        Member member = new Member("member1",10);
        em.persist(member);
        //when

        List<Member> byUsername = memberRepository.findByUsername(member.getUsername());

        //then
        assertThat(byUsername.get(0).getUsername()).isEqualTo(member.getUsername());
    }

}