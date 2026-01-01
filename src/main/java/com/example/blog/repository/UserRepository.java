package com.example.blog.repository;

import com.example.blog.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByEmail(String email);

  @EntityGraph(attributePaths = {"profile"})
  Optional<User> findByUsername(String username);


  @Modifying
  @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
  void incrementFollowingCount(Long userId);

  @Modifying
  @Query("UPDATE User u SET u.followerCount = u.followerCount + 1 WHERE u.id = :userId")
  void incrementFollowerCount(Long userId);


  @Modifying
  @Query("UPDATE User u SET u.followerCount = u.followerCount - 1 " +
          "WHERE u.id = :userId AND u.followerCount > 0")
  void decrementFollowerCount(Long userId);

  @Modifying
  @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 " +
          "WHERE u.id = :userId AND u.followingCount > 0")
  void decrementFollowingCount(Long userId);


  @Query("""
        select u from User u
        where u.username = :username
        """)
  Optional<User> findByUsernameNoFetchProfile(String username);
}
