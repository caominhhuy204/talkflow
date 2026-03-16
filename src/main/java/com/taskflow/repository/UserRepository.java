package com.taskflow.repository;

import com.taskflow.entity.User;
import com.taskflow.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByResetPasswordTokenHashAndResetPasswordTokenExpiresAtAfter(
            String resetPasswordTokenHash,
            LocalDateTime now
    );

    List<User> findByRoleOrderByFullNameAsc(Role role);

    @Query("""
            select u
            from User u
            where u.role = com.taskflow.entity.Role.EMPLOYEE
              and (lower(u.fullName) like lower(concat('%', :query, '%'))
                   or lower(u.email) like lower(concat('%', :query, '%')))
            order by u.fullName asc
            """)
    List<User> searchEmployeesByQuery(@Param("query") String query);
}
