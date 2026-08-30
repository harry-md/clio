package com.harry.clio.mapper;

import com.harry.clio.dto.review.ReviewRequest;
import com.harry.clio.dto.review.ReviewResponse;
import com.harry.clio.model.Review;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "avatar", source = "user.avatar")
    ReviewResponse toResponse(Review review);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "comment", source = "request.comment")
    void updateReview(@MappingTarget Review review, ReviewRequest request);
}
