package co.com.pragma.api;

import co.com.pragma.api.dto.UserDTO;
import co.com.pragma.api.helper.ValidationUtil;
import co.com.pragma.api.mapper.UserMapper;
import co.com.pragma.usecase.apiuser.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserHandler {
    private  final UserUseCase userUseCase;
    private  final ValidationUtil validator;


    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserDTO.class)
                .flatMap(validator::validate)
                .map(UserMapper::toUser)
                .flatMap(userUseCase::save)
                .flatMap(savedUser -> ServerResponse.ok().bodyValue(UserMapper.toUserDTO(savedUser)));
    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        // useCase2.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }
}
