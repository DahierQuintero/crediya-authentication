package co.com.pragma.config;

import co.com.pragma.decorators.TransactionalUserUseCase;
import co.com.pragma.model.user.ports.ILoggerPort;
import co.com.pragma.model.user.ports.IUserRepositoryPort;
import co.com.pragma.usecase.user.IUserUseCase;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@ComponentScan(
        basePackages = "co.com.pragma.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {co.com.pragma.usecase.user.UserUseCase.class}
        ),
        useDefaultFilters = false
)
@RequiredArgsConstructor
public class UseCasesConfig {

    private final IUserRepositoryPort userRepositoryPort;
    private final ILoggerPort logger;
    private final TransactionalOperator transactionalOperator;

    @Bean
    public IUserUseCase userUseCase() {
        IUserUseCase useCase = new UserUseCase(userRepositoryPort, logger);
        return new TransactionalUserUseCase(useCase, transactionalOperator);
    }
}
