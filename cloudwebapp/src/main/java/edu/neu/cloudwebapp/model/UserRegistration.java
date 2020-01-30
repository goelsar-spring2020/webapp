package edu.neu.cloudwebapp.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRegistration {

    @Id
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    private String id;
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "First Name Can not be empty")
    private String firstName;
    @NotNull(message = "Lst Name Can not be empty")
    private String lastName;
    @NotNull(message = "Password Can not be empty")
    private String password;
    private Date account_created;
    private Date account_updated;

}
