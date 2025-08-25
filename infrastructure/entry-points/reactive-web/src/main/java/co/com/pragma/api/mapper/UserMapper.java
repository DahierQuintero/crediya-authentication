package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.model.apiuser.User;

public class UserMapper {
    
    public static User toUser(UserDTO dto) {

        return User.builder()
                .idNumber(dto.idNumber())
                .name(dto.name())
                .lastName(dto.lastName())
                .email(dto.email())
                .dateOfBirth(dto.birthDate())
                .address(dto.address())
                .phone(dto.phone())
                .baseSalary(dto.baseSalary())
                .build();
    }

    public static UserDTO toUserDTO(User user) {

        return new UserDTO(
                user.getIdNumber(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getAddress(),
                user.getPhone(),
                user.getBaseSalary()
        );
    }
}
