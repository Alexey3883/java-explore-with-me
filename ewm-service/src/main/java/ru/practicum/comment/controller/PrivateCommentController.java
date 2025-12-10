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
    @PostMapping("/event/{eventId}/user/{userId}")
    public CommentDto createCommentPrivate(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Creating comment {}", newCommentDto);

        if (newCommentDto.getText() == null || newCommentDto.getText().trim().isEmpty()) {
            throw new ru.practicum.exception.ValidationException("Текст комментария не может быть пустым");
        }

        if (newCommentDto.getText().length() > 2000) {
            throw new ru.practicum.exception.ValidationException("Текст комментария должен содержать не более 2000 символов");
        }

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
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Updating comment {} by user {}", id, userId);

        if (updateCommentDto.getText() == null || updateCommentDto.getText().trim().isEmpty()) {
            throw new ru.practicum.exception.ValidationException("Текст комментария не может быть пустым");
        }

        if (updateCommentDto.getText().length() > 2000) {
            throw new ru.practicum.exception.ValidationException("Текст комментария должен содержать не более 2000 символов");
        }

        return commentService.updateCommentPrivate(id, userId, updateCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comment/{id}/user/{userId}")
    public void deleteCommentPrivate(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Deleting comment {} by user {}", id, userId);
        commentService.deleteCommentPrivate(id, userId);
    }
}