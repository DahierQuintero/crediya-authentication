package co.com.pragma.model.user.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

public class User {
    private Long idUser;
    private String name;
    private String lastName;
    private String email;
    private String idNumber;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private Byte roleId;
    private BigDecimal baseSalary;

    public User() {
    }

    public User(Long idUser, String name, String lastName, String email, String idNumber, LocalDate birthDate, String address, String phone, Byte roleId, BigDecimal baseSalary) {
        this.idUser = idUser;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.idNumber = idNumber;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
        this.roleId = roleId;
        this.baseSalary = baseSalary;
    }

    public User(String name, String lastName, String email, String idNumber, LocalDate birthDate, String address, String phone, Byte roleId, BigDecimal baseSalary) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.idNumber = idNumber;
        this.birthDate = birthDate;
        this.address = address;
        this.phone = phone;
        this.roleId = roleId;
        this.baseSalary = baseSalary;
    }

    public Long getIdUser() {
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public Byte getRoleId() {
        return roleId;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public User setIdUser(Long idUser) {
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

    public User setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
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

    public User setRoleId(Byte roleId) {
        this.roleId = roleId;
        return this;
    }

    public User setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
        return this;
    }
}
