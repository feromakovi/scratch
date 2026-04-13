package sk.o2.scratchcard.presentation.activation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.o2.scratchcard.R
import sk.o2.scratchcard.domain.model.ScratchCardState
import sk.o2.scratchcard.domain.model.ActivationError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val cardState by viewModel.cardState.collectAsStateWithLifecycle()
    val isActivating by viewModel.isActivating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    error?.let { activationError ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text(stringResource(R.string.error_dialog_title)) },
            text = {
                Text(
                    when (activationError) {
                        is ActivationError.Network -> stringResource(R.string.error_network)
                        is ActivationError.InvalidResponse -> stringResource(R.string.error_invalid_response)
                        is ActivationError.ThresholdNotMet -> stringResource(R.string.error_activation_failed)
                        is ActivationError.InvalidState -> stringResource(R.string.error_activation_failed)
                        is ActivationError.Unknown -> stringResource(R.string.error_unknown)
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text(stringResource(R.string.error_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activation_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (cardState) {
                is ScratchCardState.Unscratched -> {
                    if (isActivating) {
                        ActivatingIndicator()
                    } else {
                        Text(
                            text = stringResource(R.string.card_not_scratched),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is ScratchCardState.Scratched -> {
                    val scratched = cardState as ScratchCardState.Scratched
                    if (isActivating) {
                        ActivatingIndicator()
                    } else {
                        Text(
                            text = stringResource(R.string.state_scratched),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scratched.code,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.activate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(stringResource(R.string.activate_button))
                        }
                    }
                }

                is ScratchCardState.Activated -> {
                    val activated = cardState as ScratchCardState.Activated
                    Text(
                        text = stringResource(R.string.activation_success),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activated.code,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivatingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(64.dp),
        strokeWidth = 4.dp
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.activating_label),
        style = MaterialTheme.typography.bodyLarge
    )
}
