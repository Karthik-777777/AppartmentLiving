import {
  FiBell,
  FiSearch,
  FiAlertTriangle,
  FiTool,
  FiCheckCircle,
  FiCreditCard,
} from "react-icons/fi";

import {
  useEffect,
  useState,
} from "react";

import ChartsSection from "../components/dashboard/ChartsSection";
import AdminSidebar from "../components/AdminSidebar";
export default function AdminDashboard() {
  const [complaints, setComplaints] = useState([]);
const [maintenance, setMaintenance] = useState([]);
const [payments, setPayments] = useState([]);
const [time, setTime] =
  useState("");
const [pendingUsers, setPendingUsers] = useState([]);
const [notices, setNotices] = useState([]);
useEffect(() => {

  const updateTime = () => {

    const now = new Date();

    const formatted =
      now.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      });

    setTime(formatted);

  };

  updateTime();

  const interval =
    setInterval(updateTime, 1000);

  return () => clearInterval(interval);

}, []);
useEffect(() => {

  fetch("http://localhost:4000/api/complaints")
    .then((res) => res.json())
    .then((data) => {
      if(Array.isArray(data)) {
        setComplaints(data);
      }
    });
      fetch("http://localhost:4000/api/residents")
    .then((res) => res.json())
    .then((data) => {

      if(Array.isArray(data)) {

        setPendingUsers(
          data.filter(
            (item) =>
              item.status === "Pending"
          )
        );

      }

    });

  fetch("http://localhost:4000/api/notices")
    .then((res) => res.json())
    .then((data) => {

      if(Array.isArray(data)) {
        setNotices(data);
      }

    });

  fetch("http://localhost:4000/api/maintenance")
    .then((res) => res.json())
    .then((data) => {
      if(Array.isArray(data)) {
        setMaintenance(data);
      }
    });

  fetch("http://localhost:4000/api/payments")
    .then((res) => res.json())
    .then((data) => {
      if(Array.isArray(data)) {
        setPayments(data);
      }
    });

}, []);



const openComplaints =
  complaints.filter(
    (item) => item.status !== "Resolved"
  ).length;

const urgentComplaints =
  complaints.filter(
    (item) => item.priority === "Urgent"
  ).length;

const inProgressMaintenance =
  maintenance.filter(
    (item) => item.status === "In Progress"
  ).length;

const resolvedComplaints =
  complaints.filter(
    (item) => item.status === "Resolved"
  ).length;

const pendingPayments =
  payments.filter(
    (item) => item.status !== "Paid"
  );

