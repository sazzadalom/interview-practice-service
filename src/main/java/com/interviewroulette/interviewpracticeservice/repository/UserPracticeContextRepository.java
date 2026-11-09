package com.interviewroulette.interviewpracticeservice.repository;

import com.interviewroulette.interviewpracticeservice.entity.UserPracticeContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPracticeContextRepository
        extends JpaRepository<UserPracticeContext, UUID> {
}