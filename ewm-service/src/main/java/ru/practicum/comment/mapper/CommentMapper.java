package ru.practicum.comment.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.user.mapper.UserMapper;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class CommentMapper {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public Comment toComment(NewCommentDto newCommentDto) {
        return Comment.builder()
                .text(newCommentDto.getText())
                .createTime(LocalDateTime.now())
                .build();
    }

    public Comment toComment(UpdateCommentDto updateCommentDto) {
        return Comment.builder()
                .text(updateCommentDto.getText())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .event(eventMapper.toShort(comment.getEvent(), null))
                .text(comment.getText())
                .author(userMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreateTime())
                .updated(comment.getUpdateTime())
                .build();
    }
}