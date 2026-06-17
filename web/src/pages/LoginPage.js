import { useState } from "react";
import axios from "axios";
import IntroAnimation from "../components/IntroAnimation";

export default function LoginPage() {

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

    const [showIntro, setShowIntro] =
  useState(false);

const [userRole, setUserRole] =
  useState("");


  const handleLogin = async () => {

    try {

      const res = await axios.post(

        "http://localhost:4000/api/auth/login",

        {
          email,
          password,
        }

      );


      // GET DATA FROM BACKEND

      const {

        token,
        role,
        user

      } = res.data;
console.log(user);
localStorage.clear();

      // SAVE TOKEN

      localStorage.setItem(
        "token",
        token
      );


      // SAVE USER DETAILS

     // SAVE USER DETAILS

localStorage.setItem(
  "user",
  JSON.stringify(user)
);

localStorage.setItem(
  "resident",
  JSON.stringify(user)
);
localStorage.setItem(
  "role",
  role
);


      // REDIRECT USER

     setUserRole(role);

setShowIntro(true);

    }

    catch(err) {

      console.log(
        err.response?.data
      );

      alert("Invalid login");

    }

  };


  return (

    <>
{
  showIntro && (

    <IntroAnimation

      userRole={userRole}

      onComplete={() => {

        if(userRole === "admin") {

          window.location.href =
          "/admin";

        }

        else {

          window.location.href =
          "/resident";

        }

      }}

    />

  )
}

{
  !showIntro && (

    <div
      className="
        min-h-screen
        flex
        items-center
        justify-center
        bg-cover
        bg-center
      "

      style={{
        backgroundImage:
        "url('/splash.png')"
      }}
    >

      <div
        className="
          bg-white/20
          backdrop-blur-xl
          p-8
          rounded-2xl
          w-96
          shadow-xl
          border
          border-white/30
        "
      >

        <h2
          className="
            text-white
            text-2xl
            font-bold
            mb-2
            text-center
          "
        >
          NestSphere
        </h2>

        <p
          className="
            text-gray-200
            text-sm
            text-center
            mb-6
          "
        >
          Smart Community Living
        </p>


        {/* EMAIL */}

        <input

          type="email"

          placeholder="Email"

          className="
            w-full
            mb-3
            p-3
            rounded
            bg-white/30
            text-white
            placeholder-gray-200
            outline-none
          "

          value={email}

          onChange={(e) =>
            setEmail(e.target.value)
          }

        />


        {/* PASSWORD */}

        <input

          type="password"

          placeholder="Password"

          className="
            w-full
            mb-4
            p-3
            rounded
            bg-white/30
            text-white
            placeholder-gray-200
            outline-none
          "

          value={password}

          onChange={(e) =>
            setPassword(e.target.value)
          }

        />


        {/* LOGIN BUTTON */}

        <button

          onClick={handleLogin}

          className="
            w-full
            bg-gradient-to-r
            from-blue-600
            to-violet-600
            text-white
            py-3
            rounded-2xl
            font-semibold
            tracking-wide
            shadow-lg
            hover:shadow-2xl
            hover:scale-[1.02]
            transition-all
            duration-300
          "
        >

          Sign In

        </button>


        {/* REGISTER */}

        <div className="mt-4 text-center">

          <p
            className="
              text-sm
              text-gray-200
            "
          >
            Don’t have an account?
          </p>


          <button

            onClick={() =>
              (
                window.location.href =
                "/register"
              )
            }

            className="
              text-sm
              font-semibold
              text-transparent
              bg-clip-text
              bg-gradient-to-r
              from-emerald-400
              to-cyan-400
              hover:scale-[1.03]
              transition-all
              duration-300
            "
          >

            Create Account

          </button>

        </div>

      </div>

       </div>

  )
}

</>

);

}