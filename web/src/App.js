import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./styles/theme.css";

import SplashScreen from "./pages/SplashScreen";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AdminDashboard from "./pages/AdminDashboard";
import ResidentDashboard from "./pages/ResidentDashboard";
import ResidentMaps from "./pages/ResidentMaps";
import MyComplaints from "./pages/MyComplaints";
import ResidentLayout from "./layouts/ResidentLayout";
import AdminComplaints from "./pages/AdminComplaints";
import MaintenancePage from "./pages/MaintenancePage";
import AdminMaintenance from "./pages/AdminMaintenance";
import AdminResidents from "./pages/AdminResidents";
import AdminNotices from "./pages/AdminNotices";
import ResidentNotices from "./pages/ResidentNotices";
import AdminPayments from "./pages/AdminPayments";
import AdminResidentProfile from "./pages/admin/AdminResidentProfile";
import CreateBill from "./pages/admin/CreateBill";
import ResidentPayments from "./pages/resident/ResidentPayments";
import ResidentProfile from "./pages/resident/ResidentProfile";
import AdminProfile
from "./pages/AdminProfile";
function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* Splash Screen */}
        <Route path="/" element={<SplashScreen />} />

        {/* Login Page */}
        <Route path="/login" element={<LoginPage />} />

        {/* Register Page */}
        <Route path="/register" element={<RegisterPage />} />

        {/* Admin Dashboard */}
        <Route path="/admin" element={<AdminDashboard />} />
          <Route
  path="/admin/complaints"
  element={<AdminComplaints />}
/>
<Route
  path="/admin/payments"
  element={<AdminPayments />}
/>
<Route
  path="/admin/create-bill"
  element={<CreateBill />}
/>
<Route
  path="/admin/maintenance"
  element={<AdminMaintenance />}
/>
<Route
  path="/admin/residents"
  element={<AdminResidents />}

/>
<Route
  path="/admin/profile"
  element={<AdminProfile />}
/>

<Route

  path="/admin/resident/:id"

  element={
    <AdminResidentProfile />
  }

/>
<Route
  path="/admin/notices"
  element={<AdminNotices />}
/>

        
        <Route path="/resident" element={<ResidentLayout />}>

       

  <Route
    index
    element={<ResidentDashboard />}
    
  />
  <Route
  path="maintenance"
  element={<MaintenancePage />}
/>

  <Route
    path="complaints"
    element={<MyComplaints />}
  />
<Route
  path="payments"
  element={<ResidentPayments />}
/>
  <Route
    path="maps"
    element={<ResidentMaps />}
  />
  <Route
  path="notices"
  element={<ResidentNotices />}
/>

<Route
  path="/resident/profile"
  element={<ResidentProfile />}
/>


</Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;