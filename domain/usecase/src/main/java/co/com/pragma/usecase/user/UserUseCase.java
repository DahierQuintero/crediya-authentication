package co.com.pragma.usecase.user;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.ports.ILoggerPort;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserUseCase implements IUserUseCase {

    private final IUserRepositoryPort iUserRepositoryPort;
    private final ILoggerPort logger;

    public UserUseCase(IUserRepositoryPort iUserRepositoryPort, ILoggerPort logger) {
        this.iUserRepositoryPort = iUserRepositoryPort;
        this.logger = logger;
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault("traceId", "unknown");
            logger.info(traceId, "Iniciando caso de uso de guardado de usuario. ID: {} | Email: {}",
                    user.getIdUser(), user.getEmail());

            return validateUserDoesNotExist(user, traceId)
                    .then(saveUserToRepository(user, traceId))
                    .doOnSuccess(savedUser -> logger.info(traceId, "Caso de uso de guardado de usuario finalizado exitosamente"))
                    .doOnError(error -> logger.error(traceId, "Caso de uso de guardado de usuario fallido", error));
        });
    }

    @Override
    public Flux<User> findAll() {
        return Flux.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault("traceId", "unknown");
            logger.info(traceId, "Iniciando consulta de todos los usuarios");

            return iUserRepositoryPort.findAll()
                    .doOnComplete(() ->
                            logger.info(traceId, "Consulta de todos los usuarios completada exitosamente")
                    )
                    .doOnError(error ->
                            logger.error(traceId, "Error al consultar todos los usuarios", error)
                    );
        });
    }

    private Mono<Void> validateUserDoesNotExist(User user, String traceId) {
        return Mono.zip(
                        validateUserDoesNotExistByIdUser(user.getIdUser(), traceId),
                        validateUserDoesNotExistByEmail(user.getEmail(), traceId),
                        validateUserDoesNotExistByIdNumber(user.getIdNumber(), traceId)
                )
                .then();
    }

    private Mono<Boolean> validateUserDoesNotExistByIdUser(String idUser, String traceId) {
        return iUserRepositoryPort.existsByIdUser(idUser)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new UserAlreadyExistsException("User with idUser " + idUser + " already exists"));
                    }
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> validateUserDoesNotExistByEmail(String email, String traceId) {
        return iUserRepositoryPort.existsByEmail(email)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new UserAlreadyExistsException("User with email " + email + " already exists"));
                    }
                    return Mono.just(false);
                });
    }

    private Mono<Boolean> validateUserDoesNotExistByIdNumber(String idNumber, String traceId) {
        return iUserRepositoryPort.existsByIdNumber(idNumber)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new UserAlreadyExistsException("User with idNumber " + idNumber + " already exists"));
                    }
                    return Mono.just(false);
                });
    }

    private Mono<User> saveUserToRepository(User user, String traceId) {
        logger.debug(traceId, "Persistiendo usuario en repositorio");

        return iUserRepositoryPort.saveUser(user)
                .doOnSuccess(savedUser -> logger.debug(traceId, "Usuario persistido exitosamente en repositorio"))
                .doOnError(error -> logger.error(traceId, "Error al persistir usuario en repositorio", error));
    }

}
