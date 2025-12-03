package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompilationDto {

    @NotNull
    private Long id;

    @NotNull
    private Boolean pinned;

    @NotBlank
    @NotNull
    @Size(min = 1, max = 50)
    private String title;

    private List<EventShortDto> events;

}