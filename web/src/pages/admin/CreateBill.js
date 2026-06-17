import React, {
  useEffect,
  useState,
} from "react";

export default function CreateBill() {

  const [residents, setResidents] =
    useState([]);

  const [selectedResident, setSelectedResident] =
    useState(null);

  const [formData, setFormData] =
    useState({

      month: "",

      maintenance: "",

      waterBill: "",

      electricityBill: "",

      rent: "",

      parkingFee: "",

      penalty: "",

      otherCharges: "",

      upiId: "",

      phonepe: "",

      qrImage: "",

    });


  // FETCH RESIDENTS

  useEffect(() => {

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

  }, []);


  // HANDLE INPUT

  const handleChange = (e) => {

    setFormData({

      ...formData,

      [e.target.name]:
      e.target.value,

    });

  };


  // TOTAL

  const totalAmount =

    Number(formData.maintenance || 0)

    + Number(formData.waterBill || 0)

    + Number(formData.electricityBill || 0)

    + Number(formData.rent || 0)

    + Number(formData.parkingFee || 0)

    + Number(formData.penalty || 0)

    + Number(formData.otherCharges || 0);

    

  // CREATE BILL

const createBill = async () => {

  if (!selectedResident) {

    return alert(
      "Select resident"
    );

  }

  if (!formData.month) {

    return alert(
      "Please select month"
    );

  }

  try {

    console.log({

      residentId:
      selectedResident.residentId,

      residentName:
      selectedResident.residentName,

      flatNumber:
      selectedResident.flatNumber,

      month:
      formData.month,

      maintenance:
      formData.maintenance,

      waterBill:
      formData.waterBill,

      electricityBill:
      formData.electricityBill,

      rent:
      formData.rent,

      parkingFee:
      formData.parkingFee,

      penalty:
      formData.penalty,

      otherCharges:
      formData.otherCharges,

      totalAmount

    });
console.log(formData);
console.log(totalAmount);
    const res = await fetch(

      

      "http://localhost:4000/api/payments",

      {

        method: "POST",

        headers: {

          "Content-Type":
          "application/json",

        },

        body: JSON.stringify({

          residentName:
          selectedResident.residentName,

          residentId:
          selectedResident.residentId,

          flatNumber:
          selectedResident.flatNumber,

          month:
          formData.month,

          paymentType:
          "Maintenance",

          maintenance:
          Number(formData.maintenance || 0),

          waterBill:
          Number(formData.waterBill || 0),

          electricityBill:
          Number(formData.electricityBill || 0),

          rent:
          Number(formData.rent || 0),

          parkingFee:
          Number(formData.parkingFee || 0),

          penalty:
          Number(formData.penalty || 0),

          otherCharges:
          Number(formData.otherCharges || 0),

          totalAmount,

          upiId:
          formData.upiId,

          paymentApp:
          "PhonePe",

          qrCode:
          formData.qrImage,

      

        }),

      }

    );

    const data = await res.json();

console.log("STATUS:", res.status);
console.log("RESPONSE:", data);

    if (res.ok) {

      alert("Bill Created");
      window.location.reload();


      // OPTIONAL RESET

      setFormData({

        month: "",

        maintenance: "",

        waterBill: "",

        electricityBill: "",

        rent: "",

        parkingFee: "",

        penalty: "",

        otherCharges: "",

        upiId: "",

        phonepe: "",

        qrImage: "",

      });

    }

    else {

      alert(data.error);

    }

  }

  catch (err) {

    console.log(err);

    alert("Bill creation failed");

  }

};

  return (

    <div className="p-6 max-w-5xl mx-auto">

      <h1
        className="
          text-3xl
          font-black
          mb-6
        "
      >
        Create Resident Bill
      </h1>


      <div
        className="
          bg-white
          rounded-3xl
          p-6
          shadow-lg
          space-y-5
        "
      >

        {/* RESIDENT SELECT */}

        <div>

          <label
            className="
              text-sm
              font-semibold
            "
          >
            Select Resident
          </label>

          <select

            onChange={(e) => {

              const resident =
                residents.find(

                  (r) =>
                    r._id ===
                    e.target.value

                );

              setSelectedResident(
                resident
              );

            }}

            className="
              w-full
              mt-2
              border
              rounded-xl
              px-4
              py-3
            "
          >

            <option>
              Choose Resident
            </option>

            {

              residents.map((r) => (

                <option
                  key={r._id}
                  value={r._id}
                >

{r.residentId} - {r.residentName}
                </option>

              ))

            }

          </select>

        </div>

        


        {/* AUTO DETAILS */}

        {

  selectedResident && (

    <div
      className="
        bg-slate-50
        rounded-2xl
        p-5

        grid
        grid-cols-1
        md:grid-cols-2
        gap-5
      "
    >

      <div>

        <p className="text-gray-500 text-sm">
          Resident ID
        </p>

        <h2
          className="
            font-black
            text-lg
            text-blue-600
          "
        >
          {selectedResident.residentId}
        </h2>

      </div>


      <div>

        <p className="text-gray-500 text-sm">
          Resident Name
        </p>

        <h2 className="font-bold text-lg">
{selectedResident.residentName}
        </h2>

      </div>


      <div>

        <p className="text-gray-500 text-sm">
          Flat Number
        </p>

        <h2 className="font-bold text-lg">
          {selectedResident.flatNumber}
        </h2>

      </div>


      <div>

        <p className="text-gray-500 text-sm">
          Block
        </p>

        <h2 className="font-bold text-lg">
          {selectedResident.block}
        </h2>

      </div>


      <div>

        <p className="text-gray-500 text-sm">
          Flat Type
        </p>

        <h2 className="font-bold text-lg">
          {selectedResident.flatType}
        </h2>

      </div>


      <div>

        <p className="text-gray-500 text-sm">
          Resident Type
        </p>

        <h2 className="font-bold text-lg">
          {selectedResident.ownerType}
        </h2>

      </div>

    </div>

  )

}


        {/* MONTH */}

       <input

  type="month"

  name="month"

  value={formData.month}

  onChange={handleChange}

  className="
    w-full
    border
    rounded-xl
    px-4
    py-3
  "
/>

        {/* BILL INPUTS */}

        <div
          className="
            grid
            grid-cols-2
            gap-4
          "
        >

          <input
            type="number"
            name="maintenance"
            placeholder="Maintenance"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="number"
            name="waterBill"
            placeholder="Water Bill"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="number"
            name="electricityBill"
            placeholder="Electricity Bill"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="number"
            name="rent"
            placeholder="Monthly Rent"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="number"
            name="parkingFee"
            placeholder="Parking Fee"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="number"
            name="penalty"
            placeholder="Penalty"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
  type="number"
  name="otherCharges"
  placeholder="Other Charges"
  onChange={handleChange}
  className="border rounded-xl px-4 py-3"
/>

        </div>


        {/* PAYMENT INFO */}

        <div
          className="
            grid
            grid-cols-2
            gap-4
          "
        >

          <input
            type="text"
            name="upiId"
            placeholder="UPI ID"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

          <input
            type="text"
            name="phonepe"
            placeholder="PhonePe Number"
            onChange={handleChange}
            className="border rounded-xl px-4 py-3"
          />

        </div>

        <div className="col-span-2">

  <label
    className="
      block
      text-sm
      font-semibold
      mb-2
    "
  >
    Upload QR Code
  </label>

  <input

    type="file"

    accept="image/*"

    onChange={(e) => {

      const file =
        e.target.files[0];

      if(!file) return;

      const reader =
        new FileReader();

      reader.onloadend = () => {

        setFormData({

          ...formData,

          qrImage:
          reader.result,

        });

      };

      reader.readAsDataURL(file);

    }}

    className="
      w-full
      border
      rounded-xl
      px-4
      py-3
      bg-white
    "
  />

</div>

{

  formData.qrImage && (

    <div
      className="
        flex
        justify-center
        mt-4
      "
    >

      <img

        src={formData.qrImage}

        alt="QR"

        className="
          w-44
          h-44

          object-cover

          rounded-2xl

          border
          shadow-lg
        "
      />

    </div>

  )

}


        {/* TOTAL */}

        <div
          className="
            bg-blue-50
            rounded-2xl
            p-5
          "
        >

          <h2
            className="
              text-xl
              font-bold
            "
          >
            Total Amount
          </h2>

          <h1
            className="
              text-4xl
              font-black
              text-blue-600
              mt-2
            "
          >
            ₹{totalAmount}
          </h1>

        </div>


        {/* BUTTON */}

        <button

          onClick={createBill}

          className="
            w-full
            bg-blue-600
            hover:bg-blue-700
            text-white
            py-4
            rounded-2xl
            font-bold
          "
        >

          Generate Bill

        </button>

      </div>

    </div>

  );

}