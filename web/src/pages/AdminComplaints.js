import React, {
  useEffect,
  useState,
} from "react";
import AdminSidebar from "../components/AdminSidebar";

export default function AdminComplaints() {

  const [complaints, setComplaints] =
    useState([]);

  useEffect(() => {

    fetch(
      "http://localhost:4000/api/complaints"
    )
      .then((res) => res.json())
      .then((data) => {

        if (Array.isArray(data)) {

          setComplaints(data);

        }

      });

  }, []);

  const updateStatus = async (
    id,
    newStatus
  ) => {

    try {

      const response = await fetch(
        `http://localhost:4000/api/complaints/${id}`,
        {
          method: "PUT",

          headers: {
            "Content-Type":
              "application/json",
          },

          body: JSON.stringify({
            status: newStatus,
          }),
        }
      );

      const updatedData =
        await response.json();

      setComplaints((prev) =>
        prev.map((item) =>
          item._id === id
            ? updatedData
            : item
        )
      );

    } catch (err) {

      console.log(err);

    }

  };

  return (

<div
  className="
    min-h-screen

    p-8

    bg-gradient-to-br
    from-[#eef2ff]
    via-[#f5f3ff]
    to-[#ecfeff]

    relative
    overflow-hidden
  "
>

  {/* BACKGROUND GLOW */}

<div
  className="
    absolute
    top-[-120px]
    left-[-120px]

    w-[320px]
    h-[320px]

    bg-violet-400/30

    rounded-full

    blur-3xl

    animate-pulse
  "
/>

<div
  className="
    absolute
    bottom-[-120px]
    right-[-120px]

    w-[320px]
    h-[320px]

    bg-cyan-400/30

    rounded-full

    blur-3xl

    animate-pulse
  "
/>

<div className="relative z-10">

</div>
      <h1
        className="
  text-5xl
  font-black
  mb-8

  bg-gradient-to-r
  from-indigo-700
  via-violet-600
  to-cyan-500

  bg-clip-text
  text-transparent

  leading-tight
"
      >
        Complaint Management
      </h1>

      <div className="mb-6">

  <input
    type="text"

    placeholder="Search complaints..."

   className="
  w-full

  bg-white/50
  backdrop-blur-xl

  border
  border-white/40

  rounded-2xl

  px-5
  py-4

  outline-none

  shadow-lg

  text-gray-700

  placeholder-gray-400

  focus:ring-4
  focus:ring-violet-300/40

  transition-all
"
  />

</div>

      <div className="space-y-5">

        {complaints.length === 0 && (

  <div
    className="
  bg-white/40
  backdrop-blur-2xl

  border
  border-white/40

  rounded-[30px]

  p-6

  shadow-[0_10px_40px_rgba(79,70,229,0.12)]

  hover:-translate-y-2
  hover:scale-[1.01]

  hover:shadow-[0_20px_60px_rgba(79,70,229,0.20)]

  transition-all
  duration-300
"
  >

    🎉 No complaints found

  </div>

)}

        {complaints.map((item) => (

          <div
            key={item._id}
            className="
  bg-white/30
  backdrop-blur-2xl

  border
  border-white/30

  rounded-[28px]

  p-6

  shadow-[0_10px_40px_rgba(15,23,42,0.08)]

  hover:-translate-y-1
  hover:shadow-[0_20px_50px_rgba(79,70,229,0.12)]

  transition-all
  duration-300
"
          >

            <div className="flex justify-between items-start">

  {/* LEFT SIDE */}

  <div className="space-y-3">

    <div>

      <h2 className="text-xl font-bold">
        {item.title}
      </h2>

      <p className="text-gray-500 mt-1">
        {item.description}
      </p>

    </div>

    <div className="flex gap-3 flex-wrap">

      {/* CATEGORY */}

      <span
        className="
          bg-gradient-to-r
from-blue-500
to-cyan-500

text-white

shadow-md

          text-xs
          font-semibold

          px-3
          py-1

          rounded-full
        "
      >
        {item.category}
      </span>

      {/* PRIORITY */}

      <span
        className={`
  text-xs
  font-semibold

  px-3
  py-1

  rounded-full

  shadow-md

  ${
    item.priority === "Normal"

      ? "bg-gradient-to-r from-red-500 to-pink-500 text-white"

    : item.priority === "Urgent"

      ? "bg-gradient-to-r from-orange-400 to-yellow-400 text-white"

    : "bg-gradient-to-r from-slate-400 to-slate-500 text-white"
  }
`}
      >
        {item.priority}
      </span>

    </div>

    {/* RESIDENT DETAILS */}

    <div className="text-sm text-gray-500">

      <p>
        Resident:
        <span className="font-semibold text-black ml-1">
          {item.residentName}
        </span>
      </p>

      <p>
        Flat:
        <span className="font-semibold text-black ml-1">
          {item.flatNumber}
        </span>
      </p>

    </div>

  </div>


  {/* RIGHT SIDE */}

  <div className="flex flex-col items-end gap-4">

    {/* STATUS */}

    <select
      value={item.status}

      onChange={(e) =>
        updateStatus(
          item._id,
          e.target.value
        )
      }

     className="
  bg-white/70
  backdrop-blur-xl

  border
  border-white/40

  rounded-2xl

  px-5
  py-3

  font-semibold

  shadow-lg

  outline-none

  hover:scale-[1.02]

  transition-all
"
    >

      <option>
        Pending
      </option>

      <option>
        In Progress
      </option>

      <option>
        Resolved
      </option>

    </select>

    {/* CREATED DATE */}

    <p className="text-sm text-gray-500">
      {new Date(
        item.createdAt
      ).toLocaleDateString()}
    </p>

  </div>

</div>

            

          </div>

        ))}

      </div>

    </div>

  );

}