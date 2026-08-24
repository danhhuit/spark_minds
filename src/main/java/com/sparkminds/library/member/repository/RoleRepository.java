package com.sparkminds.library.member.repository;

import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}