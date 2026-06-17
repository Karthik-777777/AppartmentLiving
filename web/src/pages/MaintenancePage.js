import React, {
  useEffect,
  useState,
} from "react";
import maintenanceBg from "../assests/Maintenance-bg.png";
export default function MaintenancePage() {

  
const residentData =
localStorage.getItem("resident");

const resident =
residentData
? JSON.parse(residentData)
: {};




  const [formData, setFormData] =
    useState({

      residentName:
resident?.residentName || "",

flatNumber:
resident?.flatNumber || "",
      issue: "",
      description: "",
      category: "Electrical",
      priority: "Medium",

    });
      const [maintenance, setMaintenance] =
    useState([]);


  // FETCH MAINTENANCE
  const fetchMaintenance = () => {

    if(!resident?.residentId) return;

fetch(
`http://localhost:4000/api/maintenance/resident/${resident.residentId}`
)

      .then((res) => res.json())

      .then((data) => {

        if (Array.isArray(data)) {

          setMaintenance(data);

        }

      })

      .catch((err) => {

        console.log(err);

      });

  };


  useEffect(() => {

    fetchMaintenance();

  }, []);


  // HANDLE INPUT
  const handleChange = (e) => {

    setFormData({

      ...formData,

      [e.target.name]:
      e.target.value,

    });

  };


  // SUBMIT FORM
  const handleSubmit = async(e) => {

    e.preventDefault();

    try {

      const res = await fetch(

        "http://localhost:4000/api/maintenance",

        {

          method: "POST",

          headers: {

            "Content-Type":
            "application/json",

          },

         body: JSON.stringify({

  ...formData,

 residentId:
resident?.residentId || "",

  status:
  "Pending"

}),

        }

      );

      const data =
      await res.json();

      console.log(data);

      fetchMaintenance();

      setFormData({

  residentName:
  resident.residentName,

  flatNumber:
  resident.flatNumber,

  issue: "",
  description: "",
  category: "Electrical",
  priority: "Medium",

});

    } catch(err) {

      console.log(err);

    }
    };

  


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
    backgroundImage: `url(${maintenanceBg})`,
  }}
>
      <h1
        className="
          text-4xl
          font-bold
          mb-8
        "
      >
        Maintenance Requests
      </h1>


      {/* FORM */}
      <div
        className="
bg-white/55
border border-white/40
          backdrop-blur-xl
          rounded-3xl
          p-6
          shadow-xl
          mb-8
        "
      >

        <h2
          className="
            text-2xl
            font-bold
            mb-5
          "
        >
          Raise Maintenance Request
        </h2>

        <form
          onSubmit={handleSubmit}
          className="space-y-4"
        >

          <div className="grid grid-cols-2 gap-4">

            <input
              type="text"
              name="residentName"
              placeholder="Resident Name"
              value={formData.residentName}
                disabled
              onChange={handleChange}
              className="
                p-4
                rounded-2xl
                bg-white
                outline-none
              "
            />

            <input
              type="text"
              name="flatNumber"
              placeholder="Flat Number"
              value={formData.flatNumber}
                disabled
              onChange={handleChange}
              className="
                p-4
                rounded-2xl
                bg-white
                outline-none
              "
            />

          </div>


          <input
            type="text"
            name="issue"
            placeholder="Issue"
            value={formData.issue}
            onChange={handleChange}
            className="
              w-full
              p-4
              rounded-2xl
              bg-white
              outline-none
            "
          />


          <textarea
            rows="5"
            name="description"
            placeholder="Describe issue..."
            value={formData.description}
            onChange={handleChange}
            className="
              w-full
              p-4
              rounded-2xl
              bg-white
              outline-none
            "
          />


          <div className="grid grid-cols-2 gap-4">

            <select

  name="category"

  value={formData.category}

  onChange={handleChange}

  className="
    bg-white
    rounded-2xl
    p-4
    border-none
    outline-none
    text-lg
  "
>

  <option>Electrical</option>

  <option>Plumbing</option>

  <option>Cleaning</option>

  <option>Security</option>

  <option>Internet/WiFi</option>

  <option>Water Leakage</option>

  <option>Lift Issue</option>

  <option>Parking Issue</option>

  <option>Garbage Collection</option>

  <option>Pest Control</option>

  <option>Power Backup</option>

  <option>Housekeeping</option>

  <option>Noise Complaint</option>

  <option>Common Area Damage</option>

  <option>Garden Maintenance</option>

  <option>Street Light</option>

  <option>Visitor Issue</option>

  <option>Other</option>

</select>


            <select
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              className="
                p-4
                rounded-2xl
                bg-white
              "
            >

             <option>Routine</option>

<option>Attention Needed</option>


<option>Urgent Action</option>

<option>Emergency</option>

            </select>

          </div>


          <button
            className="
              px-8
              py-4
              rounded-2xl
              text-white
              font-semibold

              bg-gradient-to-r
              from-blue-600
              to-purple-600

              hover:scale-105
              transition-all
            "
          >
            Submit Request
          </button>

        </form>

      </div>


      {/* HISTORY */}
      <div
  className="
bg-white/55
border border-white/40
    backdrop-blur-xl
    rounded-3xl
    p-6
    shadow-xl
  "
>

  <h2
    className="
      text-2xl
      font-bold
      mb-5
    "
  >
    Maintenance History
  </h2>

  <div className="space-y-4">

    {maintenance.length === 0 ? (

      <div
        className="
          text-center
          py-10
          text-gray-500
        "
      >
        No Maintenance Requests Found
      </div>

    ) : (

      maintenance.map((item) => (

        <div

          key={item._id}

          className="
            grid
            grid-cols-5
            items-center

            bg-white/60
            rounded-2xl

            p-4

            hover:shadow-lg
            transition-all
          "
        >

          <div>

            <h3 className="font-bold">
              {item.issue}
            </h3>

            <p
              className="
                text-sm
                text-gray-500
              "
            >
              {item.description}
            </p>

          </div>

          <p>
            {item.flatNumber}
          </p>

          <p>
            {item.category}
          </p>

          <p>
            {item.assignedWorker}
          </p>

          <span
            className={`
              px-4
              py-2
              rounded-full
              text-sm
              w-fit

              ${
                item.status === "Completed"

                ? "bg-green-100 text-green-600"

                : "bg-yellow-100 text-yellow-700"
              }
            `}
          >
            {item.status}
          </span>

        </div>
      ))

    )}

  </div>

</div>

       

    </div>

  );

}