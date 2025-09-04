package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserEntityRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByIdUser(Long idUser);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByIdNumber(String idNumber);

}
