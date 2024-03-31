package ru.itmo.hict.messaging

interface HiCTMessageType

data class Create(val uid: String) : HiCTMessageType

data class Ping(val uid: String) : HiCTMessageType
