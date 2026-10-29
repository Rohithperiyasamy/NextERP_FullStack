package com.enterprise.workflow.repositories;


import com.enterprise.workflow.entity.User;
import com.enterprise.workflow.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // find user by email — used for login
    Optional<User> findByEmail(String email);

    // find all users with a specific role
    List<User> findByRole(Role role);

    // check if email already exists
    boolean existsByEmail(String email);

    long countByRole(Role role);

}