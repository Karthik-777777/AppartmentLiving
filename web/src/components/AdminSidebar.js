import React from "react";

import {
  FiGrid,
  FiAlertCircle,
  FiUsers,
  FiCreditCard,
  FiBell,
  FiLogOut,
  FiMap,
    FiTool

} from "react-icons/fi";

import { Link, useLocation } from "react-router-dom";

export default function AdminSidebar() {

  const location = useLocation();
  const handleLogout = () => {

  const confirmLogout =
    window.confirm(
      "Are you sure you want to logout?"
    );

  if(!confirmLogout) return;

  localStorage.removeItem("token");

  localStorage.removeItem("user");

  localStorage.removeItem("resident");

  localStorage.removeItem("role");

  window.location.href = "/login";

};

  const menu = [

    {
      title: "Dashboard",
      icon: <FiGrid />,
      path: "/admin",
    },

    {
      title: "Complaints",
      icon: <FiAlertCircle />,
      path: "/admin/complaints",
      badge: 7,
    },

   {
  title: "Maintenance",
  icon: <FiTool />,
  path: "/admin/maintenance",
},
    {
      title: "Residents",
      icon: <FiUsers />,
      path: "/admin/residents",
    },

    {
      title: "Payments",
      icon: <FiCreditCard />,
      path: "/admin/payments",
      badge: 3,
    },

    {
      title: "Notices",
      icon: <FiBell />,
      path: "/admin/notices",
    },

    {
      title: "Maps & Nearby",
      icon: <FiMap />,
      path: "/admin/maps",
    },

  ];

  return (

    <div
      className="
        fixed
        left-0
        top-0

        w-[220px]
        h-screen

        bg-white/75
        backdrop-blur-2xl

        border-r
        border-white/20

        shadow-[0_10px_40px_rgba(15,23,42,0.08)]

        p-4
        z-50
      "
    >

      {/* LOGO */}

      <div className="mb-8">

        <h1
          className="
            text-[30px]
            font-black
            leading-[34px]

            bg-gradient-to-r
            from-blue-600
            to-violet-600

            bg-clip-text
            text-transparent
          "
        >
          Apartment
          <br />
          Living
        </h1>

        <p className="text-slate-500 mt-2 text-xs">
          Premium Admin Portal
        </p>

      </div>

      {/* SECTION */}

      <p
        className="
          text-[11px]
          font-bold
          tracking-[2px]
          text-slate-400
          mb-3
        "
      >
        MAIN MENU
      </p>

      <div className="space-y-3">

        {menu.map((item, index) => {

          const active =
            location.pathname === item.path;

          return (

            <Link
              key={index}
              to={item.path}
            >

              <div
                className={`
                  flex
                  items-center
                  justify-between

                  px-4
                  py-4

                  rounded-[22px]

                  transition-all
                  duration-300

                  group

                  ${
                    active
                      ? `
                        bg-gradient-to-r
                        from-blue-600
                        to-violet-600

                        text-white

                        shadow-[0_10px_30px_rgba(99,102,241,0.35)]
                      `
                      : `
                        bg-white/40
                        text-slate-700

                        hover:bg-white/70
                        hover:scale-[1.02]
                      `
                  }
                `}
              >

                <div
                  className="
                    flex
                    items-center
                    gap-3

                    text-[16px]
                    font-semibold
                  "
                >

                  <span className="text-[18px]">
                    {item.icon}
                  </span>

                  {item.title}

                </div>

                {item.badge && (

                  <span
                    className={`
                      w-7
                      h-7

                      rounded-full

                      flex
                      items-center
                      justify-center

                      text-xs
                      font-bold

                      ${
                        active
                          ? "bg-white text-violet-600"
                          : `
                            bg-gradient-to-r
                            from-blue-600
                            to-violet-600

                            text-white
                            shadow-lg
                          `
                      }
                    `}
                  >
                    {item.badge}
                  </span>

                )}

              </div>

            </Link>

          );

        })}

      </div>



      {/* ACCOUNT */}

      <div className="mt-10">

        <p
          className="
            text-[11px]
            font-bold
            tracking-[2px]
            text-slate-400
            mb-3
          "
        >
          ACCOUNT
        </p>

        {/* PROFILE */}

<div
  onClick={() =>
    window.location.href =
    "/admin/profile"
  }

  className="
    flex
    items-center
    gap-4

    px-4
    py-4

    rounded-2xl

    cursor-pointer

    hover:bg-white/60

    transition-all
    duration-300
  "
>

  {/* ICON */}
  <div
    className="
      w-10
      h-10

      rounded-xl

      bg-gradient-to-r
      from-blue-500
      to-violet-500

      flex
      items-center
      justify-center

      text-white
      font-bold
      text-lg
    "
  >
    {
      JSON.parse(
        localStorage.getItem("user")
      )?.name?.charAt(0)
    }
  </div>


  {/* TEXT */}
  <div>

    <p
      className="
        text-[15px]
        font-semibold
        text-slate-800
      "
    >
      Admin Profile
    </p>

    <p
      className="
        text-[12px]
        text-slate-500
      "
    >
      View profile
    </p>

  </div>

</div>

        <button

  onClick={handleLogout}

  className="
    flex
    items-center
    gap-3

    px-4
    py-4

    rounded-[22px]

    text-red-500

    hover:bg-red-50

    transition-all
    duration-300

    font-semibold
  "
>

  <FiLogOut className="text-[20px]" />

  Logout

</button>
      </div>

    </div>

  );

}