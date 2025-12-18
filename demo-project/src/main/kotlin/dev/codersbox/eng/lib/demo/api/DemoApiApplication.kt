package dev.codersbox.eng.lib.demo.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@SpringBootApplication
class DemoApiApplication

fun main(args: Array<String>) {
    runApplication<DemoApiApplication>(*args)
}

// Data Models
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String = "user",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val inStock: Boolean = true
)

data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val category: String
)

data class Order(
    val id: Long,
    val userId: Long,
    val products: List<OrderItem>,
    val totalAmount: Double,
    val status: String = "pending",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: Long,
    val quantity: Int,
    val price: Double
)

data class CreateOrderRequest(
    val userId: Long,
    val items: List<OrderItem>
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// REST Controllers
@RestController
@RequestMapping("/api/users")
class UserController {
    private val users = ConcurrentHashMap<Long, User>()
    private val idCounter = AtomicLong(1)

    init {
        // Seed with initial data
        createUser(CreateUserRequest("admin", "admin@example.com", "Admin", "User", "admin123"))
        createUser(CreateUserRequest("john", "john@example.com", "John", "Doe", "pass123"))
    }

    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false) role: String?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) limit: Int?
    ): ApiResponse<List<User>> {
        var userList = users.values.toList()
        
        if (role != null) {
            userList = userList.filter { it.role == role }
        }
        
        val pageNum = page ?: 0
        val pageSize = limit ?: 10
        val startIndex = pageNum * pageSize
        val endIndex = minOf(startIndex + pageSize, userList.size)
        
        val pagedUsers = if (startIndex < userList.size) {
            userList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return ApiResponse(true, pagedUsers)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<User> {
        val user = users[id]
        return if (user != null) {
            ApiResponse(true, user)
        } else {
            ApiResponse(false, message = "User not found")
        }
    }

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ApiResponse<User> {
        val id = idCounter.getAndIncrement()
        val user = User(
            id = id,
            username = request.username,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName
        )
        users[id] = user
        return ApiResponse(true, user, "User created successfully")
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody request: UpdateUserRequest): ApiResponse<User> {
        val existingUser = users[id]
        return if (existingUser != null) {
            val updatedUser = existingUser.copy(
                email = request.email ?: existingUser.email,
                firstName = request.firstName ?: existingUser.firstName,
                lastName = request.lastName ?: existingUser.lastName
            )
            users[id] = updatedUser
            ApiResponse(true, updatedUser, "User updated successfully")
        } else {
            ApiResponse(false, message = "User not found")
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ApiResponse<String> {
        return if (users.remove(id) != null) {
            ApiResponse(true, "User deleted", "User deleted successfully")
        } else {
            ApiResponse(false, message = "User not found")
        }
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam query: String): ApiResponse<List<User>> {
        val results = users.values.filter {
            it.username.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.firstName.contains(query, ignoreCase = true) ||
            it.lastName.contains(query, ignoreCase = true)
        }
        return ApiResponse(true, results)
    }
}

@RestController
@RequestMapping("/api/products")
class ProductController {
    private val products = ConcurrentHashMap<Long, Product>()
    private val idCounter = AtomicLong(1)

    init {
        // Seed with initial data
        createProduct(CreateProductRequest("Laptop", "High-performance laptop", 999.99, "Electronics"))
        createProduct(CreateProductRequest("Mouse", "Wireless mouse", 29.99, "Electronics"))
        createProduct(CreateProductRequest("Desk", "Ergonomic desk", 299.99, "Furniture"))
    }

    @GetMapping
    fun getAllProducts(@RequestParam(required = false) category: String?): ApiResponse<List<Product>> {
        val productList = if (category != null) {
            products.values.filter { it.category == category }
        } else {
            products.values.toList()
        }
        return ApiResponse(true, productList)
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ApiResponse<Product> {
        val product = products[id]
        return if (product != null) {
            ApiResponse(true, product)
        } else {
            ApiResponse(false, message = "Product not found")
        }
    }

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ApiResponse<Product> {
        val id = idCounter.getAndIncrement()
        val product = Product(
            id = id,
            name = request.name,
            description = request.description,
            price = request.price,
            category = request.category
        )
        products[id] = product
        return ApiResponse(true, product, "Product created successfully")
    }
}

@RestController
@RequestMapping("/api/orders")
class OrderController {
    private val orders = ConcurrentHashMap<Long, Order>()
    private val idCounter = AtomicLong(1)

    @GetMapping
    fun getAllOrders(): ApiResponse<List<Order>> {
        return ApiResponse(true, orders.values.toList())
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: Long): ApiResponse<Order> {
        val order = orders[id]
        return if (order != null) {
            ApiResponse(true, order)
        } else {
            ApiResponse(false, message = "Order not found")
        }
    }

    @PostMapping
    fun createOrder(@RequestBody request: CreateOrderRequest): ApiResponse<Order> {
        val id = idCounter.getAndIncrement()
        val totalAmount = request.items.sumOf { it.price * it.quantity }
        val order = Order(
            id = id,
            userId = request.userId,
            products = request.items,
            totalAmount = totalAmount
        )
        orders[id] = order
        return ApiResponse(true, order, "Order created successfully")
    }

    @PatchMapping("/{id}/status")
    fun updateOrderStatus(@PathVariable id: Long, @RequestBody statusUpdate: Map<String, String>): ApiResponse<Order> {
        val order = orders[id]
        return if (order != null) {
            val newStatus = statusUpdate["status"] ?: order.status
            val updatedOrder = order.copy(status = newStatus)
            orders[id] = updatedOrder
            ApiResponse(true, updatedOrder, "Order status updated")
        } else {
            ApiResponse(false, message = "Order not found")
        }
    }
}

@RestController
@RequestMapping("/api/auth")
class AuthController {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ApiResponse<LoginResponse> {
        // Simple mock authentication
        return if (request.username == "admin" && request.password == "admin123") {
            val response = LoginResponse(
                token = "mock-jwt-token-${System.currentTimeMillis()}",
                userId = 1,
                username = request.username
            )
            ApiResponse(true, response, "Login successful")
        } else {
            ApiResponse(false, message = "Invalid credentials")
        }
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") token: String): ApiResponse<Boolean> {
        return if (token.startsWith("Bearer mock-jwt-token")) {
            ApiResponse(true, true, "Token is valid")
        } else {
            ApiResponse(false, false, "Invalid token")
        }
    }
}

// Health Check
@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to LocalDateTime.now(),
            "version" to "1.0.0"
        )
    }
}
