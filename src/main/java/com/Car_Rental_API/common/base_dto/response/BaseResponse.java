package com.Car_Rental_API.common.base_dto.response;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;
    private Pagination pagination;
    private Map<String, String> errors;

    @Builder.Default
    private LocalDateTime timestampe = LocalDateTime.now();

    public static <T> BaseResponse<T> response(T data) {
        return BaseResponse.<T>builder().success(true).status(HttpStatus.OK.value()).message("Success").data(data).build();
    }

    public static <T> BaseResponse<T> response(T data, Pagination pagination){
        return BaseResponse.<T>builder().success(true).status(HttpStatus.OK.value()).message("Success").data(data).pagination(pagination).build();
    }

    public static <T> BaseResponse<T> error(HttpStatus status, String message){
        return BaseResponse.<T>builder().success(false).status(status.value()).message(message).build();
    }

    public static <T> BaseResponse<T> error(HttpStatus status, String message, Map<String, String> errors){
        return BaseResponse.<T>builder().success(false).status(status.value()).message(message).errors(errors).build();
    }

    // * Paginated list helper — inlines pagination from PageResult
    public static <T> BaseResponse<List<T>> response(PageResult<T> pageResult, BaseFilterRequest req) {
        int page = req != null ? req.getPage() : 1;
        int size = req != null ? req.getSize() : 10;
        return BaseResponse.<List<T>>builder()
                .success(true).status(HttpStatus.OK.value()).message("Success")
                .data(pageResult.data())
                .pagination(Pagination.builder().page(page).size(size).total(pageResult.total()).build())
                .build();
    }
}
