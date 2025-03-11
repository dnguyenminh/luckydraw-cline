package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.auth.LoginResponse;
import vn.com.fecredit.app.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "accountActive", expression = "java(user.isAccountActive())")
    LoginResponse.UserInfo toUserInfo(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userInfo", source = "user")
    @Mapping(target = "token", source = "token")
    LoginResponse toLoginResponse(User user, String token);

    default LoginResponse createLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .userInfo(toUserInfo(user))
                .token(token)
                .build();
    }
}
