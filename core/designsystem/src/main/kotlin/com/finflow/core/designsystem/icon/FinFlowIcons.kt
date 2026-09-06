package com.finflow.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

/** One place to swap an icon app-wide; features never reference `Icons.*` directly. */
object FinFlowIcons {
    val Add = Icons.Filled.Add
    val Account = Icons.Filled.AccountBalanceWallet
    val Back = Icons.AutoMirrored.Filled.ArrowBack
    val Category = Icons.Filled.Category
    val Check = Icons.Filled.Check
    val Close = Icons.Filled.Close
    val Delete = Icons.Filled.Delete
    val Edit = Icons.Filled.Edit
    val Error = Icons.Filled.ErrorOutline
    val Filter = Icons.Filled.FilterList
    val Goal = Icons.Filled.Flag
    val Dashboard = Icons.Filled.Home
    val Empty = Icons.Filled.Inbox
    val Logout = Icons.AutoMirrored.Filled.Logout
    val Analytics = Icons.Filled.PieChart
    val Budget = Icons.Filled.Savings
    val Search = Icons.Filled.Search
    val Settings = Icons.Filled.Settings
    val Transactions = Icons.Filled.SwapVert
    val Income = Icons.AutoMirrored.Filled.TrendingUp
    val Expense = Icons.AutoMirrored.Filled.TrendingDown
    val PasswordVisible = Icons.Filled.Visibility
    val PasswordHidden = Icons.Filled.VisibilityOff
    val SyncDone = Icons.Filled.CloudDone
    val SyncPending = Icons.Filled.CloudSync
    val SyncOffline = Icons.Filled.CloudOff
}
