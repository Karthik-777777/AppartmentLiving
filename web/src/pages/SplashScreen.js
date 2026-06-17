import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function SplashScreen() {
  const navigate = useNavigate();
  const [fadeOut, setFadeOut] = useState(false);

  useEffect(() => {
    const t1 = setTimeout(() => setFadeOut(true), 2600); // start fade
    const t2 = setTimeout(() => navigate("/login"), 3200); // go to login
    return () => {
      clearTimeout(t1);
      clearTimeout(t2);
    };
  }, [navigate]);

  return (
    <div style={{ ...styles.container, opacity: fadeOut ? 0 : 1 }}>
      <img src="/splash.png" alt="Splash" style={styles.image} />

      {/* Dark gradient overlay for readability */}
      <div style={styles.overlay} />

      {/* Branding / tagline (optional) */}
      <div style={styles.content}>
        <h1 style={styles.title}>Apartment Living</h1>
        <p style={styles.tagline}>Smart community living</p>

        {/* Progress bar */}
        <div style={styles.progressWrap}>
          <div style={styles.progress} />
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    height: "100vh",
    width: "100%",
    position: "relative",
    overflow: "hidden",
    transition: "opacity 0.6s ease",
    backgroundColor: "#0b1320", // fallback while image loads
  },

  image: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
    transform: "scale(1.02)",
    animation: "zoomIn 2.5s ease forwards",
  },

  overlay: {
    position: "absolute",
    inset: 0,
    background:
      "linear-gradient(90deg, rgba(5,12,28,0.85) 0%, rgba(5,12,28,0.55) 40%, rgba(5,12,28,0.15) 70%, rgba(5,12,28,0) 100%)",
  },

  content: {
    position: "absolute",
    left: "6%",
    bottom: "12%",
    color: "#e8eefc",
    maxWidth: 420,
  },

  title: {
    margin: 0,
    fontSize: 36,
    letterSpacing: 0.5,
    fontWeight: 600,
  },

  tagline: {
    marginTop: 8,
    opacity: 0.8,
    fontSize: 14,
  },

  progressWrap: {
    marginTop: 18,
    width: "100%",
    height: 4,
    background: "rgba(255,255,255,0.2)",
    borderRadius: 10,
    overflow: "hidden",
  },

  progress: {
    width: "100%",
    height: "100%",
    background:
      "linear-gradient(90deg, #4f8cff, #4fd1c5)",
    animation: "loadBar 2.6s linear forwards",
  },
};