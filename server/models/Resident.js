const mongoose =
require("mongoose");

const residentSchema =
new mongoose.Schema({

  residentName: {
    type: String,
    required: true,
  },

  email: {
    type: String,
    required: true,
  },

  phone: {
    type: String,
    required: true,
  },

 

  gender: {
    type: String,
  },

  block: {
    type: String,
    required: true,
  },

  flatNumber: {
    type: String,
    required: true,
  },

  flatType: {
    type: String,
    required: true,
  },

  ownerType: {
    type: String,
    required: true,
  },

  maintenanceAmount: {
    type: Number,
    required: true,
  },

  idType: {
    type: String,
  },

  idNumber: {
    type: String,
  },

  emergencyName: {
    type: String,
  },

  emergencyPhone: {
    type: String,
  },

  // GENERATED AFTER APPROVAL

  residentId: {
    type: String,
  },

  // PENDING / APPROVED

  status: {
    type: String,
    default: "Pending",
  },

  createdAt: {
    type: Date,
    default: Date.now,
  },

});

module.exports =
mongoose.model(
  "Resident",
  residentSchema
);