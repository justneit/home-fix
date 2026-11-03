package com.homefix.user;

import com.homefix.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
