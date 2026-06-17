const router =
require("express").Router();

const Maintenance =
require("../models/Maintenance");


// CREATE MAINTENANCE
router.post("/", async(req,res)=>{

  try {

    const data =
    await Maintenance.create(
      req.body
    );

    res.json(data);

  } catch(err) {

    res.status(500).json({
      message: err.message
    });

  }

});


// GET ALL MAINTENANCE
router.get("/", async(req,res)=>{

  try {

    const data =
    await Maintenance.find();

    res.json(data);

  } catch(err) {

    res.status(500).json({
      message: err.message
    });

  }

});


// UPDATE MAINTENANCE
router.put("/:id", async(req,res)=>{

  try {

    const data =
    await Maintenance.findByIdAndUpdate(

      req.params.id,

      req.body,

      { new: true }

    );

    res.json(data);

  } catch(err) {

    res.status(500).json({
      message: err.message
    });

  }

});


// GET RESIDENT REQUESTS
router.get(

  "/resident/:residentId",

  async(req,res)=>{

    try {

      const requests =

      await Maintenance.find({

        residentId:
        req.params.residentId

      });

      res.json(requests);

    }

    catch(err){

      res.status(500).json({
        message: err.message
      });

    }

});


// EXPORT ROUTER
module.exports = router;