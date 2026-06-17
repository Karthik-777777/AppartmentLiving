const router = require("express").Router();
const Complaint = require("../models/Complaint");
const auth = require("../middleware/auth");

// Admin authorization helper middleware
const adminOnly = (req, res, next) => {
  if (req.user && (req.user.role === "admin" || req.user.role === "Admin")) {
    next();
  } else {
    res.status(403).json({ message: "Access denied: Admin only" });
  }
};

// ================= CREATE COMPLAINT =================
// POST /api/complaints/create
router.post("/create", async (req, res) => {
    try {
    const flatVal = req.body.flatNumber || req.body.apartmentNumber || "";
    const newComplaint = await Complaint.create({
      ...req.body,
      flatNumber: flatVal,
      apartmentNumber: flatVal
    });
    res.status(201).json(newComplaint);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Backward compatibility CREATE
router.post("/", async (req, res) => {
    try {
    const flatVal = req.body.flatNumber || req.body.apartmentNumber || "";
    const newComplaint = await Complaint.create({
      ...req.body,
      flatNumber: flatVal,
      apartmentNumber: flatVal
    });
    res.status(201).json(newComplaint);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// ================= GET RESIDENT COMPLAINTS =================
// GET /api/complaints/resident/:residentId
router.get("/resident/:residentId", async (req, res) => {
    try {
    const complaints = await Complaint.find({
      residentId: req.params.residentId
    });
    res.json(complaints);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// ================= GET ALL COMPLAINTS (ADMIN) =================
// GET /api/complaints/all
router.get("/all", auth, adminOnly, async (req, res) => {
  try {
    const complaints = await Complaint.find();
    res.json(complaints);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Backward compatibility GET ALL
router.get("/", async (req, res) => {
    try {
    const complaints = await Complaint.find();
    res.json(complaints);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// ================= UPDATE COMPLAINT STATUS (ADMIN) =================
// PUT /api/complaints/:id/status
router.put("/:id/status", auth, adminOnly, async (req, res) => {
  try {
    const updates = {};
    if (req.body.status !== undefined) updates.status = req.body.status;
    if (req.body.adminNote !== undefined) updates.adminNote = req.body.adminNote;

    const updated = await Complaint.findByIdAndUpdate(
      req.params.id,
      updates,
      { new: true }
    );
    if (!updated) {
      return res.status(404).json({ message: "Complaint not found" });
    }
    res.json(updated);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// Backward compatibility UPDATE
router.put("/:id", async (req, res) => {
    try {
    const updated = await Complaint.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true }
    );
    if (!updated) {
      return res.status(404).json({ message: "Complaint not found" });
    }
    res.json(updated);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

// ================= DELETE COMPLAINT (ADMIN) =================
// DELETE /api/complaints/:id
router.delete("/:id", auth, adminOnly, async (req, res) => {
  try {
    const deleted = await Complaint.findByIdAndDelete(req.params.id);
    if (!deleted) {
      return res.status(404).json({ message: "Complaint not found" });
    }
    res.json({ message: "Complaint deleted successfully" });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;