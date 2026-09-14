# AutoPay API (Firebase Cloud Functions)

A tiny HTTPS API in front of the same Firestore project your Android app
writes to. Your website calls these plain HTTP endpoints — no Firebase SDK,
no extra library, works from any language.

The API key is generated directly inside the Android app (bottom nav →
**API Setup** → "Generate API key"). The function checks incoming requests
against whatever key is currently stored there.

## Endpoints (after deploy, Firebase gives you the real URLs)
- `POST /verifyPayment` — check a customer's trx ID + amount against SMS-verified payments, marks it used if it matches
- `GET /balance?method=Nagad` — read current wallet balance
- `GET /health` — uptime check (no key needed)

## Deploy — no terminal needed, via GitHub Actions

1. Push this whole `AutoPayApi` folder to a new GitHub repository
2. In Firebase console → ⚙️ **Project settings → Service accounts** →
   "Generate new private key" → downloads a `.json` file
3. In your GitHub repo → **Settings → Secrets and variables → Actions** →
   add two secrets:
   - `FIREBASE_SERVICE_ACCOUNT` — paste the entire content of that downloaded json file
   - `FIREBASE_PROJECT_ID` — your Firebase project ID (Project settings → General)
4. Go to the **Actions** tab → run the "Deploy API to Firebase" workflow
5. When it finishes, your endpoints are live at:
   `https://us-central1-<your-project-id>.cloudfunctions.net/verifyPayment`
   (and `/balance`, `/health`)

Any time you edit `functions/index.js` and push to `main`, it redeploys automatically.

## Get your API key and code snippets from the app
Open the app → **API Setup** tab:
1. Tap "Generate API key" (copy it)
2. Paste your deployed Functions base URL (from step 5 above)
3. Copy the ready-made code snippet (PHP, Node, Python, HTML/JS, or curl) shown there into your site

## (Optional) Manual deploy from your own computer instead
```bash
npm install -g firebase-tools
firebase login
# edit .firebaserc with your project ID
firebase deploy --only functions,firestore:rules
```

**PHP**
```php
<?php
$ch = curl_init("https://us-central1-your-project.cloudfunctions.net/verifyPayment");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Content-Type: application/json",
    "x-api-key: YOUR_APP_GENERATED_KEY"
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
    "method" => "Nagad",
    "trxId"  => $_POST['trx_id'],
    "amount" => $_POST['amount']
]));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = json_decode(curl_exec($ch), true);

if ($response['status'] === true) {
    // payment confirmed - unlock the order / add balance / etc.
} else {
    // not found or amount mismatch - show $response['message']
}
```

**Node.js**
```js
const res = await fetch("https://us-central1-your-project.cloudfunctions.net/verifyPayment", {
  method: "POST",
  headers: { "Content-Type": "application/json", "x-api-key": "YOUR_APP_GENERATED_KEY" },
  body: JSON.stringify({ method: "Nagad", trxId: "8N7XXXXXXX", amount: 100 })
});
const data = await res.json();
```

**Python**
```python
import requests

response = requests.post(
    "https://us-central1-your-project.cloudfunctions.net/verifyPayment",
    headers={"x-api-key": "YOUR_APP_GENERATED_KEY"},
    json={"method": "Nagad", "trxId": "8N7XXXXXXX", "amount": 100}
)
data = response.json()
```

**curl (quick test from any terminal)**
```bash
curl -X POST "https://us-central1-your-project.cloudfunctions.net/verifyPayment" \
  -H "Content-Type: application/json" \
  -H "x-api-key: YOUR_APP_GENERATED_KEY" \
  -d '{"method":"Nagad","trxId":"8N7XXXXXXX","amount":100}'
```

## Notes
- `verifyPayment` checks BOTH the transaction ID and the amount before marking
  anything as used, and it won't match a trx ID that's already been used —
  so the same transaction ID can't be replayed twice.
- Keep the API key out of any public frontend JS if possible; call it from
  your server-side code instead so the key isn't visible to site visitors.
- `firestore.rules` in this folder keeps direct client writes to the
  sensitive `status` field blocked; only the API (Admin SDK) can mark a
  transaction "used", regardless of what any client sends.
