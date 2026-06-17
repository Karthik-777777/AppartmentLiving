import React, { useEffect, useState } from "react";

import {
  GoogleMap,
  Marker,
  TrafficLayer,
  useJsApiLoader,
} from "@react-google-maps/api";

const containerStyle = {
  width: "100%",
  height: "420px",
  borderRadius: "24px",
};

export default function GoogleNearbyMap() {

  // USER LOCATION
  const [userLocation, setUserLocation] = useState({
    lat: 13.0827,
    lng: 80.2707,
  });

  // REAL PLACES
  const [places, setPlaces] = useState([]);

  // FILTER
  const [selectedCategory, setSelectedCategory] =
    useState("all");

  // LOAD GOOGLE MAPS
  const { isLoaded } = useJsApiLoader({
    googleMapsApiKey: "AIzaSyBh0EilleKwXJwGdBjCTOkm10puHaXGB3w",
    libraries: ["places", "geometry"],
  });

  // GET LIVE LOCATION
  useEffect(() => {

    navigator.geolocation.getCurrentPosition(

      (position) => {

        setUserLocation({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });

      },

      () => {

        console.log("Location permission denied");

      }

    );

  }, []);

  // FETCH REAL PLACES
  useEffect(() => {

    if (!window.google || !isLoaded) return;

    const service =
      new window.google.maps.places.PlacesService(
        document.createElement("div")
      );

    const allResults = [];

    const searchTypes = [
      "hospital",
      "pharmacy",
      "grocery_or_supermarket",
    ];

    searchTypes.forEach((type) => {

      const request = {
        location: userLocation,
        radius: 5000,
        query: type,
      };

      service.textSearch(
        request,
        (results, status) => {

          if (
            status ===
            window.google.maps.places.PlacesServiceStatus.OK
          ) {

            allResults.push(...results);

            // REMOVE DUPLICATES
            const uniquePlaces = allResults.filter(
              (place, index, self) =>
                index ===
                self.findIndex(
                  (p) => p.place_id === place.place_id
                )
            );

            // FILTER ONLY REQUIRED TYPES
            const filteredPlaces =
              uniquePlaces.filter(
                (place) =>
                  place.types?.includes("hospital") ||
                  place.types?.includes("pharmacy") ||
                  place.types?.includes(
                    "grocery_or_supermarket"
                  )
              );

            setPlaces(filteredPlaces);

          } else {

            console.log("ERROR:", status);

          }

        }
      );

    });

  }, [isLoaded, userLocation]);

  // LOADING
  if (!isLoaded) {

    return (
      <div className="text-xl">
        Loading map...
      </div>
    );

  }

  // FILTERED PLACES
  const filteredPlaces = places.filter((place) => {

    if (selectedCategory === "all") return true;

    if (
      selectedCategory === "hospital" &&
      place.types?.includes("hospital")
    )
      return true;

    if (
      selectedCategory === "pharmacy" &&
      place.types?.includes("pharmacy")
    )
      return true;

    if (
      selectedCategory === "supermarket" &&
      place.types?.includes(
        "grocery_or_supermarket"
      )
    )
      return true;

    return false;

  });

  return (

    <div className="
      bg-white/75
      backdrop-blur-xl
      rounded-[32px]
      p-6
      shadow-xl
      border border-white/40
    ">

      {/* HEADER */}
      <div className="flex items-center justify-between mb-5">

        <div>

          <h1 className="
            text-[24px]
            font-bold
            text-slate-800
          ">
            Nearby Essentials
          </h1>

          <p className="
            text-slate-500
            mt-1
          ">
            Real-time nearby hospitals,
            pharmacies & supermarkets
          </p>

        </div>

        {/* LIVE BADGE */}
        <div className="
          flex items-center gap-2
          text-green-600
          font-medium
          text-sm
        ">

          <div className="
            w-2 h-2
            bg-green-500
            rounded-full
            animate-pulse
          "></div>

          Live

        </div>

      </div>

      {/* FILTER BUTTONS */}
      <div className="
        flex gap-3
        mb-5
        flex-wrap
      ">

        <button
          onClick={() => setSelectedCategory("all")}
          className={`
            px-4 py-2 rounded-2xl
            font-medium transition
            ${
              selectedCategory === "all"
                ? "bg-blue-600 text-white"
                : "bg-slate-100 text-slate-700"
            }
          `}
        >
          All
        </button>

        <button
          onClick={() =>
            setSelectedCategory("hospital")
          }
          className={`
            px-4 py-2 rounded-2xl
            font-medium transition
            ${
              selectedCategory === "hospital"
                ? "bg-red-500 text-white"
                : "bg-slate-100 text-slate-700"
            }
          `}
        >
          Hospitals
        </button>

        <button
          onClick={() =>
            setSelectedCategory("pharmacy")
          }
          className={`
            px-4 py-2 rounded-2xl
            font-medium transition
            ${
              selectedCategory === "pharmacy"
                ? "bg-green-500 text-white"
                : "bg-slate-100 text-slate-700"
            }
          `}
        >
          Pharmacies
        </button>

        <button
          onClick={() =>
            setSelectedCategory("supermarket")
          }
          className={`
            px-4 py-2 rounded-2xl
            font-medium transition
            ${
              selectedCategory === "supermarket"
                ? "bg-blue-500 text-white"
                : "bg-slate-100 text-slate-700"
            }
          `}
        >
          Supermarkets
        </button>

      </div>

      {/* GOOGLE MAP */}
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={userLocation}
        zoom={13}
      >

        {/* LIVE TRAFFIC */}
        <TrafficLayer />

        {/* USER LOCATION */}
        <Marker
          position={userLocation}
          icon={{
            url:
              "http://maps.google.com/mapfiles/ms/icons/red-dot.png",
          }}
        />

        {/* NEARBY PLACES */}
        {filteredPlaces
          .slice(0, 8)
          .map((place, index) => {

            let iconColor =
              "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";

            if (
              place.types?.includes("hospital")
            ) {

              iconColor =
                "http://maps.google.com/mapfiles/ms/icons/red-dot.png";

            }

            if (
              place.types?.includes("pharmacy")
            ) {

              iconColor =
                "http://maps.google.com/mapfiles/ms/icons/green-dot.png";

            }

            if (
              place.types?.includes(
                "grocery_or_supermarket"
              )
            ) {

              iconColor =
                "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";

            }

            return (

              <Marker
                key={index}
                position={{
                  lat:
                    place.geometry.location.lat(),
                  lng:
                    place.geometry.location.lng(),
                }}
                icon={{
                  url: iconColor,
                }}
              />

            );

          })}

      </GoogleMap>

      {/* CARDS HEADER */}
      <div className="
        flex items-center
        justify-between
        mt-7 mb-5
      ">

        <h2 className="
          text-[20px]
          font-bold
          text-slate-800
        ">
          Emergency & Essentials
        </h2>

        <p className="
          text-red-500
          font-medium
        ">
          Live nearby services
        </p>

      </div>

      {/* PLACE CARDS */}
     <div className="
  grid grid-cols-1 md:grid-cols-2
  gap-4
  max-h-[420px]
  overflow-y-auto
  pr-1
">

  {filteredPlaces.length === 0 ? (

    <div className="
      text-slate-500
      text-sm
    ">
      Loading nearby places...
    </div>

  ) : (

   [
  ...filteredPlaces
    .filter((place) =>
      place.types?.includes("hospital")
    )
    .slice(0, 2),

  ...filteredPlaces
    .filter((place) =>
      place.types?.includes("pharmacy")
    )
    .slice(0, 1),

  ...filteredPlaces
    .filter((place) =>
      place.types?.includes(
        "grocery_or_supermarket"
      )
    )
    .slice(0, 1),

].map((place, index) => {

        const distance =
          (
            window.google.maps.geometry.spherical.computeDistanceBetween(
              new window.google.maps.LatLng(
                userLocation
              ),
              place.geometry.location
            ) / 1000
          ).toFixed(1);

        return (

          <div
            key={index}
            className="
              bg-white
              rounded-2xl
              p-4
              border border-slate-100
              shadow-sm
              hover:shadow-md
              transition
            "
          >

            {/* NAME */}
            <h2 className="
              text-[16px]
              font-semibold
              text-slate-800
              leading-6
              line-clamp-2
            ">
              {place.name}
            </h2>

            {/* CATEGORY */}
            <p className="
              text-[13px]
              text-slate-500
              mt-2
            ">

              {place.types?.includes("hospital") &&
                "🏥 Hospital"}

              {place.types?.includes("pharmacy") &&
                "💊 Pharmacy"}

              {place.types?.includes(
                "grocery_or_supermarket"
              ) &&
                "🛒 Supermarket"}

            </p>

            {/* STATUS */}
            <p className="
              mt-2
              text-[13px]
              font-medium
            ">

              {place.opening_hours?.open_now ? (

                <span className="text-green-600">
                  ● Open
                </span>

              ) : (

                <span className="text-red-500">
                  ● Closed
                </span>

              )}

            </p>

            {/* RATING */}
            <p className="
              text-[13px]
              text-yellow-500
              mt-1
            ">
              ⭐ {place.rating || "4.2"}
            </p>

            {/* DISTANCE */}
            <p className="
              text-[13px]
              text-blue-600
              mt-1
            ">
              📍 {distance} km away
            </p>

           {/* BUTTON */}
<a
  href={`https://www.google.com/maps/dir/?api=1&destination=${place.geometry.location.lat()},${place.geometry.location.lng()}`}
  target="_blank"
  rel="noreferrer"
  className="
    inline-flex
    items-center
    gap-2
    mt-4
    bg-gradient-to-r
    from-blue-600
    to-violet-600
    text-white
    text-[13px]
    font-semibold
    tracking-wide
    px-5
    py-2.5
    rounded-2xl
    shadow-lg
    hover:shadow-2xl
    hover:scale-[1.03]
    transition-all
    duration-300
  "
>
  📍 Directions
</a>

          </div>
              );

            }
          )

        )}

      </div>

    </div>

  );

}