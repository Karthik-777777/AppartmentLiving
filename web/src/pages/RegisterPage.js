import { useState } from "react";

function RegisterPage() {

  const [step, setStep] =
    useState(1);

  // FORM DATA

  const [formData, setFormData] =
    useState({

      

      residentName: "",
      email: "",
      phone: "",
      password: "",

      gender: "",

      block: "",
      flatNumber: "",

      flatType: "",

      ownerType: "",

      idType: "",
      idNumber: "",

      emergencyName: "",
      emergencyPhone: "",

      maintenanceAmount: 0,

    });
    const [otp, setOtp] =
useState("");

const [otpSent, setOtpSent] =
useState(false);

const [otpVerified, setOtpVerified] =
useState(false);
const [sendingOtp, setSendingOtp] =
useState(false);

const [verifyingOtp, setVerifyingOtp] =
useState(false);


  // AUTO MAINTENANCE CALCULATION

  const calculateMaintenance =
  (flatType) => {

    switch(flatType) {

      case "1BHK":
        return 2500;

      case "2BHK":
        return 4500;

      case "3BHK":
        return 7000;

      case "4BHK":
        return 9500;

      default:
        return 0;

    }

  };
  // SEND OTP

const sendOtp = async () => {

  try {

    setSendingOtp(true);

    const res = await fetch(

      "http://localhost:4000/api/auth/send-otp",

      {

        method: "POST",

        headers: {

          "Content-Type":
          "application/json"

        },

        body: JSON.stringify({

          email:
          formData.email

        })

      }

    );

    const data =
    await res.json();

    alert(data.message);

    setOtpSent(true);

  }

  catch(err){

    console.log(err);

    alert("OTP failed");

  }

  finally {

    setSendingOtp(false);

  }

};

// VERIFY OTP

const verifyOtp = async () => {

  try {

    setVerifyingOtp(true);

    const res = await fetch(

      "http://localhost:4000/api/auth/verify-otp",

      {

        method: "POST",

        headers: {

          "Content-Type":
          "application/json"

        },

        body: JSON.stringify({

          email:
          formData.email,

          otp

        })

      }

    );

    const data =
    await res.json();

    if(data.verified){

      alert(
        "OTP Verified"
      );

      setOtpVerified(true);

    }

  }

  catch(err){

    alert(
      "Invalid OTP"
    );

  }

  finally {

    setVerifyingOtp(false);

  }

};

  // REGISTER API

  const handleRegister =
  async () => {
    if(!otpVerified){

 alert(
  "Verify OTP first"
 );

 return;

}
    // REQUIRED FIELD VALIDATION

if(

 !formData.residentName ||

 !formData.email ||

 !formData.phone ||
 

 !formData.password ||

 !formData.gender ||

 !formData.block ||

 !formData.flatNumber ||

 !formData.flatType ||

 !formData.ownerType ||

 !formData.idType ||

 !formData.idNumber ||

 !formData.emergencyName ||

 !formData.emergencyPhone

){

 alert(
  "Please fill all fields"
 );

 return;

}


// EMAIL VALIDATION

const emailRegex =

/^[^\s@]+@[^\s@]+\.[^\s@]+$/;

if(

 !emailRegex.test(
   formData.email
 )

){

 alert(
  "Enter valid email address"
 );

 return;

}


// PASSWORD VALIDATION

const passwordRegex =

/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;


if(

 !passwordRegex.test(
   formData.password
 )

){

 alert(

`Password must contain:

• 8 characters
• Uppercase letter
• Lowercase letter
• Number
• Special character`

 );

 return;

}
// PHONE VALIDATION

if(

 formData.phone.length !== 10

){

 alert(
  "Phone number must be 10 digits"
 );

 return;

}
if(

 formData.idType === "Aadhar" &&

 formData.idNumber.length !== 12

){

 alert(
  "Aadhar must be 12 digits"
 );

 return;

}
if(

 formData.idType === "PAN" &&

 formData.idNumber.length !== 10

){

 alert(
  "PAN must be 10 characters"
 );

 return;

}

    try {

      const res =
      await fetch(

        "http://localhost:4000/api/auth/register",

        {

          method: "POST",

          headers: {

            "Content-Type":
            "application/json",

          },

          body: JSON.stringify(
            formData
          ),

        }

      );

      const data =
      await res.json();

      console.log(data);

      alert(
        "Registration Submitted Successfully"
      );
      setTimeout(() => {

  window.location.href =
    "/login";

}, 1500);

    } catch(err) {

      console.log(err);

    }

  };


  return (

    <div

      className="
        h-screen
        w-full

        flex
        items-center
        justify-center

        bg-cover
        bg-center

        overflow-hidden
      "

      style={{
        backgroundImage:
        "url('/login-bg.png')"
      }}
    >

      {/* OUTER WRAPPER */}

      <div
        className="
          relative

          w-[430px]
          h-[620px]

          overflow-hidden
        "
      >

        {/* SLIDER */}

        <div

          className="
            flex

            w-full
            h-full

            transition-transform
            duration-500
          "

          style={{

            transform:

            step === 1

              ? "translateX(0%)"

              : "translateX(-100%)"

          }}
        >

          {/* ================= STEP 1 ================= */}

          <div
            className="
              w-full
              h-full

              shrink-0

              backdrop-blur-xl
              bg-black/30

              p-8

              rounded-3xl

              shadow-2xl

              text-white

              flex
              flex-col
            "
          >

            <h2
              className="
                text-3xl
                font-black

                text-center

                mb-6
              "
            >
              Personal Details
            </h2>

            {/* FORM */}

            <div
              className="
                flex-1

                overflow-y-auto

                space-y-4
              "
            >

              {/* FULL NAME */}

              <input

                className="input"

                placeholder="Full Name"

                value={
                  formData.residentName
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    residentName:
                    e.target.value,

                  })

                }
                required
              />

              {/* EMAIL */}

              <input

                className="input"

                placeholder="Email"

                value={formData.email}

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    email:
e.target.value.toLowerCase(),

                  })

                }
                required
              />
              <button

  onClick={sendOtp}

  disabled={sendingOtp}

  className={`
    w-full
    py-2
    rounded-xl
    font-semibold
    transition-all
    duration-300

    ${
      sendingOtp

      ? "bg-gray-500"

      : "bg-blue-600 hover:bg-blue-700"
    }

  `}

