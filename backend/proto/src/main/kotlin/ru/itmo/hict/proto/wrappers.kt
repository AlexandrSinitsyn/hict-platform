package ru.itmo.hict.proto

import com.google.protobuf.Empty
import ru.itmo.hict.proto.user.UserId

fun userId(body: UserId.Builder.() -> Unit): UserId = UserId.newBuilder().apply(body).build()

fun empty(body: Empty.Builder.() -> Unit): Empty = Empty.newBuilder().apply(body).build()
