package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NewUserRequest {

    @Size(min = 2, max = 250)
    @NotNull
    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^(?!\\s*$).+", message = "Name cannot consist of spaces only")
    private String name;

    @Size(min = 6, max = 254)
    @Email(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @NotNull
    @Pattern(regexp = "^(?!\\s*$).+", message = "Email cannot consist of spaces only")
    private String email;

}