package co.com.pragma.model.apiuser;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class User {
    private String idUser;
    private String name;
    private String lastName;
    private String email;
    private String idNumber;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private BigDecimal baseSalary;
}
