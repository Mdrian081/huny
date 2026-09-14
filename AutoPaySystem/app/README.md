# AutoPay Manager

A native Android app (Kotlin + Jetpack Compose) that reads incoming Nagad/bKash
payment SMS **on the device it is installed on** and syncs them to your own
Firebase project so you can match customer transaction IDs against real
incoming payments — the same idea used in the PipraPay open-source project,
rebuilt as a standalone app with a Firebase backend and extra features
(withdraw requests, a game top-up order form, wallet balance tracking).

The app is NOT tied to one Firebase project at build time. The first time
you open it, it asks you to pick your own `google-services.json` (downloaded
from your Firebase console) and connects to that project at runtime — so the
same APK works with any Firebase project.

## Features
- **Connect screen**: upload your own `google-services.json` on first launch
- Dashboard: total balance, verified/awaiting-review counts, recent activity
- Transactions: auto-filled from SMS, plus manual add
- Withdraw: request + history
- Top-up: simple Free Fire diamond order form (edit `packages` in
  `TopUpScreen.kt` for your own catalog/prices)
- **API Setup**: generate/regenerate an API key from inside the app, and copy
  ready-made PHP/Node.js code to paste into your website
- Settings: per-method wallet balance

## Permissions used (and why)
- `RECEIVE_SMS` / `READ_SMS` — read SMS that arrive on this phone
- `INTERNET` — sync with Firebase
- `FOREGROUND_SERVICE*`, `POST_NOTIFICATIONS` — keep the sync reliable in the background

Note: the original project you shared also requested `BROADCAST_SMS` as an
app permission. That permission lets an app *send/inject* fabricated SMS
broadcasts to other apps — it is not needed to read your own incoming SMS,
so it has been left out here.

## 1. Create a Firebase project
1. Go to https://console.firebase.google.com → Add project
2. Add an Android app (any package name works, since config is loaded at runtime)
3. Download `google-services.json`
4. Enable **Firestore Database** (Build → Firestore Database → Create database)

## 2. Deploy the companion API (see the separate AutoPayApi project)
That project turns the same Firestore data into plain HTTP endpoints your
website can call in any language.

## 3. Build the APK via GitHub Actions
1. Push this whole folder to a new GitHub repository
2. Go to the **Actions** tab → run the "Build APK" workflow
3. Once it finishes, download the APK from the workflow run's **Artifacts** section

## 4. Install and connect
1. Sideload `app-debug.apk` onto the phone that will receive the Nagad/bKash SMS
2. Grant the SMS permission when asked
3. On first launch, upload the `google-services.json` from step 1
4. Go to the **API Setup** tab to generate your API key and get the website code snippets

