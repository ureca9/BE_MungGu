package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderId(String providerId);

    boolean existsByNickname(String nickname);

    @Query("SELECT m FROM Member m JOIN FETCH m.profileImage WHERE m.memberId = :memberId")
    Optional<Member> findMemberWithProfileImage(Long memberId);
}
