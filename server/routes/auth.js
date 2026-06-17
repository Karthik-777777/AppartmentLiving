const router = require("express").Router();

const User =
require("../models/User");

const Resident =
require("../models/Resident");

const Admin =
require("../models/Admin");

const bcrypt =
require("bcryptjs");

const jwt =
require("jsonwebtoken");

const transporter =
require("../config/mail");

const otpStore =
require("../utils/otpStore");


// ================= SEND OTP =================

router.post(

  "/send-otp",

  async (req, res) => {

    try {

      const { email } =
      req.body;

      const otp =

      Math.floor(

        100000 +

        Math.random() * 900000

      );


      // SAVE OTP

      otpStore[email] = otp;


      // SEND EMAIL

      await transporter.sendMail({

        from:
        "YOUR_GMAIL@gmail.com",

        to:
        email,

        subject:
        "Apartment Living OTP",

        text:
        `Your OTP is ${otp}`

      });


      res.json({

        message:
        "OTP sent"

      });

    }

    catch (err) {

      console.log(err);

      res.status(500).json({

        message:
        "OTP failed"

      });

    }

  }

);


// ================= VERIFY OTP =================

router.post(

  "/verify-otp",

  (req, res) => {

    const {

      email,

      otp

    } = req.body;


    // CHECK OTP

    if (

      otpStore[email] == otp

    ) {

      return res.json({

        verified: true,

        message:
        "OTP Verified"

      });

    }


    // INVALID OTP

    res.status(400).json({

      verified: false,

      message:
      "Invalid OTP"

    });

  }

);


// ================= REGISTER =================

router.post(

  "/register",

  async (req, res) => {

    try {

      // HASH PASSWORD

      const hashed =

      await bcrypt.hash(

        req.body.password,

        10

      );


      // CREATE LOGIN USER

      const user =

      await User.create({

        ...req.body,

        email:
        req.body.email.toLowerCase(),

        password:
        hashed,

        role:
        req.body.role || "resident"

      });


      // CREATE RESIDENT ENTRY

      await Resident.create({

        residentName:
        req.body.residentName,

        email:
        req.body.email.toLowerCase(),

        phone:
        req.body.phone,

        gender:
        req.body.gender,

        block:
        req.body.block,

        flatNumber:
        req.body.flatNumber,

        flatType:
        req.body.flatType,

        ownerType:
        req.body.ownerType,

        idType:
        req.body.idType,

        idNumber:
        req.body.idNumber,

        emergencyName:
        req.body.emergencyName,

        emergencyPhone:
        req.body.emergencyPhone,

        maintenanceAmount:
        req.body.maintenanceAmount,

        residentId:
        "",

        status:
        "pending"

      });


      res.json(user);

    }

    catch (err) {

      console.log(err);

      res.status(500).json({

        message:
        err.message

      });

    }

  }

);


// ================= LOGIN =================

router.post(

  "/login",

  async (req, res) => {
    try {
      console.log("[LOGIN] Incoming request body:", req.body);
      if (!req.body.email || !req.body.password) {
        console.log("[LOGIN] Error: Missing email or password");
        return res.status(400).json({ message: "Please fill in all fields" });
      }

      const email = req.body.email.toLowerCase();
      const password = req.body.password;
      console.log("[LOGIN] Incoming email:", email);

      // ================= ADMIN LOGIN =================
      const admin = await Admin.findOne({ email });
      if(admin) {
        console.log("[LOGIN] Admin found:", admin.email);
        if(admin.password !== password) {
          console.log("[LOGIN] Admin login failed: Wrong password");
          return res.status(400).json({
            message: "Wrong password"
          });
        }
        admin.lastLogin = new Date();
        await admin.save();

        console.log("[LOGIN] Admin login successful:", admin.email);
        const token = jwt.sign(
          {
            id: admin._id,
            role: admin.role || "admin"
          },
          process.env.JWT_SECRET
        );
        return res.json({
          token,
          role: "admin",
          user: {
            name: admin.name,
            email: admin.email,
            role: admin.role,
            designation: admin.designation
          }
        });
      }

      // ================= RESIDENT LOGIN =================
      const user = await User.findOne({ email });
      if(!user) {
        console.log("[LOGIN] User found: false");
        return res.status(400).json({ message: "User not found" });
      }
      console.log("[LOGIN] User found: true");

      const isMatch = await bcrypt.compare(password, user.password);
      console.log("[LOGIN] Password comparison result:", isMatch);
      if(!isMatch) {
        return res.status(400).json({ message: "Wrong password" });
      }


      const resident =
      await Resident.findOne({
        email
      });


      const token = jwt.sign(

        {
          id: user._id,
          role: user.role
        },

        process.env.JWT_SECRET

      );


      res.json({

        token,

        role:
        user.role,

        user: {

          _id:
          user._id,

          email:
          user.email,

          role:
          user.role,

          residentName:
          resident?.residentName,

          residentId:
          resident?.residentId,

          flatNumber:
          resident?.flatNumber,

          block:
          resident?.block,

          flatType:
          resident?.flatType,

          ownerType:
          resident?.ownerType,

          maintenanceAmount:
          resident?.maintenanceAmount,

          status:
          resident?.status

        }

      });

    }

    catch(err) {

      console.log(err);

      res.status(500).json({
        message:
        err.message
      });

    }

  }

);

module.exports = router;