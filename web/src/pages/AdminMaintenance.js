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
  Tooltip
} from "recharts";

export default function AdminMaintenance() {

  const [maintenance, setMaintenance] =
    useState([]);


  // FETCH DATA
  const fetchData = () => {

    fetch(
      "http://localhost:4000/api/maintenance"
    )

      .then((res) => res.json())

      .then((data) => {

        setMaintenance(data);

      })

      .catch((err) => {

        console.log(err);

      });

  };


  useEffect(() => {

    fetchData();

  }, []);


  // UPDATE STATUS
  const updateStatus = async(id, status) => {

    try {

      await fetch(

        `http://localhost:4000/api/maintenance/${id}`,

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

      fetchData();

    } catch(err) {

      console.log(err);

    }

  };


  // ASSIGN WORKER
  const assignWorker = async(id, worker) => {

    try {

      await fetch(

        `http://localhost:4000/api/maintenance/${id}`,

        {

          method: "PUT",

          headers: {

            "Content-Type":
            "application/json",

          },

          body: JSON.stringify({
            assignedWorker: worker
          }),

        }

      );

      fetchData();

    } catch(err) {

      console.log(err);

    }

  };


  return (

    <div className="p-6">

      <h1
        className="
          text-4xl
          font-bold
          mb-8
        "
      >
        Maintenance Management
      </h1>
{/* ANALYTICS CARDS */}

<div
  className="
    grid
    grid-cols-4
    gap-5
    mb-8
  "
>

  {/* TOTAL */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
    "
  >

    <p className="text-slate-500 text-sm">
      Total Requests
    </p>

    <h2
      className="
        text-4xl
        font-black
        mt-2
      "
    >
      {maintenance.length}
    </h2>

  </div>

  {/* PENDING */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
    "
  >

    <p className="text-slate-500 text-sm">
      Pending
    </p>

    <h2
      className="
        text-4xl
        font-black
        text-yellow-500
        mt-2
      "
    >
      {
        maintenance.filter(
          (item) =>
            item.status === "Pending"
        ).length
      }
    </h2>

  </div>

  {/* IN PROGRESS */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
    "
  >

    <p className="text-slate-500 text-sm">
      In Progress
    </p>

    <h2
      className="
        text-4xl
        font-black
        text-blue-600
        mt-2
      "
    >
      {
        maintenance.filter(
          (item) =>
            item.status ===
            "In Progress"
        ).length
      }
    </h2>

  </div>

  {/* COMPLETED */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
    "
  >

    <p className="text-slate-500 text-sm">
      Completed
    </p>

    <h2
      className="
        text-4xl
        font-black
        text-green-600
        mt-2
      "
    >
      {
        maintenance.filter(
          (item) =>
            item.status ===
            "Completed"
        ).length
      }
    </h2>

  </div>

</div>

{/* CHARTS */}

<div
  className="
    grid
    grid-cols-2
    gap-6
    mb-8
  "
>

  {/* PIE CHART */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
      h-[350px]
    "
  >

    <h2
      className="
        text-2xl
        font-bold
        mb-5
      "
    >
      Status Analytics
    </h2>

    <ResponsiveContainer
      width="100%"
      height="100%"
    >

      <PieChart>

        <Pie

          data={[

            {
              name: "Pending",

              value:
                maintenance.filter(
                  (item) =>
                    item.status ===
                    "Pending"
                ).length,
            },

            {
              name: "In Progress",

              value:
                maintenance.filter(
                  (item) =>
                    item.status ===
                    "In Progress"
                ).length,
            },

            {
              name: "Completed",

              value:
                maintenance.filter(
                  (item) =>
                    item.status ===
                    "Completed"
                ).length,
            },

          ]}

          dataKey="value"

          outerRadius={110}

          label

        >

          <Cell fill="#facc15" />

          <Cell fill="#2563eb" />

          <Cell fill="#16a34a" />

        </Pie>

        <Tooltip />

      </PieChart>

    </ResponsiveContainer>

  </div>


  {/* BAR CHART */}

  <div
    className="
      bg-white
      rounded-3xl
      p-6
      shadow-lg
      h-[350px]
    "
  >

    <h2
      className="
        text-2xl
        font-bold
        mb-5
      "
    >
      Category Analytics
    </h2>

    <ResponsiveContainer
      width="100%"
      height="100%"
    >

      <BarChart

        data={[

          {
            category: "Electrical",

            total:
              maintenance.filter(
                (item) =>
                  item.category ===
                  "Electrical"
              ).length,
          },

          {
            category: "Plumbing",

            total:
              maintenance.filter(
                (item) =>
                  item.category ===
                  "Plumbing"
              ).length,
          },

          {
            category: "Cleaning",

            total:
              maintenance.filter(
                (item) =>
                  item.category ===
                  "Cleaning"
              ).length,
          },

        ]}

      >

        <XAxis dataKey="category" />

        <YAxis />

        <Tooltip />

        <Bar
          dataKey="total"
          fill="#4f46e5"
          radius={[10,10,0,0]}
        />

      </BarChart>

    </ResponsiveContainer>

  </div>

</div>

      <div className="space-y-5">

        {maintenance.map((item) => (

          <div

            key={item._id}

            className="
              bg-white/50
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              grid
              grid-cols-6
              gap-4
              items-center
            "
          >

            {/* ISSUE */}
            <div>

              <h2 className="font-bold">
                {item.issue}
              </h2>

              <p
                className="
                  text-sm
                  text-gray-500
                "
              >
                {item.description}
              </p>

            </div>


            {/* FLAT */}
            <p>
              {item.flatNumber}
            </p>


            {/* CATEGORY */}
            <p>
              {item.category}
            </p>


            {/* PRIORITY */}
            <p
              className="
                font-semibold
                text-red-500
              "
            >
              {item.priority}
            </p>


            {/* ASSIGN WORKER */}
            <select

              value={item.assignedWorker}

              onChange={(e)=>

                assignWorker(
                  item._id,
                  e.target.value
                )

              }

              className="
                p-3
                rounded-xl
                border
              "
            >

              <option>
                Not Assigned
              </option>

              <option>
                Raj Worker
              </option>

              <option>
                Electrical Team
              </option>

              <option>
                Plumbing Team
              </option>

              <option>
                Cleaning Team
              </option>

            </select>


            {/* STATUS */}
            <select

              value={item.status}

              onChange={(e)=>

                updateStatus(
                  item._id,
                  e.target.value
                )

              }

              className="
                p-3
                rounded-xl
                border
              "
            >

              <option>
                Pending
              </option>

              <option>
                In Progress
              </option>

              <option>
                Completed
              </option>

            </select>

          </div>

        ))}

      </div>

    </div>

  );

}