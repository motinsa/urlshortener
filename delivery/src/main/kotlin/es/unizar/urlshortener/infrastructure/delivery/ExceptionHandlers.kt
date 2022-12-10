package es.unizar.urlshortener.infrastructure.delivery

import es.unizar.urlshortener.core.InvalidLocationException
import es.unizar.urlshortener.core.InvalidUrlException
import es.unizar.urlshortener.core.RedirectionNotFound
import es.unizar.urlshortener.core.TooManyRedirectionsException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ResponseBody
    @ExceptionHandler(value = [InvalidUrlException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidUrls(ex: InvalidUrlException) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

    @ResponseBody
    @ExceptionHandler(value = [RedirectionNotFound::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun redirectionNotFound(ex: RedirectionNotFound) = ErrorMessage(HttpStatus.NOT_FOUND.value(), ex.message)

    @ResponseBody
    @ExceptionHandler(value = [InvalidLocationException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidLocation(ex: InvalidLocationException) = ErrorMessage(HttpStatus.BAD_REQUEST.value(), ex.message)

    @ResponseBody
    @ExceptionHandler(value = [TooManyRedirectionsException::class])
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun tooManyRedirects(ex: TooManyRedirectionsException) : ResponseEntity<ErrorMessage> {
        val h = HttpHeaders()
        h.set("Retry-After", ex.refillTime.toString())
        return ResponseEntity<ErrorMessage>(
            ErrorMessage(HttpStatus.TOO_MANY_REQUESTS.value(), ex.message),
            h,
            HttpStatus.TOO_MANY_REQUESTS
        )
    }
}

data class ErrorMessage(
    val statusCode: Int,
    val message: String?,
    val timestamp: String = DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())
)