>

  {

    sendingOtp

    ? "Sending OTP..."

    : "Send OTP"

  }

</button>
{

otpSent && (

<div className="space-y-3">

  <input

    className="input"

    placeholder="Enter OTP"

    value={otp}

    onChange={(e)=>
      setOtp(e.target.value)
    }

  />

  <button

  onClick={verifyOtp}

  disabled={verifyingOtp}

  className={`
    w-full
    py-2
    rounded-xl
    font-semibold
    transition-all
    duration-300

    ${
      otpVerified

      ? "bg-green-700"

      : verifyingOtp

      ? "bg-gray-500"

      : "bg-green-600 hover:bg-green-700"
    }

  `}

>

  {

    otpVerified

    ? "OTP Verified ✓"

    : verifyingOtp

    ? "Verifying..."

    : "Verify OTP"

  }

</button>

</div>

)}

              {/* PHONE */}

              <input

                className="input"

                placeholder="Phone Number"

                value={formData.phone}

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    phone:
                    e.target.value,

                  })

                }
                required
              />

              {/* PASSWORD */}

              <input

                className="input"

                type="password"

                placeholder="Password"

                value={
                  formData.password
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    password:
                    e.target.value,

                  })

                }
                              required

              />

              {/* GENDER */}

              <select

                className="input"

                value={
                  formData.gender
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    gender:
                    e.target.value,

                  })

                }
              >

                <option value="">
                  Select Gender
                </option>

                <option>
                  Male
                </option>

                <option>
                  Female
                </option>

                <option>
                  Other
                </option>

              </select>

            </div>

            {/* NEXT BUTTON */}

            <button

              onClick={() =>
                setStep(2)
              }

              className="
                w-full

                mt-5

                bg-gradient-to-r
                from-blue-600
                to-violet-600

                text-white

                py-3

                rounded-2xl

                font-semibold

                tracking-wide

                shadow-lg

                hover:shadow-2xl
                hover:scale-[1.02]

                transition-all
                duration-300
              "
            >
              Next →
            </button>

            <div className="text-center mt-5">

  <p className="text-white/80 text-sm">
    Already have an account?
  </p>

  <button

    onClick={() =>
      window.location.href = "/login"
    }

    className="
      mt-2
      text-sm
      font-semibold

      text-transparent
      bg-clip-text

      bg-gradient-to-r
      from-cyan-300
      to-blue-400

      hover:scale-[1.03]

      transition-all
      duration-300
    "
  >

    Go to Login

  </button>

