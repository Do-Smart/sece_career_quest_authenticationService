package com.example.authenticationService.repository;

import com.example.authenticationService.model.StudentDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends CrudRepository<StudentDetails, String> {
    Optional<StudentDetails> findByEmail(String userName);
}