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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/comments")
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/events/{eventId}")
    public CommentDto createCommentPrivate(
            @Valid @RequestBody NewCommentDto newCommentDto,
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        log.info("Creating comment {}", newCommentDto);
        return commentService.createCommentPrivate(eventId, userId, newCommentDto);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public CommentDto getComment(@PathVariable Long id) {
        log.info("Retrieving comment by id {}", id);
        return commentService.getCommentById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/comment/{id}/user/{userId}")
    public CommentDto updateCommentPrivate(
            @Valid @RequestBody UpdateCommentDto updateCommentDto,
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Updating comment {} by user {}", id, userId);
        return commentService.updateCommentPrivate(id, userId, updateCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comment/{id}/user/{userId}")
    public void deleteCommentPrivate(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Deleting comment {} by user {}", id, userId);
        commentService.deleteCommentPrivate(id, userId);
    }
}