@file:Suppress("WildcardImport")

package es.unizar.urlshortener.core.usecases

import es.unizar.urlshortener.core.*

/**
 * Given a key returns a [Redirection] that contains a [URI target][Redirection.target]
 * and an [HTTP redirection mode][Redirection.mode].
 *
 * **Note**: This is an example of functionality.
 */
interface RedirectUseCase {
    fun redirectTo(key: String): Redirection
}

/**
 * Implementation of [RedirectUseCase].
 */
class RedirectUseCaseImpl(
    private val shortUrlRepository: ShortUrlRepositoryService,
    private val redirectionLimitService: RedirectionLimitService
) : RedirectUseCase {
    override fun redirectTo(key: String) : Redirection {
        val redirection = shortUrlRepository
            .findByKey(key)
            ?.redirection
            ?: throw RedirectionNotFound(key)

        // This throws only if redirection exists but rate limit has been reached
        redirectionLimitService.checkLimit(key)

        val shortUrl = shortUrlRepository.findByKey(key)

        if (shortUrl != null) {
            if (shortUrl.properties.safe != null) {
                // Ha sido validada
                if (!shortUrl.properties.safe) {
                    throw RedirectUnsafeException()
                }
            } else {
                // No ha sido validada aun
                throw RedirectionNotValidatedException(5)
            }
        }

        return redirection
    }
}
