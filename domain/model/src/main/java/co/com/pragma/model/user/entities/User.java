package co.com.pragma.model.user;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    public User() {
    }

    public User(String idUser, String name, String lastName, String email, String idNumber, LocalDate dateOfBirth, String address, String phone, BigDecimal baseSalary) {
        this.idUser = idUser;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.baseSalary = baseSalary;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public User setIdUser(String idUser) {
        this.idUser = idUser;
        return this;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setIdNumber(String idNumber) {
        this.idNumber = idNumber;
        return this;
    }

    public User setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public User setAddress(String address) {
        this.address = address;
        return this;
    }

    public User setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public User setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
        return this;
    }
}
