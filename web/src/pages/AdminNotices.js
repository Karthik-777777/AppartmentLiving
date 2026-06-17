import React, {
  useEffect,
  useState,
} from "react";

export default function AdminNotices() {

  const [notices, setNotices] =
    useState([]);

  const [form, setForm] =
    useState({

      title: "",
      message: "",
      category: "General",
      priority: "Normal",

    });


  // FETCH NOTICES

  const fetchNotices = () => {

    fetch(
      "http://localhost:4000/api/notices"
    )

      .then((res) => res.json())

      .then((data) => {

        setNotices(data);

      })

      .catch((err) => {

        console.log(err);

      });

  };


  useEffect(() => {

    fetchNotices();

  }, []);


  // HANDLE CHANGE

  const handleChange = (e) => {

    setForm({

      ...form,

      [e.target.name]:
      e.target.value,

    });

  };


  // CREATE NOTICE

  const createNotice = async(e) => {

    e.preventDefault();

    try {

      await fetch(

        "http://localhost:4000/api/notices",

        {

          method: "POST",

          headers: {

            "Content-Type":
            "application/json",

          },

          body: JSON.stringify(form),

        }

      );

      setForm({

        title: "",
        message: "",
        category: "General",
        priority: "Normal",

      });

      fetchNotices();

    } catch(err) {

      console.log(err);

    }

  };


  // DELETE NOTICE

  const deleteNotice = async(id) => {

    try {

      await fetch(

        `http://localhost:4000/api/notices/${id}`,

        {

          method: "DELETE",

        }

      );

      fetchNotices();

    } catch(err) {

      console.log(err);

    }

  };


  return (

    <div className="p-6">

      {/* TITLE */}

      <h1
        className="
          text-4xl
          font-bold
          mb-8
        "
      >
        Notices Management
      </h1>


      {/* CREATE FORM */}

      <form

        onSubmit={createNotice}

        className="
          bg-white/60
          backdrop-blur-xl

          rounded-3xl

          p-6

          shadow-lg

          mb-8

          space-y-5
        "
      >

        <input

          type="text"

          name="title"

          placeholder="Notice title"

          value={form.title}

          onChange={handleChange}

          className="
            w-full
            p-4
            rounded-2xl
            border
          "

          required
        />


        <textarea

          name="message"

          placeholder="Notice message"

          value={form.message}

          onChange={handleChange}

          className="
            w-full
            p-4
            rounded-2xl
            border
            h-32
          "

          required
        />


        <div className="grid grid-cols-2 gap-5">

          <select

            name="category"

            value={form.category}

            onChange={handleChange}

            className="
              p-4
              rounded-2xl
              border
            "
          >

            <option>Security</option>

<option>Water Supply</option>

<option>Electricity</option>

<option>Parking</option>

<option>Lift Service</option>

<option>Housekeeping</option>

<option>Visitor Alert</option>

<option>Festival</option>

<option>Meeting</option>

<option>Community Update</option>
          </select>


          <select

            name="priority"

            value={form.priority}

            onChange={handleChange}

            className="
              p-4
              rounded-2xl
              border
            "
          >

            <option>
              Normal
            </option>

            <option>
              High
            </option>

            <option>
              Urgent
            </option>

          </select>

        </div>


        <button

          type="submit"

          className="
            px-6
            py-3

            rounded-2xl

            bg-gradient-to-r
            from-blue-600
            to-violet-600

            text-white
            font-semibold
          "
        >
          Publish Notice
        </button>

      </form>


      {/* NOTICE LIST */}

      <div className="space-y-5">

        {notices.map((item) => (

          <div

            key={item._id}

            className="
              bg-white/60
              backdrop-blur-xl

              rounded-3xl

              p-5

              shadow-lg

              flex
              justify-between
              items-center
            "
          >

            <div>

              <h2
                className="
                  text-xl
                  font-bold
                "
              >
                {item.title}
              </h2>

              <p className="text-gray-600 mt-2">
                {item.message}
              </p>

              <div
                className="
                  flex
                  gap-3
                  mt-3
                "
              >

                <span
                  className="
                    px-3
                    py-1

                    rounded-full

                    bg-blue-100
                    text-blue-600

                    text-sm
                  "
                >
                  {item.category}
                </span>

                <span
                  className="
                    px-3
                    py-1

                    rounded-full

                    bg-red-100
                    text-red-500

                    text-sm
                  "
                >
                  {item.priority}
                </span>

              </div>

            </div>


            <button

              onClick={() =>

                deleteNotice(item._id)

              }

              className="
                px-5
                py-3

                rounded-2xl

                bg-red-500
                text-white

                font-semibold
              "
            >
              Delete
            </button>

          </div>

        ))}

      </div>

    </div>

  );

}