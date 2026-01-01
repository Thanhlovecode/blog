package com.example.blog.repository;

import com.example.blog.domain.Follow;
import com.example.blog.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Đếm số followers của một user
     * Performance: Index scan on following_id
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countFollowersByUserId(Long userId);

    /**
     * Đếm số following của một user
     * Performance: Index scan on follower_id
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowingByUserId(Long userId);


    @Query("""
               select f.following.id from Follow f
               where f.follower.id = :userId
            """)
    Page<Long> findFollowingByUserId(Long userId,Pageable pageable);


    @Query("""
               select f.follower.id from Follow f
               where f.following.id = :userId
            """)
    Page<Long> findFollowerByUserId(Long userId,Pageable pageable);



    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);


}
