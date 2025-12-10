package ru.practicum.comment.service;

import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createCommentPrivate(Long eventId, Long userId, NewCommentDto newCommentDto);

    List<CommentDto> findAllByEventId(Long eventId);

    List<CommentDto> getCommentsByAuthorId(Long authorId);

    CommentDto getComment(Long commentId);
    
    CommentDto getCommentById(Long commentId);

    CommentDto updateCommentPrivate(Long commentId, Long authorId, UpdateCommentDto updateCommentDto);

    CommentDto updateCommentAdmin(Long commentId, CommentDto commentDto);

    void deleteCommentPrivate(Long commentId, Long authorId);

    void deleteCommentAdmin(Long commentId);
}