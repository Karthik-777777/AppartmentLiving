import { useEffect, useState } from "react";

export default function ResidentProfile() {

  const [resident, setResident] =
    useState(null);
    const [isEditing, setIsEditing] =
  useState(false);
  const [showEditModal, setShowEditModal] =
  useState(false);
  

  useEffect(() => {

    const storedResident =
      JSON.parse(
        localStorage.getItem("user")
      );

    setResident(storedResident);

  }, []);

  if (!resident) {

    return (

      <div
        style={{
          padding: "40px",
          fontSize: "20px",
        }}
      >
        Loading...
      </div>

    );

  }

  return (

    <div
      style={{
        padding: "30px",
      }}
    >

      <div
        style={{
          background: "#ffffff",
          borderRadius: "32px",
          overflow: "hidden",
          boxShadow:
            "0 10px 40px rgba(0,0,0,0.08)",
        }}
      >

        {/* COVER */}

        <div
          style={{
height: "220px",
            background:
              "linear-gradient(135deg,#2563eb,#7c3aed)",
            position: "relative",
          }}
        >

          <div
            style={{
              position: "absolute",
              left: "40px",
              bottom: "30px",
              color: "white",
            }}
          >

            <h1
              style={{
                margin: 0,
fontSize: "34px",
                fontWeight: "700",
              }}
            >
              Apartment Living
            </h1>

            <p
              style={{
                marginTop: "8px",
                color: "#dbeafe",
fontSize: "16px",
              }}
            >
              Premium Resident Portal
            </p>

          </div>

        </div>

        {/* PROFILE SECTION */}

        <div
          style={{
            padding: "0 50px 50px",
marginTop: "-35px",
          }}
        >

          {/* TOP PROFILE */}
<div
  style={{
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "0 50px",
    marginTop: "-40px",
    position: "relative",
    zIndex: 10,
    flexWrap: "wrap",
    gap: "20px",
  }}
>

  {/* LEFT SIDE */}

  <div
    style={{
      display: "flex",
      alignItems: "center",
      gap: "40px",
    }}
  >

    {/* AVATAR */}

    <div
      style={{
        width: "90px",
height: "90px",
        borderRadius: "50%",
        background:
          "linear-gradient(135deg,#6366f1,#a855f7)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "white",
        fontSize: "42px",
        fontWeight: "700",
        border: "5px solid white",
        boxShadow:
          "0 10px 30px rgba(0,0,0,0.15)",
      }}
    >

      {resident.residentName?.charAt(0)}

    </div>

    {/* NAME */}

    <div
      style={{
        marginTop: "55px",
      }}
    >

      <h1
        style={{
          margin: 0,
fontSize: "30px",
fontWeight: "700",
letterSpacing: "-0.5px",
          color: "#0f172a",
        }}
      >
        {resident.residentName}
      </h1>

    </div>

  </div>

  {/* EDIT BUTTON */}

   <div
  style={{
    marginTop: "35px",
    display: "flex",
    alignItems: "center",
  }}
>

  <button

    onClick={() =>
      setShowEditModal(true)
    }

    style={{
      background:
        "linear-gradient(135deg,#2563eb,#7c3aed)",
      color: "white",
      border: "none",
      padding: "16px 34px",
      borderRadius: "18px",
      fontSize: "18px",
      fontWeight: "600",
      cursor: "pointer",
      boxShadow:
        "0 10px 25px rgba(99,102,241,0.35)",
    }}
  >

    Edit Profile

  </button>

</div>
</div>

          {/* DETAILS */}

          <div
            style={{
              marginTop: "45px",
              display: "grid",
              gridTemplateColumns:
                "repeat(2,minmax(250px,1fr))",
              gap: "35px",
            }}
          >

           <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Resident ID
  </h3>

  <p
    style={{
      margin: 0,
fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
    }}
  >
    {resident.residentId}
  </p>

</div>

           <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Flat Number
  </h3>

  <p
    style={{
      margin: 0,
      fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
    }}
  >
    {resident.flatNumber}
  </p>

</div>


            <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Email ID
  </h3>

  <p
    style={{
      margin: 0,
      fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
      wordBreak: "break-word",
    }}
  >
    {resident.email}
  </p>

</div>

            <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Resident Phone
  </h3>

  <p
    style={{
      margin: 0,
      fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
    }}
  >
    {resident.phone}
  </p>

</div>

           <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Flat Type
  </h3>

  <p
    style={{
      margin: 0,
      fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
    }}
  >
    {resident.flatType}
  </p>

</div>

            <div
  style={{
    background: "#f8fafc",
    padding: "24px",
    borderRadius: "20px",
    border: "1px solid #e2e8f0",
  }}
>

  <h3
    style={{
      marginBottom: "10px",
      color: "#64748b",
      fontSize: "15px",
      fontWeight: "600",
    }}
  >
    Owner Type
  </h3>

  <p
    style={{
      margin: 0,
      fontSize: "18px",
      fontWeight: "700",
      color: "#0f172a",
    }}
  >
    {resident.ownerType}
  </p>

</div>
          </div>

          {/* STATS CARDS */}

          <div
            style={{
              marginTop: "45px",
              display: "grid",
              gridTemplateColumns:
                "repeat(2,minmax(280px,1fr))",
              gap: "25px",
            }}
          >

            {/* CARD */}

            <div
              style={{
                background: "#f8fafc",
                borderRadius: "24px",
                padding: "28px",
                border: "1px solid #e2e8f0",
              }}
            >

              <h2
                style={{
                  margin: 0,
fontSize: "24px",
                  color: "#2563eb",
                }}
              >
                92%
              </h2>

              <p
                style={{
                  color: "#64748b",
fontSize: "15px",                }}
              >
                Profile Completion
              </p>

              <div
                style={{
                  marginTop: "20px",
                  width: "100%",
                  height: "10px",
                  borderRadius: "20px",
                  background: "#dbeafe",
                  overflow: "hidden",
                }}
              >

                <div
                  style={{
                    width: "92%",
                    height: "100%",
                    background:
                      "linear-gradient(90deg,#2563eb,#7c3aed)",
                  }}
                />

              </div>

            </div>

            {/* CARD */}

            <div
              style={{
                background: "#f8fafc",
                borderRadius: "24px",
                padding: "28px",
                border: "1px solid #e2e8f0",
              }}
            >

              <h2
                style={{
                  margin: 0,
                  fontSize: "42px",
                  color: "#2563eb",
                }}
              >
                2026
              </h2>

              <p
                style={{
                  color: "#64748b",
fontSize: "15px",
                }}
              >
                Membership Since
              </p>

              <h3
                style={{
                  marginTop: "20px",
                }}
              >
                Trusted Resident
              </h3>

            </div>

            {/* CARD */}

            <div
              style={{
                background: "#f8fafc",
                borderRadius: "24px",
                padding: "28px",
                border: "1px solid #e2e8f0",
              }}
            >

              <h2
                style={{
                  margin: 0,
                  fontSize: "42px",
                  color: "#16a34a",
                }}
              >
                ₹8999
              </h2>

              <p
                style={{
                  color: "#64748b",
                  fontSize: "18px",
                }}
              >
                Last Payment
              </p>

              <h3
                style={{
                  marginTop: "20px",
                }}
              >
                Paid Successfully
              </h3>

            </div>

            {/* CARD */}

            <div
              style={{
                background: "#f8fafc",
                borderRadius: "24px",
                padding: "28px",
                border: "1px solid #e2e8f0",
              }}
            >

              <h2
                style={{
                  margin: 0,
                  fontSize: "18px",
                  color: "#dc2626",
                  lineHeight: "1.3",
                }}
              >
                +91 9876543210
              </h2>

              <p
                style={{
                  color: "#64748b",
                  fontSize: "18px",
                }}
              >
                Emergency Contact
              </p>

              <h3
                style={{
                  marginTop: "20px",
                }}
              >
                Apartment Security Desk
              </h3>

            </div>

          </div>

        </div>

      </div>

      {
  showEditModal && (

    <div
      style={{
        position: "fixed",
        inset: 0,
        background:
          "rgba(0,0,0,0.45)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 999,
      }}
    >

      <div
        style={{
          width: "500px",
          background: "white",
          borderRadius: "28px",
          padding: "35px",
          boxShadow:
            "0 25px 60px rgba(0,0,0,0.25)",
        }}
      >

        <h2
          style={{
            marginTop: 0,
            fontSize: "30px",
            color: "#0f172a",
          }}
        >
          Edit Profile
        </h2>

        {/* EMAIL */}

        <input
          value={resident.email}

          onChange={(e)=>
            setResident({
              ...resident,
              email: e.target.value
            })
          }

          placeholder="Email"

          style={{
            width: "100%",
            padding: "15px",
            marginBottom: "18px",
            borderRadius: "14px",
            border: "1px solid #cbd5e1",
            fontSize: "16px",
          }}
        />

        {/* PHONE */}

        <input
          value={resident.phone}

          onChange={(e)=>
            setResident({
              ...resident,
              phone: e.target.value
            })
          }

          placeholder="Phone"

          style={{
            width: "100%",
            padding: "15px",
            marginBottom: "18px",
            borderRadius: "14px",
            border: "1px solid #cbd5e1",
            fontSize: "16px",
          }}
        />

        {/* FLAT TYPE */}

        <input
          value={resident.flatType}

          onChange={(e)=>
            setResident({
              ...resident,
              flatType: e.target.value
            })
          }

          placeholder="Flat Type"

          style={{
            width: "100%",
            padding: "15px",
            marginBottom: "18px",
            borderRadius: "14px",
            border: "1px solid #cbd5e1",
            fontSize: "16px",
          }}
        />

        {/* OWNER TYPE */}

        <input
          value={resident.ownerType}

          onChange={(e)=>
            setResident({
              ...resident,
              ownerType: e.target.value
            })
          }

          placeholder="Owner Type"

          style={{
            width: "100%",
            padding: "15px",
            marginBottom: "25px",
            borderRadius: "14px",
            border: "1px solid #cbd5e1",
            fontSize: "16px",
          }}
        />

        {/* BUTTONS */}

        <div
          style={{
            display: "flex",
            justifyContent: "flex-end",
            gap: "15px",
          }}
        >

          <button

            onClick={() =>
              setShowEditModal(false)
            }

            style={{
              padding: "14px 24px",
              borderRadius: "14px",
              border: "none",
              background: "#e2e8f0",
              cursor: "pointer",
              fontWeight: "600",
            }}
          >

            Cancel

          </button>

          <button

            onClick={() => {

              localStorage.setItem(
                "user",
                JSON.stringify(resident)
              );

              setShowEditModal(false);

            }}

            style={{
              padding: "14px 28px",
              borderRadius: "14px",
              border: "none",
              background:
                "linear-gradient(135deg,#2563eb,#7c3aed)",
              color: "white",
              cursor: "pointer",
              fontWeight: "600",
            }}
          >

            Save Changes

          </button>

        </div>

      </div>

    </div>

  )
}

    </div>

  );

}