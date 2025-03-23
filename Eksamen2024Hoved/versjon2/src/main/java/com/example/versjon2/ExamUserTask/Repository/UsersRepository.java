package com.example.versjon2.ExamUserTask.Repository;

import com.example.versjon2.ExamUserTask.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {
    Optional<Users> findByEmail(String email);
    List<Users> findAllByOrderByFirstNameAsc(); // Stigende sortering
    List<Users> findAllByOrderByFirstNameDesc(); // Synkende sortering
}
