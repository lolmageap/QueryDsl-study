package study.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Commit
public class MemberTeamTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

//        List<Member> result1 = memberRepository.findAll_Querydsl();
//        assertThat(result1).containsExactly(member);

//        List<Member> result2 =
//                memberRepository.findByUsername_Querydsl("member1");
//        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTestBuilder(){ //search query, 동적쿼리는 데이터가 많을 때 limit(paging)이나 기본 조건을 걸어주는게 좋다
        //given
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //when
        List<MemberTeamDto> memberTeamDtos = memberRepository.search(condition);

        for (MemberTeamDto memberTeamDto : memberTeamDtos) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }
        //then
        assertThat(memberTeamDtos.size()).isEqualTo(1);
        assertThat(memberTeamDtos).extracting("username").containsExactly("member4");
    }

}
