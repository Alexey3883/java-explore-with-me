package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/comments/users/{userId}/events/{eventId}")
    public CommentDto createCommentPrivate(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Создание комментария {}", newCommentDto);
        return commentService.createCommentPrivate(eventId, userId, newCommentDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/comments/{eventId}")
    public List<CommentDto> getComments(@PathVariable Long eventId) {
        log.info("Retrieving comments for event {}", eventId);
        return commentService.findAllByEventId(eventId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/comments/users/{authorId}")
    public List<CommentDto> getCommentsByAuthorId(@PathVariable Long authorId) {
        log.info("Retrieving comments for author {}", authorId);
        return commentService.getCommentsByAuthorId(authorId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/comments/users/{userId}/{commentId}")
    public CommentDto getComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        log.info("Retrieving comment {}", commentId);
        return commentService.getComment(userId, commentId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/comments/users/{userId}/{commentId}")
    public CommentDto updateCommentPrivate(
            @PathVariable Long commentId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Updating comment {}", updateCommentDto);
        return commentService.updateCommentPrivate(commentId, userId, updateCommentDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/admin/{commentId}")
    public CommentDto updateCommentAdmin(@PathVariable Long commentId, @Valid @RequestBody CommentDto commentDto) {
        log.info("Updating comment {}", commentDto);
        return commentService.updateCommentAdmin(commentId, commentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comments/users/{userId}/{commentId}")
    public void deleteCommentPrivate(@PathVariable Long commentId, @PathVariable Long userId) {
        log.info("Deleting comment {}", commentId);
        commentService.deleteCommentPrivate(commentId, userId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/comments/{commentId}")
    public void deleteCommentAdmin(@PathVariable Long commentId) {
        log.info("Deleting comment {}", commentId);
        commentService.deleteCommentAdmin(commentId);
    }
}
