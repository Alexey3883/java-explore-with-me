package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import ru.practicum.event.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Annotation cannot consist of spaces only")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Description cannot consist of spaces only")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;

    private Boolean requestModeration;

    private String stateAction;

    @Size(min = 3, max = 120)
    @Pattern(regexp = "^(?!\\s*$).+", message = "Title cannot consist of spaces only")
    private String title;

}