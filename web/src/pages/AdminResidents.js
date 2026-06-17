import React, {
  useEffect,
  useState,
} from "react";

import {
  useNavigate
} from "react-router-dom";

export default function AdminResidents() {

    const navigate =
useNavigate();

  const [residents, setResidents] =
    useState([]);
    const [search, setSearch] =
useState("");
const filteredResidents =

residents.filter((item) => {

  const searchText =
  search.toLowerCase();

  return (

    item.residentName
    ?.toLowerCase()
    .includes(searchText)

    ||

    item.residentId
    ?.toLowerCase()
    .includes(searchText)

    ||

    item.flatNumber
    ?.toLowerCase()
    .includes(searchText)

  );

});


  // FETCH RESIDENTS

  const fetchResidents = () => {

    fetch(
      "http://localhost:4000/api/residents"
    )

      .then((res) => res.json())

      .then((data) => {

        setResidents(data);

      })

      .catch((err) => {

        console.log(err);

      });

  };


  useEffect(() => {

    fetchResidents();

  }, []);




  // DELETE RESIDENT

  const deleteResident = async(id) => {

    try {

      await fetch(

        `http://localhost:4000/api/residents/${id}`,

        {

          method: "DELETE",

        }

      );

      fetchResidents();

    } catch(err) {

      console.log(err);

    }

  };

  const updateResidentStatus =
async (id, status) => {

  try {

    await fetch(

      `http://localhost:4000/api/residents/${id}`,

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

    fetchResidents();

  } catch(err) {

    console.log(err);

  }

};


  return (

<div
  className="
    p-6

    min-h-screen

    bg-gradient-to-br
    from-slate-100
    via-blue-50
    to-violet-100
  "
>
      {/* TITLE */}

      <h1
        className="
          text-4xl
          font-bold
          mb-8
        "
      >
        Residents Management
      </h1>



      {/* SEARCH BAR */}

<div
  className="
    mb-6
  "
>

  <input

    type="text"

    placeholder="
      Search Resident ID,
      Name or Flat Number
    "

    value={search}

    onChange={(e)=>

      setSearch(
        e.target.value
      )

    }

    className="
      w-full

      p-4

      rounded-2xl

      border

      bg-white/70
      backdrop-blur-xl

      shadow-md

      outline-none

      focus:ring-2
      focus:ring-blue-500
    "
  />

</div>


      {/* ANALYTICS */}

      <div
        className="
          grid
          grid-cols-3
          gap-6
          mb-8
        "
      >

        {/* TOTAL */}

        <div
          className="
bg-white/70 backdrop-blur-xl border border-white/40
            rounded-3xl
            p-6
            shadow-lg
          "
        >

          <p className="text-gray-500">
            Total Residents
          </p>

          <h2
            className="
              text-5xl
              font-bold
              mt-3
            "
          >
            {residents.length}
          </h2>

        </div>


        {/* APPROVED */}

        <div
          className="
            bg-white
            rounded-3xl
            p-6
            shadow-lg
          "
        >

          <p className="text-gray-500">
            Approved
          </p>

          <h2
            className="
              text-5xl
              font-bold
              text-green-500
              mt-3
            "
          >

            {

              residents.filter(

                (item) =>

                  item.status ===
                  "Approved"

              ).length

            }

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

          <p className="text-gray-500">
            Pending
          </p>

          <h2
            className="
              text-5xl
              font-bold
              text-yellow-500
              mt-3
            "
          >

            {

              residents.filter(

                (item) =>

                  item.status ===
                  "Pending"

              ).length

            }

          </h2>

        </div>

      </div>


      {/* RESIDENT TABLE */}

      <div className="space-y-5">

        {filteredResidents.map((item) => (

          <div

  key={item._id}

  onClick={() =>

  navigate(

    `/admin/resident/${item.residentId}`

  )

}

  className="
    bg-white/70
    backdrop-blur-xl

    rounded-3xl

    p-6

    shadow-xl

    border
    border-white/40

    grid
    grid-cols-12

    gap-6
    items-center

    hover:scale-[1.01]
    transition-all
    duration-300
  "
>

  {/* PROFILE */}

  <div className="col-span-3 flex items-center gap-4">

    <div
      className="
        h-14
        w-14

        rounded-2xl

        bg-gradient-to-r
        from-blue-500
        to-violet-500

        flex
        items-center
        justify-center

        text-white
        text-xl
        font-bold

        shadow-lg
      "
    >
      {item.residentName?.charAt(0)}
    </div>

    <div>

      <h2
        className="
          text-lg
          font-bold
          text-slate-800
        "
      >
        {item.residentName}
      </h2>

      <p
        className="
          text-sm
          text-slate-500
        "
      >
        {item.email}
      </p>

      <p
        className="
          text-xs
          font-semibold
          text-blue-600
          mt-1
        "
      >
        {item.residentId || "ID Pending"}
      </p>

    </div>

  </div>


  {/* PHONE */}

  <div className="col-span-2">

    <p className="text-sm text-slate-400">
      Phone
    </p>

    <h2
      className="
        font-semibold
        text-slate-700
      "
    >
      {item.phone}
    </h2>

  </div>


  {/* FLAT */}

  <div className="col-span-2">

    <p className="text-sm text-slate-400">
      Flat
    </p>

    <h2
      className="
        font-semibold
        text-slate-700
      "
    >
      {item.block}-{item.flatNumber}
    </h2>

  </div>


  {/* FLAT TYPE */}

  <div className="col-span-1">

    <p className="text-sm text-slate-400">
      Type
    </p>

    <h2
      className="
        font-semibold
        text-slate-700
      "
    >
      {item.flatType}
    </h2>

  </div>


  {/* STATUS */}

  <div className="col-span-2">

    <span
      className={`
        px-4
        py-2

        rounded-full

        text-sm
        font-bold

        ${
          item.status === "Approved"
          ? "bg-green-100 text-green-600"
          : item.status === "Rejected"
          ? "bg-red-100 text-red-500"
          : "bg-yellow-100 text-yellow-600"
        }
      `}
    >
      {item.status}
    </span>

  </div>


  {/* ACTIONS */}

  <div
    className="
      col-span-2

      flex
      gap-2
    "
  >

    <button

      onClick={() =>
        updateResidentStatus(
          item._id,
          "Approved"
        )
      }

      className="
        flex-1

        bg-green-500
        hover:bg-green-600

        text-white

        py-2

        rounded-xl

        font-semibold

        transition-all
      "
    >
      Approve
    </button>

    <button

      onClick={() =>
        updateResidentStatus(
          item._id,
          "Rejected"
        )
      }

      className="
        flex-1

        bg-yellow-500
        hover:bg-yellow-600

        text-white

        py-2

        rounded-xl

        font-semibold

        transition-all
      "
    >
      Reject
    </button>

  </div>

</div>
        ))}

      </div>

    </div>

  );

}