const totalPendingAmount =
  pendingPayments.reduce(
    (total, item) =>
      total + Number(item.amount || 0),
    0
  );

  return (
    
     <div className="flex h-screen bg-[#f5f7fb] overflow-hidden">

    <AdminSidebar />

    
      
      {/* MAIN CONTENT */}
      <div
className="flex-1 ml-[260px] p-8 overflow-y-auto relative bg-cover bg-center"
        style={{
backgroundImage: "url('/admin-bg.png')",        }}
      >

        {/* OVERLAY */}
        <div className="absolute inset-0 bg-white/88 backdrop-blur-[1px]"></div>

        {/* CONTENT */}
        <div className="relative z-10">

          {/* HEADER */}
         {/* PREMIUM ADMIN NAVBAR */}

<div
  className="
    relative
    overflow-hidden
    flex
    items-center
    justify-between
    mb-8

    rounded-[28px]

    px-7
    py-5

    border
    border-white/30

    bg-white/45
    backdrop-blur-[20px]

    shadow-[0_12px_40px_rgba(15,23,42,0.08)]
  "
>

  {/* LEFT */}
  <div className="relative z-10">

    <h1
      className="
        text-[32px]
        font-black
        tracking-[-1px]
        leading-tight

        bg-gradient-to-r
        from-slate-900
        via-blue-900
        to-violet-700

        bg-clip-text
        text-transparent
      "
    >
{
  (() => {

    const hour =
      new Date().getHours();

    let greeting = "";

    if(hour < 12) {

      greeting = "Good morning";

    }

    else if(hour < 17) {

      greeting = "Good afternoon";

    }

    else {

      greeting = "Good evening";

    }

    return `${greeting}, ${
      JSON.parse(
        localStorage.getItem("user")
      )?.name || "Admin"
    } 👋`;

  })()

} </h1>

    <p
      className="
        mt-2
        text-[14px]
        text-slate-600
        font-medium
      "
    >
      Manage residents, complaints & payments
    </p>

  </div>

  {/* RIGHT */}
  <div
    className="
      relative
      z-10
      flex
      items-center
      gap-4
    "
  >

    {/* SEARCH */}
    <div
      className="
        flex
        items-center
        gap-2
        bg-white/80
        px-4
        py-2.5
        rounded-2xl
        shadow-sm
      "
    >

      <FiSearch className="text-slate-400" />

      <input
        type="text"
        placeholder="Search..."
        className="
          bg-transparent
          outline-none
          text-sm
          text-slate-700
          placeholder:text-slate-400
          w-[160px]
        "
      />

    </div>

    {/* LIVE TIME */}
    <div
      className="
        bg-gradient-to-r
        from-blue-600
        to-violet-600
        text-white

        px-4
        py-2.5

        rounded-2xl
        shadow-lg

        font-semibold
        text-sm
      "
    >
      {time}
    </div>

    {/* NOTIFICATION */}
    <button
      className="
        w-11
        h-11

        rounded-2xl
        bg-white/80

        shadow-md

        flex
        items-center
        justify-center

        hover:scale-105
        transition-all
      "
    >

      <FiBell
        className="
          text-slate-700
          text-[18px]
        "
      />

    </button>

    {/* PROFILE */}
    <div
      className="
        w-12
        h-12

        rounded-2xl

        bg-gradient-to-r
        from-blue-600
        to-violet-600

        text-white

        flex
        items-center
        justify-center

        font-bold
        text-[16px]

        shadow-xl
      "
    >
{
  JSON.parse(localStorage.getItem("user"))
    ?.name?.charAt(0)
}    </div>

  </div>

</div>

          {/* TOP STATS */}
        {/* PREMIUM STATS */}

<div className="grid grid-cols-4 gap-5 mb-8">

  {/* CARD 1 */}
  <div
    className="
      relative
      overflow-hidden
      min-h-[190px]

      rounded-[22px]

      bg-white/50
      backdrop-blur-xl

      border
      border-white/30

      p-4

      shadow-[0_10px_30px_rgba(15,23,42,0.06)]

      hover:-translate-y-1
      hover:shadow-[0_18px_40px_rgba(37,99,235,0.12)]

      transition-all
      duration-300
    "
  >

    <div
      className="
        absolute
        top-0
        right-0

        w-24
        h-24

        bg-red-400/10
        rounded-full
        blur-3xl
      "
    />

    <div className="relative z-10">

      <div
        className="
          w-11
          h-11

          rounded-xl

          bg-gradient-to-r
          from-red-500
          to-rose-500

          text-white

          flex
          items-center
          justify-center

          shadow-md
        "
      >
        <FiAlertTriangle size={18} />
      </div>

      <p className="text-slate-500 text-[13px] mt-3">
        Open complaints
      </p>

      <h2
        className="
          text-[26px]
          font-black
          text-slate-900
          mt-1
        "
      >
{openComplaints}
      </h2>

      <p className="text-red-500 text-[13px] mt-1 font-medium">
{urgentComplaints} urgent
      </p>

    </div>

  </div>

  {/* CARD 2 */}
  <div
    className="
      relative
      overflow-hidden
      min-h-[190px]

      rounded-[22px]

      bg-white/50
      backdrop-blur-xl

      border
      border-white/30

      p-4

      shadow-[0_10px_30px_rgba(15,23,42,0.06)]

      hover:-translate-y-1
      hover:shadow-[0_18px_40px_rgba(251,191,36,0.12)]

      transition-all
      duration-300
    "
  >

    <div
      className="
        absolute
        top-0
        right-0

        w-24
        h-24

        bg-yellow-400/10
        rounded-full
        blur-3xl
      "
    />

    <div className="relative z-10">

      <div
        className="
          w-11
          h-11

          rounded-xl

          bg-gradient-to-r
          from-yellow-400
          to-orange-500

          text-white

          flex
          items-center
          justify-center

          shadow-md
        "
      >
        <FiTool size={18} />
      </div>

      <p className="text-slate-500 text-[13px] mt-3">
        In progress
      </p>

      <h2
        className="
          text-[26px]
          font-black
          text-slate-900
          mt-1
        "
      >
{inProgressMaintenance}
      </h2>

      <p className="text-orange-500 text-[13px] mt-1 font-medium">
        Maintenance
      </p>

    </div>

  </div>

  {/* CARD 3 */}
  <div
    className="
      relative
      overflow-hidden
      min-h-[190px]

      rounded-[22px]

      bg-white/50
      backdrop-blur-xl

      border
      border-white/30

      p-4

      shadow-[0_10px_30px_rgba(15,23,42,0.06)]

      hover:-translate-y-1
      hover:shadow-[0_18px_40px_rgba(34,197,94,0.12)]

      transition-all
      duration-300
    "
  >

    <div
      className="
        absolute
        top-0
        right-0

        w-24
        h-24

        bg-green-400/10
        rounded-full
        blur-3xl
      "
    />

    <div className="relative z-10">

      <div
        className="
          w-11
          h-11

          rounded-xl

          bg-gradient-to-r
          from-green-500
          to-emerald-500

          text-white

          flex
          items-center
          justify-center

          shadow-md
        "
      >
        <FiCheckCircle size={18} />
      </div>

      <p className="text-slate-500 text-[13px] mt-3">
        Resolved this week
      </p>

      <h2
        className="
          text-[26px]
          font-black
          text-slate-900
          mt-1
        "
      >
{resolvedComplaints}
      </h2>

      <p className="text-green-500 text-[13px] mt-1 font-medium">
        Excellent progress
      </p>

    </div>

  </div>

  {/* CARD 4 */}
  <div
    className="
      relative
      overflow-hidden
      min-h-[190px]

      rounded-[22px]

      bg-white/50
      backdrop-blur-xl

      border
      border-white/30

      p-4

      shadow-[0_10px_30px_rgba(15,23,42,0.06)]

      hover:-translate-y-1
      hover:shadow-[0_18px_40px_rgba(37,99,235,0.12)]

      transition-all
      duration-300
    "
  >

    <div
      className="
        absolute
        top-0
        right-0

        w-24
        h-24

        bg-blue-400/10
        rounded-full
        blur-3xl
      "
    />

    <div className="relative z-10">

      <div
        className="
          w-11
          h-11

          rounded-xl

          bg-gradient-to-r
          from-blue-600
          to-violet-600

          text-white

          flex
          items-center
          justify-center

          shadow-md
        "
      >
        <FiCreditCard size={18} />
      </div>

      <p className="text-slate-500 text-[13px] mt-3">
        Dues overdue
      </p>

      <h2
        className="
          text-[24px]
          font-black
          text-slate-900
          mt-1
        "
      >
₹{totalPendingAmount}
      </h2>

      <p className="text-blue-500 text-[13px] mt-1 font-medium">
        Pending amount
      </p>

    </div>

  </div>

</div>
<ChartsSection
  complaints={complaints}
  payments={payments}
/>
          {/* PENDING USERS */}
<div className="bg-white rounded-2xl p-6 shadow-sm mb-8">

  <div className="flex items-center justify-between mb-6">

    <h2 className="text-[17px] font-semibold">
      Pending User Approvals
    </h2>

    <button
  className="
    bg-gradient-to-r
    from-blue-600
    to-violet-600
    text-white
    px-5
    py-2.5
    rounded-2xl
    text-sm
    font-semibold
    tracking-wide
    shadow-lg
    hover:shadow-2xl
    hover:scale-[1.03]
    transition-all
    duration-300
  "
>
  View All
</button>

  </div>

  <div className="space-y-5">

   <div className="space-y-5">

  {pendingUsers.map((user) => (

    <div
      key={user._id}
      className="
        grid
        grid-cols-5
        items-center
        border-b
        pb-4
        gap-4
      "
    >

      <div>

        <p className="text-[15px] font-medium">
          {user.name}
        </p>

        <p className="text-[13px] text-gray-500">
          {user.email}
        </p>

      </div>

      <p>
        {user.block}-{user.flatNumber}
      </p>

      <p>
        {user.ownerType}
      </p>

      <span
        className="
          bg-yellow-100
          text-yellow-700
          text-xs
          px-3
          py-1
          rounded-full
          w-fit
        "
      >
        Pending
      </span>

      <div className="flex gap-3">

        <button
          className="
            bg-gradient-to-r
            from-green-500
            to-emerald-500
            text-white
            px-4
            py-2
            rounded-xl
          "
        >
          Accept
        </button>

        <button
          className="
            bg-gradient-to-r
            from-red-500
            to-rose-600
            text-white
            px-4
            py-2
            rounded-xl
          "
        >
          Reject
        </button>

      </div>

    </div>

  ))}

</div>
  </div>

</div>
             {/* LOWER GRID */}
<div className="grid grid-cols-2 gap-6 mb-8">

  {/* MAINTENANCE REQUESTS */}
<div
  className="
    bg-white/30
    backdrop-blur-2xl

    border
    border-white/30

    rounded-[32px]

    p-8

    shadow-[0_10px_40px_rgba(15,23,42,0.06)]

    hover:-translate-y-1
    hover:shadow-[0_20px_50px_rgba(79,70,229,0.14)]

    transition-all
    duration-300
  "
>

  {/* TOP */}
  <div
    className="
      flex
      items-center
      justify-between

      mb-8
    "
  >

    <h2
      className="
        text-[20px]
        font-bold
        text-slate-800
      "
    >
      Maintenance requests
    </h2>

    <button
      className="
        text-indigo-500
        font-semibold
        text-sm

        hover:translate-x-1

        transition-all
        duration-300
      "
    >
      View all →
    </button>

  </div>


  {/* LIVE ITEMS */}
  <div className="space-y-4">

    {maintenance.slice(0, 3).map((item) => (

      <div
        key={item._id}

        className="
          flex
          justify-between
          items-center

          p-4

          rounded-2xl

          hover:bg-white/50

          transition-all
          duration-300
        "
      >

        <div>

          <p
            className="
              text-[15px]
              font-semibold
              text-slate-800
            "
          >
            {item.issue}
          </p>

          <p
            className="
              text-[13px]
              text-slate-500
              mt-1
            "
          >
            {item.priority} priority
          </p>

        </div>

        <span
          className={`
            text-[12px]
            font-semibold

            px-4
            py-1.5

            rounded-full

            ${
              item.priority === "High"
                ? "bg-red-100/80 text-red-500"

                : item.priority === "Medium"
                ? "bg-yellow-100/80 text-yellow-700"

                : "bg-green-100/80 text-green-600"
            }
          `}
        >
          {item.priority}
        </span>

      </div>

    ))}

  </div>

</div>
  {/* NOTICE BOARD */}
<div
  className="
    bg-white/50
    backdrop-blur-xl

    border
    border-white/30

    rounded-[28px]

    p-6

    shadow-[0_10px_40px_rgba(15,23,42,0.06)]

    hover:shadow-[0_20px_60px_rgba(15,23,42,0.10)]

    transition-all
    duration-300
  "
>

  {/* TOP */}
  <div
    className="
      flex
      justify-between
      items-center

      mb-6
    "
  >

    <h2
      className="
        text-[22px]
        font-bold
        text-slate-900

        tracking-[-0.5px]
      "
    >
      Notice board
    </h2>

    <button
      className="
        text-sm
        font-semibold

        text-transparent
        bg-clip-text

        bg-gradient-to-r
        from-blue-600
        to-violet-600

        hover:scale-[1.03]

        transition-all
        duration-300
      "
    >
      View all →
    </button>

  </div>


  {/* LIVE NOTICES */}
  <div className="space-y-4">

    {notices.slice(0, 3).map((notice) => (

      <div
        key={notice._id}

        className="
          p-4

          rounded-2xl

          hover:bg-white/40

          transition-all
          duration-300
        "
      >

        <p
          className="
            text-[15px]
            font-semibold
            text-slate-800
          "
        >
          {notice.title}
        </p>

        <p
          className="
            text-[13px]
            text-slate-500
            mt-1
          "
        >
          {notice.message}
        </p>

      </div>

    ))}

  </div>

</div>

</div>

{/* OVERDUE PAYMENTS */}
<div
  className="
    bg-white/50
    backdrop-blur-xl

    border
    border-white/30

    rounded-[28px]

    p-6

    shadow-[0_10px_40px_rgba(15,23,42,0.06)]

    hover:shadow-[0_20px_60px_rgba(15,23,42,0.10)]

    transition-all
    duration-300
  "
>

  {/* TOP */}
  <div
    className="
      flex
      justify-between
      items-center

      mb-6
    "
  >

    <h2
      className="
        text-[22px]
        font-bold
        text-slate-900

        tracking-[-0.5px]
      "
    >
      Overdue payments
    </h2>

    <button
      className="
        text-sm
        font-semibold

        text-transparent
        bg-clip-text

        bg-gradient-to-r
        from-blue-600
        to-violet-600

        hover:scale-[1.03]

        transition-all
        duration-300
      "
    >
      View all →
    </button>

  </div>


  {/* LIVE ROWS */}
  <div className="space-y-3">

    {payments
      .filter(
        (item) =>
          item.status !== "Paid"
      )
      .slice(0, 3)
      .map((item) => (

        <div
          key={item._id}

          className="
            grid
            grid-cols-4
            items-center

            p-4

            rounded-2xl

            hover:bg-white/50

            transition-all
            duration-300
          "
        >

          {/* USER */}
          <div>

            <p
              className="
                text-[15px]
                font-semibold
                text-slate-800
              "
            >
              {item.residentName}
            </p>

            <p
              className="
                text-[13px]
                text-slate-500
                mt-1
              "
            >
              Flat {item.flatNumber}
            </p>

          </div>


          {/* AMOUNT */}
          <p
            className="
              text-[14px]
              font-medium
            "
          >
            ₹{item.amount}
          </p>


          {/* DUE DATE */}
          <p
            className="
              text-[14px]
              text-slate-500
            "
          >
            Due {
              new Date(
                item.dueDate
              ).toLocaleDateString()
            }
          </p>


          {/* STATUS */}
          <span
            className={`
              text-[12px]
              font-semibold

              px-4
              py-1.5

              rounded-full

              w-fit

              ${
                item.status === "Overdue"

                  ? "bg-red-100/80 text-red-500"

                  : "bg-gray-200/80 text-gray-600"
              }
            `}
          >
            {item.status}
          </span>

        </div>

      ))}

  </div>

</div>

                </div>

              </div>

            </div>

          
  );
}