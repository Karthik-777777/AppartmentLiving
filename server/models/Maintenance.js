const mongoose = require("mongoose");

const maintenanceSchema =
new mongoose.Schema({

  residentId: {
    type: String,
    required: true,
  },

  residentName: {
    type: String,
    required: true,
  },

  flatNumber: {
    type: String,
    required: true,
  },

  issue: {
    type: String,
    required: true,
  },

  description: {
    type: String,
    required: true,
  },

  category: {
    type: String,
    default: "General",
  },

  priority: {
    type: String,
    default: "Medium",
  },

  status: {
    type: String,
    default: "Pending",
  },

  assignedWorker: {
    type: String,
    default: "Not Assigned",
  },

  createdAt: {
    type: Date,
    default: Date.now,
  },

});

module.exports =
mongoose.model(
  "Maintenance",
  maintenanceSchema
);