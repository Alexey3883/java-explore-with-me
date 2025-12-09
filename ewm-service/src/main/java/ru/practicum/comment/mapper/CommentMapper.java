package ru.practicum.comment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.StatsClient;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;

import static ru.practicum.utils.HitsEventViewUtil.getHitsEvent;

@RequiredArgsConstructor
@Component
public class CommentMapper {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final StatsClient statsClient;

    public Comment toNewComment(Long eventId, Long userId, NewCommentDto newCommentDto) {

        return Comment.builder()
                .event(eventRepository.findById(eventId).orElseThrow())
                .text(newCommentDto.getText())
                .author(userRepository.findById(userId).orElseThrow())
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .event(eventMapper.toShort(comment.getEvent(), getHitsEvent(
                        comment.getEvent().getId(),
                        LocalDateTime.now().minusDays(100),
                        LocalDateTime.now(), false, statsClient
                )))
                .text(comment.getText())
                .author(userMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }

    public Comment toUpdateComment(UpdateCommentDto updateCommentDto) {

        return Comment.builder()
                .text(updateCommentDto.getText())
                .updated(LocalDateTime.now())
                .build();
    }

}