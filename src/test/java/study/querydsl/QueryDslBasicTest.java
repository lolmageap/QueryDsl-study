package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
@Commit
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @PersistenceUnit
    EntityManagerFactory emf;
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
    public void startJPQL(){
        String username = "member1";
        Member findMember = em.createQuery("select m from Member m where m.username =:username", Member.class)
                .setParameter("username", username)
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQueryDSL(){

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))//???????????? ????????? ??????
                .fetchOne();

        System.out.println("findMember = " + findMember);

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        Member findMember = queryFactory.
                selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory.
                selectFrom(member)
                .where(
                        member.username.eq("member1")
                        ,(member.age.between(10,30))
                        ,null
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void ResultFetch(){
//        List<Member> findMembers = queryFactory.
//                selectFrom(member)
//                .fetch();

//        Member fetchOne = queryFactory.
//                selectFrom(member)
//                .fetchOne();

//        Member fetchFirst = queryFactory.
//                selectFrom(member)
//                .fetchFirst();

        QueryResults<Member> fetchResults = queryFactory.
                selectFrom(member)
                .fetchResults();

        long l = queryFactory.selectFrom(member)
                .fetchCount();

        int size = queryFactory.selectFrom(member)
                .fetch().size();

        long total = fetchResults.getTotal();
        long limit = fetchResults.getLimit();
        long offset = fetchResults.getOffset();
        List<Member> results = fetchResults.getResults();

//        assertThat(findMembers.size()).isEqualTo(4);
//        System.out.println("findMembers = " + findMembers);
//        System.out.println("fetchOne = " + fetchOne);
//        System.out.println("fetchFirst = " + fetchFirst);
        System.out.println("total = " + total);
        System.out.println("limit = " + limit);
        System.out.println("offset = " + offset);
        System.out.println("results = " + results);
        System.out.println("l = " + l);
        System.out.println("size = " + size);
        System.out.println("fetchResults = " + fetchResults);
    }


    /*
    ?????? ?????? ??????
    1. ?????? ?????? ???????????? desc
    2. ???????????? asc
    ! 2?????? ??????????????? ????????? ???????????? ?????? (null last)
     */
    @Test
    public void sort(){

        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member(null,102));

        List<Member> findMember = queryFactory.
                selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        assertThat(findMember.get(0).getUsername()).isEqualTo("member5");
        assertThat(findMember.get(1).getUsername()).isNull();
        System.out.println("findMember = " + findMember);

    }
    @Test
    public void paging1(){

        List<Member> findMember = queryFactory.
                selectFrom(member)
                .orderBy(member.age.desc())
                .offset(1)
                .limit(2)
                .fetch();

        for (Member m : findMember) {
            System.out.println("m = " + m);
        }

    }
    @Test
    public void paging2(){

        QueryResults<Member> findMember = queryFactory.
                selectFrom(member)
                .orderBy(member.age.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        System.out.println("findMember = " + findMember.getTotal());
        System.out.println("findMember = " + findMember.getOffset());
        System.out.println("findMember = " + findMember.getLimit());


    }
    @Test
    public void aggregation(){

        List<Tuple> fetch = queryFactory.
                select(member.count(), member.age.sum(),
                        member.age.avg(),member.age.max(),member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /*
    ?????? ????????? ??? ?????? ?????? ????????? ?????????
     */
        @Test
            public void group() throws Exception{

            //given
            List<Tuple> fetch = queryFactory.select(team.name, member.age.avg())
                    .from(member)
                    .join(member.team, team)
                    .groupBy(team.name)
                    .fetch();

            //when
            Tuple teamA = fetch.get(0);
            Tuple teamB = fetch.get(1);

             //then
            assertThat(teamA.get(team.name)).isEqualTo("teamA");
            assertThat(teamA.get(member.age.avg())).isEqualTo(15);

            assertThat(teamB.get(team.name)).isEqualTo("teamB");
            assertThat(teamB.get(member.age.avg())).isEqualTo(35);

            }

    /*
       TeamA??? ????????? ????????????
    */
    @Test
    public void join(){

        List<Member> teamA = queryFactory.
                selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(teamA)
                .extracting("username")
                        .containsExactly("member1" , "member2");

        System.out.println("teamA = " + teamA);
        System.out.println("teamA = " + teamA.get(0).getTeam());
        System.out.println("teamA = " + teamA.get(1).getTeam());

    }

    /*
        ????????????
        ????????? ????????? ??? ????????? ?????? ?????? ??????
     */
    @Test
    public void thetaJoin(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> fetch = queryFactory.
                select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }

    }
    /*
        ????????? ?????? ??????????????? ???????????? teamA??? ?????? ?????? , ????????? ?????? ??????
     */
    @Test
    public void join_on_filtering(){

        List<Tuple> fetch = queryFactory.
                select(member, team)
                .from(member)
                .leftJoin(member.team , team) //member.team <-> id ?????? ????????? ?????? -> ?????? ??? team.name.eq("teamA") ??????
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

    }

    /*
        ????????? ?????? ??????????????? ???????????? teamA??? ?????? ?????? , ?????? ?????? ??????
    */
    @Test
    public void join_on_filtering2(){

        List<Tuple> fetch = queryFactory.
                select(member, team)
                .from(team)
                .where(team.name.eq("teamA"))
                .leftJoin(team.members , member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

    }
    /*
        ??????????????? ?????? ????????? ????????????
        ????????? ????????? ??? ????????? ?????? ?????? ?????? ?????? //????????????
     */
    @Test
    public void join_on_no_relation(){

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> teamA = queryFactory.
                select(member, team)
                .from(member)
                .leftJoin(team) // ?????? ?????? ???????????? on?????? username?????? ?????? -> ?????? ?????? ?????? ??? member.username.eq(team.name)??? ??????
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : teamA) {
            System.out.println("tuple = " + tuple);
        }
    }


    @Test
    public void fetchJoinNo(){
        //given
        em.flush();
        em.clear();
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); //lazyLoading ??? ??? ???????????? ??????????????? ????????? ?????? ??????
        assertThat(loaded).isFalse();
        //then
    }

    @Test
    public void fetchJoinUse(){
        //given
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); //lazyLoading ??? ??? ???????????? ??????????????? ????????? ?????? ??????
        assertThat(loaded).isTrue();
        //then
    }
    /*
     ????????? ?????? ?????? ?????? ??????
     */
    @Test
    public void subQuery(){
        QMember memberSub = new QMember("memberSub");

        Member findMember = queryFactory.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max()).from(memberSub)
                ))
                .fetchOne();

        System.out.println("findMember = " + findMember);
    }
    /*
     ????????? ?????? ????????? ??????
     */
    @Test
    public void subQueryGoe(){
        QMember memberSub = new QMember("memberSub");

        List<Member> findMembers = queryFactory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg()).from(memberSub)
                ))
                .fetch();

        for (Member findMember : findMembers) {
            System.out.println("findMember = " + findMember);
        }
    }

    @Test
    public void subQueryIn(){
        QMember memberSub = new QMember("memberSub");

        List<Member> findMembers = queryFactory.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age).from(memberSub)
                                .where(member.age.loe(20))
                ))
                .fetch();

        for (Member findMember : findMembers) {
            System.out.println("findMember = " + findMember);
        }
    }

    @Test
    public void selectSubQuery(){
        QMember memberSub = new QMember("memberSub");

        List<Tuple> findMembers = queryFactory.select(member.username,
                        JPAExpressions.select(member.age.avg()).from(member))
                .from(member)
                .fetch();

        for (Tuple findMember : findMembers) {
            System.out.println("findMember = " + findMember);
        }
    }

    @Test
    public void basicCase(){

        List<Tuple> fetch = queryFactory
                .select(member.username, member.age.when(10).then("??????")
                        .when(20).then("?????????")
                        .when(30).then("?????????")
                        .otherwise("??????"), team)
                .from(member)
                .leftJoin(team)
                .on(member.team.eq(team).and(team.name.eq("teamB")))
                .fetch();

        //?????? ??????
//        List<Tuple> fetch = queryFactory
//                .select(member.username, member.age.when(10).then("??????")
//                        .when(20).then("?????????")
//                        .when(30).then("?????????")
//                        .otherwise("??????"), team)
//                .from(member)
//                .leftJoin(member.team, team)
//                .on(team.name.eq("teamB"))
//                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }
    @Test
    public void complexCase(){
        List<Tuple> fetch = queryFactory.select(
                member.username, new CaseBuilder().when(member.age.between(0,20)).then("?????????")
                        .when(member.age.between(21,40)).then("????????????")
                        .otherwise("????????????"), member.team.name)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test // ?????? ??????
    public void constant(){
        List<Tuple> fetch = queryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

    }

    @Test //???????????????
    public void concat(){
        List<String> fetch = queryFactory.select(member.username.concat("_")
                        .concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void simpleProjection(){
        List<String> fetch = queryFactory.select(member.username)
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection(){
        List<Tuple> fetch = queryFactory.select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple s : fetch) {
            String s1 = s.get(member.username);
            Integer integer = s.get(member.age);
            System.out.println("s1 = " + s1);
            System.out.println("integer = " + integer);
        }
    }

    @Test
    public void findDtoByJpql(){
        List jpql = em.createQuery("select new study.querydsl.dto.MemberDto(m.username,m.age)" +
                " from Member m", MemberDto.class).getResultList();

        for (Object o : jpql) {
            System.out.println("o = " + o);
        }

    }

    @Test // Setter ????????? DTO??? ?????????????????? ????????????
    public void findDtoByQueryDslSetter(){
        List<MemberDto> fetch = queryFactory.select(Projections.bean
                        (MemberDto.class,
                                member.username,
                                member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }

    }

    @Test //Field ????????? DTO??? ?????????????????? ?????????????????? DTO??? @setter??? ????????? ????????? ?????????
    public void findDtoByQueryDslField(){
        List<MemberDto> fetch = queryFactory.select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test //????????? ????????? DTO??? ?????????????????? @setter??? ????????? ????????? ?????????
    public void findDtoByQueryDslConstructor(){
        List<MemberDto> fetch = queryFactory.select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test // Field ????????? ???????????? Dto ??????(?????????)??? ???????????? alias??? ??????????????????  ???????????? ????????? null ??? ??????
    public void findAnotherDtoByQueryDslField(){
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory.select(Projections.fields(UserDto.class,
                        member.username.as("name"),

                        Expressions.as(
                                JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")  // and ??????????????? alias ??????
                ))
                .from(member)
                .fetch();

        for (UserDto member : fetch) {
            System.out.println("member = " + member);
        }
    }

    @Test //????????? ????????? Dto??? ??????(?????????)??? ????????? ???????????? ????????????????????? ?????? ???????????? ????????? alias ????????????
    public void findAnotherDtoByQueryDslConstructor(){
        List<UserDto> fetch = queryFactory.select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : fetch) {
            System.out.println("userDto = " + userDto);
        }
    }
    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> fetch = queryFactory.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void dynamicQueryBooleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void dynamicQueryWhereParam(){
        String usernameCond = "member1";
        Integer ageCond = null;

        List<Member> result = searchMember2(usernameCond, ageCond);
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory.selectFrom(member)
//                .where(usernameEq(usernameCond).and(ageEq(ageCond)))
                .where(allEq(usernameCond,ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private Predicate allEq(String usernameCond, Integer ageCond){
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null){
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory.selectFrom(member)
                .where(builder)
                .fetch();
    }

    //?????? ??????
    @Test
    public void bulkUpdate(){
        //given
        long count = queryFactory.update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(28))
                .execute();

        //when
        em.flush();
        em.clear();

        List<Member> fetch = queryFactory.selectFrom(member)
                .fetch();
        //then
        for (Member fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }

    @Test
    public void bulkAdd(){
        //given
        long count = queryFactory.update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }
    @Test
    public void bulkMultiply(){
        //given
        long count = queryFactory.update(member)
                .set(member.age, member.age.multiply(2))
                .execute();
    }
    @Test
    public void bulkDelete(){
        //given
        long count = queryFactory.delete(member)
                .where(member.age.loe(21))
                .execute();

        assertThat(count).isEqualTo(2);
    }

}
