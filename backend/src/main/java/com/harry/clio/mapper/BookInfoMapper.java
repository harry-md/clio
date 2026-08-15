package com.harry.clio.mapper;

import com.harry.clio.dto.book.BookInfoResponse;
import com.harry.clio.model.BookInfo;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookInfoMapper {
    BookInfoResponse toResponse(BookInfo bookInfo);
}
