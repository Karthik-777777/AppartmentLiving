import React from "react";

import {
  FiHome,
  FiAlertCircle,
  FiTool,
  FiBell,
  FiCreditCard,
  FiMap,
  FiUser,
  FiLogOut,
} from "react-icons/fi";

import { Link, useLocation } from "react-router-dom";


export default function Sidebar() {

  const location = useLocation();

  const handleLogout = () => {

  localStorage.removeItem("token");

  localStorage.removeItem("user");

  localStorage.removeItem("resident");

  window.location.href = "/login";

};

  const menuItems = [
    {
      name: "Dashboard",
      icon: <FiHome size={18} />,
      path: "/resident",
    },

    {
      name: "My complaints",
      icon: <FiAlertCircle size={18} />,
path: "/resident/complaints",
      badge: 2,
      dot: "bg-red-500",
    },

    {
      name: "Maintenance",
      icon: <FiTool size={18} />,
path: "/resident/maintenance",
      dot: "bg-yellow-500",
    },

    {
  name: "Notices",
  icon: <FiBell size={18} />,
  path: "/resident/notices",
  dot: "bg-blue-500",
},

   {
  name: "Payments",
  icon: <FiCreditCard size={18} />,
  path: "/resident/payments",
  badge: 3,
  dot: "bg-green-500",
},
    {
      name: "Maps & Nearby",
      icon: <FiMap size={18} />,
      path: "/resident/maps",
      dot: "bg-indigo-500",
    },
  ];

  return (

    <div
      className="
        w-[290px]
        min-h-screen

        bg-gradient-to-b
from-[#f5f7ff]/95
via-[#eef2ff]/90
to-[#f3e8ff]/90
        backdrop-blur-3xl

        border-r
        border-white/20
        dark:border-white/10

        shadow-[0_10px_60px_rgba(0,0,0,0.08)]
       before:absolute
before:inset-0
before:-z-10
before:bg-[radial-gradient(circle_at_top_left,rgba(99,102,241,0.12),transparent_35%)]

relative
overflow-hidden

        flex
        flex-col

        transition-all
        duration-500
      "
    >

      <div
  className="
    absolute
    top-0
    left-0
    w-full
    h-[4px]

    bg-gradient-to-r
    from-blue-500
    via-indigo-500
    to-violet-500

    pointer-events-none
  "
/>
      {/* LOGO */}

      <div className="px-8 py-10 border-b border-white/20">

        <h1
          className="
            text-[34px]
            leading-tight
            font-black

            bg-gradient-to-r
            from-[#4f46e5]
via-[#6366f1]
to-[#8b5cf6]
            bg-clip-text
            text-transparent

            tracking-[-1px]
          "
        >
          Apartment Living
        </h1>

        <p className="text-gray-400 text-sm mt-2">
          Premium Resident Portal
        </p>

      </div>

      {/* MENU */}

      <div className="flex-1 px-6 py-8 overflow-y-auto">

        {/* MY SPACE */}

        <div className="mb-10">

          <h3
            className="
              text-[12px]
              uppercase
              tracking-[3px]
              text-slate-400
              font-black
              mb-5
            "
          >
            My Space
          </h3>

          <Link to="/resident">

            <button
              className={`
                relative

                w-full

overflow-visible

                rounded-[28px]

                px-6
                py-5

                flex
                items-center
                gap-3

                font-bold
                text-[15px]

                transition-all
                duration-300

                ${
                  location.pathname === "/resident"
                    ? `
                      bg-gradient-to-r
                      from-blue-600
                      via-indigo-600
                      to-violet-600

                      text-white

                      shadow-[0_12px_30px_rgba(79,70,229,0.35)]

                      hover:scale-[1.02]
                    `
                    : `
                      bg-white/30
backdrop-blur-xl
text-slate-700
hover:bg-white/60
hover:shadow-[0_8px_25px_rgba(99,102,241,0.15)]
                    `
                }
              `}
            >

              <span className="animate-pulse text-[10px]">
                ●
              </span>

              Dashboard

            </button>

          </Link>

        </div>

        {/* ACTIONS */}

        <div className="mb-10">

          <h3
            className="
              text-[12px]
              uppercase
              tracking-[3px]
              text-slate-400
              font-black
              mb-5
            "
          >
            Actions
          </h3>

          <div className="space-y-2">

            {menuItems.slice(1).map((item, index) => (

              <Link key={index} to={item.path}>

                <div
                  className={`
                    group

                    w-full

                    flex
                    items-center
                    justify-between

                    px-5
                    py-4

                    rounded-[22px]

                    transition-all
                    duration-300

                    hover:bg-white/60
                    dark:hover:bg-slate-800/60

                    hover:shadow-lg

                    hover:-translate-y-[2px]
                    hover:translate-x-1

                    ${
                      location.pathname === item.path
                        ? "bg-white/70 shadow-lg"
                        : ""
                    }
                  `}
                >

                  <div className="flex items-center gap-4">

                    <div
                      className={`
                        w-[10px]
                        h-[10px]
                        rounded-full
                        ${item.dot}
                      `}
                    />

                    <div
                      className="
                        flex
                        items-center
                        gap-3

                        text-[15px]
                        font-semibold
                        text-slate-700
                        dark:text-white

group-hover:text-indigo-600
group-hover:drop-shadow-[0_0_10px_rgba(99,102,241,0.4)]

                        transition-all
                      "
                    >

                      {item.icon}

                      {item.name}

                    </div>

                  </div>

                  {item.badge && (

                    <span
                      className="
                        min-w-[30px]
                        h-[30px]

                        px-2

                        rounded-full

                        bg-gradient-to-r
                        from-blue-600
                        to-violet-600

                        text-white

                        text-[12px]
                        font-bold

                        flex
                        items-center
                        justify-center

                        shadow-[0_8px_20px_rgba(79,70,229,0.35)]

                        group-hover:scale-110

                        transition-all
                      "
                    >
                      {item.badge}
                    </span>

                  )}

                </div>

              </Link>

            ))}

          </div>

        </div>

        {/* ACCOUNT */}

        <div>

          <h3
            className="
              text-[12px]
              uppercase
              tracking-[3px]
              text-slate-400
              font-black
              mb-5
            "
          >
            Account
          </h3>

          <div className="space-y-2">

            {/* PROFILE */}

           <Link
  to="/resident/profile"

  style={{
    display: "flex",
    alignItems: "center",
    gap: "16px",

    padding: "18px 24px",

    marginTop: "20px",

background:
"linear-gradient(135deg, rgba(255,255,255,0.7), rgba(255,255,255,0.45))",
backdropFilter: "blur(20px)",
border: "1px solid rgba(255,255,255,0.3)",
    borderRadius: "24px",

    textDecoration: "none",

    color: "#0f172a",

    fontSize: "18px",

    fontWeight: "600",

    boxShadow:
      "0 10px 25px rgba(0,0,0,0.08)",
  }}
>

  <div
    style={{
      width: "16px",
      height: "16px",
      borderRadius: "50%",
      background:
        "linear-gradient(135deg,#a855f7,#6366f1)",
    }}
  />

  <i
    className="fi fi-rr-user"
    style={{
      fontSize: "20px",
    }}
  />

  <span>
    Profile
  </span>

</Link>

            {/* LOGOUT */}

            <button

  onClick={handleLogout}

  className="
                w-full

                flex
                items-center
                gap-4

                px-5
                py-4

                rounded-[22px]

                text-red-500

                hover:bg-red-50
                dark:hover:bg-red-900/20

                hover:text-red-600

                hover:translate-x-1

                transition-all
                duration-300
              "
            >

              <div className="w-[10px] h-[10px] rounded-full bg-red-400" />

              <FiLogOut size={18} />

              <span className="font-semibold">
                Logout
              </span>

            </button>

          </div>

        </div>

      </div>

    </div>

  );

}