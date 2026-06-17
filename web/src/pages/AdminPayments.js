import React, {
  useEffect,
  useState,
} from "react";

import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
} from "recharts";
import { useNavigate } from "react-router-dom";

export default function AdminPayments() {

  const [payments, setPayments] =
    useState([]);
    
    const navigate = useNavigate();

    const [billMonth, setBillMonth] =
useState("May 2026");

const [loadingBills, setLoadingBills] =
useState(false);

const [previewImage, setPreviewImage] =
  useState("");

const [showPreview, setShowPreview] =
  useState(false);


  // FETCH PAYMENTS

  const fetchPayments = () => {

    fetch(
      "http://localhost:4000/api/payments"
    )

      .then((res) => res.json())

     .then((data) => {

  console.log("PAYMENTS API:", data);

  setPayments(
    Array.isArray(data)
      ? data
      : []
  );

})

      .catch((err) => {

        console.log(err);

      });

  };


  useEffect(() => {

    fetchPayments();

  }, []);


  // UPDATE STATUS

  const updateStatus = async(
    id,
    status
  ) => {

    try {

      await fetch(

        `http://localhost:4000/api/payments/${id}`,

        {

          method: "PUT",

          headers: {

            "Content-Type":
            "application/json",

          },

          body: JSON.stringify({
            status
          }),

        }

      );

      fetchPayments();

    } catch(err) {

      console.log(err);

    }

  };


  // UPDATE APPROVAL

  const updateApproval = async(
    id,
    approvalStatus
  ) => {

    try {

      await fetch(

        `http://localhost:4000/api/payments/${id}`,

        {

          method: "PUT",

          headers: {

            "Content-Type":
            "application/json",

          },

          body: JSON.stringify({
            approvalStatus
          }),

        }

      );

      fetchPayments();

    } catch(err) {

      console.log(err);

    }

  };


  // ANALYTICS

  const totalRevenue =
(Array.isArray(payments)
  ? payments
  : []
).reduce(
    (acc, item) =>
      acc + Number(item.totalAmount || 0),
    0
  );


  const pendingRevenue =
(Array.isArray(payments)
  ? payments
  : []
)
.filter(
      (item) =>
        item.status !== "Paid"
    )
    .reduce(
      (acc, item) =>
        acc + Number(item.totalAmount || 0),
      0
    );

  const paidBills =
(Array.isArray(payments)
  ? payments
  : []
).filter(
  (item) => item.status === "Paid"
).length;

const pendingBills =
(Array.isArray(payments)
  ? payments
  : []
).filter(
  (item) => item.status !== "Paid"
).length;

  // PIE DATA

  const pieData = [

    {
      name: "Paid",
      value: paidBills,
    },

    {
      name: "Pending",
      value: pendingBills,
    },

  ];


  const COLORS = [
    "#22c55e",
    "#ef4444",
  ];


  // BAR DATA

  const revenueData = [

    {
      month: "Jan",
      revenue: 12000,
    },

    {
      month: "Feb",
      revenue: 18000,
    },

    {
      month: "Mar",
      revenue: 15000,
    },

    {
      month: "Apr",
      revenue: 4500,
    },

  ];

  const approvePayment = async (id) => {

  try {

    await fetch(

      `http://localhost:4000/api/payments/${id}`,

      {

        method: "PUT",

        headers: {

          "Content-Type":
          "application/json",

        },

        body: JSON.stringify({

          status: "Paid",

          approvalStatus:
          "Approved",

        }),

      }

    );

    fetchPayments();

  }

  catch(err) {

    console.log(err);

  }

};



const rejectPayment = async (id) => {

  try {

    await fetch(

      `http://localhost:4000/api/payments/${id}`,

      {

        method: "PUT",

        headers: {

          "Content-Type":
          "application/json",

        },

        body: JSON.stringify({

          status: "Rejected",

          approvalStatus:
          "Rejected",

        }),

      }

    );

    fetchPayments();

  }

  catch(err) {

    console.log(err);

  }

};

  const generateBills = async () => {

  try {

    setLoadingBills(true);

    const res = await fetch(

      "http://localhost:4000/api/payments/generate-bills",

      {

        method: "POST",

        headers: {

          "Content-Type":
          "application/json",

        },

        body: JSON.stringify({

          month: billMonth

        }),

      }

    );

    const data =
    await res.json();

    alert(

      `${data.totalBills} bills generated`

    );

    fetchPayments();

  }

  catch(err) {

    console.log(err);

  }

  finally {

    setLoadingBills(false);

  }

};


  return (

    <div className="p-4 max-w-[1450px] mx-auto">

      {/* TITLE */}

      <h1
        className="
          text-3xl
          font-black
          mb-5
        "
      >
        Payments Management
      </h1>



{/* BILL GENERATOR */}

<div
  className="
    mt-6

    bg-white/70
    backdrop-blur-xl

    rounded-3xl

px-6 py-5
    shadow-xl

    border
    border-white/40

   flex
flex-col
xl:flex-row
xl:items-center
xl:justify-between
gap-5 "
>

  {/* LEFT */}

  <div>

    <h2
      className="
        text-xl
        font-bold
        text-slate-800
      "
    >
      Generate Monthly Bills
    </h2>

    <p
      className="
        text-slate-500
        mt-1
      "
    >
      Automatically create maintenance bills
      for all approved residents
    </p>

  </div>


  {/* RIGHT */}

<div
  className="
    flex
flex-col
sm:flex-row
items-start
sm:items-end
gap-3
w-full
xl:w-auto
  "
>

  {/* MONTH SELECTOR */}

  <div className="flex flex-col">

    <label
      className="
        text-sm
        text-slate-500
        mb-2
        font-medium
      "
    >
      Select Billing Month
    </label>

    <input

      type="month"

      value={billMonth}

      onChange={(e) =>

        setBillMonth(
          e.target.value
        )

      }

      className="
        px-5
        py-3

        rounded-2xl

        border
        border-slate-200

        bg-white/90

        shadow-sm

        outline-none

        focus:ring-2
        focus:ring-blue-400

        text-slate-700
        font-medium

w-[180px]
sm:w-[200px]    "
    />

  </div>


  {/* GENERATE BUTTON */}

  <button

onClick={() =>
    navigate("/admin/create-bill")
  }

  className="
    px-7
    py-3

    rounded-2xl

    bg-gradient-to-r
    from-blue-600
    to-violet-600

    text-white
    font-semibold

    shadow-lg

    hover:shadow-2xl
    hover:scale-105

    transition-all
    duration-300
  "
>

  Generate Bill

  </button>

</div>

  

</div>

      

{/* TOP CARDS */}

<div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4 mt-6 mb-5">
        {/* TOTAL REVENUE */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-4

            shadow-lg

            min-h-[120px]

            flex
            flex-col
            justify-center
          "
        >

          <p
            className="
              text-gray-500
              text-sm
              font-medium
            "
          >
            Total Revenue
          </p>

          <h1
            className="
              text-3xl
              font-black
              mt-2
            "
          >
            ₹{totalRevenue}
          </h1>

        </div>


        {/* PENDING REVENUE */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-4

            shadow-lg

            min-h-[120px]

            flex
            flex-col
            justify-center
          "
        >

          <p
            className="
              text-gray-500
              text-sm
              font-medium
            "
          >
            Pending Revenue
          </p>

          <h1
            className="
              text-3xl
              font-black
              text-red-500
              mt-2
            "
          >
            ₹{pendingRevenue}
          </h1>

        </div>


        {/* PAID BILLS */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-4

            shadow-lg

            min-h-[120px]

            flex
            flex-col
            justify-center
          "
        >

          <p
            className="
              text-gray-500
              text-sm
              font-medium
            "
          >
            Paid Bills
          </p>

          <h1
            className="
              text-3xl
              font-black
              text-green-500
              mt-2
            "
          >
            {paidBills}
          </h1>

        </div>


        {/* PENDING BILLS */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-4

            shadow-lg

            min-h-[120px]

            flex
            flex-col
            justify-center
          "
        >

          <p
            className="
              text-gray-500
              text-sm
              font-medium
            "
          >
            Pending Bills
          </p>

          <h1
            className="
              text-3xl
              font-black
              text-yellow-500
              mt-2
            "
          >
            {pendingBills}
          </h1>

        </div>

      </div>


      {/* CHARTS */}

<div className="grid grid-cols-1 xl:grid-cols-2 gap-5 mb-5">

        {/* PIE CHART */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-5

            shadow-lg
          "
        >

          <h2
            className="
              text-2xl
              font-bold
              mb-4
            "
          >
            Payment Status
          </h2>

          <div className="h-[280px]">

            <ResponsiveContainer>

              <PieChart>

                <Pie

                  data={pieData}

                  dataKey="value"

                  outerRadius={90}

                  innerRadius={50}

                >

                  {

                    pieData.map(
                      (
                        entry,
                        index
                      ) => (

                        <Cell
                          key={index}
                          fill={
                            COLORS[index]
                          }
                        />

                      )
                    )

                  }

                </Pie>

              </PieChart>

            </ResponsiveContainer>

          </div>

        </div>


        {/* BAR CHART */}

        <div
          className="
            bg-white/60
            backdrop-blur-xl

            rounded-3xl

            p-5

            shadow-lg
          "
        >

          <h2
            className="
              text-2xl
              font-bold
              mb-4
            "
          >
            Monthly Revenue
          </h2>

          <div className="h-[280px]">

            <ResponsiveContainer>

              <BarChart
                data={revenueData}
              >

                <XAxis dataKey="month" />

                <YAxis />

                <Tooltip />

                <Bar
                  dataKey="revenue"
                  fill="#4f46e5"
                  radius={[8,8,0,0]}
                />

              </BarChart>

            </ResponsiveContainer>

          </div>

        </div>

      </div>


      {/* PAYMENTS LIST */}

      <div className="space-y-4">

        {

Array.isArray(payments) &&
payments.map((item) => (
            <div

              key={item._id}

              className="
                bg-white/60
                backdrop-blur-xl

                rounded-3xl

                px-5
                py-3

                shadow-lg

                grid
grid-cols-1
lg:grid-cols-[1fr_1fr_0.7fr_0.9fr]

                gap-5

                items-center
              "
            >

              {/* RESIDENT INFO */}

              <div>

                <h2
                  className="
                    text-xl
font-bold
leading-tight
                  "
                >
                  {item.residentName}
                </h2>

                <p
                  className="
                    text-gray-500
                    mt-1
                  "
                >
                  Flat: {item.flatNumber}
                </p>

                <p
                  className="
                    text-gray-400
                    mt-1
                  "
                >
                  {item.month}
                </p>

              </div>


              {/* PAYMENT INFO */}

              <div>

                <p
                  className="
                    text-gray-400
                    text-sm
                  "
                >
                  Payment Type
                </p>

                <h3
                  className="
                    text-2xl
                    font-bold
                  "
                >
                  {item.paymentType}
                </h3>

                <p
                  className="
                    text-2xl
font-extrabold
mt-1
                  "
                >
₹{item.totalAmount}
                </p>
                <p className="text-sm text-gray-500 mt-1">
  Maintenance: ₹{item.maintenance}
</p>

<p className="text-sm text-gray-500">
  Water: ₹{item.waterBill}
</p>

<p className="text-sm text-gray-500">
  Electricity: ₹{item.electricityBill}
</p>

                <p
                  className="
                    text-blue-600
                    font-bold
                    mt-2
                  "
                >
                  {item.transactionId}
                </p>

                <p
                  className="
                    inline-block

                    mt-2

                    px-3
                    py-1

                    rounded-full

                    bg-blue-100
                    text-blue-600

                    text-xs
                    font-semibold
                  "
                >
                  UPI Payment
                </p>

              </div>


              {/* PAYMENT PROOF */}

              <div
                className="
                  flex
                  flex-col
                  items-center
                "
              >

                <p
                  className="
                    text-gray-400
                    text-sm
                    mb-2
                  "
                >
                  Payment Proof
                </p>

                {

                  item.paymentProof

                    ? (

                     <img

  src={item.paymentProof}

  alt="proof"

  onClick={() => {

  setPreviewImage(
    item.paymentProof
  );

  setShowPreview(true);

}}
  className="
    w-16
    h-16

    object-cover

    rounded-2xl

    border
    shadow-md

    cursor-pointer

    hover:scale-110

    transition-all
  "
/>

                    )

                    : (

                      <div
                        className="
                          w-20
                          h-20

                          rounded-2xl

                          bg-gray-100

                          flex
                          items-center
                          justify-center

                          text-gray-400
                          text-sm
                        "
                      >
                        No Proof
                      </div>

                    )

                }

              </div>


              {/* ACTIONS */}

              <div
                className="
                  flex
                  flex-col
                  gap-3
                "
              >

                <span
                  className={`
                    w-fit

                    px-3
                    py-1

                    rounded-full

                    text-sm
                    font-bold

                    ${
  item.status === "Paid"

    ? `
      bg-green-100
      text-green-600
    `

    : item.status ===
      "Rejected"

    ? `
      bg-red-100
      text-red-600
    `

    : item.status ===
      "Verification Pending"

    ? `
      bg-blue-100
      text-blue-600
    `

    : `
      bg-yellow-100
      text-yellow-600
    `
}
                  `}
                >
                  {item.status}
                </span>


                <p
                  className="
                    text-sm
                    text-gray-500
                  "
                >
                  {item.approvalStatus}
                </p>


                <select

                  value={item.status}

                  onChange={(e)=>

                    updateStatus(
                      item._id,
                      e.target.value
                    )

                  }

                  className="
                    w-full

                   px-3
py-2
text-sm

                    rounded-xl
                    border
                  "
                >

                  <option>
                    Pending
                  </option>

                  <option>
                    Paid
                  </option>

                </select>


                <div className="flex gap-2">

                  <button

                    onClick={async () => {

  await updateApproval(
    item._id,
    "Approved"
  );

  await updateStatus(
    item._id,
    "Paid"
  );

}}

                    className="
                      flex-1

                      bg-green-500
                      hover:bg-green-600

                      text-white

                      py-1.5

                      rounded-xl

                      font-semibold
                      text-sm
                    "
                  >
                    Verify
                  </button>


                  <button

                    onClick={async () => {

  await updateApproval(
    item._id,
    "Rejected"
  );

  await updateStatus(
    item._id,
    "Rejected"
  );

}}

                    className="
                      flex-1

                      bg-red-500
                      hover:bg-red-600

                      text-white

                      py-2

                      rounded-xl

                      font-semibold
                      text-sm
                    "
                  >
                    Reject
                  </button>

                </div>

              </div>

            </div>

          ))

        }

      </div>


      {

  showPreview && (

    <div
      className="
        fixed
        inset-0

        bg-black/70

        flex
        items-center
        justify-center

        z-50
      "
    >

      <div
        className="
          relative

          bg-white

          rounded-3xl

          p-4

          shadow-2xl
        "
      >

        {/* CLOSE BUTTON */}

        <button

          onClick={() =>
            setShowPreview(false)
          }

          className="
            absolute
            top-3
            right-3

            bg-red-500
            hover:bg-red-600

            text-white

            w-8
            h-8

            rounded-full

            font-bold
          "
        >

          ✕

        </button>


        {/* BIG IMAGE */}

        <img

          src={previewImage}

          alt="Payment Proof"

          className="
            max-w-[90vw]
            max-h-[85vh]

            rounded-2xl
          "
        />

      </div>

    </div>

  )

}

    </div>

  );

}