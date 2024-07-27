package com.outstagram.outstagram.async;

import com.outstagram.outstagram.dto.UserDTO;
import com.outstagram.outstagram.exception.ApiException;
import com.outstagram.outstagram.exception.errorcode.ErrorCode;
import com.outstagram.outstagram.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.outstagram.outstagram.common.constant.DBConst.DB_COUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncUserService {
    private final UserService userService;

    @Async
    public CompletableFuture<UserDTO> loginFromDB(String email, String password, Long shardId) {
        RequestContextHolder.getRequestAttributes().setAttribute("shardId", shardId, RequestAttributes.SCOPE_REQUEST);
        UserDTO user = userService.login(email, password);
        RequestContextHolder.getRequestAttributes().removeAttribute("shardId", RequestAttributes.SCOPE_REQUEST);
        return CompletableFuture.completedFuture(user);
    }
    public UserDTO login(String email, String password) throws ExecutionException, InterruptedException {
        List<CompletableFuture<UserDTO>> futures = new ArrayList<>();

        for (long i = 0; i < DB_COUNT; i++) {
            futures.add(loginFromDB(email, password, i));
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.join();

        for (CompletableFuture<UserDTO> future : futures) {
            UserDTO user = future.get();
            if (user != null) {
                return user;
            }
        }

        throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }
}
