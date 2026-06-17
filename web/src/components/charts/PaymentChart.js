import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  Tooltip,
  YAxis,
} from "recharts";

import { useEffect, useState } from "react";

import axios from "axios";


export default function PaymentChart() {
  const [data, setData] =
useState([]);
useEffect(() => {

  fetchPayments();

}, []);
const fetchPayments = async () => {

  try {
    const resident =
JSON.parse(
  localStorage.getItem(
    "resident"
  )
);

  const res = await axios.get(
  `http://localhost:4000/api/payments/resident/${resident.residentId}`
);

    const payments =
      res.data;

    const paymentData = [

      {
        month: "Paid",

        paid:
          payments.filter(
            p =>
              p.status ===
              "Paid"
          ).length,
      },

      {
        month: "Pending",

        paid:
          payments.filter(
            p =>
              p.status ===
              "Pending"
          ).length,
      },

      {
        month: "Overdue",

        paid:
          payments.filter(
            p =>
              p.status ===
              "Overdue"
          ).length,
      },

    ];

    setData(paymentData);

  }

  catch(err) {

    console.log(err);

  }

};

  return (

    <div
     className="
  bg-white/30
  backdrop-blur-2xl

  border
  border-white/30

  rounded-[24px]

  p-4

  shadow-[0_10px_40px_rgba(15,23,42,0.06)]

  hover:-translate-y-1
  hover:shadow-[0_20px_50px_rgba(79,70,229,0.14)]

  transition-all
  duration-300
"
    >

      <h2 className="text-[17px] font-bold mb-4">
        Payment history
      </h2>

      <div className="h-[220px]">

        <ResponsiveContainer width="100%" height="100%">

          <BarChart data={data}>

            <XAxis dataKey="month" />
            <YAxis />

            <Tooltip />

            <Bar
              dataKey="paid"
              fill="#7C3AED"
              radius={[10, 10, 0, 0]}
            />

          </BarChart>

        </ResponsiveContainer>

      </div>

    </div>

  );

}