const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");

const adminRoutes =
  require("./routes/adminRoutes");

require("dotenv").config();

const app = express();


// Middleware
app.use(cors());

app.use(

  express.json({

    limit: "50mb"

  })

);

app.use(

  express.urlencoded({

    limit: "50mb",

    extended: true

  })

);


// MongoDB Connection
mongoose.connect(process.env.MONGO_URI)

  .then(() =>
    console.log("MongoDB Connected")
  )

  .catch((err) =>
    console.log(err)
  );


// Routes
app.use(
  "/api/auth",
  require("./routes/auth")
);

app.use(
  "/api/complaints",
  require("./routes/complaints")
);

app.use(
  "/api/maintenance",
  require("./routes/maintenance")
);

app.use(
  "/api/residents",
  require("./routes/residents")
);

app.use(
  "/api/payments",
  require("./routes/payments")
);

app.use(
  "/api/notices",
  require("./routes/notices")
);

app.use(
  "/api/admin",
  adminRoutes
);

app.use(
  "/api/analytics",
  require("./routes/analytics")
);


// Test Route
app.get("/", (req, res) => {

  res.send(
    "Backend running successfully"
  );

});


// Start Server
app.listen(4000, () => {

  console.log(
    "Server running on port 4000"
  );

});