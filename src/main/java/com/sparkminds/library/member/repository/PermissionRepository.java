package com.sparkminds.library.member.repository;

import com.sparkminds.library.member.entity.Permission;
import com.sparkminds.library.member.entity.PermissionName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

  Optional<Permission> findByName(PermissionName name);

  List<Permission> findAllByNameIn(Collection<PermissionName> names);
}
