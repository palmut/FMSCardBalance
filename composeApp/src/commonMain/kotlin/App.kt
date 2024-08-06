import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.BalanceRepository
import data.DefaultBalanceRepository
import data.Preferences
import fmscardbalance.composeapp.generated.resources.Res
import fmscardbalance.composeapp.generated.resources.app_title
import fmscardbalance.composeapp.generated.resources.balance_amount
import fmscardbalance.composeapp.generated.resources.balance_error
import fmscardbalance.composeapp.generated.resources.card_number
import fmscardbalance.composeapp.generated.resources.check_button
import fmscardbalance.composeapp.generated.resources.error_connection
import fmscardbalance.composeapp.generated.resources.error_other
import fmscardbalance.composeapp.generated.resources.phone_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    AppTheme {

        val scope = rememberCoroutineScope()
        val phone = mutableStateOf(Preferences.INSTANCE.getString("phone") ?: "")
        val card = mutableStateOf(Preferences.INSTANCE.getString("card") ?: "")
        val balance = mutableStateOf(BalanceRepository.Response())
        val keyboardController = LocalSoftwareKeyboardController.current
        val loading = mutableStateOf(false)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(Res.string.app_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.size(8.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    label = {
                        Text(
                            text = stringResource(Res.string.phone_label),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    value = phone.value,
                    onValueChange = { phone.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )
                Spacer(modifier = Modifier.size(16.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    label = {
                        Text(
                            text = stringResource(Res.string.card_number),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    value = card.value,
                    onValueChange = { card.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    enabled = !loading.value,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        scope.launch {
                            Preferences.INSTANCE.putString("phone", phone.value)
                            Preferences.INSTANCE.putString("card", card.value)
                            keyboardController?.hide()
                            try {
                                loading.value = true
                                val repository = DefaultBalanceRepository()
                                balance.value = repository.getBalance(phone.value, card.value)
                            } catch (e: Exception) {
                                balance.value = BalanceRepository.Response(
                                    status = "CONNECTION",
                                )
                            } finally {
                                loading.value = false
                            }
                        }
                    }
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(Res.string.check_button),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                AnimatedVisibility(balance.value.data?.history != null) {
                    LazyColumn {
                        stickyHeader {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface
                                    )
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .weight(1f),
                                    style = MaterialTheme.typography.headlineMedium,
                                    text = stringResource(Res.string.balance_amount),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.headlineMedium,
                                    text = balance.value.data?.balance?.availableAmount.toString(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        balance.value.data?.history?.let { transactions ->
                            items(transactions.size) { index ->
                                val item = transactions[index]
                                Row {
                                    Text(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(8.dp)
                                            .weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = item.locationName?.firstOrNull() ?: ""
                                    )
                                    Text(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(start = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = item.amount.toString(),
                                        color = if ((item.amount ?: 0.0) > 0.0) {
                                            Color.Green
                                        } else {
                                            Color.Red
                                        }
                                    )
                                    Text(
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(8.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        text = item.currency.toString()
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(balance.value.status != null && balance.value.status != "OK") {
                    Row {
                        Text(
                            modifier = Modifier
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(Res.string.balance_error),
                            color = Color.Red
                        )
                        Text(
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            text = balance.value.status.errorToString(),
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun String?.errorToString() = when (this) {
    "CONNECTION" -> stringResource(Res.string.error_connection)
    else -> stringResource(Res.string.error_other)
}
