package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.IllegalArgumentException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Comment getCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));
    }

    @Override
    @Transactional
    public CommentDto createCommentPrivate(Long eventId, Long userId, NewCommentDto newCommentDto) {
        log.info("Создание комментария {}", newCommentDto);

        Event event = getEventOrThrow(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Комментировать возможно только опубликованные события");
        }

        User user = getUserOrThrow(userId);

        Comment comment = commentMapper.toNewComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(user);

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public List<CommentDto> findAllByEventId(Long eventId) {
        log.info("Получение комментариев по событию с id {}", eventId);

        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        return commentRepository.findAllByEventId(eventId).stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public List<CommentDto> getCommentsByAuthorId(Long authorId) {
        log.info("Получение комментариев пользователя с id {}", authorId);

        if (!userRepository.existsById(authorId)) {
            throw new NotFoundException("Пользователь с id=" + authorId + " не найден");
        }

        return commentRepository.findByAuthorId(authorId).stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto getComment(Long commentId) {
        log.info("Получение комментария по id {}", commentId);

        Comment comment = getCommentOrThrow(commentId);

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto getCommentById(Long commentId) {
        log.info("Получение комментария по id {}", commentId);

        Comment comment = getCommentOrThrow(commentId);

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentPrivate(Long commentId, Long authorId, UpdateCommentDto updateCommentDto) {

        log.info("Обновление комментария пользователем {}", commentId);

        Comment comment = getCommentOrThrow(commentId);

        if (!Objects.equals(comment.getAuthor().getId(), authorId)) {
            throw new IllegalArgumentException("Только автор может изменять комментарий");
        }

        comment.setId(commentId);
        commentMapper.updateCommentFields(comment, updateCommentDto);
        Comment updateComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(updateComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentAdmin(Long commentId, CommentDto commentDto) {

        log.info("Обновление комментария админом {}", commentId);
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий не найден");
        }

        Comment comment = getCommentOrThrow(commentId);

        comment.setId(commentId);

        if (commentDto.getEvent() != null) {
            comment.setEvent(getEventOrThrow(commentDto.getEvent().getId()));
        }
        if (commentDto.getText() != null) {
            comment.setText(commentDto.getText());
        }
        if (commentDto.getAuthor() != null) {
            comment.setAuthor(getUserOrThrow(commentDto.getAuthor().getId()));
        }
        if (commentDto.getCreated() != null) {
            comment.setCreateTime(commentDto.getCreated());
        }
        comment.setUpdateTime(LocalDateTime.now());

        log.info("Updating comment {}", comment);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long commentId, Long authorId) {
        log.info("Удаление комментария автором {}", commentId);

        Comment comment = getCommentOrThrow(commentId);

        if (!Objects.equals(comment.getAuthor().getId(), authorId)) {
            throw new ValidationException("Только автор может удалить комментарий");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        log.info("Удаление комментария админом {}", commentId);
        Comment comment = getCommentOrThrow(commentId);
        commentRepository.delete(comment);
    }
}