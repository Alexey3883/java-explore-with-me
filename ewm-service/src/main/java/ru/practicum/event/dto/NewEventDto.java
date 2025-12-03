package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.Location;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Title cannot consist of spaces only")
    private String title;

    @NotBlank
    @NotNull
    @Size(min = 20, max = 2000)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Annotation cannot consist of spaces only")
    private String annotation;

    @NotNull
    private Long category;

    private Boolean paid;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    @NotBlank
    @Size(min = 20, max = 7000)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Description cannot consist of spaces only")
    private String description;

    @PositiveOrZero
    private Long participantLimit;

    @NotNull
    private Location location;

    private Boolean requestModeration;

}