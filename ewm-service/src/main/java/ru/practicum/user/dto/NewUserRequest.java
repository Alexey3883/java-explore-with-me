package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    private String name;

    @Size(min = 6, max = 254)
    @Email
    @NotBlank
    @NotNull
    private String email;

}