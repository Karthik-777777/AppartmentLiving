const express = require("express");
const router = express.Router();
const Payment = require("../models/Payment");
const auth = require("../middleware/auth");

// Admin authorization helper middleware
const adminOnly = (req, res, next) => {
  if (req.user && (req.user.role === "admin" || req.user.role === "Admin")) {
    next();
  } else {
    res.status(403).json({ message: "Access denied: Admin only" });
  }
};

// ================= GET ALL PAYMENTS =================
// GET /api/payments
router.get("/", async (req, res) => {
  try {
    const payments = await Payment.find().sort({ createdAt: -1 });
    res.json(payments);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= GET PENDING PAYMENTS =================
// GET /api/payments/pending
router.get("/pending", auth, adminOnly, async (req, res) => {
  try {
    const payments = await Payment.find({
      status: { $in: ["Pending", "pending", "Unpaid", "unpaid"] }
    }).sort({ createdAt: -1 });
    res.json(payments);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= GET PAYMENTS BY RESIDENT ID =================
// GET /api/payments/resident/:residentId
router.get("/resident/:residentId", async (req, res) => {
    try {
    const payments = await Payment.find({
      residentId: req.params.residentId
    }).sort({ createdAt: -1 });
    res.json(payments);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= AUTO GENERATE MONTHLY BILLS =================
// POST /api/payments/generate
router.post("/generate", auth, adminOnly, async (req, res) => {
  try {
    const Resident = require("../models/Resident");
    const { month, year } = req.body;
    if (!month || !year) {
      return res.status(400).json({ message: "Month and year are required" });
    }

    // Get all approved residents
    const residents = await Resident.find({
      status: { $in: ["Approved", "approved"] }
    });

    let createdBills = [];

    for (const resident of residents) {
      if (!resident.residentId) {
        continue; // Skip residents without a generated residentId
      }

      // Check if bill for this resident, month, and year already exists
      const existingBill = await Payment.findOne({
        residentId: resident.residentId,
        month,
        year
      });

      if (existingBill) {
        continue; // Skip duplicate
      }

      // BHK Rules: 1BHK = 2500, 2BHK = 3500, 3BHK = 5000
      let amount = 2500;
      const flatType = (resident.flatType || "").toUpperCase();
      if (flatType.includes("1 BHK") || flatType.includes("1BHK")) {
        amount = 2500;
      } else if (flatType.includes("2 BHK") || flatType.includes("2BHK")) {
        amount = 3500;
      } else if (flatType.includes("3 BHK") || flatType.includes("3BHK")) {
        amount = 5000;
      } else {
        // Fallback to configured maintenanceAmount
        amount = resident.maintenanceAmount || 2500;
      }

      const payment = new Payment({
        residentName: resident.residentName,
        residentId: resident.residentId,
        flatNumber: resident.flatNumber,
        amount,
        totalAmount: amount,
        month,
        year,
        paymentType: "Maintenance",
        status: "Pending",
        generatedDate: new Date(),
        createdAt: new Date(),
        maintenance: amount,
        upiId: "apartmentliving@upi",
        qrCode: "/qr.png",
        bankName: "State Bank of India",
        accountNumber: "1234567890",
        ifscCode: "SBIN0001234",
        paymentApp: "PhonePe / GPay / Paytm"
      });

      await payment.save();
      createdBills.push(payment);
    }

    res.json({
      message: "Bills generated successfully",
      totalBills: createdBills.length,
      bills: createdBills
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= MARK BILL AS PAID =================
// PUT /api/payments/:id/pay
router.put("/:id/pay", auth, async (req, res) => {
  try {
    const payment = await Payment.findById(req.params.id);
    if (!payment) {
      return res.status(404).json({ message: "Bill not found" });
    }

    payment.status = "Paid";
    payment.paidDate = new Date();
    if (req.body.paymentApp) payment.paymentApp = req.body.paymentApp;
    if (req.body.transactionId) payment.transactionId = req.body.transactionId;

    await payment.save();
    res.json(payment);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= CREATE MANUAL PAYMENT (COMPATIBILITY) =================
router.post("/", async (req, res) => {
    try {
    const yearVal = req.body.year || new Date().getFullYear().toString();
    const amountVal = req.body.amount || req.body.totalAmount || 0;

    const payment = new Payment({
      ...req.body,
      amount: amountVal,
      totalAmount: amountVal,
      year: yearVal,
      generatedDate: new Date(),
      status: req.body.status || "Pending"
    });

    const savedPayment = await payment.save();
    res.status(201).json(savedPayment);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= UPDATE PAYMENT (COMPATIBILITY) =================
router.put("/:id", async (req, res) => {
    try {
    const updated = await Payment.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true }
    );
    res.json(updated);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ================= DELETE PAYMENT (COMPATIBILITY) =================
router.delete("/:id", auth, adminOnly, async (req, res) => {
  try {
    await Payment.findByIdAndDelete(req.params.id);
    res.json({ message: "Payment deleted" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;