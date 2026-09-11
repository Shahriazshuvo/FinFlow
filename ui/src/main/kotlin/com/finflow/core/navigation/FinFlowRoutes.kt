package com.finflow.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

/**
 * Every destination key in the app, in one module (APP_SPEC.md §6).
 *
 * Keys live here rather than in the feature that renders the screen so that one feature can
 * navigate to another without depending on it: the dashboard needs to send the user to the
 * transaction form, but it must not be able to call into `feature:transactions`. A key is a
 * `@Serializable` object with no behaviour, so sharing it couples nothing.
 *
 * The `NavGraphBuilder.xScreen()` builders stay in their own feature — those reference the
 * screen composable, which is exactly what must not be shared.
 *
 * Navigation arguments are ids and primitives only, never domain objects: the destination
 * loads what it needs from the data layer, so there is one source of truth.
 */

// --- Graphs (§6: the root graph has exactly two children) ---

/**
 * The signed-out half of the graph. Auth state decides which of the two the app starts in,
 * and crossing between them pops the other inclusively so back never re-enters it.
 */
@Serializable
data object AuthGraphRouteKey

/** The signed-in half of the graph. Everything behind the bottom bar lives under it. */
@Serializable
data object MainGraphRouteKey

// --- Auth graph ---

/**
 * Sign in and sign up, as one destination.
 *
 * §6 draws them as two children of the auth graph; §13 draws them as one "Login/Signup
 * Screen". This follows §13, because `feature:auth` renders both from a single `AuthScreen`
 * with a mode flag and one ViewModel: the credential handling is shared, and two screens
 * would drift apart. Splitting the key without splitting the screen would also scope a
 * separate ViewModel to each destination, so toggling from sign-in to sign-up would silently
 * discard the email the user had already typed.
 *
 * The graph shape §6 actually cares about — auth and main as siblings under the root, so the
 * back stack can be popped a graph at a time — is honoured by [AuthGraphRouteKey].
 */
@Serializable
data object AuthRouteKey

// --- Main graph: top-level destinations ---

@Serializable
data object DashboardRouteKey

@Serializable
data object TransactionsRouteKey

@Serializable
data object AccountsRouteKey

@Serializable
data object CategoriesRouteKey

@Serializable
data object BudgetsRouteKey

@Serializable
data object GoalsRouteKey

@Serializable
data object AnalyticsRouteKey

@Serializable
data object SettingsRouteKey

// --- Main graph: detail destinations ---
//
// §6 specifies `transactions/{transactionId}`, `accounts/{accountId}`, `budgets/{budgetId}`
// and `goals/{goalId}`. Typed route classes give the same addressing without hand-parsed
// string arguments. Each carries an id and nothing else — the screen loads the record from
// the data layer, so a stale object can never be passed across a navigation boundary.
//
// The keys are declared here, with the rest of the contract, rather than appearing one at a
// time as screens land. A destination is live once its feature registers a
// `composable<XDetailRouteKey>`; today that is none of them, and the `navigateTo…` helpers
// below are the call sites that will light up when each screen is built.

@Serializable
data class TransactionDetailRouteKey(val transactionId: String)

@Serializable
data class AccountDetailRouteKey(val accountId: String)

@Serializable
data class BudgetDetailRouteKey(val budgetId: String)

@Serializable
data class GoalDetailRouteKey(val goalId: String)

// --- Navigation helpers ---

fun NavController.navigateToAuthGraph(navOptions: NavOptions? = null) =
    navigate(AuthGraphRouteKey, navOptions)

fun NavController.navigateToMainGraph(navOptions: NavOptions? = null) =
    navigate(MainGraphRouteKey, navOptions)

fun NavController.navigateToAuth(navOptions: NavOptions? = null) =
    navigate(AuthRouteKey, navOptions)

fun NavController.navigateToDashboard(navOptions: NavOptions? = null) =
    navigate(DashboardRouteKey, navOptions)

fun NavController.navigateToTransactions(navOptions: NavOptions? = null) =
    navigate(TransactionsRouteKey, navOptions)

fun NavController.navigateToAccounts(navOptions: NavOptions? = null) =
    navigate(AccountsRouteKey, navOptions)

fun NavController.navigateToCategories(navOptions: NavOptions? = null) =
    navigate(CategoriesRouteKey, navOptions)

fun NavController.navigateToBudgets(navOptions: NavOptions? = null) =
    navigate(BudgetsRouteKey, navOptions)

fun NavController.navigateToGoals(navOptions: NavOptions? = null) =
    navigate(GoalsRouteKey, navOptions)

fun NavController.navigateToAnalytics(navOptions: NavOptions? = null) =
    navigate(AnalyticsRouteKey, navOptions)

fun NavController.navigateToSettings(navOptions: NavOptions? = null) =
    navigate(SettingsRouteKey, navOptions)

fun NavController.navigateToTransactionDetail(
    transactionId: String,
    navOptions: NavOptions? = null,
) = navigate(TransactionDetailRouteKey(transactionId), navOptions)

fun NavController.navigateToAccountDetail(
    accountId: String,
    navOptions: NavOptions? = null,
) = navigate(AccountDetailRouteKey(accountId), navOptions)

fun NavController.navigateToBudgetDetail(
    budgetId: String,
    navOptions: NavOptions? = null,
) = navigate(BudgetDetailRouteKey(budgetId), navOptions)

fun NavController.navigateToGoalDetail(
    goalId: String,
    navOptions: NavOptions? = null,
) = navigate(GoalDetailRouteKey(goalId), navOptions)