</div>

          </div>
          


          {/* ================= STEP 2 ================= */}

          <div
            className="
              w-full
              h-full

              shrink-0

              backdrop-blur-xl
              bg-black/30

              p-6

              rounded-3xl

              shadow-2xl

              text-white

              flex
              flex-col
            "
          >

            <h2
              className="
                text-3xl
                font-black

                text-center

                mb-5
              "
            >
              Apartment Details
            </h2>

            {/* FORM */}

            <div
              className="
                flex-1

                overflow-y-auto

                pr-2

                space-y-4
              "
            >

              {/* BLOCK */}

              <input

                className="input"

                placeholder="Block (A/B/C)"

                value={
                  formData.block
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    block:
                    e.target.value,

                  })

                }
                required
              />

              {/* FLAT NUMBER */}

              <input

                className="input"

                placeholder="Flat No (A-101)"

                value={
                  formData.flatNumber
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    flatNumber:
                    e.target.value,

                  })

                }
                              required

              />
              

              {/* FLAT TYPE */}

              <select

                className="input"

                value={
                  formData.flatType
                }

                onChange={(e)=> {

                  const flatType =
                  e.target.value;

                  setFormData({

                    ...formData,

                    flatType,

                    maintenanceAmount:

                    calculateMaintenance(
                      flatType
                    ),

                  });

                }}
              >

                <option value="">
                  Select Flat Type
                </option>

                <option>
                  1BHK
                </option>

                <option>
                  2BHK
                </option>

                <option>
                  3BHK
                </option>

                <option>
                  4BHK
                </option>

              </select>

              {/* MAINTENANCE */}

              <div
                className="
                  bg-white/10

                  border
                  border-white/20

                  rounded-2xl

                  p-4

                  text-center
                "
              >

                <p
                  className="
                    text-sm
                    text-gray-300
                  "
                >
                  Monthly Maintenance
                </p>

                <h2
                  className="
                    text-4xl
                    font-black

                    mt-2
                  "
                >
                  ₹{
                    formData
                    .maintenanceAmount
                  }
                </h2>

              </div>

              {/* OWNER TYPE */}

              <select

                className="input"

                value={
                  formData.ownerType
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    ownerType:
                    e.target.value,

                  })

                }
              >

                <option value="">
                  Ownership Type
                </option>

                <option>
                  Owner
                </option>

                <option>
                  Tenant
                </option>

              </select>

              {/* ID TYPE */}

              <select

                className="input"

                value={
                  formData.idType
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    idType:
                    e.target.value,

                  })

                }
              >

                <option value="">
                  Select ID Type
                </option>

                <option>
                  Aadhar
                </option>

                <option>
                  PAN
                </option>

              </select>

              {/* ID NUMBER */}

              <input

                className="input"

                placeholder="ID Number"

                value={
                  formData.idNumber
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    idNumber:
                    e.target.value,

                  })

                }
                required
              />

              {/* FILE */}

              <input
                className="input"
                type="file"
                required
              />
              

              {/* EMERGENCY NAME */}

              <input

                className="input"

                placeholder="Emergency Contact Name"

                value={
                  formData.emergencyName
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    emergencyName:
                    e.target.value,

                  })

                }
              />

              {/* EMERGENCY PHONE */}

              <input

                className="input"

                placeholder="Emergency Phone"

                value={
                  formData.emergencyPhone
                }

                onChange={(e)=>

                  setFormData({

                    ...formData,

                    emergencyPhone:
                    e.target.value,

                  })

                }
              />

            </div>

            {/* BUTTONS */}

            <div
              className="
                flex
                gap-3

                mt-5
              "
            >

              {/* BACK */}

              <button

                onClick={() =>
                  setStep(1)
                }

                className="
                  w-1/2

                  border
                  border-white/30

                  bg-white/50

                  backdrop-blur-xl

                  text-slate-700

                  py-3

                  rounded-2xl

                  font-semibold

                  tracking-wide

                  shadow-md

                  hover:bg-white/70
                  hover:shadow-xl
                  hover:scale-[1.02]

                  transition-all
                  duration-300
                "
              >
                ← Back
              </button>

              {/* REGISTER */}

              <button

                onClick={
                  handleRegister
                }

                className="
                  w-1/2

                  bg-gradient-to-r
                  from-green-500
                  to-emerald-500

                  text-white

                  py-3

                  rounded-2xl

                  font-semibold

                  tracking-wide

                  shadow-lg

                  hover:shadow-2xl
                  hover:scale-[1.02]

                  transition-all
                  duration-300
                "
              >
                Register
              </button>

            </div>

          </div>

        </div>

      </div>

    </div>

  );

}

export default RegisterPage;