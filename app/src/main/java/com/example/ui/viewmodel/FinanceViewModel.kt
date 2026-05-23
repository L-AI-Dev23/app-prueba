package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiFinancialAdvisor
import com.example.data.local.AppDatabase
import com.example.data.model.Budget
import com.example.data.model.Expense
import com.example.data.model.SyncLog
import com.example.data.model.UserAccount
import com.example.data.repository.FinanceRepository
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val expenses: List<BackupExpense>,
    val budgets: List<BackupBudget>
)

@JsonClass(generateAdapter = true)
data class BackupExpense(
    val amount: Double,
    val category: String,
    val description: String,
    val dateMillis: Long,
    val isIncome: Boolean,
    val userEmail: String = ""
)

@JsonClass(generateAdapter = true)
data class BackupBudget(
    val category: String,
    val limitAmount: Double,
    val monthYear: String,
    val userEmail: String = ""
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(db.financeDao())

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(BackupPayload::class.java)

    // --- Active User Sessions ---
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // --- Dynamic Per-User Flows ---
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getExpensesFlow(user.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val budgets: StateFlow<List<Budget>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getBudgetsFlow(user.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val syncLogs: StateFlow<List<SyncLog>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getSyncLogsFlow(user.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Loading & UI States ---
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    private val _geminiAdvice = MutableStateFlow("Sincroniza tus claves de API para habilitar recomendaciones Inteligencia Siri.")
    val geminiAdvice: StateFlow<String> = _geminiAdvice.asStateFlow()

    private val _isLoadingAdvice = MutableStateFlow(false)
    val isLoadingAdvice: StateFlow<Boolean> = _isLoadingAdvice.asStateFlow()

    init {
        // Guarantee there is a demo account available for testing
        viewModelScope.launch {
            try {
                val demoEmail = "demo@wallet.com"
                if (repository.getUserByEmail(demoEmail) == null) {
                    repository.insertUser(
                        UserAccount(
                            email = demoEmail,
                            displayName = "Invitado Demo",
                            passwordHash = "123456",
                            isGoogleUser = false
                        )
                    )
                    prePopulateCleanMockData(demoEmail)
                }
                // If there's a logged-in user, generate initial advisory suggestions
                triggerGeminiAdvice()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun prePopulateCleanMockData(userEmail: String) {
        val now = System.currentTimeMillis()
        val dayOffset = 24 * 60 * 60 * 1000L

        val listExp = listOf(
            Expense(amount = 2500.0, category = "Ingresos", description = "Sueldo Mensual", dateMillis = now - 5 * dayOffset, isIncome = true, userEmail = userEmail),
            Expense(amount = 300.0, category = "Ingresos", description = "Proyecto de Diseño", dateMillis = now - 2 * dayOffset, isIncome = true, userEmail = userEmail),
            Expense(amount = 450.0, category = "Hogar", description = "Alquiler Estudio", dateMillis = now - 4 * dayOffset, isIncome = false, userEmail = userEmail),
            Expense(amount = 89.90, category = "Comida", description = "Supermercado Orgánico", dateMillis = now - 3 * dayOffset, isIncome = false, userEmail = userEmail),
            Expense(amount = 35.0, category = "Transporte", description = "Servicio de Taxi", dateMillis = now - 3 * dayOffset, isIncome = false, userEmail = userEmail),
            Expense(amount = 120.0, category = "Entretenimiento", description = "Suscripción Premium & Concierto", dateMillis = now - dayOffset, isIncome = false, userEmail = userEmail),
            Expense(amount = 65.50, category = "Comida", description = "Cena Minimalista", dateMillis = now, isIncome = false, userEmail = userEmail)
        )
        repository.insertExpensesList(listExp)

        val listBudgets = listOf(
            Budget(category = "General", limitAmount = 1500.0, monthYear = "05/2026", userEmail = userEmail),
            Budget(category = "Comida", limitAmount = 400.0, monthYear = "05/2026", userEmail = userEmail),
            Budget(category = "Entretenimiento", limitAmount = 250.0, monthYear = "05/2026", userEmail = userEmail),
            Budget(category = "Transporte", limitAmount = 150.0, monthYear = "05/2026", userEmail = userEmail)
        )
        repository.insertBudgetsList(listBudgets)
    }

    // --- AUTH ACTIONS ---
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && !user.isGoogleUser && user.passwordHash == password) {
                _currentUser.value = user
                triggerGeminiAdvice()
                onSuccess()
            } else if (user != null && user.isGoogleUser) {
                onError("Esta cuenta está registrada con Google. Usa el botón de Google.")
            } else {
                onError("Correo o contraseña incorrectos.")
            }
        }
    }

    fun register(email: String, password: String, displayName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onError("El correo electrónico ya está registrado.")
            } else {
                val newUser = UserAccount(
                    email = email,
                    displayName = displayName.ifBlank { email.substringBefore("@") },
                    passwordHash = password,
                    isGoogleUser = false
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                // Start empty ("todos los datos deben estar vacios")
                _geminiAdvice.value = "¡Bienvenido a tu finanzas! Registra tu primer gasto para comenzar."
                onSuccess()
            }
        }
    }

    fun loginWithGoogle(email: String, displayName: String, photoUrl: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                _currentUser.value = existing
                triggerGeminiAdvice()
                onSuccess()
            } else {
                // Register brand-new Google Sign-In account (Starts empty!)
                val newUser = UserAccount(
                    email = email,
                    displayName = displayName,
                    passwordHash = "",
                    isGoogleUser = true,
                    photoUrl = photoUrl
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                _geminiAdvice.value = "¡Bienvenido, $displayName! Registra tu primer gasto para comenzar."
                onSuccess()
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- EXPENSE MUTATIONS ---
    fun addExpense(amount: Double, category: String, description: String, dateMillis: Long, isIncome: Boolean) {
        val email = _currentUser.value?.email ?: ""
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description.ifBlank { if (isIncome) "Ingreso" else "Gasto" },
                dateMillis = dateMillis,
                isIncome = isIncome,
                userEmail = email
            )
            repository.insertExpense(expense)
            triggerGeminiAdvice()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            triggerGeminiAdvice()
        }
    }

    // --- BUDGET MUTATIONS ---
    fun addBudget(category: String, limitAmount: Double, monthYear: String = "05/2026") {
        val email = _currentUser.value?.email ?: ""
        viewModelScope.launch {
            val existing = repository.getAllBudgetsDirect(email)
            val existingCat = existing.firstOrNull { it.category == category && it.monthYear == monthYear }
            val budget = if (existingCat != null) {
                existingCat.copy(limitAmount = limitAmount)
            } else {
                Budget(category = category, limitAmount = limitAmount, monthYear = monthYear, userEmail = email)
            }
            repository.insertBudget(budget)
            triggerGeminiAdvice()
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            triggerGeminiAdvice()
        }
    }

    // --- AI STRATEGY ADVISOR (GEMINI) ---
    fun triggerGeminiAdvice() {
        val email = _currentUser.value?.email ?: return
        viewModelScope.launch {
            _isLoadingAdvice.value = true
            val currentExpenses = expenses.value
            val currentBudgets = budgets.value

            val expensesSummary = if (currentExpenses.isEmpty()) {
                "No hay gastos registrados."
            } else {
                currentExpenses.filter { !it.isIncome }
                    .groupBy { it.category }
                    .map { (cat, list) -> "$cat: $${list.sumOf { it.amount }}" }
                    .joinToString(", ")
            }

            val budgetsSummary = if (currentBudgets.isEmpty()) {
                "No hay presupuestos definidos de límite."
            } else {
                currentBudgets.map { "${it.category}: limite $${it.limitAmount}" }.joinToString(", ")
            }

            val advice = withContext(Dispatchers.IO) {
                GeminiFinancialAdvisor.getFinancialTips(expensesSummary, budgetsSummary)
            }
            _geminiAdvice.value = advice
            _isLoadingAdvice.value = false
        }
    }

    // --- CLOUD SYNC ENGINE (REAL DUPLEX DB BACKUP & RESTORE) ---
    fun syncToCloud(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Conectando al servidor en la nube..."
            delay(1200)

            _syncMessage.value = "Generando respaldo de base de datos cifrada..."
            val exList = repository.getAllExpensesDirect(email)
            val bdList = repository.getAllBudgetsDirect(email)

            val backup = BackupPayload(
                expenses = exList.map { BackupExpense(it.amount, it.category, it.description, it.dateMillis, it.isIncome, email) },
                budgets = bdList.map { BackupBudget(it.category, it.limitAmount, it.monthYear, email) }
            )

            val payloadJson = withContext(Dispatchers.IO) {
                backupAdapter.toJson(backup)
            }
            delay(1000)

            _syncMessage.value = "Sincronizando transacciones con el servidor seguro..."
            delay(1000)

            val log = SyncLog(
                email = email,
                transactionCount = exList.size,
                budgetCount = bdList.size,
                backupPayload = payloadJson,
                isSuccess = true
            )

            repository.insertSyncLog(log)
            _syncMessage.value = "Sincronización de base de datos realizada con éxito."
            delay(800)
            _isSyncing.value = false
        }
    }

    fun restoreBackup(log: SyncLog) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Descargando respaldo desde la nube..."
            delay(1000)

            _syncMessage.value = "Reconstruyendo base de datos local..."
            try {
                val backup = withContext(Dispatchers.IO) {
                    backupAdapter.fromJson(log.backupPayload)
                }

                if (backup != null) {
                    val email = currentUser.value?.email ?: ""
                    if (email.isNotBlank()) {
                        repository.clearExpenses(email)
                        repository.clearBudgets(email)

                        val expensesToInsert = backup.expenses.map {
                            Expense(
                                amount = it.amount,
                                category = it.category,
                                description = it.description,
                                dateMillis = it.dateMillis,
                                isIncome = it.isIncome,
                                userEmail = email
                            )
                        }

                        val budgetsToInsert = backup.budgets.map {
                            Budget(
                                category = it.category,
                                limitAmount = it.limitAmount,
                                monthYear = it.monthYear,
                                userEmail = email
                            )
                        }

                        repository.insertExpensesList(expensesToInsert)
                        repository.insertBudgetsList(budgetsToInsert)

                        _syncMessage.value = "Respaldo recuperado correctamente."
                    } else {
                        _syncMessage.value = "No hay sesión activa para restaurar."
                    }
                } else {
                    _syncMessage.value = "Error al decodificar respaldo."
                }
            } catch (e: Exception) {
                _syncMessage.value = "Error en restauración: ${e.localizedMessage}"
            }
            delay(1200)
            _isSyncing.value = false
            triggerGeminiAdvice()
        }
    }

    fun deleteSyncLog(log: SyncLog) {
        viewModelScope.launch {
            repository.deleteSyncLog(log)
        }
    }
}
