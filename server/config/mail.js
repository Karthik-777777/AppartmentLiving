const nodemailer =
require("nodemailer");

const transporter =

nodemailer.createTransport({

  service: "gmail",

  auth: {

    user:
    "karthiksai5567@gmail.com",

    pass:
    "tzvl syfn dmve pvfl"

  }

});

module.exports =
transporter;