const router = require("express").Router();
const Notice = require("../models/Notice");
const Admin = require("../models/Admin");
const auth = require("../middleware/auth");

const adminOnly = (req, res, next) => {
  if (req.user && (req.user.role === "admin" || req.user.role === "Admin")) {
    next();
  } else {
    res.status(403).json({ message: "Access denied: Admin only" });
  }
};

// CREATE NOTICE (Admin Only)
router.post("/", auth, adminOnly, async (req, res) => {
  try {
    let createdBy = "Admin";
    if (req.user && req.user.id) {
      const admin = await Admin.findById(req.user.id);
      if (admin && admin.name) {
        createdBy = admin.name;
      }
    }
    
    const noticeData = {
      title: req.body.title,
      description: req.body.description,
      category: req.body.category || "General",
      priority: req.body.priority || "Normal",
      createdBy: req.body.createdBy || createdBy,
      createdAt: req.body.createdAt || new Date()
    };

    const notice = await Notice.create(noticeData);
    res.status(201).json(notice);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET ALL NOTICES (Protected)
router.get("/", async (req, res) => {
    try {
    const notices = await Notice.find().sort({ createdAt: -1 });
    res.json(notices);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET LATEST 3 NOTICES (Protected)
router.get("/latest", async (req, res) => {
    try {
    const notices = await Notice.find().sort({ createdAt: -1 }).limit(3);
    res.json(notices);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// DELETE NOTICE (Admin Only)
router.delete("/:id", auth, adminOnly, async (req, res) => {
  try {
    const notice = await Notice.findByIdAndDelete(req.params.id);
    if (!notice) {
      return res.status(404).json({ message: "Notice not found" });
    }
    res.json({ message: "Notice deleted successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;