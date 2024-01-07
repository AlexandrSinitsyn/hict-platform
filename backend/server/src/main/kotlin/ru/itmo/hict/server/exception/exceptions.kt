package ru.itmo.hict.server.exception

class InvalidAuthorizationTypeException(message: String) : RuntimeException(message)

class InvalidJwtException : RuntimeException("Invalid jwt token")
