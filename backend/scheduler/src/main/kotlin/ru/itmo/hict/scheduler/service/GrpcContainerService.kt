package ru.itmo.hict.scheduler.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import ru.itmo.hict.proto.empty
import ru.itmo.hict.proto.user.ContainerControllerGrpc
import ru.itmo.hict.proto.user.UserId
import ru.itmo.hict.scheduler.logging.Logger

@GrpcService
class GrpcContainerService (
    private val dindService: DindService,
    private val containerMonitor: ContainerMonitor,
    private val logger: Logger,
) : ContainerControllerGrpc.ContainerControllerImplBase() {
    override fun create(request: UserId, responseObserver: StreamObserver<Empty>) = runBlocking {
        logger.info("grpc", "create", "$request")

        val id = request.uuid

        containerMonitor.register(id)

        launch {
            val res = dindService.runDocker(id)
            logger.info("creation", id, when {
                res.isSuccess -> "success"
                else -> res.exceptionOrNull()!!.run { this.message ?: toString() }
            })
        }

        responseObserver.onNext(empty {})

        responseObserver.onCompleted()
    }

    override fun ping(request: UserId, responseObserver: StreamObserver<Empty>) = runBlocking {
        logger.info("grpc", "ping", "$request")

        containerMonitor.extend(request.uuid)

        responseObserver.onNext(empty {})

        responseObserver.onCompleted()
    }
}
