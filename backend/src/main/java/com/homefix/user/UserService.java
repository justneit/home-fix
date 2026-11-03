package com.homefix.user;

import com.homefix.common.error.BusinessException;
import com.homefix.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository users;
    private final UserMapper mapper;

    public UserService(UserRepository users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    public UserResponse getProfile(Long userId) {
        return users.findById(userId).map(mapper::toResponse).orElseThrow(() -> BusinessException.notFound("User"));
    }
}
