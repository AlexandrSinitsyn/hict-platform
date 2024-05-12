package ru.itmo.hict.server.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.itmo.hict.entity.User
import ru.itmo.hict.proto.user.ContainerControllerGrpc
import ru.itmo.hict.proto.userId
import ru.itmo.hict.server.logging.Logger

@Service
class GrpcContainerService(
    private val logger: Logger,
) {
    @GrpcClient("grpc-user-client-container")
    private lateinit var containerController: ContainerControllerGrpc.ContainerControllerStub

    private val ackReceiver = object : StreamObserver<Empty> {
        override fun onNext(e: Empty) {
            logger.info("grpc-stream", "accepted", "empty {}")
        }

        override fun onError(e: Throwable) {
            logger.info("grpc-stream", "error", "${e::class}: ${e.message}")
        }

        override fun onCompleted() {
            logger.info("grpc-stream", "completed", "ok")
        }
    }

    fun publish(user: User) = containerController.create(userId {
        id = user.id!!
    }, ackReceiver)

    fun ping(user: User) = containerController.ping(userId {
        id = user.id!!
    }, ackReceiver)
}
