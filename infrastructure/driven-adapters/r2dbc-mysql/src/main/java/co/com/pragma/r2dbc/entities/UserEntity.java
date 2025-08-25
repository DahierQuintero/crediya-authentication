package co.com.pragma.r2dbc.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("user")
public class UserEntity {
    @Id
    @Column("id_user")
    private String idUser;
    private String name;
    @Column("last_name")
    private String lastName;
    private String email;
    @Column("id_number")
    private String idNumber;
    @Column("date_of_birth")
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    @Column("base_salary")
    private BigDecimal baseSalary;
}
