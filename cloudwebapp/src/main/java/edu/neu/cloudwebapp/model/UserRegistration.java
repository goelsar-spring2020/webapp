package edu.neu.cloudwebapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    private String userID;
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
