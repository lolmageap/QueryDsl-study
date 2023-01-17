package study.querydsl.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Commit;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
@Commit
class MemberTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void a(){
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
    public void testEntity() {

        //초기화
        em.flush();
        em.clear();
        //확인
        List<Member> members = em.createQuery("select m from Member m",
                        Member.class)
                .getResultList();
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }
    }
    @Test
    public void testSearch() {
        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0,3);

        Page<MemberTeamDto> result1 = memberRepository.searchPageSimple(condition, pageRequest);
        Page<MemberTeamDto> result2 = memberRepository.searchPageComplex(condition, pageRequest);

        assertThat(result1.getSize()).isEqualTo(3);
        assertThat(result1.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

        assertThat(result2.getSize()).isEqualTo(3);
        assertThat(result2.getContent()).extracting("username").containsExactly("member1", "member2", "member3");

        for (MemberTeamDto memberTeamDto : result1) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }
        for (MemberTeamDto memberTeamDto2 : result2) {
            System.out.println("memberTeamDto2 = " + memberTeamDto2);
        }

    }

    @Test
    public void testMember(){
        Iterable findMembers = memberRepository
                .findAll(member.age.between(20, 40)
                .and(member.username.contains("member3")));

        for (Object o : findMembers) {
            System.out.println("o = " + o);
        }
    }

}