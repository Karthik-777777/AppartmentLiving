import GoogleNearbyMap from "../components/dashboard/GoogleNearbyMap";

export default function ResidentMaps() {

  return (

    <div className="
      min-h-screen
      bg-slate-100
      p-6
    ">

      {/* PAGE HEADER */}
      <div className="mb-6">

        <h1 className="
          text-4xl
          font-bold
          text-slate-800
        ">
          Smart Maps
        </h1>

        <p className="
          text-slate-500
          mt-2
        ">
          Live nearby hospitals,
          pharmacies, supermarkets
          and emergency services
        </p>

      </div>

      {/* MAP COMPONENT */}
      <GoogleNearbyMap />

    </div>

  );

}