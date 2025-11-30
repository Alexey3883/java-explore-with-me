package ru.practicum.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {

    private String app;
    private String uri;
    private Long hits;
}
