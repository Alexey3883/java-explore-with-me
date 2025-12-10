package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
@Validated
public class AdminCommentController {

    private final CommentService commentService;

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/comment/{id}")
    public CommentDto updateCommentAdmin(@PathVariable Long id, @Valid @RequestBody CommentDto commentDto) {
        log.info("Updating comment {} by admin", id);
        return commentService.updateCommentAdmin(id, commentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comments/{id}")
    public void deleteCommentAdmin(@PathVariable Long id) {
        log.info("Deleting comment {} by admin", id);
        commentService.deleteCommentAdmin(id);
    }
}