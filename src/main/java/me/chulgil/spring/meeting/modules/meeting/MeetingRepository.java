package me.chulgil.spring.meeting.modules.meeting;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Transactional(readOnly = true)
 * 스프링 프레임워크가 하이버네이트 세션 플러시 모드를 Manual로 설정한다.
 * 트랜잭션을 커밋하더라도 영속성 컨텍스트가 플러시 되지 않아서 엔티티의 수정이 동작하지 않고
 * 변경감지를 위한 스냅샷을 보관하지 않기때문에 성능이 향상된다.
 */
@Transactional(readOnly = true)
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Meeting findByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Meeting findMeetingWithZonesByPath(String path);

}
