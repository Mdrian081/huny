const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const cors = require("cors")({ origin: true });

admin.initializeApp();
const db = admin.firestore();

/**
 * The API key is generated and stored from inside the Android app
 * (Setup Guide screen -> "Generate API key"), at config/apiAccess -> key.
 * Your website must send that same value in the "x-api-key" header.
 */
async function isValidApiKey(providedKey) {
  if (!providedKey) return false;
  const doc = await db.collection("config").doc("apiAccess").get();
  const expected = doc.exists ? doc.data().key : null;
  return expected != null && providedKey === expected;
}

/**
 * POST /verifyPayment
 * Headers: x-api-key: <key generated in the app>
 * Body (JSON): { "method": "Nagad", "trxId": "8N7XXXXXXX", "amount": 100 }
 */
exports.verifyPayment = onRequest({ cors: true }, async (req, res) => {
  cors(req, res, async () => {
    if (req.method !== "POST") {
      return res.status(405).json({ status: false, message: "Use POST" });
    }

    const providedKey = req.get("x-api-key");
    if (!(await isValidApiKey(providedKey))) {
      return res.status(401).json({ status: false, message: "Invalid or missing API key" });
    }

    const { method, trxId, amount } = req.body || {};
    if (!method || !trxId || amount === undefined) {
      return res.status(400).json({
        status: false,
        message: "method, trxId and amount are all required",
      });
    }

    try {
      const snap = await db
        .collection("transactions")
        .where("method", "==", method)
        .where("trxId", "==", trxId)
        .where("status", "==", "unused")
        .limit(1)
        .get();

      if (snap.empty) {
        return res.json({
          status: false,
          message: "No matching, unused payment found for that transaction ID.",
        });
      }

      const doc = snap.docs[0];
      const data = doc.data();

      const expected = Number(data.amount);
      const given = Number(amount);
      if (Math.abs(expected - given) > 0.01) {
        return res.json({
          status: false,
          message: `Amount mismatch. Expected ${expected}, got ${given}.`,
        });
      }

      await doc.ref.update({ status: "used" });

      return res.json({
        status: true,
        message: "Payment verified.",
        transactionId: doc.id,
        amount: expected,
        senderNumber: data.senderNumber || null,
      });
    } catch (err) {
      console.error(err);
      return res.status(500).json({ status: false, message: "Server error" });
    }
  });
});

/**
 * GET /balance?method=Nagad
 * Headers: x-api-key: <key generated in the app>
 */
exports.balance = onRequest({ cors: true }, async (req, res) => {
  cors(req, res, async () => {
    const providedKey = req.get("x-api-key");
    if (!(await isValidApiKey(providedKey))) {
      return res.status(401).json({ status: false, message: "Invalid or missing API key" });
    }

    const method = req.query.method;
    try {
      if (method) {
        const doc = await db.collection("balances").doc(method).get();
        if (!doc.exists) {
          return res.json({ status: true, method, currentBalance: 0 });
        }
        return res.json({ status: true, ...doc.data() });
      }
      const snap = await db.collection("balances").get();
      return res.json({ status: true, balances: snap.docs.map((d) => d.data()) });
    } catch (err) {
      console.error(err);
      return res.status(500).json({ status: false, message: "Server error" });
    }
  });
});

/**
 * GET /health - simple uptime check, no API key required.
 */
exports.health = onRequest((req, res) => {
  res.json({ status: true, message: "AutoPay API is running" });
});
