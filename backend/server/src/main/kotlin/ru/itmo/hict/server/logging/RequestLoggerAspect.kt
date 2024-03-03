package ru.itmo.hict.server.logging

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*

@Aspect
@Component
class RequestLoggerAspect(
    private val logger: Logger,
) {
    @Before("@annotation(get)")
    fun getBefore(jp: JoinPoint, get: GetMapping) = before("GET", get.value, jp)

    @After("@annotation(get)")
    fun getAfter(jp: JoinPoint, get: GetMapping) = after("GET", get.value, jp)

    @Before("@annotation(post)")
    fun postBefore(jp: JoinPoint, post: PostMapping) = before("POST", post.value, jp)

    @After("@annotation(post)")
    fun postAfter(jp: JoinPoint, post: PostMapping) = after("POST", post.value, jp)

    @Before("@annotation(put)")
    fun putBefore(jp: JoinPoint, put: PutMapping) = before("PUT", put.value, jp)

    @After("@annotation(put)")
    fun putAfter(jp: JoinPoint, put: PutMapping) = after("PUT", put.value, jp)

    @Before("@annotation(patch)")
    fun patchBefore(jp: JoinPoint, patch: PatchMapping) = before("PATCH", patch.value, jp)

    @After("@annotation(patch)")
    fun patchAfter(jp: JoinPoint, patch: PatchMapping) = after("PATCH", patch.value, jp)

    @Before("@annotation(delete)")
    fun deleteBefore(jp: JoinPoint, delete: DeleteMapping) = before("DELETE", delete.value, jp)

    @After("@annotation(delete)")
    fun deleteAfter(jp: JoinPoint, delete: DeleteMapping) = after("DELETE", delete.value, jp)

    @Before("@annotation(eh)")
    fun exceptionHandleBefore(jp: JoinPoint, eh: ExceptionHandler) =
        logger.error("exceptionHandler", eh.value.joinToString { it.simpleName ?: "j:${it.java.simpleName}" },
            "%s(%s)".format(jp.signature.name, jp.args.joinToString { (it as? Exception)?.message ?: "?" }))

    private fun before(method: String, paths: Array<String>, jp: JoinPoint) =
        logRequest(method, paths.joinToString(), "before", jp.signature.name)

    private fun after(method: String, paths: Array<String>, jp: JoinPoint) =
        logRequest(method, paths.joinToString(), "after", jp.signature.name)

    private fun logRequest(method: String, paths: String, process: String, message: String) =
        logger.request(method, paths, process, message)
}
