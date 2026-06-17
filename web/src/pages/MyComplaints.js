import React, {
  useEffect,
  useState,
} from "react";
import complaintBg from "../assests/complaint-bg.png";
export default function MyComplaints() {

  const resident = JSON.parse(
  localStorage.getItem("resident")
) || {};
console.log("RESIDENT OBJECT:", resident);
console.log("RESIDENT ID:", resident.residentId);


  const [formData, setFormData] =
useState({
  

  residentId:
resident.residentId,

  residentName:
resident.residentName,

  flatNumber:
resident.flatNumber,

  title: "",

  description: "",

  category: "Noise Disturbance",

  priority: "Routine",

});
const [complaints, setComplaints] =
useState([]);
  // FETCH COMPLAINTS
  useEffect(() => {
console.log("FETCHING COMPLAINTS FOR:", resident.residentId);

if (!resident?.residentId) {
  console.log("residentId missing");
  return;
}
    fetch(

`http://localhost:4000/api/complaints/resident/${resident.residentId}`

)

      .then((res) => res.json())
.then((data) => {

  console.log("COMPLAINT DATA:", data);
  console.log("IS ARRAY:", Array.isArray(data));

  if (Array.isArray(data)) {
    setComplaints(data);
  } else {
    console.log("API did not return array");
  }

})
      .catch((err) => {

        console.log(err);

      });

  }, []);

  // HANDLE INPUT
  const handleChange = (e) => {

    setFormData({

      ...formData,

      [e.target.name]:
      e.target.value,

    });

  };

  // SUBMIT COMPLAINT
  const handleSubmit = async (e) => {

    e.preventDefault();

    try {

      const response = await fetch(
        "http://localhost:4000/api/complaints",
        {

          method: "POST",

          headers: {
            "Content-Type":
              "application/json",
          },

          body: JSON.stringify(
            formData
          ),

        }
      );

      const data =
        await response.json();

      // ADD NEW COMPLAINT TO UI
      setComplaints([
        data,
        ...complaints,
      ]);

      // RESET FORM
     setFormData({

  residentId:
  resident.residentId,

  residentName:
  resident.residentName,

  flatNumber:
  resident.flatNumber,

  title: "",

  description: "",

  category:
  "Noise Disturbance",

  priority:
  "Routine",

});

    } catch (err) {

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
    backgroundImage: `url(${complaintBg})`,
  }}
>
      {/* PAGE TITLE */}
      <h1
        className="
          text-3xl
          font-bold
          mb-6
        "
      >
        My Complaints
      </h1>

      {/* FORM */}
      <div
        className="
bg-white/50
          backdrop-blur-2xl
          border border-white/30
          rounded-[24px]
          p-6
          mb-6
        "
      >

        <h2
          className="
            text-xl
            font-bold
            mb-5
          "
        >
          Raise Complaint
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
                outline-none
              "
              required
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
                outline-none
              "
              required
            />

          </div>

          <input
            type="text"
            name="title"
            placeholder="Complaint Title"
            value={formData.title}
            onChange={handleChange}
            className="
              w-full
              p-4
              rounded-2xl
              outline-none
            "
            required
          />

          <textarea
            name="description"
            placeholder="Describe issue..."
            value={formData.description}
            onChange={handleChange}
            rows="5"
            className="
              w-full
              p-4
              rounded-2xl
              outline-none
            "
            required
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

  <option>Noise Disturbance</option>

  <option>Neighbor Issue</option>

  <option>Parking Violation</option>

  <option>Unauthorized Visitors</option>

  <option>Security Concern</option>

  <option>Pet Disturbance</option>

  <option>Garbage Mismanagement</option>

  <option>Water Supply Issue</option>

  <option>Common Area Misuse</option>

  <option>Staff Misbehavior</option>

  <option>Lift Overcrowding</option>

  <option>Smoking in Common Areas</option>

  <option>Illegal Activities</option>

  <option>Vehicle Blocking</option>

  <option>Late Night Disturbance</option>

  <option>Children Playing Issue</option>

  <option>Maintenance Delay</option>

  <option>Rule Violation</option>

  <option>Suspicious Activity</option>

  <option>Other</option>

</select>

            <select
              name="priority"
              value={formData.priority}
              onChange={handleChange}
              className="
                p-4
                rounded-2xl
              "
            >

<option>Routine</option>

<option>Attention Needed</option>


<option>Urgent Action</option>

<option>Emergency</option>
            </select>

          </div>

          <button
            type="submit"
            className="
              bg-gradient-to-r
              from-blue-600
              to-violet-600

              text-white

              px-6
              py-3

              rounded-2xl

              font-semibold
            "
          >
            Submit Complaint
          </button>

        </form>

      </div>

      {/* RECENT COMPLAINTS */}
      <div
        className="
bg-white/50
backdrop-blur-2xl
          border border-white/30
          rounded-[24px]
          p-6
        "
      >

        <h2
          className="
            text-xl
            font-bold
            mb-5
          "
        >
          Complaint History
        </h2>

        <div className="space-y-4">

          {complaints.map(
            (item, index) => (

              <div
                key={index}
                className="
                  grid
                  grid-cols-4
                  items-center

                  p-4

                  rounded-2xl

                  border-b
                  border-white/20
                "
              >

                <p className="font-semibold">
                  {item.title}
                </p>

                <p>
                  {item.flatNumber}
                </p>

                <p>
                  {item.category}
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
                  {item.status}
                </span>

              </div>

            )
          )}

        </div>

      </div>

    </div>

  );

}