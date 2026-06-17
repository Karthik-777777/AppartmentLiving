import Sidebar from "../components/Sidebar";
import { Outlet } from "react-router-dom";

export default function ResidentLayout() {

  return (

    <div
      style={{
        display: "flex",
      }}
    >

      <Sidebar />

      <div
        style={{
          flex: 1,
        }}
      >

        <Outlet />

      </div>

    </div>

  );

}