package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.model.user.entities.User;

public class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
    
    public static User toUser(UserDTO dto) {

        return new User()
                .setName(dto.name())
                .setLastName(dto.lastName())
                .setEmail(dto.email())
                .setIdNumber(dto.idNumber())
                .setBirthDate(dto.birthDate())
                .setAddress(dto.address())
                .setPhone(dto.phone())
                .setRoleId(dto.roleId())
                .setBaseSalary(dto.baseSalary());
    }

    public static UserDTO toUserDTO(User user) {

        return new UserDTO(
                user.getIdNumber(),
                user.getName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate(),
                user.getAddress(),
                user.getPhone(),
                user.getRoleId(),
                user.getBaseSalary()
        );
    }
}
