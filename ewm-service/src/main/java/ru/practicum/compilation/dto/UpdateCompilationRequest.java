package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCompilationRequest {

    @Size(min = 1, max = 50)
    @NotBlank(message = "Compilation title cannot be blank")
    private String title;

    private Boolean pinned;

    private List<Long> events;

}