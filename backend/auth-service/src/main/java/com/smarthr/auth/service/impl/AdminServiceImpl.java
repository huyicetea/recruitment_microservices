package com.smarthr.auth.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smarthr.auth.model.entity.Outbox;
import com.smarthr.auth.model.entity.User;
import com.smarthr.auth.model.enums.Role;
import com.smarthr.auth.model.enums.UserStatus;
import com.smarthr.auth.repo.OutboxRepo;
import com.smarthr.auth.repo.UserRepo;
import com.smarthr.auth.service.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepo userRepo;
    private final OutboxRepo outboxRepo;

    @Override
    @Transactional
    public void approveHr(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.HR) {
            throw new RuntimeException("User is not an HR account");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepo.save(user);

        Outbox outbox = Outbox.builder()
                .aggregateType("user")
                .aggregateId(user.getId().toString())
                .type("user.hr_approved")
                .payload("{\"companyId\":\"" + user.getCompanyId() + "\"}")
                .processed(false)
                .build();
        outboxRepo.save(outbox);
    }
}