import React, {
  useState,
  useEffect
} from "react";

export default function AdminProfile() {



const [residentCount,
setResidentCount] =
  useState(0);

const [complaintCount,
setComplaintCount] =
  useState(0);

const [pendingPayments,
setPendingPayments] =
  useState(0);

const [revenue,
setRevenue] =
  useState(0);


  const admin =
    JSON.parse(
      localStorage.getItem("user")
    );

  const [editMode, setEditMode] =
    useState(false);

  const [name, setName] =
    useState(admin?.name || "");

  const [phone, setPhone] =
    useState(admin?.phone || "");

  const [apartmentName,
  setApartmentName] =
    useState(
      admin?.apartmentName ||
      "Apartment Living"
    );

  const saveProfile = () => {

    const updatedAdmin = {

      ...admin,

      name,
      phone,
      apartmentName,

    };

    localStorage.setItem(
      "user",
      JSON.stringify(updatedAdmin)
    );

    setEditMode(false);

    window.location.reload();

  };

  useEffect(() => {

  // RESIDENTS

  fetch(
    "http://localhost:4000/api/residents"
  )
    .then((res) => res.json())
    .then((data) => {

      if(Array.isArray(data)) {

        setResidentCount(
          data.length
        );

      }

    });


  // COMPLAINTS

  fetch(
    "http://localhost:4000/api/complaints"
  )
    .then((res) => res.json())
    .then((data) => {

      if(Array.isArray(data)) {

        setComplaintCount(
          data.length
        );

      }

    });


  // PAYMENTS

  fetch(
    "http://localhost:4000/api/payments"
  )
    .then((res) => res.json())
    .then((data) => {

      if(Array.isArray(data)) {

        // PENDING

        const pending =
          data.filter(

            (item) =>

              item.status !== "Paid"

          );

        setPendingPayments(
          pending.length
        );


        // REVENUE

        const total =
          data.reduce(

            (acc, item) =>

              acc +
              Number(
                item.amount || 0
              ),

            0
          );

        setRevenue(total);

      }

    });

}, []);

  return (

    <div
      className="
        min-h-screen
        p-8

        bg-gradient-to-br
        from-slate-100
        via-blue-50
        to-violet-100
      "
    >

      <div
        className="
          max-w-6xl
          mx-auto

          bg-white/50
          backdrop-blur-xl

          rounded-[32px]

          p-8

          shadow-[0_20px_80px_rgba(15,23,42,0.10)]

          border
          border-white/40
        "
      >

        {/* HEADER */}

        <div
          className="
            bg-gradient-to-r
            from-blue-600
            via-violet-600
            to-cyan-500

            rounded-[30px]

            p-8
            mb-10

            text-white

            flex
            flex-col
            lg:flex-row

            justify-between
            items-center

            gap-8
          "
        >

          {/* LEFT */}

          <div className="flex items-center gap-6">

            <div
              className="
                w-32
                h-32

                rounded-full

                border-4
                border-white

                bg-white/20

                shadow-[0_0_40px_rgba(255,255,255,0.3)]

                flex
                items-center
                justify-center

                text-5xl
                font-bold
              "
            >
              {name?.charAt(0)}
            </div>

            <div>

              {

                editMode ? (

                  <input

                    value={name}

                    onChange={(e) =>
                      setName(
                        e.target.value
                      )
                    }

                    className="
                      bg-white/20

                      border
                      border-white/30

                      px-4
                      py-2

                      rounded-2xl

                      outline-none

                      text-3xl
                      font-bold
                    "
                  />

                ) : (

                  <h1 className="text-4xl font-bold">
                    {name}
                  </h1>

                )

              }

              <p className="mt-3 text-white/80">
                {admin?.designation}
              </p>

              <div className="flex gap-3 mt-4">

                <span
                  className="
                    bg-white/20

                    px-4
                    py-1

                    rounded-full

                    text-sm
                  "
                >
                  {admin?.role}
                </span>

                <span
                  className="
                    bg-green-400/20
                    text-green-100

                    px-4
                    py-1

                    rounded-full

                    text-sm
                  "
                >
                  Active
                </span>

              </div>

            </div>

          </div>


          {/* RIGHT */}

          <button

            onClick={() =>
              setEditMode(
                !editMode
              )
            }

            className="
              bg-white
              text-blue-600

              px-6
              py-3

              rounded-2xl

              font-semibold

              hover:scale-[1.03]

              transition-all
              duration-300
            "
          >

            {
              editMode
                ? "Cancel"
                : "Edit Profile"
            }

          </button>

        </div>


        {/* DETAILS */}

        <div
          className="
            grid
            grid-cols-1
            md:grid-cols-2

            gap-6
          "
        >

          {/* EMAIL */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              📧 Email
            </p>

            <h2 className="font-semibold mt-2">
              {admin?.email}
            </h2>

          </div>


          {/* PHONE */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              📱 Phone
            </p>

            {

              editMode ? (

                <input

                  value={phone}

                  onChange={(e) =>
                    setPhone(
                      e.target.value
                    )
                  }

                  className="
                    w-full
                    mt-2

                    border
                    border-slate-200

                    rounded-2xl

                    px-4
                    py-3

                    outline-none
                  "
                />

              ) : (

                <h2 className="font-semibold mt-2">
                  {
                    phone ||
                    "Not added"
                  }
                </h2>

              )

            }

          </div>


          {/* APARTMENT */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              🏢 Apartment
            </p>

            {

              editMode ? (

                <input

                  value={
                    apartmentName
                  }

                  onChange={(e) =>
                    setApartmentName(
                      e.target.value
                    )
                  }

                  className="
                    w-full
                    mt-2

                    border
                    border-slate-200

                    rounded-2xl

                    px-4
                    py-3

                    outline-none
                  "
                />

              ) : (

                <h2 className="font-semibold mt-2">
                  {apartmentName}
                </h2>

              )

            }

          </div>


          {/* ROLE */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              🛡️ Role
            </p>

            <h2 className="font-semibold mt-2">
              {admin?.role}
            </h2>

          </div>


          {/* STATUS */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              ✅ Status
            </p>

            <span
              className="
                inline-block

                mt-3

                bg-green-100
                text-green-600

                px-4
                py-1

                rounded-full

                text-sm
                font-semibold
              "
            >
              {admin?.status || "Active"}
            </span>

          </div>


          {/* LAST LOGIN */}

          <div
            className="
              bg-white/70
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              border
              border-white/40

              hover:scale-[1.02]

              transition-all
              duration-300
            "
          >

            <p className="text-sm text-slate-500">
              🕒 Last Login
            </p>

            <h2 className="font-semibold mt-2">

              {

                admin?.lastLogin

                  ? new Date(
                      admin.lastLogin
                    ).toLocaleString()

                  : "No login data"

              }

            </h2>

          </div>

        </div>


        {/* SAVE BUTTON */}

        {

          editMode && (

            <div className="mt-8 flex justify-end">

              <button

                onClick={saveProfile}

                className="
                  bg-gradient-to-r
                  from-blue-600
                  to-violet-600

                  text-white

                  px-8
                  py-3

                  rounded-2xl

                  font-semibold

                  shadow-lg

                  hover:scale-[1.03]

                  transition-all
                  duration-300
                "
              >
                Save Changes
              </button>

            </div>

          )

        }


        {/* QUICK STATS */}

        <div
          className="
            grid
            grid-cols-2
            md:grid-cols-4

            gap-5
            mt-10
          "
        >

          <div className="bg-white rounded-3xl p-5 shadow-lg">

            <p className="text-slate-500 text-sm">
              Residents
            </p>

            <h1 className="text-3xl font-bold mt-2">
{residentCount}
            </h1>

          </div>


          <div className="bg-white rounded-3xl p-5 shadow-lg">

            <p className="text-slate-500 text-sm">
              Complaints
            </p>

            <h1 className="text-3xl font-bold mt-2">
              {complaintCount}
            </h1>

          </div>


          <div className="bg-white rounded-3xl p-5 shadow-lg">

            <p className="text-slate-500 text-sm">
              Revenue
            </p>

            <h1 className="text-3xl font-bold mt-2">
₹{revenue}
            </h1>

          </div>


          <div className="bg-white rounded-3xl p-5 shadow-lg">

            <p className="text-slate-500 text-sm">
              Pending
            </p>

            <h1 className="text-3xl font-bold mt-2">
{pendingPayments}            </h1>

          </div>

        </div>

      </div>

    </div>

  );

}