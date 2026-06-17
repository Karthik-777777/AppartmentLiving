import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

import { useEffect, useState }
from "react";

import axios from "axios";


const COLORS = [
  "#22C55E",
  "#EF4444",
  "#F59E0B",
];

export default function StatusPieChart() {
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

    const chartData = [

      {
        name: "Resolved",

        value:
          complaints.filter(
            c =>
              c.status ===
              "Resolved"
          ).length,
      },

      {
        name: "Pending",

        value:
          complaints.filter(
            c =>
              c.status ===
              "Pending"
          ).length,
      },

      {
        name: "In Progress",

        value:
          complaints.filter(
            c =>
              c.status ===
              "In Progress"
          ).length,
      },

    ];

    setData(chartData);

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
        Complaint status
      </h2>

      <div className="h-[220px]">

        <ResponsiveContainer width="100%" height="100%">

          <PieChart>

            <Pie
              data={data}
              cx="50%"
              cy="50%"
              outerRadius={80}
              dataKey="value"
            >

              {data.map((entry, index) => (

                <Cell
                  key={index}
                  fill={COLORS[index]}
                />

              ))}

            </Pie>

            <Tooltip />

          </PieChart>

        </ResponsiveContainer>

      </div>

    </div>

  );

}