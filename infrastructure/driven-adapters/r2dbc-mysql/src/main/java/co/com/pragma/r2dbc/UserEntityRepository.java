package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entities.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

// TODO: This file is just an example, you should delete or modify it
public interface UserEntityRepository extends ReactiveCrudRepository<UserEntity, String>, ReactiveQueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByIdUserAndEmail(String idNumber, String email);

}
