package com.example.blog.repository;

import com.example.blog.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByEmail(String email);

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByUsername(String username);


  @Query("""
        select u from User u
        where u.username = :username
        """)
  Optional<User> findByUsernameNoFetchProfile(String username);
}
