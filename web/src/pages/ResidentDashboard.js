import axios from "axios";
import React, {
  useEffect,
  useState,
} from "react";

import {
  FiBell,
  FiSearch,
  FiAlertCircle,
  FiTool,
  FiCheckCircle,
  FiCreditCard,
  FiActivity,
} from "react-icons/fi";
import ComplaintChart from "../components/charts/ComplaintChart";

import PaymentChart from "../components/charts/PaymentChart";

import StatusPieChart from "../components/charts/StatusPieChart";

import GoogleNearbyMap from "../components/dashboard/GoogleNearbyMap";


  export default function ResidentDashboard() {
    const [formData, setFormData] =
useState({

  residentName: "",

  flatNumber: "",

  title: "",

  description: "",

  category: "General",

  priority: "Medium",

});

  const [complaints, setComplaints] =
  useState([]);
  const [payments, setPayments] =
useState([]);


const [maintenance, setMaintenance] =
useState([]);



const [notices, setNotices] =
useState([]);


const resident =


JSON.parse(

 localStorage.getItem("resident")

) || {};
console.log(resident);

const paidCount =
  payments.filter(
    (p) => p.status === "Paid"
  ).length;

const pendingCount =
  payments.filter(
    (p) => p.status === "Pending"
  ).length;

const overdueCount =
  payments.filter(
    (p) => p.status === "Overdue"
  ).length;

const totalOverdue =
  payments
    .filter(
      (p) =>
        p.status !== "Paid"
    )
    .reduce(
      (acc, item) =>
        acc + item.totalAmount,
      0
    );
  

  
  console.log(complaints);
  const handleChange = (e) => {

  setFormData({

    ...formData,

    [e.target.name]:
    e.target.value,

  });

};
const handleSubmit =
async (e) => {

  e.preventDefault();

  try {

    const response =
    await fetch(

      "http://localhost:4000/api/complaints",

      {

        method: "POST",

        headers: {

          "Content-Type":
          "application/json",

        },

      body: JSON.stringify({

  ...formData,

  residentId:
  resident.residentId,

  residentName:
  resident.residentName,

  flatNumber:
  resident.flatNumber

})

      }

    );

    const data =
    await response.json();

    // ADD NEW COMPLAINT TO UI
    setComplaints([
      data,
      ...complaints
    ]);

    // RESET FORM
    setFormData({

      residentName: "",

      flatNumber: "",

      title: "",

      description: "",

      category: "General",

      priority: "Medium",

    });

    alert(
      "Complaint submitted successfully"
    );

  } catch (err) {

    console.log(err);

  }

};


  const [time, setTime] = useState("");

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
   const currentHour = new Date().getHours();

let weatherCondition = "Sunny";
let weatherEmoji = "☀️";
let temp = 31;

if (currentHour >= 18) {

  weatherCondition = "Cloudy";
  weatherEmoji = "☁️";
  temp = 27;

}

if (currentHour >= 21) {

  weatherCondition = "Night breeze";
  weatherEmoji = "🌙";
  temp = 25;

}
const currentTime =
  new Date().getHours();

let greeting = "Good morning";

if (currentTime >= 12) {

  greeting = "Good afternoon";

}

if (currentTime >= 18) {

  greeting = "Good evening";

}
const [darkMode, setDarkMode] =
  useState(false);
  const [search, setSearch] =
  useState("");
  useEffect(() => {

  if (darkMode) {

    document.documentElement.classList.add("dark");

  } else {

    document.documentElement.classList.remove("dark");

  }

}, [darkMode]);


 
 const residentName =

resident.residentName || "Resident";

const flatNumber =

resident.flatNumber || "Flat";

const apartmentName =
"Apartment Living";

const fetchComplaints =
async () => {

  try {

    const resident =
    JSON.parse(
      localStorage.getItem(
        "resident"
      )
    ) || {};

    const res =
    await axios.get(

`http://localhost:4000/api/complaints/resident/${resident.residentId}`

    );

    setComplaints(res.data);

  }

  catch(err){

    console.log(err);

  }

};
const fetchMaintenance = async () => {

try {

const resident =
JSON.parse(
localStorage.getItem("resident")
) || {};

const res =
await axios.get(

`http://localhost:4000/api/maintenance/resident/${resident.residentId}`

);

console.log(
"Maintenance Data:",
res.data
);

setMaintenance(res.data);

}

catch(err){

console.log(err);

}

};
const fetchPayments =
async () => {

  try {

    const resident =
    JSON.parse(
      localStorage.getItem(
        "resident"
      )
    ) || {};

    const res =
    await axios.get(

`http://localhost:4000/api/payments/resident/${resident.residentId}`

    );

    setPayments(res.data);

  }

  catch(err){

    console.log(err);

  }

};
const fetchNotices =
async () => {

  try {

    const res =
    await axios.get(

"http://localhost:4000/api/notices"

    );

    setNotices(res.data);

  }

  catch(err){

    console.log(err);

  }

};

useEffect(() => {

  fetchComplaints();

  fetchMaintenance();

  fetchPayments();

  fetchNotices();

}, []);




  return (

    <div className="flex min-h-screen bg-[#F5F7FB]
dark:bg-[#0F172A]">

      

      {/* MAIN */}
      <div
        className="
          flex-1
          overflow-y-auto
          p-8
          relative
          bg-cover
          bg-center
        "
        style={{
          backgroundImage:
            "url('/resident-bg.png')",
        }}
      >

        {/* OVERLAY */}
        <div className="absolute inset-0 bg-white/88 backdrop-blur-[2px]" />

        <div className="relative z-10">

          {/* TOP NAVBAR */}
          <div
            className="
              flex
              items-center
              justify-between

              mb-6

bg-white/35
dark:bg-slate-900/50
              backdrop-blur-2xl

              border
border-white/30
dark:border-white/10
              rounded-[26px]

              px-7
              py-4

              shadow-[0_12px_40px_rgba(15,23,42,0.08)]
            "
          >

            {/* LEFT */}
            <div>

              <h1
                className="
                  text-[30px]
                  font-black
                  tracking-[-1px]

                  bg-gradient-to-r
                  from-slate-900
                  via-blue-900
                  to-violet-700

                  bg-clip-text
                  text-transparent
                "
              >
{greeting}, {residentName} 👋
              </h1>

              <p
                className="
                  text-[14px]
                  text-slate-600
                  mt-2
                  font-medium
                "
              >
                Welcome back to Apartment Living
              </p>

            </div>

            {/* RIGHT */}
            <div className="flex items-center gap-3">

              {/* SEARCH */}
              <div
                className="
                  flex
                  items-center
                  gap-2

bg-white/70
dark:bg-slate-800/80
                  px-4
                  py-2.5

                  rounded-2xl
                "
              >

                <FiSearch className="text-slate-400" />

                <input

  type="text"

  placeholder="Search..."

  value={search}

  onChange={(e) =>
    setSearch(e.target.value)
  }

  className="
    bg-transparent
    outline-none
    text-sm
    w-full
  "
/>

              </div>

              {/* TIME */}
              <div
                className="
                  bg-gradient-to-r
                  from-blue-600
                  to-violet-600

                  text-white

                  px-4
                  py-2.5

                  rounded-2xl

                  text-sm
                  font-semibold

                  shadow-lg
                "
              >
                {time}
              </div>
              <button
  onClick={() =>
    setDarkMode(!darkMode)
  }

  className="
    w-11
    h-11

    rounded-2xl

    bg-white/70
    dark:bg-slate-800/80

    shadow-md

    flex
    items-center
    justify-center

    hover:scale-105

    transition-all
  "
>

  {darkMode ? "☀️" : "🌙"}

</button>

              {/* BELL */}
              <button

  onClick={() =>
    window.location.href =
      "/resident/notices"
  }

  className="
    w-11
    h-11
    rounded-2xl
    bg-white/80
    flex
    items-center
    justify-center
    shadow-lg
    hover:scale-105
    transition-all
  "
>

  <FiBell />

</button>

              {/* PROFILE */}
              <button

  onClick={() =>
    window.location.href =
      "/resident/profile"
  }

  className="
    w-11
    h-11

    rounded-2xl

    bg-gradient-to-r
    from-blue-600
    to-violet-600

    text-white

    flex
    items-center
    justify-center

    font-bold

    shadow-xl

    hover:scale-105
    transition-all
  "
>
  {residentName?.charAt(0)}
</button>

            </div>

          </div>

          {/* APARTMENT INFO */}
          <p className="text-[14px] text-gray-500 mb-6">
            {apartmentName} • {flatNumber}
          </p>

          {/* STATS */}
          <div className="grid grid-cols-4 gap-5 mb-6">

            {[
            {
  title: "Open complaints",

  value:
    complaints.filter(
      (c) =>
        c.status === "Pending"
    ).length,

  sub: "Active tickets",

  color: "text-red-500",

  icon: <FiAlertCircle />,
},
{
  title: "In progress",
  value:
    complaints.filter(
      (c) => c.status === "In Progress"
    ).length,

  sub: "maintenance",
  color: "text-orange-500",
  icon: <FiTool />,
},

{
  title: "Resolved this week",
  value:
    complaints.filter(
      (c) => c.status === "Resolved"
    ).length,

  sub: "completed",
  color: "text-green-500",
  icon: <FiCheckCircle />,
},

{
  title: "Dues overdue",

  value: `₹ ${
    payments
      .filter(
        (p) => p.status !== "Paid"
      )
      .reduce(
        (total, item) =>
          total +
          Number(
            item.totalAmount || 0
          ),
        0
      )
  }`,

  sub: "pending",
  color: "text-red-500",
  icon: <FiCreditCard />,
},
            ].map((card, index) => (

              <div
                key={index}
                className="
                 bg-white/30
                dark:bg-slate-900/40
                  backdrop-blur-2xl

                  border
border-white/30
dark:border-white/10
                  rounded-[24px]

                  p-4

                  shadow-[0_10px_40px_rgba(15,23,42,0.06)]

                  hover:-translate-y-1
                  hover:scale-[1.01]
                  hover:shadow-[0_20px_60px_rgba(79,70,229,0.18)]

                  transition-all
                  duration-300
                "
              >

                <div
                  className="
                    w-10
                    h-10

                    rounded-2xl

bg-white/70
dark:bg-slate-800/80
                    flex
                    items-center
                    justify-center

                    text-[18px]

                    shadow-md
                    mb-4
                  "
                >
                  {card.icon}
                </div>

                <p className="text-[14px] text-gray-500">
                  {card.title}
                </p>

                <h2
                  className={`
                    text-[24px]
                    font-black
                    mt-2
                    ${card.color}
                  `}
                >
                  {card.value}
                </h2>

                <p className="text-[13px] text-gray-400 mt-1">
                  {card.sub}
                </p>

              </div>

            ))}

          </div>
          {/* ANALYTICS */}
<div className="mb-4">

  <h2
    className="
      text-[22px]
      font-black
      tracking-[-1px]
text-slate-800
dark:text-white    "
  >
    Analytics overview
  </h2>

  <p
    className="
      text-sm
text-slate-500
dark:text-slate-400
      mt-1
    "
  >
    Insights and complaint statistics
  </p>

</div>

<div className="grid grid-cols-3 gap-6 mb-6">

  <ComplaintChart />

<PaymentChart

  paid={paidCount}

  pending={pendingCount}

  overdue={overdueCount}

/>
<StatusPieChart

  paid={paidCount}

  pending={pendingCount}

  overdue={overdueCount}

/></div>
{/* QUICK ACTIONS */}
<div className="mb-6">

  <div className="flex items-center justify-between mb-4">

    <div>

      <h2
        className="
          text-[22px]
          font-black
          tracking-[-1px]
text-slate-800
dark:text-white        "
      >
        Quick actions
      </h2>

      <p className="text-sm text-slate-500 mt-1">
        Frequently used shortcuts
      </p>

    </div>

  </div>

  <div className="grid grid-cols-4 gap-5">

    {[
  {
    title: "Raise complaint",
    icon: "⚠️",
    path: "/resident/complaints",
  },

  {
    title: "Pay maintenance",
    icon: "💳",
    path: "/resident/payments",
  },

  {
    title: "Book amenities",
    icon: "🏊",
    path: "/resident/amenities",
  },

  {
    title: "Emergency help",
    icon: "🚨",
    path: "/resident/emergency",
  },
].map((item, index) => (

      <button
        key={index}
        className="
bg-white/30
dark:bg-slate-900/40
          backdrop-blur-2xl

          border
border-white/30
dark:border-white/10
          rounded-[24px]

          p-5

          text-left

          shadow-[0_10px_40px_rgba(15,23,42,0.06)]

          hover:-translate-y-1
          hover:scale-[1.02]
          hover:shadow-[0_20px_50px_rgba(79,70,229,0.14)]

          transition-all
          duration-300
        "
      >

        <div
          className="
            w-14
            h-14

            rounded-2xl

            bg-gradient-to-r
            from-blue-600
            to-violet-600

            flex
            items-center
            justify-center

            text-[24px]

            shadow-lg

            mb-4
          "
        >
          {item.icon}
        </div>

        <h3
          className="
            text-[16px]
            font-bold
text-slate-800
dark:text-white
          "
        >
          {item.title}
        </h3>

        <p
          className="
            text-sm
text-slate-500
dark:text-slate-400
            mt-2
          "
        >
          Open module
        </p>

      </button>

    ))}

  </div>

</div>
{/* WEATHER + ENVIRONMENT */}
<div className="grid grid-cols-3 gap-5 mb-6">

  {/* WEATHER CARD */}
  <div
    className="
      col-span-2

      relative
      overflow-hidden

      bg-gradient-to-br
      from-blue-600
      via-violet-600
      to-indigo-700

      rounded-[24px]

      p-5

      text-white

      shadow-[0_16px_50px_rgba(79,70,229,0.22)]

      hover:-translate-y-1
      hover:shadow-[0_22px_60px_rgba(79,70,229,0.28)]

      transition-all
      duration-300
    "
  >

    {/* GLOW */}
    <div
      className="
        absolute
        top-[-60px]
        right-[-60px]

        w-[180px]
        h-[180px]

        bg-white/10

        rounded-full

        blur-3xl
      "
    />

    <div className="relative z-10">

      {/* TOP */}
      <div className="flex justify-between items-start">

        <div>

          <p className="text-white/70 text-[13px]">
            Chennai, India
          </p>

          <h2
            className="
              text-[42px]
              font-black
              leading-none
              mt-2
            "
          >
            {temp}°
          </h2>

          <p className="mt-2 text-[16px] font-semibold">
            {weatherCondition}
          </p>

        </div>

        <div className="text-[58px]">
          {weatherEmoji}
        </div>

      </div>

      {/* STATS */}
      <div className="grid grid-cols-3 gap-3 mt-5">

        {/* HUMIDITY */}
        <div
          className="
            bg-white/10
            backdrop-blur-xl

            rounded-2xl

            p-3
          "
        >

          <p className="text-white/70 text-[12px]">
            Humidity
          </p>

          <h3 className="text-[22px] font-bold mt-1">
            {currentHour > 18 ? "72%" : "68%"}
          </h3>

        </div>

        {/* WIND */}
        <div
          className="
            bg-white/10
            backdrop-blur-xl

            rounded-2xl

            p-3
          "
        >

          <p className="text-white/70 text-[12px]">
            Wind
          </p>

          <h3 className="text-[22px] font-bold mt-1">
            {currentHour > 18
              ? "10 km/h"
              : "14 km/h"}
          </h3>

        </div>

        {/* AIR QUALITY */}
        <div
          className="
            bg-white/10
            backdrop-blur-xl

            rounded-2xl

            p-3
          "
        >

          <p className="text-white/70 text-[12px]">
            Air quality
          </p>

          <h3 className="text-[22px] font-bold mt-1">
            {currentHour > 18
              ? "Moderate"
              : "Good"}
          </h3>

        </div>

      </div>

    </div>

  </div>

  {/* EVENTS CARD */}
  <div
    className="
bg-white/30
dark:bg-slate-900/40
      backdrop-blur-2xl

      border
border-white/30
dark:border-white/10
      rounded-[24px]

      p-4

      shadow-[0_10px_40px_rgba(15,23,42,0.06)]

      hover:-translate-y-1
      hover:shadow-[0_20px_50px_rgba(79,70,229,0.12)]

      transition-all
      duration-300
    "
  >

    {/* HEADER */}
    <div className="flex justify-between items-center mb-4">

      <h2
        className="
          text-[17px]
          font-bold
          tracking-[-0.5px]
        "
      >
        Upcoming events
      </h2>

      <button
        className="
          text-blue-600
          text-sm
          font-semibold

          hover:scale-[1.03]

          transition-all
        "
      >
        All →
      </button>

    </div>

    {/* EVENTS */}
    <div className="space-y-2">

      {[
        {
          title: "Community meeting",
          date: "5 May",
          color: "bg-blue-500",
        },

        {
          title: "Pool maintenance",
          date: "8 May",
          color: "bg-yellow-500",
        },

        {
          title: "Fire safety drill",
          date: "12 May",
          color: "bg-red-500",
        },

      ].map((event, index) => (

        <div
          key={index}
          className="
            flex
            items-center
            gap-3

            p-3

            rounded-2xl

            hover:bg-white/40

            transition-all
            duration-300
          "
        >

          <div
            className={`
              w-3
              h-3
              rounded-full
              ${event.color}
            `}
          />

          <div className="flex-1">

            <p
              className="
                font-semibold
text-slate-800
dark:text-white
                text-[14px]
              "
            >
              {event.title}
            </p>

            <p className="text-[13px] text-slate-500 mt-1">
              {event.date}
            </p>

          </div>

        </div>

      ))}

    </div>

  </div>

</div>




         

          {/* LOWER GRID */}
          <div className="grid grid-cols-2 gap-6 mb-6">

            {/* MAINTENANCE */}
            <div
              className="
                bg-white/30
                dark:bg-slate-900/40
                backdrop-blur-2xl

                border
border-white/30
dark:border-white/10
                rounded-[24px]

                p-4

                shadow-[0_10px_40px_rgba(15,23,42,0.06)]

                hover:-translate-y-1
                hover:scale-[1.01]

                transition-all
                duration-300
              "
            >

              <div className="flex justify-between items-center mb-5">

                <h2
                  className="
                    text-[18px]
                    font-bold
                    tracking-[-0.5px]
                  "
                >
                  Maintenance requests
                </h2>

                <button
                  className="
bg-white/70
dark:bg-slate-800/80
                    px-4
                    py-2

                    rounded-2xl

                    text-sm
                    font-semibold

                    shadow-md

                    hover:scale-[1.03]
                    active:scale-[0.98]

                    transition-all
                  "
                >
                  + New
                </button>

              </div>

              <div className="space-y-3">

{
maintenance.length > 0 ? (

maintenance.slice(0,2).map((item) => (

<div
key={item._id}
className="
p-4
rounded-2xl
hover:bg-white/40
transition-all
"
>

<div
className="
flex
justify-between
items-start
"
>

<div>

<p className="font-semibold">
{item.issue}
</p>

<p
className="
text-sm
text-gray-500
mt-1
"
>
{item.description}
</p>

</div>

<div className="flex gap-2 mt-1">

<span
className="
bg-yellow-100
text-yellow-700
text-xs
px-3
py-1
rounded-full
"
>
{item.priority}
</span>

<span
className="
bg-blue-100
text-blue-700
text-xs
px-3
py-1
rounded-full
"
>
{item.status}
</span>

</div>

</div>

</div>

))

) : (

<p className="text-gray-500">
No maintenance requests
</p>

)

}

</div>

            </div>

            {/* NOTICES */}
            <div
              className="
bg-white/30
dark:bg-slate-900/40
                backdrop-blur-2xl

                border
border-white/30
dark:border-white/10
                rounded-[24px]

                p-4

                shadow-[0_10px_40px_rgba(15,23,42,0.06)]

                hover:-translate-y-1
                hover:scale-[1.01]

                transition-all
                duration-300
              "
            >

              <div className="flex justify-between items-center mb-5">

                <h2
                  className="
                    text-[18px]
                    font-bold
                    tracking-[-0.5px]
                  "
                >
                  Society notices
                </h2>

                <button
                  className="
                    text-blue-600
                    text-sm
                    font-semibold

                    hover:scale-[1.03]

                    transition-all
                  "
                >
                  All →
                </button>

              </div>

              <div className="space-y-3">

                {
notices.length > 0 ? (

notices.slice(0,3).map((notice) => (

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

<p className="font-semibold">
{notice.title}
</p>

<p
className="
text-sm
text-gray-500
mt-1
"
>
Admin • {

new Date(
notice.createdAt
).toLocaleDateString()

}
</p>

</div>

))

) : (

<p className="text-gray-500">
No notices available
</p>

)
}

              </div>

            </div>

          </div>

          {/* PAYMENTS + ACTIVITY */}
          <div className="grid grid-cols-2 gap-6">

            {/* PAYMENTS */}
            <div
              className="
                bg-white/30
                dark:bg-slate-900/40
                backdrop-blur-2xl

                border
border-white/30
dark:border-white/10
                rounded-[24px]

                p-4

                shadow-[0_10px_40px_rgba(15,23,42,0.06)]
              "
            >

              <div className="flex justify-between items-center mb-5">

                <h2
                  className="
                    text-[18px]
                    font-bold
                    tracking-[-0.5px]
                  "
                >
                  Payments & dues
                </h2>

                <button
                  className="
                    text-blue-600
                    text-sm
                    font-semibold

                    hover:scale-[1.03]

                    transition-all
                  "
                >
                  History →
                </button>

              </div>

              <div
                className="
                  border
border-white/30
dark:border-white/10
bg-white/30
dark:bg-slate-900/40

                  rounded-[22px]

                  p-5

                  backdrop-blur-xl
                "
              >

                <div className="flex justify-between items-start">

                  <div>

{
payments.length > 0 ? (

<>

<div>

<p className="font-semibold">

{
payments[0].title
||

"Monthly Maintenance"
}

</p>

<p
className="
text-sm
text-gray-500
mt-1
"
>

Due {

new Date(
payments[0].dueDate
).toLocaleDateString()

}

</p>

</div>

<div className="text-right">

<p className="text-[22px] font-black">

₹ {
payments[0].amount
}

</p>

<button
className="
bg-gradient-to-r
from-blue-600
to-violet-600

text-white

px-4
py-2

rounded-xl

mt-3

text-sm

shadow-lg

hover:scale-[1.03]
active:scale-[0.98]

transition-all
"
>

{
payments[0].status ===
"Paid"

? "Paid"

: "Pay Now"
}

</button>

</div>

</>

) : (

<p className="text-gray-500">
No pending payments
</p>

)
}
                    

                  </div>

                  <div className="text-right">

                    

                    <button
                      className="
                        bg-gradient-to-r
                        from-blue-600
                        to-violet-600

                        text-white

                        px-4
                        py-2

                        rounded-xl

                        mt-3

                        text-sm

                        shadow-lg

                        hover:scale-[1.03]
                        active:scale-[0.98]

                        transition-all
                      "
                    >
                      Pay now
                    </button>

                  </div>

                </div>

              </div>

            </div>

            {/* ACTIVITY */}
            <div
              className="
bg-white/30
dark:bg-slate-900/40
                backdrop-blur-2xl

                border
border-white/30
dark:border-white/10
                rounded-[24px]

                p-4

                shadow-[0_10px_40px_rgba(15,23,42,0.06)]
              "
            >

              <h2
                className="
                  text-[18px]
                  font-bold
                  tracking-[-0.5px]
                  mb-5
                "
              >
                Recent activity
              </h2>

              <div className="space-y-5">

               {
[
...complaints.slice(0,2),

...maintenance.slice(0,2),

...payments.slice(0,1)

].map((activity,index)=>{

let color = "bg-blue-500";

let text = "";


// COMPLAINT ACTIVITY
if(activity.complaintTitle){

color = "bg-yellow-500";

text =
`Complaint raised: ${activity.complaintTitle}`;

}


// MAINTENANCE ACTIVITY
else if(activity.issue){

color = "bg-green-500";

text =
`Maintenance request: ${activity.issue}`;

}


// PAYMENT ACTIVITY
else if(activity.amount){

color = "bg-violet-500";

text =
`Payment of ₹${activity.amount} generated`;

}

return(

<div
key={index}
className="flex gap-4"
>

<div
className={`
w-3
h-3
rounded-full
mt-2
${color}
`}
/>

<div>

<p className="font-medium">

{text}

</p>

<p
className="
text-sm
text-gray-500
mt-1
"
>

{
new Date(
activity.createdAt
).toLocaleString()
}

</p>

</div>

</div>

);

})
}

              </div>

            </div>

          </div>

          {/* MAP */}
          <div className="mt-6">
            <GoogleNearbyMap />
          </div>

        </div>

      </div>

    </div>

  );
}