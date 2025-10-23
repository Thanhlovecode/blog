package com.example.blog.repository;

import com.example.blog.domain.Post;
import com.example.blog.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"postContent"})
    Optional<Post> findPostWithContentBySlug(String slug);

    @Query("""
            select p from Post p where p.slug = :slug
            """)
    Optional<Post> findPostWithoutContentBySlug(String slug);


    @Query("""
              update Post p set p.thumbnailUrl = :thumbnailUrl where p.user.id = :userId
            """)
    @Modifying
    void updateUserThumbnailUrl(Long userId, String thumbnailUrl);


    @Query("""
            select p.id from Post p
            where p.status = :status
            """)
    Page<Long> findNewestPostsByStatus(PostStatus status, Pageable pageable);

    @Query("""
                 SELECT p.id FROM Post p
                 WHERE p.username = :username AND p.status = :status
            """)
    Page<Long> findPostIdsByUsernameAndStatus(String username, PostStatus status, Pageable pageable);

    @Query("""
            select p.id from Post p
            join p.tags t
            where t.slug = :slug and p.status = :status
            """)
    Page<Long> findPostIdsByTagSlug(String slug, PostStatus status, Pageable pageable);


    @Query(value = """
            select p.id from posts p
            where match(p.title)  against(:keyword)
            and p.status = :status
            order by p.published_at desc,p.id desc
            """, nativeQuery = true)
    Page<Long> findPostIdsByKeyword(String keyword, String status, Pageable pageable);


    @Query("""
            select p from Post p
            left join fetch p.tags
            where p.id IN :ids
            """)
    List<Post> findPostWithTagsByIds(List<Long> ids, Sort sort);


    boolean existsBySlugAndUsername(String slug, String username);

    @Modifying
    @Query("""
            update Post p
            set p.totalViews = p.totalViews+ :increment
            where p.id=:postId
            """)
    void incrementViewCount(Long postId,Long increment);
}
