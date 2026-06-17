const express = require("express");
const router = express.Router();
const Complaint = require("../models/Complaint");
const Resident = require("../models/Resident");
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

// GET /api/analytics
router.get("/", auth, adminOnly, async (req, res) => {
  try {
    // ----------------------------------------------------
    // 1. Complaint Analytics
    // ----------------------------------------------------
    const totalComplaints = await Complaint.countDocuments();
    const pendingComplaints = await Complaint.countDocuments({
      status: { $in: ["Pending", "pending", "In Progress", "in progress"] }
    });
    const resolvedComplaints = await Complaint.countDocuments({
      status: { $in: ["Resolved", "resolved"] }
    });

    // Average resolution time (in hours)
    const avgResTimeResult = await Complaint.aggregate([
      {
        $match: {
          status: { $in: ["Resolved", "resolved"] },
          createdAt: { $exists: true },
          updatedAt: { $exists: true }
        }
      },
      {
        $project: {
          resolutionTimeHours: {
            $divide: [
              { $subtract: ["$updatedAt", "$createdAt"] },
              1000 * 60 * 60 // milliseconds to hours
            ]
          }
        }
      },
      {
        $group: {
          _id: null,
          avgHours: { $avg: "$resolutionTimeHours" }
        }
      }
    ]);
    const averageResolutionTime = avgResTimeResult.length > 0 ? avgResTimeResult[0].avgHours : 0;

    // Complaints by category
    const complaintsByCategory = await Complaint.aggregate([
      {
        $group: {
          _id: "$category",
          count: { $sum: 1 }
        }
      },
      {
        $project: {
          category: "$_id",
          count: 1,
          _id: 0
        }
      },
      {
        $sort: { count: -1 }
      }
    ]);

    // Daily trends (last 7 days) stacked by category
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6);
    sevenDaysAgo.setHours(0, 0, 0, 0);

    const dailyTrends = await Complaint.aggregate([
      {
        $match: {
          createdAt: { $gte: sevenDaysAgo }
        }
      },
      {
        $project: {
          dateString: { $dateToString: { format: "%Y-%m-%d", date: "$createdAt" } },
          category: "$category"
        }
      },
      {
        $group: {
          _id: {
            date: "$dateString",
            category: "$category"
          },
          count: { $sum: 1 }
        }
      },
      {
        $project: {
          date: "$_id.date",
          category: "$_id.category",
          count: 1,
          _id: 0
        }
      },
      {
        $sort: { date: 1 }
      }
    ]);

    const daysOfWeek = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    const dailyStats = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const dateStr = d.toISOString().split("T")[0];
      const dayLabel = daysOfWeek[d.getDay()];
      
      const matches = dailyTrends.filter(t => t.date === dateStr);
      let plumbing = 0;
      let electrical = 0;
      let lift = 0;
      let other = 0;

      matches.forEach(m => {
        const cat = (m.category || "").toLowerCase();
        if (cat.includes("plumb")) {
          plumbing += m.count;
        } else if (cat.includes("electr") || cat.includes("power")) {
          electrical += m.count;
        } else if (cat.includes("lift")) {
          lift += m.count;
        } else {
          other += m.count;
        }
      });

      dailyStats.push({
        day: dayLabel,
        date: dateStr,
        plumbing,
        electrical,
        lift,
        other
      });
    }

    // Resolution Time Histogram distribution
    const histogramResult = await Complaint.aggregate([
      {
        $match: {
          status: { $in: ["Resolved", "resolved"] },
          createdAt: { $exists: true },
          updatedAt: { $exists: true }
        }
      },
      {
        $project: {
          durationHours: {
            $divide: [
              { $subtract: ["$updatedAt", "$createdAt"] },
              1000 * 60 * 60
            ]
          }
        }
      },
      {
        $bucket: {
          groupBy: "$durationHours",
          boundaries: [0, 4, 12, 24, 36, 48, 72],
          default: "72+",
          output: {
            count: { $sum: 1 }
          }
        }
      }
    ]);

    const bins = {
      "0-4h": 0,
      "4-12h": 0,
      "12-24h": 0,
      "24-36h": 0,
      "36-48h": 0,
      "48-72h": 0,
      "72h+": 0
    };

    histogramResult.forEach(bucket => {
      if (bucket._id === 0) bins["0-4h"] = bucket.count;
      else if (bucket._id === 4) bins["4-12h"] = bucket.count;
      else if (bucket._id === 12) bins["12-24h"] = bucket.count;
      else if (bucket._id === 24) bins["24-36h"] = bucket.count;
      else if (bucket._id === 36) bins["36-48h"] = bucket.count;
      else if (bucket._id === 48) bins["48-72h"] = bucket.count;
      else bins["72h+"] = bucket.count;
    });

    const resolutionHistogram = Object.keys(bins).map(k => ({
      range: k,
      count: bins[k]
    }));

    // ----------------------------------------------------
    // 2. Resident Analytics
    // ----------------------------------------------------
    const totalResidents = await Resident.countDocuments();
    const approvedResidents = await Resident.countDocuments({
      status: { $in: ["Approved", "approved"] }
    });
    const pendingResidents = await Resident.countDocuments({
      status: { $in: ["Pending", "pending"] }
    });

    // Monthly registrations (last 6 months)
    const sixMonthsAgo = new Date();
    sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 5);
    sixMonthsAgo.setDate(1);
    sixMonthsAgo.setHours(0, 0, 0, 0);

    const registrationsResult = await Resident.aggregate([
      {
        $match: {
          createdAt: { $gte: sixMonthsAgo }
        }
      },
      {
        $group: {
          _id: {
            year: { $year: "$createdAt" },
            month: { $month: "$createdAt" }
          },
          count: { $sum: 1 }
        }
      },
      {
        $sort: { "_id.year": 1, "_id.month": 1 }
      }
    ]);

    const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const monthlyRegistrations = [];
    for (let i = 5; i >= 0; i--) {
      const targetDate = new Date();
      targetDate.setMonth(targetDate.getMonth() - i);
      const year = targetDate.getFullYear();
      const month = targetDate.getMonth() + 1;
      const label = `${monthNames[month - 1]} ${year}`;

      const found = registrationsResult.find(r => r._id.year === year && r._id.month === month);
      monthlyRegistrations.push({
        month: label,
        count: found ? found.count : 0
      });
    }

    // ----------------------------------------------------
    // 3. Payment Analytics
    // ----------------------------------------------------
    const revenueResult = await Payment.aggregate([
      {
        $match: {
          status: { $in: ["Paid", "paid"] }
        }
      },
      {
        $group: {
          _id: null,
          total: { $sum: { $ifNull: ["$amount", "$totalAmount"] } }
        }
      }
    ]);
    const totalRevenue = revenueResult.length > 0 ? revenueResult[0].total : 0;

    const paidBills = await Payment.countDocuments({
      status: { $in: ["Paid", "paid"] }
    });
    const unpaidBills = await Payment.countDocuments({
      status: { $in: ["Pending", "pending", "Unpaid", "unpaid"] }
    });

    const totalBilledResult = await Payment.aggregate([
      {
        $group: {
          _id: null,
          total: { $sum: { $ifNull: ["$amount", "$totalAmount"] } }
        }
      }
    ]);
    const totalBilled = totalBilledResult.length > 0 ? totalBilledResult[0].total : 0;
    const collectionRate = totalBilled > 0 ? (totalRevenue / totalBilled) * 100 : 0;

    // Monthly collections (last 6 months)
    const collectionsResult = await Payment.aggregate([
      {
        $match: {
          status: { $in: ["Paid", "paid"] },
          paidDate: { $gte: sixMonthsAgo }
        }
      },
      {
        $group: {
          _id: {
            year: { $year: "$paidDate" },
            month: { $month: "$paidDate" }
          },
          totalCollected: { $sum: { $ifNull: ["$amount", "$totalAmount"] } }
        }
      },
      {
        $sort: { "_id.year": 1, "_id.month": 1 }
      }
    ]);

    const monthlyCollections = [];
    for (let i = 5; i >= 0; i--) {
      const targetDate = new Date();
      targetDate.setMonth(targetDate.getMonth() - i);
      const year = targetDate.getFullYear();
      const month = targetDate.getMonth() + 1;
      const label = `${monthNames[month - 1]} ${year}`;

      const found = collectionsResult.find(c => c._id.year === year && c._id.month === month);
      monthlyCollections.push({
        month: label,
        amount: found ? found.totalCollected : 0
      });
    }

    // ----------------------------------------------------
    // Response Package
    // ----------------------------------------------------
    res.json({
      complaints: {
        total: totalComplaints,
        pending: pendingComplaints,
        resolved: resolvedComplaints,
        averageResolutionTime,
        byCategory: complaintsByCategory,
        dailyStats,
        resolutionHistogram
      },
      residents: {
        total: totalResidents,
        approved: approvedResidents,
        pending: pendingResidents,
        monthlyRegistrations
      },
      payments: {
        totalRevenue,
        paidBills,
        unpaidBills,
        collectionRate,
        monthlyCollections
      }
    });

  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
