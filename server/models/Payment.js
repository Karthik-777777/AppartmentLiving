const mongoose = require("mongoose");

const paymentSchema =
  new mongoose.Schema({

    residentName: {
      type: String,
      required: true,
    },
    residentId: {

  type: String,

  required: true,

},

    flatNumber: {
      type: String,
      required: true,
    },

    

    month: {
      type: String,
      required: true,
    },

    paymentType: {
      type: String,
      default: "Maintenance",
    },

    // PAYMENT METHODS

    upiId: {
      type: String,
      default: "apartmentliving@upi",
    },

    paymentApp: {
      type: String,
      default: "PhonePe",
    },

    qrCode: {
      type: String,
      default: "",
    },

    bankName: {
      type: String,
      default: "State Bank of India",
    },

    accountNumber: {
      type: String,
      default: "9876543210",
    },

    ifscCode: {
      type: String,
      default: "SBIN0004587",
    },

    // PAYMENT VERIFICATION

    transactionId: {
      type: String,
    },

    paymentProof: {
      type: String,
    },

    approvalStatus: {
      type: String,
      default: "Waiting Verification",
    },

    // MAIN STATUS

    status: {
      type: String,
      default: "Pending",
    },

    paidDate: {
      type: Date,
    },

    createdAt: {
      type: Date,
      default: Date.now,
    },

    maintenance: {
  type: Number,
  default: 0,
},

waterBill: {
  type: Number,
  default: 0,
},

electricityBill: {
  type: Number,
  default: 0,
},

rent: {
  type: Number,
  default: 0,
},

parkingFee: {
  type: Number,
  default: 0,
},

penalty: {
  type: Number,
  default: 0,
},

otherCharges: {
  type: Number,
  default: 0,
},

totalAmount: {
  type: Number,
  required: true,
},
amount: {
  type: Number,
},
year: {
  type: String,
},
generatedDate: {
  type: Date,
  default: Date.now,
},
  });

  

module.exports =
  mongoose.model(
    "Payment",
    paymentSchema
  );