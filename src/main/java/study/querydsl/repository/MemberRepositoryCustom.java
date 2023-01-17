package study.querydsl.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;


import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
//    List<MemberTeamDto> searchSupport(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable page);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable page);
    Page<MemberTeamDto> searchPageComplex2(MemberSearchCondition condition, Pageable page);
}
