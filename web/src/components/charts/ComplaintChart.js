import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  Tooltip,
  CartesianGrid,
  YAxis,
} from "recharts";

import { useEffect, useState } from "react";

import axios from "axios";



export default function ComplaintChart() {
  const [data, setData] =
useState([]);
useEffect(() => {

  fetchComplaints();

}, []);

const fetchComplaints = async () => {

  try {
    const resident =
JSON.parse(
  localStorage.getItem(
    "resident"
  )
);


    const res = await axios.get(
  `http://localhost:4000/api/complaints/resident/${resident.residentId}`
);

    const complaints =
      res.data;

    const monthlyData = [

      {
        month: "Pending",

        complaints:
          complaints.filter(
            c =>
              c.status ===
              "Pending"
          ).length,
      },

      {
        month: "Resolved",

        complaints:
          complaints.filter(
            c =>
              c.status ===
              "Resolved"
          ).length,
      },

      {
        month: "In Progress",

        complaints:
          complaints.filter(
            c =>
              c.status ===
              "In Progress"
          ).length,
      },

    ];

    setData(monthlyData);

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
        Complaint trend
      </h2>

      <div className="h-[220px]">

        <ResponsiveContainer width="100%" height="100%">

          <LineChart data={data}>

<CartesianGrid
  strokeDasharray="3 3"
  stroke="#E2E8F0"
  opacity={0.4}
/>
            <XAxis dataKey="month" />
            <YAxis />

            <Tooltip />

            <Line
              type="monotone"
              dataKey="complaints"
              stroke="#4F46E5"
              strokeWidth={3}
            />

          </LineChart>

        </ResponsiveContainer>

      </div>

    </div>

  );

}