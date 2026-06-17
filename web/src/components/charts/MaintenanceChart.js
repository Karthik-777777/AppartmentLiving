import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

import { useEffect, useState } from "react";

import axios from "axios";



export default function MaintenanceChart() {
  const [data, setData] =
useState([]);
useEffect(() => {

  fetchMaintenance();

}, []);

const fetchMaintenance = async () => {

  try {

    const res = await axios.get(
      "http://localhost:4000/api/maintenance"
    );

    const maintenance =
      res.data;

    const maintenanceData = [

      {
        month: "Pending",

        requests:
          maintenance.filter(
            m =>
              m.status ===
              "Pending"
          ).length,
      },

      {
        month: "In Progress",

        requests:
          maintenance.filter(
            m =>
              m.status ===
              "In Progress"
          ).length,
      },

      {
        month: "Completed",

        requests:
          maintenance.filter(
            m =>
              m.status ===
              "Completed"
          ).length,
      },

    ];

    setData(maintenanceData);

  }

  catch(err) {

    console.log(err);

  }

};

  return (

    <div
      className="
        bg-white/50
        rounded-3xl
        p-5
        shadow-lg
      "
    >

      <h2
        className="
          text-2xl
          font-bold
          mb-5
        "
      >
        Maintenance Analytics
      </h2>

      <ResponsiveContainer
        width="100%"
        height={300}
      >

        <BarChart data={data}>

          <XAxis dataKey="month" />

          <YAxis />

          <Tooltip />

<Bar
  dataKey="requests"
  fill="#7C3AED"
  radius={[10,10,0,0]}
/>
        </BarChart>

      </ResponsiveContainer>

    </div>

  );

}