package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Transactional
@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{ //MemberRepositoryCustom 구현체
//public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom{ //QuerydslRepositorySupport

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberRepository;


//    public MemberRepositoryImpl(EntityManager em, JPAQueryFactory queryFactory, MemberJpaRepository memberRepository) {
//        super(Member.class);
//        this.em = em;
//        this.queryFactory = queryFactory;
//        this.memberRepository = memberRepository;
//    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        ,(teamNameEq(condition.getTeamName()))
                        ,(ageGeo(condition.getAgeGoe()))
                        ,(ageLeo(condition.getAgeLoe())))
                .fetch();
    }

//    @Override
//    public List<MemberTeamDto> searchSupport(MemberSearchCondition condition) {
//        return from(member)
//                .leftJoin(member.team, team)
//                .where(usernameEq(condition.getUsername())
//                        , (teamNameEq(condition.getTeamName()))
//                        , (ageGeo(condition.getAgeGoe()))
//                        , (ageLeo(condition.getAgeLoe())))
//                .select(new QMemberTeamDto(
//                        member.id.as("memberId"),
//                        member.username,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")
//                )).fetch();
//    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable page) {
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , (teamNameEq(condition.getTeamName()))
                        , (ageGeo(condition.getAgeGoe()))
                        , (ageLeo(condition.getAgeLoe())))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), page, results.getTotal());
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable page) {
        List<MemberTeamDto> content = getMemberTeamDtos(condition, page);
        Long totalCount = getTotalCount(condition);

        return new PageImpl<>(content, page, totalCount);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex2(MemberSearchCondition condition, Pageable page) {
        List<MemberTeamDto> content = getMemberTeamDtos(condition, page);
        JPAQuery<Long> totalCount = getTotalCount2(condition);
        return PageableExecutionUtils.getPage(content,page, totalCount::fetchCount);
    }

    private Long getTotalCount(MemberSearchCondition condition) {
        return queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , (teamNameEq(condition.getTeamName()))
                        , (ageGeo(condition.getAgeGoe()))
                        , (ageLeo(condition.getAgeLoe())))
                .orderBy(team.name.desc(),member.username.desc())
                .fetchOne();
    }

    private JPAQuery<Long> getTotalCount2(MemberSearchCondition condition) {
        return queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , (teamNameEq(condition.getTeamName()))
                        , (ageGeo(condition.getAgeGoe()))
                        , (ageLeo(condition.getAgeLoe())));
    }

    private List<MemberTeamDto> getMemberTeamDtos(MemberSearchCondition condition, Pageable page) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                        , (teamNameEq(condition.getTeamName()))
                        , (ageGeo(condition.getAgeGoe()))
                        , (ageLeo(condition.getAgeLoe())))
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
        return content;
    }

    private BooleanExpression usernameEq(String username) {return username != null ? member.username.eq(username) : null;}
    private BooleanExpression teamNameEq(String teamName) {
        return teamName != null ? team.name.eq(teamName) : null;
    }
    private BooleanExpression ageGeo(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
    private BooleanExpression ageLeo(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}
