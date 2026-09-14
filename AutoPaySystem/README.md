# AutoPay System

One repository, two parts, that work together:

```
AutoPaySystem/
├── app/   → the Android app (installs on your phone, reads Nagad/bKash SMS)
└── api/   → the backend (deploys to Firebase, your website calls this)
```

Both talk to the same Firebase project's Firestore database.

## Full setup (start to finish)

### 1. Create your Firebase project (one time, ~2 minutes)
1. Go to https://console.firebase.google.com → **Add project**
2. Inside it: **Build → Firestore Database → Create database** (test mode)
3. **⚙️ Project settings → Your apps → Android icon** → register any package
   name → download **google-services.json** (you'll upload this inside the
   app later — keep the file somewhere you can find it)
4. **⚙️ Project settings → Service accounts** → "Generate new private key" →
   downloads a second `.json` file (keep this one too, for step 3 below)
5. Also note your **Project ID**, shown at the top of Project settings

### 2. Push this whole folder to GitHub
Create one new GitHub repository and push everything in `AutoPaySystem/`
(both `app/` and `api/` folders, and the `.github/` folder) to it.

### 3. Add two GitHub secrets (for deploying the API)
In your repo → **Settings → Secrets and variables → Actions → New repository secret**:
- `FIREBASE_SERVICE_ACCOUNT` — paste the entire content of the service-account json from step 1.4
- `FIREBASE_PROJECT_ID` — your project ID from step 1.5

### 4. Run both GitHub Actions
Go to the **Actions** tab:
- Run **"Deploy API to Firebase"** → wait for it to finish → note the URLs it prints (e.g. `https://us-central1-<project-id>.cloudfunctions.net/verifyPayment`)
- Run **"Build APK"** → download the APK from that run's **Artifacts** section

### 5. Install the app and connect it
1. Sideload the APK onto the phone that will receive Nagad/bKash SMS
2. Grant the SMS permission when asked
3. On first launch: upload the `google-services.json` from step 1.3
4. Go to the **API Setup** tab → tap "Generate API key" → paste your Functions
   URL from step 4 → copy the ready-made code (PHP/Node/Python/HTML/curl)

### 6. Add the copied code to your website
Paste it wherever your site currently asks the customer for a transaction ID,
so it calls `/verifyPayment` and gets a real yes/no answer back.

---
See `app/README.md` and `api/README.md` for more detail on each part
individually (permissions used, endpoint reference, security notes).
