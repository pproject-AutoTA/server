package com.autota.server.domain.user.repository;

import com.autota.server.domain.user.entity.Role;
import com.autota.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    /** 파생 쿼리는 find와 By 사이 이름으로 컬럼을 고르지 않아 role만 직접 조회한다. 없는 id면 null. */
    @Query("select u.role from User u where u.id = :id")
    Role findRoleById(@Param("id") Long id);
}
