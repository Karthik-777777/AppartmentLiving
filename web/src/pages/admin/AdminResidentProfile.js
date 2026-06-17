import React, {
  useEffect,
  useState,
} from "react";

import {
  useParams,
} from "react-router-dom";

export default function AdminResidentProfile() {

  const { id } = useParams();

  const [resident, setResident] =
  useState(null);
  const [payments, setPayments] =
useState([]);


  // FETCH RESIDENT

  const fetchResident = async () => {
    

    try {

      const res = await fetch(

        `http://localhost:4000/api/residents/${id}`

      );

      const data = await res.json();

      setResident(data);

    } catch(err) {

      console.log(err);

    }

  };
const fetchPayments = async () => {

  try {

    const res = await fetch(

      `http://localhost:4000/api/payments/resident/${id}`

    );

    const data = await res.json();

    setPayments(data);

  }

  catch(err) {

    console.log(err);

  }

};

  useEffect(() => {

  fetchResident();

  fetchPayments();

}, []);


  if(!resident) {

    return (

      <div
        className="
          min-h-screen

          flex
          items-center
          justify-center

          text-2xl
          font-bold
        "
      >
        Loading...
      </div>

    );

  }


  return (

    <div
      className="
        min-h-screen

        p-6

        bg-gradient-to-br
        from-slate-100
        via-blue-50
        to-violet-100
      "
    >

      {/* HEADER */}

      <div
        className="
          bg-white/70
          backdrop-blur-xl

          rounded-3xl

          p-8

          shadow-xl

          border
          border-white/40

          flex
          justify-between
          items-center
        "
      >

        {/* LEFT */}

        <div className="flex items-center gap-6">

          {/* AVATAR */}

          <div
            className="
              h-24
              w-24

              rounded-3xl

              bg-gradient-to-r
              from-blue-500
              to-violet-500

              flex
              items-center
              justify-center

              text-white
              text-4xl
              font-bold

              shadow-xl
            "
          >
            {resident.residentName?.charAt(0)}
          </div>


          {/* DETAILS */}

          <div>

            <h1
              className="
                text-4xl
                font-bold
                text-slate-800
              "
            >
              {resident.residentName}
            </h1>

            <p
              className="
                text-slate-500
                mt-1
              "
            >
              {resident.email}
            </p>

            <p
              className="
                text-blue-600
                font-bold
                mt-2
              "
            >
              {resident.residentId}
            </p>

          </div>

        </div>


        {/* STATUS */}

        <div>

          <span
            className={`
              px-5
              py-3

              rounded-full

              font-bold

              ${
                resident.status === "Approved"

                ? "bg-green-100 text-green-600"

                : "bg-yellow-100 text-yellow-600"
              }
            `}
          >
            {resident.status}
          </span>

        </div>

      </div>


      {/* ANALYTICS */}

      <div
        className="
          grid
          grid-cols-4
          gap-6

          mt-8
        "
      >

        {/* FLAT */}

        <div
          className="
            bg-white/70
            backdrop-blur-xl

            rounded-3xl

            p-6

            shadow-lg
          "
        >

          <p className="text-slate-400">
            Flat
          </p>

          <h2
            className="
              text-3xl
              font-bold
              mt-2
            "
          >
            {resident.block}-
            {resident.flatNumber}
          </h2>

        </div>


        {/* TYPE */}

        <div
          className="
            bg-white/70
            backdrop-blur-xl

            rounded-3xl

            p-6

            shadow-lg
          "
        >

          <p className="text-slate-400">
            Flat Type
          </p>

          <h2
            className="
              text-3xl
              font-bold
              mt-2
            "
          >
            {resident.flatType}
          </h2>

        </div>


        {/* PHONE */}

        <div
          className="
            bg-white/70
            backdrop-blur-xl

            rounded-3xl

            p-6

            shadow-lg
          "
        >

          <p className="text-slate-400">
            Phone
          </p>

          <h2
            className="
              text-2xl
              font-bold
              mt-2
            "
          >
            {resident.phone}
          </h2>

        </div>


        {/* MAINTENANCE */}

        <div
          className="
            bg-white/70
            backdrop-blur-xl

            rounded-3xl

            p-6

            shadow-lg
          "
        >

          <p className="text-slate-400">
            Maintenance
          </p>

          <h2
            className="
              text-3xl
              font-bold
              mt-2
              text-green-600
            "
          >
            ₹{resident.maintenanceAmount}
          </h2>

        </div>

      </div>


      {/* PERSONAL DETAILS */}

      <div
        className="
          bg-white/70
          backdrop-blur-xl

          rounded-3xl

          p-8

          shadow-xl

          mt-8
        "
      >

        <h2
          className="
            text-2xl
            font-bold
            mb-6
          "
        >
          Resident Information
        </h2>


        <div
          className="
            grid
            grid-cols-2
            gap-6
          "
        >

          <div>

            <p className="text-slate-400">
              Ownership Type
            </p>

            <h2
              className="
                font-semibold
                text-lg
              "
            >
              {resident.ownerType}
            </h2>

          </div>


          <div>

            <p className="text-slate-400">
              Gender
            </p>

            <h2
              className="
                font-semibold
                text-lg
              "
            >
              {resident.gender}
            </h2>

          </div>


          <div>

            <p className="text-slate-400">
              Emergency Contact
            </p>

            <h2
              className="
                font-semibold
                text-lg
              "
            >
              {resident.emergencyName}
            </h2>

          </div>


          <div>

            <p className="text-slate-400">
              Emergency Phone
            </p>

            <h2
              className="
                font-semibold
                text-lg
              "
            >
              {resident.emergencyPhone}
            </h2>

          </div>

          

        </div>

      </div>
      {/* PAYMENT HISTORY */}

<div
  className="
    mt-8

    bg-white/70
    backdrop-blur-xl

    rounded-3xl

    shadow-xl

    border
    border-white/40

    overflow-hidden
  "
>

  {/* HEADER */}

  <div
    className="
      flex
      justify-between
      items-center

      px-8
      py-6

      border-b
      border-slate-200
    "
  >

    <div>

      <h2
        className="
          text-2xl
          font-bold
          text-slate-800
        "
      >
        Payment History
      </h2>

      <p
        className="
          text-slate-500
          mt-1
        "
      >
        Resident monthly payment records
      </p>

    </div>


    <div
      className="
        px-4
        py-2

        rounded-2xl

        bg-blue-100
        text-blue-600

        font-semibold
      "
    >
      {payments.length} Payments
    </div>

  </div>


  {/* TABLE HEADER */}

  <div
    className="
      grid
      grid-cols-6

      px-8
      py-4

      bg-slate-50

      text-sm
      font-semibold
      text-slate-500
    "
  >

    <div>Month</div>

    <div>Amount</div>

    <div>Type</div>

    <div>Status</div>

    <div>Transaction</div>

    <div>Proof</div>

  </div>


  {/* PAYMENT ROWS */}

  {

    payments.length > 0

    ? (

      payments.map((item) => (

        <div

          key={item._id}

          className="
            grid
            grid-cols-6

            items-center

            px-8
            py-5

            border-t
            border-slate-100

            hover:bg-blue-50/40

            transition-all
          "
        >

          {/* MONTH */}

          <div
            className="
              font-semibold
              text-slate-700
            "
          >
            {item.month}
          </div>


          {/* AMOUNT */}

          <div
            className="
              font-bold
              text-green-600
              text-lg
            "
          >
            ₹{item.amount}
          </div>


          {/* TYPE */}

          <div>

            <span
              className="
                px-3
                py-1

                rounded-full

                bg-violet-100
                text-violet-600

                text-sm
                font-semibold
              "
            >
              {item.paymentType}
            </span>

          </div>


          {/* STATUS */}

          <div>

            <span
              className={`
                px-3
                py-1

                rounded-full

                text-sm
                font-semibold

                ${
                  item.status === "Paid"

                  ? `
                    bg-green-100
                    text-green-600
                  `

                  : `
                    bg-yellow-100
                    text-yellow-700
                  `
                }
              `}
            >
              {item.status}
            </span>

          </div>


          {/* TRANSACTION */}

          <div
            className="
              text-blue-600
              font-semibold
            "
          >
            {

              item.transactionId

              ||

              "Not Paid"

            }
          </div>


          {/* PROOF */}

          <div>

            {

              item.paymentProof

              ? (

                <img

                  src={item.paymentProof}

                  alt="proof"

                  className="
                    h-14
                    w-14

                    rounded-xl

                    object-cover

                    border
                    border-slate-200
                  "
                />

              )

              : (

                <span
                  className="
                    text-slate-400
                    text-sm
                  "
                >
                  No Proof
                </span>

              )

            }

          </div>

        </div>

      ))

    )

    : (

      <div
        className="
          flex
          flex-col
          items-center
          justify-center

          py-20
        "
      >

        <div
          className="
            text-6xl
            mb-4
          "
        >
          💳
        </div>

        <h2
          className="
            text-xl
            font-semibold
            text-slate-600
          "
        >
          No Payment History
        </h2>

        <p
          className="
            text-slate-400
            mt-2
          "
        >
          Resident payments will appear here
        </p>

      </div>

    )

  }

</div>

    </div>

    

  );

}