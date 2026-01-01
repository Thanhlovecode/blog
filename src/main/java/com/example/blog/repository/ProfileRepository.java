package com.example.blog.repository;

import com.example.blog.domain.Post;
import com.example.blog.domain.Profile;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("""
           select new com.example.blog.dto.response.PersonalInfoResponse(
           p.firstName,p.lastName,p.birthday,p.gender) from Profile p
           where p.id = :id
           """)
    PersonalInfoResponse getPersonalInfo(@Param("id") Long id);


    @Query("""
           select p from Profile p
           where p.user.id in :ids
           """)
    List<Profile> findProfileByIds(List<Long> ids);


    @Query("""
           select new com.example.blog.dto.response.ContactInfoResponse(
           p.phone,p.address) from Profile p
           where p.id = :id
           """)
    ContactInfoResponse getContactInfo(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Profile> findByUserId(Long id);






}
