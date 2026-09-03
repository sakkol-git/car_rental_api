package com.Car_Rental_API.common.base_dto.response;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> data, long total) {

    // * Map PageResult payload from one type to another (e.g. Entity to DTO)
    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(data.stream().map(mapper).toList(), total);
    }
}