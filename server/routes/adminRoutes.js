const router =
require("express").Router();

const Admin =
require("../models/Admin");


// ADMIN LOGIN

router.post(

  "/login",

  async(req, res) => {

    try {

      const {
        email,
        password,
      } = req.body;

      const admin =
      await Admin.findOne({
        email,
      });

      if(!admin) {

        return res.status(404).json({
          message:
          "Admin not found",
        });

      }

      if(
        admin.password !== password
      ) {

        return res.status(400).json({
          message:
          "Invalid password",
        });

      }

      res.json({

        success: true,

        role: "admin",

        user: admin,

      });

    }

    catch(err) {

      console.log(err);

      res.status(500).json({
        message:
        "Server error",
      });

    }

  }

);

module.exports = router;