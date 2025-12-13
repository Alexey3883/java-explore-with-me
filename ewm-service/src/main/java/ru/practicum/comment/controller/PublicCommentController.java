package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/event/{eventId}")
    public List<CommentDto> getComments(@PathVariable Long eventId) {
        log.info("Retrieving comments for event {}", eventId);
        return commentService.findAllByEventId(eventId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/user/{userId}")
    public List<CommentDto> getCommentsByAuthorId(@PathVariable Long userId) {
        log.info("Retrieving comments for user {}", userId);
        return commentService.getCommentsByAuthorId(userId);
    }
}