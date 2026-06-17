const mongoose =
require("mongoose");

const adminSchema =
new mongoose.Schema({

  profileImage: {
    type: String,
    default: "",
  },

  name: {
    type: String,
    required: true,
  },

  email: {
    type: String,
    required: true,
    unique: true,
  },

  password: {
    type: String,
    required: true,
  },

  phone: {
    type: String,
    default: "",
  },

  designation: {
    type: String,
    default: "Manager",
  },

  apartmentName: {
    type: String,
    default: "Apartment Living",
  },

  role: {
    type: String,
    default: "admin",
  },

  status: {
    type: String,
    default: "Active",
  },

  notifications: {
    type: Boolean,
    default: true,
  },

  joinedAt: {
    type: Date,
    default: Date.now,
  },

  lastLogin: {
    type: Date,
    default: Date.now,
  },

});

module.exports =
mongoose.model(
  "Admin",
  adminSchema
);