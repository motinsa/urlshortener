package es.unizar.urlshortener

import es.unizar.urlshortener.core.RabbitMQService
import es.unizar.urlshortener.core.usecases.CreateShortUrlUseCaseImpl
import es.unizar.urlshortener.core.usecases.LogClickUseCaseImpl
import es.unizar.urlshortener.core.usecases.QRCodeUseCaseImpl
import es.unizar.urlshortener.core.usecases.RedirectUseCaseImpl
import es.unizar.urlshortener.infrastructure.delivery.*
import es.unizar.urlshortener.infrastructure.repositories.ClickEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ClickRepositoryServiceImpl
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlEntityRepository
import es.unizar.urlshortener.infrastructure.repositories.ShortUrlRepositoryServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Wires use cases with service implementations, and services implementations with repositories.
 *
 * **Note**: Spring Boot is able to discover this [Configuration] without further configuration.
 */
@Configuration
class ApplicationConfiguration(
    @Autowired val shortUrlEntityRepository: ShortUrlEntityRepository,
    @Autowired val clickEntityRepository: ClickEntityRepository,
) {
    @Bean
    fun clickRepositoryService() = ClickRepositoryServiceImpl(clickEntityRepository)

    @Bean
    fun shortUrlRepositoryService() = ShortUrlRepositoryServiceImpl(shortUrlEntityRepository)

    @Bean
    fun validatorService() = ValidatorServiceImpl()

    @Bean
    fun hashService() = HashServiceImpl()

    @Bean
    fun logClickUseCase() = LogClickUseCaseImpl(clickRepositoryService())

    @Bean
    fun locationService() = LocationServiceImpl()

    @Bean
    fun redirectionLimitService() = RedirectionLimitServiceImpl()

    @Bean
    fun validatorServiceImpl() = ValidatorServiceImpl()

    @Bean
    fun qrService() = QRServiceImpl()

    @Bean
    fun qrCodeUseCase() = QRCodeUseCaseImpl()

    @Bean
    fun rabbitMQService() = RabbitMQServiceImpl(shortUrlRepositoryService(), validatorServiceImpl())

    @Bean
    fun createShortUrlUseCase() =
        CreateShortUrlUseCaseImpl(shortUrlRepositoryService(), validatorService(),
            hashService(), locationService(), redirectionLimitService(), qrService(), rabbitMQService())

    @Bean
    fun redirectUseCase() = RedirectUseCaseImpl(shortUrlRepositoryService(), redirectionLimitService())
}
