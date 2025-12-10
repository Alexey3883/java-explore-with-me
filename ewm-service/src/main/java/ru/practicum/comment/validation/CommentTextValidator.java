package ru.practicum.comment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CommentTextValidator implements ConstraintValidator<ValidCommentText, String> {

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context) {
        if (text == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Текст комментария не может быть пустым")
                    .addConstraintViolation();
            return false;
        }

        if (text.trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Текст комментария не может быть пустым")
                    .addConstraintViolation();
            return false;
        }

        if (text.length() > 2000) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Текст комментария должен содержать не более 2000 символов")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}