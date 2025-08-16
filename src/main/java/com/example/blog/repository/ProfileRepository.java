package com.example.blog.repository;

import com.example.blog.domain.Profile;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("""
           select new com.example.blog.dto.response.PersonalInfoResponse(
           p.firstName,p.lastName,p.birthday,p.gender) from Profile p
           where p.id = :id
           """)
    PersonalInfoResponse getPersonalInfo(@Param("id") Long id);


    @Query("""
           select new com.example.blog.dto.response.ContactInfoResponse(
           p.phone,p.address) from Profile p
           where p.id = :id
           """)
    ContactInfoResponse getContactInfo(@Param("id") Long id);




}
