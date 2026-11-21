package com.example.learningplatform.course.dto;

import com.example.learningplatform.course.entity.Tag;

public record TagResponse(Long id, String name) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
