package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.common.AbstractMapper;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;

@UtilityClass
public class CommentMapper extends AbstractMapper {
    public Comment toComment(CommentCreateDto commentCreateDto) {
        return Comment.builder()
                .text(commentCreateDto.getText())
                .created(LocalDateTime.now().withNano(0))
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        if (isNull(comment)) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
