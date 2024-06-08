package ru.itmo.hict.authorization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import ru.itmo.hict.authorization.repository.UserRepository
import ru.itmo.hict.authorization.service.UserService
import ru.itmo.hict.entity.User
import java.util.*
import kotlin.jvm.optionals.getOrNull

class UserServiceTests {
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    companion object {
        private const val USERNAME = "test"
        private const val LOGIN = "login"
        private const val EMAIL = "email@test.com"
        private const val PASS = "pass"

        private const val INVALID = "invalid"
    }

    private val user = User(USERNAME, LOGIN, EMAIL, PASS)
    private val optUser = Optional.of(user)
    private val empty = Optional.empty<User>()

    @BeforeEach
    fun setup() {
        userRepository = mock<UserRepository>()
        userService = UserService(userRepository)
    }

    @Test
    fun initialized() {
        Assertions.assertNotNull(userService)
    }

    @Test
    fun `find user`() {
        whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, EMAIL, PASS)) doReturn optUser
        whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, null, PASS)) doReturn optUser
        whenever(userRepository.findByLoginOrEmailAndPassword(null, EMAIL, PASS)) doReturn optUser

        val found = userService.findByCredentials(LOGIN, EMAIL, PASS).run {
            Assertions.assertNotNull(this.getOrNull())

            get()
        }

        Assertions.assertEquals(user.login, found.login)
        Assertions.assertEquals(user.email, found.email)
        Assertions.assertEquals(user, found)

        Assertions.assertEquals(found, userService.findByCredentials(LOGIN, null, PASS).getOrNull())
        Assertions.assertEquals(found, userService.findByCredentials(null, EMAIL, PASS).getOrNull())


        Assertions.assertTrue(userService.findByCredentials(INVALID, INVALID, PASS).isEmpty)
        Assertions.assertTrue(userService.findByCredentials(INVALID, null, PASS).isEmpty)
        Assertions.assertTrue(userService.findByCredentials(null, INVALID, PASS).isEmpty)
        Assertions.assertTrue(userService.findByCredentials(null, null, PASS).isEmpty)
    }

    @Test
    fun `check unique`() {
        Assertions.assertTrue(userService.checkUnique(LOGIN, EMAIL))

        whenever(userRepository.findByLoginOrEmail(LOGIN, EMAIL)) doReturn optUser
        whenever(userRepository.findByLoginOrEmail(LOGIN, null)) doReturn optUser
        whenever(userRepository.findByLoginOrEmail(null, EMAIL)) doReturn optUser

        Assertions.assertFalse(userService.checkUnique(LOGIN, EMAIL))
        Assertions.assertTrue(userService.checkUnique("different", "different@test.com"))
    }

    @Test
    fun `save new user`() {
        whenever(userRepository.findByLoginOrEmail(any(), any())) doReturn empty
        whenever(userRepository.findByLoginOrEmailAndPassword(any(), any(), any())) doReturn empty

        Assertions.assertTrue(userService.checkUnique(LOGIN, EMAIL))
        Assertions.assertFalse(userService.findByCredentials(LOGIN, EMAIL, PASS).isPresent)

        whenever(userRepository.save(any())) doReturn optUser

        val saved = userService.register(USERNAME, LOGIN, EMAIL, PASS).run {
            Assertions.assertNotNull(this)

            this!!
        }

        Assertions.assertEquals(user, saved)

        whenever(userRepository.findByLoginOrEmail(LOGIN, EMAIL)) doReturn optUser
        whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, EMAIL, PASS)) doReturn optUser

        Assertions.assertFalse(userService.checkUnique(LOGIN, EMAIL))
        Assertions.assertTrue(userService.findByCredentials(LOGIN, EMAIL, PASS).isPresent)
    }

    @Test
    fun `double save`() {
        whenever(userRepository.save(any())) doReturn optUser

        val saved = userService.register(USERNAME, LOGIN, EMAIL, PASS)

        Assertions.assertNotNull(saved)
        Assertions.assertEquals(user, saved)

        whenever(userRepository.save(any())) doReturn empty

        val again = userService.register(USERNAME, LOGIN, EMAIL, PASS)

        Assertions.assertNull(again)

        whenever(userRepository.findByLoginOrEmailAndPassword(LOGIN, EMAIL, PASS)) doReturn optUser

        Assertions.assertTrue(userService.findByCredentials(LOGIN, EMAIL, PASS).isPresent)
    }
}
