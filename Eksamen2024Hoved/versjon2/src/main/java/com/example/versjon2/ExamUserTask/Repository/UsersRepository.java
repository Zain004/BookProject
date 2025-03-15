package com.example.versjon2.ExamUserTask.Repository;

import com.example.versjon2.ExamUserTask.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    List<Users> findAllByOrderByFirstNameAsc(); // Stigende sortering
    List<Users> findAllByOrderByFirstNameDesc(); // Synkende sortering
}
