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

    @Override
    @Transactional
    public CommentDto createCommentPrivate(Long eventId, Long userId, NewCommentDto newCommentDto) {
        log.info("Создание комментария {}", newCommentDto);

        if (newCommentDto.getText() == null || newCommentDto.getText().trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Event event = eventRepository.findById(eventId).orElseThrow();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Комментировать возможно только опубликованные события");
        }

        return commentMapper.toCommentDto(
                commentRepository.save(commentMapper.toNewComment(eventId, userId, newCommentDto))
        );
    }

    @Override
    @Transactional
    public List<CommentDto> findAllByEventId(Long eventId) {
        log.info("Получение комментариев по событию с id {}", eventId);
        return commentRepository.findAllByEventId(eventId).stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public List<CommentDto> getCommentsByAuthorId(Long authorId) {
        log.info("Получение комментариев пользователя с id {}", authorId);
        return commentRepository.findByAuthorId(authorId).stream()
                .map(comment -> {
                    commentMapper.toCommentDto(comment);
                    return commentMapper.toCommentDto(comment);
                })
                .toList();
    }

    @Override
    @Transactional
    public CommentDto getComment(Long userId, Long commentId) {
        log.info("Получение комментария по id {}", commentId);

        Comment comment = commentRepository.findById(commentId).orElseThrow();

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentPrivate(Long commentId, Long authorId, UpdateCommentDto updateCommentDto) {

        log.info("Обновление комментария пользователем {}", commentId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ValidationException("Нельзя обновить несуществующий комментарий")
        );

        if (!Objects.equals(comment.getAuthor().getId(), authorId)) {
            throw new IllegalArgumentException("Только автор может изменять комментарий");
        }

        comment.setId(commentId);
        if (updateCommentDto.getText() == null || updateCommentDto.getText().trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        comment.setText(updateCommentDto.getText());
        comment.setUpdated(LocalDateTime.now());
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

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ValidationException("Нельзя обновить несуществующий комментарий")
        );

        comment.setId(commentId);

        if (commentDto.getEvent() != null) {
            comment.setEvent(eventRepository.findById(commentId).orElseThrow(
                    () -> new ValidationException("Нельзя обновить комментарий к отсутствущему событию")
            ));
        }
        if (commentDto.getText() != null) {
            comment.setText(commentDto.getText());
        }
        if (commentDto.getAuthor() != null) {
            comment.setAuthor(userRepository.findById(commentDto.getAuthor().getId()).orElseThrow());
        }
        if (commentDto.getCreated() != null) {
            comment.setCreated(commentDto.getCreated());
        }
        comment.setUpdated(commentDto.getUpdated());

        log.info("Updating comment {}", comment);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentPrivate(Long commentId, Long authorId) {
        log.info("Удаление комментария автором {}", commentId);

        Comment comment = commentRepository.findById(commentId).orElseThrow();

        if (!Objects.equals(comment.getAuthor().getId(), authorId)) {
            throw new ValidationException("Только автор может удалить комментарий");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(Long commentId) {
        log.info("Удаление комментария админом {}", commentId);
        commentRepository.delete(commentRepository.findById(commentId).orElseThrow());
    }
}
