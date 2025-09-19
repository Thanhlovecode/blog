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

import java.util.Collection;
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
            SELECT p FROM Post p
            LEFT JOIN FETCH p.tags WHERE p.user.id = :userId AND p.status = :status
            order by p.publishedAt desc
            """)
    Page<Post> findPostsWithTagsByUserIdAndStatus(Long userId, PostStatus status, Pageable pageable);



//    @Query("""
//    SELECT DISTINCT p FROM Post p
//    LEFT JOIN FETCH p.tags t
//    WHERE t.slug = :slug AND p.status = :status
//    """)
//    Page<Post> findPublishedPostsByTagSlugWithPaging(String slug, PostStatus status, Pageable pageable);

    @Query("""
            select p.id from Post p
            join p.tags t
            where t.slug = :slug and p.status = :status
            """)
    List<Long> findPostIdsByTagSlug(String slug,PostStatus status);



    @Query(value = """
                    select p.id from posts p
                    where match(p.title)  against(:keyword)
                    and p.status = "PUBLISHED"
                    """,nativeQuery = true)
    List<Long> findPostIdsByKeyword(String keyword);


    @Query("""
            select p from Post p
            left join fetch p.tags
            where p.id IN :ids
            """)
    Page<Post> findPostWithTagsByIds(List<Long> ids, Pageable pageable);
}
