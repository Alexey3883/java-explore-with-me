package ru.practicum.comment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CommentTextValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCommentText {
    String message() default "Текст комментария не может быть пустым";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}