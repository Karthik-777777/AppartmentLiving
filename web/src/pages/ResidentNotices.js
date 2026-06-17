import React, {
  useEffect,
  useState,
} from "react";
import noticesBg from "../assests/Notices-bg.png";

export default function ResidentNotices() {

  const [notices, setNotices] =
    useState([]);


  // FETCH NOTICES

  useEffect(() => {

    fetch(
      "http://localhost:4000/api/notices"
    )

      .then((res) => res.json())

      .then((data) => {
  console.log("API RESPONSE:", data);
  setNotices(data);
})

      .catch((err) => {

        console.log(err);

      });

  }, []);


  return (

<div
  className="
    p-6
    min-h-screen
    bg-cover
    bg-center
    bg-no-repeat
  "
  style={{
    backgroundImage: `url(${noticesBg})`,
  }}
>
      {/* TITLE */}

      <h1
        className="
         text-5xl
font-extrabold
tracking-tight
text-slate-900
          mb-8
        "
      >
        Society Notices
      </h1>


      {/* NOTICE LIST */}

      <div className="space-y-6">

{Array.isArray(notices) &&
  notices.map((item) => (
          <div

            key={item._id}

            className="
bg-white/50
              backdrop-blur-xl

              rounded-3xl

              p-6

              shadow-lg

              border
              border-white/30
            "
          >

            {/* HEADER */}

            <div
              className="
                flex
                justify-between
                items-start
                mb-4
              "
            >

              <div>

                <h2
                  className="
                    text-2xl
                    font-bold
                  "
                >
                  {item.title}
                </h2>

                <p
                  className="
                    text-gray-500
                    mt-1
                  "
                >
                  Posted by {item.postedBy}
                </p>

              </div>


              {/* PRIORITY */}

              <span
                className={`
                  px-4
                  py-2

                  rounded-full

                  text-sm
                  font-semibold

                  ${
                    item.priority ===
                    "Urgent"

                      ? `
                        bg-red-100
                        text-red-500
                      `

                      : item.priority ===
                        "High"

                      ? `
                        bg-yellow-100
                        text-yellow-600
                      `

                      : `
                        bg-blue-100
                        text-blue-500
                      `
                  }
                `}
              >
                {item.priority}
              </span>

            </div>


            {/* MESSAGE */}

            <p
              className="
                text-lg
                text-gray-700
                leading-relaxed
              "
            >
              {item.message}
            </p>


            {/* FOOTER */}

            <div
              className="
                flex
                justify-between
                items-center

                mt-6
              "
            >

              <span
                className="
                  px-4
                  py-2

                  rounded-full

                  bg-indigo-100
                  text-indigo-600

                  text-sm
                  font-semibold
                "
              >
                {item.category}
              </span>


              <p
                className="
                  text-sm
                  text-gray-400
                "
              >
                {

                  new Date(
                    item.createdAt
                  ).toLocaleString()

                }
              </p>

            </div>

          </div>

        ))}

      </div>

    </div>

  );

}