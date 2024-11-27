package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderId(String providerId);

    @Query("SELECT m.memberId FROM Member m WHERE m.lastActivity >= :week")
    List<Long> findActiveMembers(@Param("week") LocalDateTime week);

}
