package com.sparkminds.library.member.repository;

import com.sparkminds.library.member.entity.Role;
import com.sparkminds.library.member.entity.RoleName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  @EntityGraph(attributePaths = "permissions")
  Optional<Role> findByName(RoleName name);

  @EntityGraph(attributePaths = "permissions")
  List<Role> findAllByOrderByNameAsc();
}
