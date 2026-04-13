package sk.o2.scratchcard.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sk.o2.scratchcard.R
import sk.o2.scratchcard.domain.model.ScratchCardState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToScratch: () -> Unit,
    onNavigateToActivation: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val cardState by viewModel.cardState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.main_title)) },
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
            StateCard(cardState = cardState)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToScratch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = cardState is ScratchCardState.Unscratched
            ) {
                Text(stringResource(R.string.go_to_scratch))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToActivation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = cardState is ScratchCardState.Scratched
            ) {
                Text(stringResource(R.string.go_to_activation))
            }
        }
    }
}

@Composable
private fun StateCard(cardState: ScratchCardState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.state_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (cardState) {
                    is ScratchCardState.Unscratched -> stringResource(R.string.state_unscratched)
                    is ScratchCardState.Scratched -> stringResource(R.string.state_scratched)
                    is ScratchCardState.Activated -> stringResource(R.string.state_activated)
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (cardState) {
                    is ScratchCardState.Unscratched -> MaterialTheme.colorScheme.onSurface
                    is ScratchCardState.Scratched -> MaterialTheme.colorScheme.primary
                    is ScratchCardState.Activated -> MaterialTheme.colorScheme.tertiary
                }
            )

            val code = when (cardState) {
                is ScratchCardState.Scratched -> cardState.code
                is ScratchCardState.Activated -> cardState.code
                else -> null
            }
            if (code != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
