import React, {
  useEffect,
  useState,
} from "react";
import paymentBg from "../../assests/Payment-bg.png";
export default function ResidentPayments() {

  const [bills, setBills] =
    useState([]);

    const [selectedBill, setSelectedBill] =
  useState(null);

const [showPaymentModal, setShowPaymentModal] =
  useState(false);

const [transactionId, setTransactionId] =
  useState("");

const [paymentProof, setPaymentProof] =
  useState("");

  // GET LOGGED RESIDENT

  const resident =
    JSON.parse(
      localStorage.getItem("user")
    );
    console.log("USER:", resident);

  useEffect(() => {

    if(!resident) return;

    fetch(

`http://localhost:4000/api/payments/resident/${resident.residentId}`
    )

      .then((res) => res.json())

     .then((data) => {

  console.log("PAYMENT RESPONSE:", data);

  if (Array.isArray(data)) {

    setBills(data);

  } else {

    setBills([]);

  }

})

      .catch((err) => {

        console.log(err);

      });

}, [resident?.residentId]);


const submitPayment = async () => {

  try {

    const res = await fetch(

      `http://localhost:4000/api/payments/${selectedBill._id}`,

      {

        method: "PUT",

        headers: {

          "Content-Type":
          "application/json",

        },

        body: JSON.stringify({

          transactionId,

          paymentProof,

          approvalStatus:
          "Verification Pending",

          status:
          "Verification Pending",

        }),

      }

    );

    const data =
    await res.json();

    console.log(data);

    alert(
      "Payment Submitted Successfully"
    );

    // REFRESH BILLS

    setBills(

      bills.map((bill) =>

        bill._id === selectedBill._id

        ? {

            ...bill,

            transactionId,

            paymentProof,

            status:
            "Verification Pending",

            approvalStatus:
            "Verification Pending",

          }

        : bill

      )

    );

    setShowPaymentModal(false);

    setTransactionId("");

    setPaymentProof("");

  }

  catch(err) {

    console.log(err);

    alert("Payment Submission Failed");

  }

};

  return (

<div
  className="
    p-6
    max-w-6xl
    mx-auto
    min-h-screen
    bg-cover
    bg-center
    bg-no-repeat
  "
  style={{
    backgroundImage: `url(${paymentBg})`,
  }}
>
      <h1
        className="
          text-3xl
          font-black
          mb-6
        "
      >
        My Bills
      </h1>

      <div className="mb-10">

  <h2
    className="
      text-2xl
      font-black
      text-gray-800
      mb-3
    "
  >
    Pending Bills
  </h2>

  <p className="text-gray-500">
    Bills awaiting payment
  </p>

</div>


      <div className="space-y-5">

        {

bills.filter(

  (bill) =>

    bill.status !== "Paid"

).length === 0 && (

    <div
      className="
bg-white/55
backdrop-blur-xl
border border-white/30
        rounded-3xl
        p-10
        text-center
        shadow-lg
      "
    >

      <h2
        className="
          text-2xl
          font-bold
          text-green-600
        "
      >
🎉 All Bills Cleared
      </h2>

      <p className="text-gray-500 mt-3">
        No pending bills available.
      </p>

    </div>

  )
}
{

bills

.filter(

  (bill) =>

    bill.status !== "Paid"

)

.map((bill) => (

            <div

              key={bill._id}

              className="
bg-white/55
backdrop-blur-xl
border border-white/30
                rounded-3xl
                shadow-lg
                p-6
              "
            >

              {/* TOP */}

             <div
  className="
    flex
    flex-col
    lg:flex-row
    lg:justify-between
    items-start
    gap-5
  "
>

                {/* LEFT */}

                <div>

                  <h2
                    className="
                      text-2xl
                      font-black
                    "
                  >
                    {bill.month}
                  </h2>

                  <p className="mt-2 text-gray-500">
                    Resident ID:
                    {" "}
                    {bill.residentId}
                  </p>

                  <p className="text-gray-500">
                    Flat:
                    {" "}
                    {bill.flatNumber}
                  </p>

                </div>


                {/* STATUS */}

                <div>

                  <span
                    className={`
                      px-4
                      py-2
                      rounded-full
                      text-sm
                      font-bold

                      ${
  bill.status === "Paid"

  ? `
    bg-green-100
    text-green-600
  `

  : bill.status === "Rejected"

  ? `
    bg-red-100
    text-red-600
  `

  : `
    bg-yellow-100
    text-yellow-600
  `
}
                    `}
                  >

                    {bill.status}

                  </span>

                </div>

              </div>


              {/* BILL DETAILS */}

              <div
                className="
                  grid
                  grid-cols-2
                  md:grid-cols-3
                  gap-4
                  mt-6
                "
              >

                <div>
                  <p className="text-gray-400">
🏢 Society Maintenance
                  </p>

                  <h2 className="font-bold">
                    ₹{bill.maintenance}
                  </h2>
                </div>

                <div>
                  <p className="text-gray-400">
🚰 Water Charges
                  </p>

                  <h2 className="font-bold">
                    ₹{bill.waterBill}
                  </h2>
                </div>

                <div>
                  <p className="text-gray-400">
⚡ Electricity Charges
                  </p>

                  <h2 className="font-bold">
                    ₹{bill.electricityBill}
                  </h2>
                </div>

                <div>
                  <p className="text-gray-400">
🏠 Monthly Rent                  </p>

                  <h2 className="font-bold">
                    ₹{bill.rent}
                  </h2>
                </div>

                <div>
                  <p className="text-gray-400">
🚗 Parking Charges                  </p>

                  <h2 className="font-bold">
                    ₹{bill.parkingFee}
                  </h2>
                </div>

              </div>


              {/* TOTAL */}

              <div
                className="
                  mt-6
bg-gradient-to-r
from-blue-50
to-indigo-50

shadow-inner

border
border-blue-100               
   rounded-2xl
                  p-5
                "
              >

                <h2 className="text-lg font-semibold">
                  Total Amount
                </h2>

                <h1
                  className="
                    text-4xl
                    font-black
                    text-blue-600
                    mt-2
                  "
                >
                  ₹{bill.totalAmount}
                </h1>

              </div>


              <div
  className="
    flex
    gap-3
    mt-4
  "
>

  <span
    className="
      px-3
      py-1

      rounded-full

      bg-blue-100
      text-blue-700

      text-xs
      font-bold
    "
  >

    Secure Payment

  </span>


  <span
    className="
      px-3
      py-1

      rounded-full

      bg-green-100
      text-green-700

      text-xs
      font-bold
    "
  >

    UPI Enabled

  </span>

</div>

           {

  bill.status !== "Paid" && (

<button

  onClick={() => {

    setSelectedBill(bill);

    setShowPaymentModal(true);

  }}

  className="
    mt-6
    bg-gradient-to-r
    from-blue-600
    to-purple-600
    hover:scale-105
    text-white
    px-6
    py-3
    rounded-2xl
    font-bold
    transition
    duration-300
    shadow-lg
  "

>

  Pay Now

</button>
  )

}

{

  bill.status === "Paid" && (

    <div
      className="
        mt-6

        bg-green-50

        border
        border-green-200

        rounded-2xl

        p-4
      "
    >

      <h2
        className="
          text-green-700
          font-bold
          text-lg
        "
      >

        ✅ Payment Verified Successfully

      </h2>

      <p
        className="
          text-green-600
          text-sm
          mt-1
        "
      >

        Your payment has been approved
        by apartment management.

      </p>

    </div>

  )

}

{

bill.status !== "Paid" && (

<>

{/* PAYMENT SECTION */}

<div className="mt-6">

  <h2
    className="
      text-xl
      font-black
      mb-5
    "
  >

    Payment Details

  </h2>

  <div
    className="
      grid
      grid-cols-1
      lg:grid-cols-2
      gap-5
    "
  >

    {/* QR */}

    <div
      className="
        flex
        justify-center
      "
    >

      {

        bill.qrCode && (

          <img

            src={bill.qrCode}

            alt="QR"

            className="
              w-32
              h-32
              object-contain
              rounded-2xl
              border
              shadow-lg
            "
          />

        )

      }

    </div>


    {/* UPI */}

    <div className="space-y-4">

      <div
        className="
          w-full
          lg:w-auto
        "
      >

        <p className="text-gray-400">
          UPI ID
        </p>

        <p
          className="
            font-bold
            text-xl
            break-all
          "
        >
          {bill.upiId}
        </p>


        <div className="mt-6">

          <p className="text-gray-400">
            PhonePe Number
          </p>

          <p
            className="
              font-bold
              text-lg
              text-gray-800
            "
          >
            {bill.accountNumber}
          </p>

        </div>

      </div>

    </div>

  </div>

</div>

</>

)

}

            </div>

          ))

        }

      </div>

      {

showPaymentModal && (

<div

className="
fixed
inset-0
bg-black/50
flex
items-center
justify-center
z-50
"

>

<div

className="
bg-white/55
backdrop-blur-xl
border border-white/30
rounded-3xl
p-8
w-full
max-w-lg
shadow-2xl
"

>

<h2
className="
text-2xl
font-black
mb-6
"
>

Submit Payment

</h2>

<input

type="text"

placeholder="Transaction ID"

value={transactionId}

onChange={(e) =>
setTransactionId(e.target.value)
}

className="
w-full
border
rounded-2xl
p-4
mb-4
outline-none
"

/>

<input

type="file"

onChange={(e) => {

const file =
e.target.files[0];

const reader =
new FileReader();

reader.onloadend = () => {

setPaymentProof(
reader.result
);

};

if(file){

reader.readAsDataURL(file);

}

}}

className="
w-full
border
rounded-2xl
p-4
mb-6
"

/>

<div
className="
flex
gap-4
"
>

<button

onClick={() =>
setShowPaymentModal(false)
}

className="
flex-1
bg-gray-200
py-3
rounded-2xl
font-bold
"

>

Cancel

</button>

<button

onClick={submitPayment}

className="
flex-1
bg-blue-600
text-white
py-3
rounded-2xl
font-bold
"

>

Submit Payment

</button>

</div>

</div>

</div>

)

}

{/* PAYMENT HISTORY */}

<div className="mt-16">

  <h2
    className="
      text-3xl
      font-black
      mb-6
    "
  >
    Payment History
  </h2>

  {

    bills

    .filter(

      (bill) =>

        bill.status === "Paid"

    )

    .map((bill) => (

      <div
  key={bill._id}

  onClick={() =>
    setSelectedBill(bill)
  }

  className="
bg-white/55
backdrop-blur-xl
border border-white/30
    rounded-3xl
    shadow-lg
    p-6
    mb-5
    cursor-pointer
    hover:scale-[1.01]
    transition
  "
>

        <div
          className="
            flex
            justify-between
            items-center
          "
        >

          <div>

            <h2
              className="
                text-2xl
                font-black
              "
            >
              {bill.month}
            </h2>

            <p className="text-gray-500">
              ₹{bill.totalAmount}
            </p>

          </div>

          <span
            className="
              bg-green-100
              text-green-700
              px-4
              py-2
              rounded-full
              font-bold
            "
          >
            Paid
          </span>

        </div>

      </div>

    ))

  }

</div>

{/* PAYMENT HISTORY POPUP */}

{

selectedBill && (

<div
  className="
    fixed
    inset-0
    bg-black/50
    z-50
    flex
    items-center
    justify-center
    p-6
  "
>

  <div
    className="
bg-white/55
backdrop-blur-xl
border border-white/30
      rounded-3xl
      w-full
      max-w-2xl
      p-8
      relative
      shadow-2xl
    "
  >

    {/* CLOSE BUTTON */}

    <button

      onClick={() =>
        setSelectedBill(null)
      }

      className="
        absolute
        top-4
        right-4
        text-2xl
        font-bold
        text-gray-400
      "
    >
      ×
    </button>


    {/* TITLE */}

    <h2
      className="
        text-3xl
        font-black
        mb-6
      "
    >
      Payment Details
    </h2>


    {/* MONTH */}

    <div className="mb-6">

      <p className="text-gray-500">
        Billing Month
      </p>

      <h3
        className="
          text-2xl
          font-bold
        "
      >
        {selectedBill.month}
      </h3>

    </div>


    {/* BILL BREAKDOWN */}

    <div
      className="
        grid
        grid-cols-2
        gap-5
        mb-8
      "
    >

      <div>
        <p className="text-gray-500">
          Society Maintenance
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.maintenance}
        </h3>
      </div>

      <div>
        <p className="text-gray-500">
          Water Charges
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.waterBill}
        </h3>
      </div>

      <div>
        <p className="text-gray-500">
          Electricity Charges
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.electricityBill}
        </h3>
      </div>

      <div>
        <p className="text-gray-500">
          Monthly Rent
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.rent}
        </h3>
      </div>

      <div>
        <p className="text-gray-500">
          Parking Charges
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.parkingFee}
        </h3>
      </div>

      <div>
        <p className="text-gray-500">
          Penalty
        </p>

        <h3 className="font-bold text-xl">
₹{selectedBill.penalty}
        </h3>
      </div>

    </div>


    {/* TOTAL */}

    <div
      className="
        bg-blue-50
        rounded-2xl
        p-6
      "
    >

      <p className="text-gray-500">
        Total Paid Amount
      </p>

      <h2
        className="
          text-4xl
          font-black
          text-blue-600
        "
      >
₹{selectedBill.totalAmount}
      </h2>

    </div>


    {/* STATUS */}

    <div className="mt-6">

      <span
        className="
          bg-green-100
          text-green-700
          px-5
          py-2
          rounded-full
          font-bold
        "
      >
        Payment Verified
      </span>

    </div>

  </div>

</div>

)
}

    </div>

  );

}