package ru.practicum.comment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CommentTextValidator implements ConstraintValidator<ValidCommentText, String> {

    @Override
    public boolean isValid(String text, ConstraintValidatorContext context) {
        return text != null && !text.trim().isEmpty() && text.length() <= 2000;
    }
}