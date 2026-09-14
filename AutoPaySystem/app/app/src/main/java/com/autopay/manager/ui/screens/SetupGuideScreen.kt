package com.autopay.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.autopay.manager.data.FirebaseConfigManager
import com.autopay.manager.ui.AppViewModel
import com.autopay.manager.ui.theme.TextSecondary
import android.widget.Toast

@Composable
fun SetupGuideScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val apiKey by viewModel.apiKey.collectAsState()
    var functionsBaseUrl by remember { mutableStateOf("") }

    val config = remember { FirebaseConfigManager.load(context) }
    val projectId = config?.projectId ?: "your-project-id"

    fun copy(text: String, label: String) {
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Website Setup Guide", style = MaterialTheme.typography.headlineMedium)

        StepCard(number = 1, title = "Deploy the API (one time)") {
            Text(
                "Deploy the AutoPay Cloud Functions project to Firebase project " +
                    "\"$projectId\" using: firebase deploy --only functions,firestore:rules",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        StepCard(number = 2, title = "Generate an API key") {
            Text(
                "Your website sends this key on every request so only your site can verify payments.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            if (apiKey != null) {
                CopyableBox(label = "API key", value = apiKey!!) { copy(apiKey!!, "API key") }
            } else {
                Text("No key generated yet.", color = TextSecondary)
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { viewModel.generateApiKey() }) {
                Text(if (apiKey == null) "Generate API key" else "Regenerate (invalidates old key)")
            }
        }

        StepCard(number = 3, title = "Enter your deployed Functions URL") {
            OutlinedTextField(
                value = functionsBaseUrl,
                onValueChange = { functionsBaseUrl = it },
                label = { Text("e.g. https://us-central1-$projectId.cloudfunctions.net") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        val baseUrl = functionsBaseUrl.ifBlank { "https://us-central1-$projectId.cloudfunctions.net" }
        val key = apiKey ?: "YOUR_API_KEY"

        StepCard(number = 4, title = "Add this to your website (PHP)") {
            val code = phpSnippet(baseUrl, key)
            CodeBlock(code)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { copy(code, "PHP code") }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy PHP code")
            }
        }

        StepCard(number = 5, title = "Or Node.js") {
            val code = nodeSnippet(baseUrl, key)
            CodeBlock(code)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { copy(code, "Node.js code") }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Node.js code")
            }
        }

        StepCard(number = 6, title = "Or Python") {
            val code = pythonSnippet(baseUrl, key)
            CodeBlock(code)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { copy(code, "Python code") }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Python code")
            }
        }

        StepCard(number = 7, title = "Or plain HTML / JavaScript (browser)") {
            Text(
                "Only use this if the key doesn't need to stay secret (e.g. a " +
                    "private admin tool) - anyone who views the page source can see it. " +
                    "For a public checkout page, call your own server instead (steps 4-6) " +
                    "and have your server call the API.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            val code = htmlSnippet(baseUrl, key)
            CodeBlock(code)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { copy(code, "HTML/JS code") }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy HTML/JS code")
            }
        }

        StepCard(number = 8, title = "Quick test (curl, from any terminal)") {
            val code = curlSnippet(baseUrl, key)
            CodeBlock(code)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { copy(code, "curl command") }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy curl command")
            }
        }
    }
}

@Composable
private fun StepCard(number: Int, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Step $number: $title", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun CopyableBox(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onCopy) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label")
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            code,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun phpSnippet(baseUrl: String, apiKey: String) = """
${'$'}ch = curl_init("$baseUrl/verifyPayment");
curl_setopt(${'$'}ch, CURLOPT_POST, true);
curl_setopt(${'$'}ch, CURLOPT_HTTPHEADER, [
    "Content-Type: application/json",
    "x-api-key: $apiKey"
]);
curl_setopt(${'$'}ch, CURLOPT_POSTFIELDS, json_encode([
    "method" => "Nagad",
    "trxId"  => ${'$'}_POST['trx_id'],
    "amount" => ${'$'}_POST['amount']
]));
curl_setopt(${'$'}ch, CURLOPT_RETURNTRANSFER, true);
${'$'}response = json_decode(curl_exec(${'$'}ch), true);
""".trimIndent()

private fun nodeSnippet(baseUrl: String, apiKey: String) = """
const res = await fetch("$baseUrl/verifyPayment", {
  method: "POST",
  headers: { "Content-Type": "application/json", "x-api-key": "$apiKey" },
  body: JSON.stringify({ method: "Nagad", trxId, amount })
});
const data = await res.json();
""".trimIndent()

private fun pythonSnippet(baseUrl: String, apiKey: String) = """
import requests

response = requests.post(
    "$baseUrl/verifyPayment",
    headers={"x-api-key": "$apiKey"},
    json={"method": "Nagad", "trxId": trx_id, "amount": amount}
)
data = response.json()

if data["status"]:
    pass  # payment confirmed - unlock the order / add balance / etc.
else:
    pass  # not found or amount mismatch - see data["message"]
""".trimIndent()

private fun htmlSnippet(baseUrl: String, apiKey: String) = """
<script>
async function verifyPayment(trxId, amount) {
  const res = await fetch("$baseUrl/verifyPayment", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-api-key": "$apiKey" },
    body: JSON.stringify({ method: "Nagad", trxId, amount })
  });
  const data = await res.json();
  if (data.status) {
    alert("Payment verified!");
  } else {
    alert(data.message);
  }
}
</script>
""".trimIndent()

private fun curlSnippet(baseUrl: String, apiKey: String) = """
curl -X POST "$baseUrl/verifyPayment" \
  -H "Content-Type: application/json" \
  -H "x-api-key: $apiKey" \
  -d '{"method":"Nagad","trxId":"8N7XXXXXXX","amount":100}'
""".trimIndent()
