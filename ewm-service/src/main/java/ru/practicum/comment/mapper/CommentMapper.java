package ru.practicum.comment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.StatsClient;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;

import static ru.practicum.utils.HitsEventViewUtil.getHitsEvent;

@RequiredArgsConstructor
@Component
public class CommentMapper {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final StatsClient statsClient;

    public Comment toNewComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .createTime(LocalDateTime.now())
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
                .created(comment.getCreateTime())
                .updated(comment.getUpdateTime())
                .build();
    }

    public void updateCommentFields(Comment comment, UpdateCommentDto updateCommentDto) {
        comment.setText(updateCommentDto.getText());
        comment.setUpdateTime(LocalDateTime.now());
    }
}