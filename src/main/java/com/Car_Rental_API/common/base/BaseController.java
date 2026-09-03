package com.Car_Rental_API.common.base;

import com.Car_Rental_API.common.base_dto.request.BaseFilterRequest;
import com.Car_Rental_API.common.base_dto.response.BaseResponse;
import com.Car_Rental_API.common.base_dto.response.PageResult;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseController {

    protected Long getCurrentUserId() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails u) {
//         return u.getUserId();
//       }getUserId
        return null;
    }

    // * Standard Data Operations
    protected <T> ResponseEntity<BaseResponse<T>> success(T data) {
        return ResponseEntity.ok(BaseResponse.response(data));
    }

    protected <T> ResponseEntity<BaseResponse<T>> success(Function<Long, T> action) {
        return success(action.apply(getCurrentUserId()));
    }

    // * Void Mutation Operations
    protected ResponseEntity<BaseResponse<Void>> successVoid(Runnable action) {
        action.run();
        return ResponseEntity.ok(BaseResponse.response(null));
    }

    protected ResponseEntity<BaseResponse<Void>> successVoid(Consumer<Long> action) {
        action.accept(getCurrentUserId());
        return ResponseEntity.ok(BaseResponse.response(null));
    }

    // * Paginated (List) Operations
    protected <T> ResponseEntity<BaseResponse<List<T>>> successPage(PageResult<T> result, BaseFilterRequest req) {
        return ResponseEntity.ok(BaseResponse.response(result, req));
    }

    protected <T> ResponseEntity<BaseResponse<List<T>>> successPage(Function<Long, PageResult<T>> action, BaseFilterRequest req) {
        return successPage(action.apply(getCurrentUserId()), req);
    }
}
