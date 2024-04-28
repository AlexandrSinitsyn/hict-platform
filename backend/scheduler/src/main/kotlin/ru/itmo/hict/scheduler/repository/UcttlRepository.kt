package ru.itmo.hict.scheduler.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.itmo.hict.scheduler.dto.UserContainerTimeToLive

/**
 * User container time to live repository
 */
@Repository
interface UcttlRepository : CrudRepository<UserContainerTimeToLive, String>
