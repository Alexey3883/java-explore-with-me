package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewCompilationDto {

    @NotNull
    @NotBlank(message = "Compilation title cannot be blank")
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;

    private List<Long> events;

}