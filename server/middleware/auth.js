const jwt = require("jsonwebtoken");

module.exports = (req, res, next) => {
  const authHeader = req.header("Authorization");

  if (!authHeader) {
    return res.status(401).json("Access denied");
  }

  try {
    // remove "Bearer "
    const token = authHeader.split(" ")[1];

    const verified = jwt.verify(token, process.env.JWT_SECRET);
    req.user = verified;

    next();
  } catch (err) {
    console.log(err); // 👈 debug
    res.status(401).json("Invalid token");
  }
};