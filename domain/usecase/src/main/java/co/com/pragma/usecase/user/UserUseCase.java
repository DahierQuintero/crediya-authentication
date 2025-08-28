package co.com.pragma.usecase.user;

import co.com.pragma.model.user.entities.User;
import co.com.pragma.model.user.exceptions.UserAlreadyExistsException;
import co.com.pragma.model.user.ports.ILoggerPort;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import reactor.core.publisher.Mono;

public class UserUseCase implements IUserUseCase{

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

    private Mono<Void> validateUserDoesNotExist(User user, String traceId) {
        return iUserRepositoryPort.existsByIdUserAndEmail(user.getIdUser(), user.getEmail())
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new UserAlreadyExistsException(user.getEmail())))
                .then();
    }

    private Mono<User> saveUserToRepository(User user, String traceId) {
        logger.debug(traceId, "Persistiendo usuario en repositorio");

        return iUserRepositoryPort.saveUser(user)
                .doOnSuccess(savedUser -> logger.debug(traceId, "Usuario persistido exitosamente en repositorio"))
                .doOnError(error -> logger.error(traceId, "Error al persistir usuario en repositorio", error));
    }

}
