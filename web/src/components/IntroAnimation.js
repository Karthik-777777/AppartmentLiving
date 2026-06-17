// IntroAnimation.jsx
// Full-screen premium 3D login transition animation
// Stages: Building reveal → Module icons → Welcome text → Zoom + fade → Navigate

import React, { useEffect, useRef, useState, Suspense } from "react";
import { Canvas, useFrame, useThree } from "@react-three/fiber";
import { Environment, Stars, OrbitControls, Float } from "@react-three/drei";
import { motion, AnimatePresence } from "framer-motion";
import * as THREE from "three";
import ApartmentBuilding from "./ApartmentModel";

// ─── Module icon definitions ──────────────────────────────────────────────────
const MODULE_ICONS = [
  {
    id: "residents",
    label: "Residents",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    ),
    angle: 0,
    color: "#38bdf8",
    glow: "#0ea5e9",
  },
  {
    id: "payments",
    label: "Payments",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
        <line x1="1" y1="10" x2="23" y2="10" />
      </svg>
    ),
    angle: 90,
    color: "#a78bfa",
    glow: "#7c3aed",
  },
  {
    id: "complaints",
    label: "Complaints",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
      </svg>
    ),
    angle: 180,
    color: "#34d399",
    glow: "#059669",
  },
  {
    id: "notices",
    label: "Notices",
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
    ),
    angle: 270,
    color: "#fb923c",
    glow: "#ea580c",
  },
];

// ─── Camera animation controller (inside Canvas) ─────────────────────────────
function CameraAnimator({ stage }) {
  const { camera } = useThree();
  const targetZ = useRef(6);
  const targetY = useRef(0.5);

  useEffect(() => {
    if (stage >= 3) {
      targetZ.current = 4.2; // zoom in on stage 3+
      targetY.current = 0.2;
    }
  }, [stage]);

  useFrame(() => {
    camera.position.z += (targetZ.current - camera.position.z) * 0.025;
    camera.position.y += (targetY.current - camera.position.y) * 0.025;
    camera.lookAt(0, 0, 0);
  });

  return null;
}

// ─── Orbit light rig ─────────────────────────────────────────────────────────
function LightRig() {
  const lightRef = useRef();
  useFrame((state) => {
    if (!lightRef.current) return;
    lightRef.current.position.x = Math.sin(state.clock.elapsedTime * 0.5) * 5;
    lightRef.current.position.z = Math.cos(state.clock.elapsedTime * 0.5) * 5;
  });
  return (
    <>
      <ambientLight intensity={0.25} />
      <directionalLight ref={lightRef} intensity={1.8} color="#93c5fd" position={[5, 4, 5]} />
      <pointLight color="#818cf8" intensity={2.5} position={[-3, 2, -3]} distance={10} />
      <pointLight color="#38bdf8" intensity={1.5} position={[3, -1, 3]} distance={8} />
      <pointLight color="#a78bfa" intensity={1.0} position={[0, 5, 0]} distance={12} />
    </>
  );
}

// ─── Floating orbit icon (HTML overlay, not 3D) ───────────────────────────────
function ModuleIcon({ icon: def, index, total, isVisible }) {
  const radius = window.innerWidth < 640 ? 130 : 200;
  const angleRad = (def.angle * Math.PI) / 180;
  const x = Math.sin(angleRad) * radius;
  const y = -Math.cos(angleRad) * radius;

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          key={def.id}
          initial={{ opacity: 0, scale: 0, x: 0, y: 0 }}
          animate={{
            opacity: 1,
            scale: 1,
            x,
            y,
            transition: {
              delay: index * 0.18,
              duration: 0.6,
              type: "spring",
              stiffness: 200,
              damping: 18,
            },
          }}
          exit={{ opacity: 0, scale: 0, transition: { duration: 0.3 } }}
          style={{
            position: "absolute",
            left: "50%",
            top: "50%",
            marginLeft: -32,
            marginTop: -32,
            filter: `drop-shadow(0 0 12px ${def.glow})`,
          }}
        >
          {/* Float bob */}
          <motion.div
            animate={{ y: [0, -8, 0] }}
            transition={{ duration: 2.5 + index * 0.3, repeat: Infinity, ease: "easeInOut" }}
          >
            <div
              className="w-16 h-16 rounded-2xl flex flex-col items-center justify-center gap-1 cursor-default select-none"
              style={{
                background: `linear-gradient(135deg, ${def.color}22, ${def.glow}44)`,
                border: `1.5px solid ${def.color}66`,
                backdropFilter: "blur(12px)",
                boxShadow: `0 0 20px ${def.glow}55, inset 0 1px 0 ${def.color}33`,
              }}
            >
              <div style={{ color: def.color }}>{def.icon}</div>
              <span className="text-[9px] font-semibold tracking-wide" style={{ color: def.color }}>
                {def.label}
              </span>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

// ─── Scanline overlay ─────────────────────────────────────────────────────────
function ScanlineOverlay() {
  return (
    <div
      className="absolute inset-0 pointer-events-none"
      style={{
        backgroundImage:
          "repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,0,0,0.07) 2px, rgba(0,0,0,0.07) 4px)",
        zIndex: 5,
      }}
    />
  );
}

// ─── Main IntroAnimation component ───────────────────────────────────────────
/**
 * @param {Object} props
 * @param {"admin"|"resident"} props.userRole
 * @param {Function} props.onComplete  - called when animation ends
 */
export default function IntroAnimation({ userRole = "resident", onComplete }) {
  const [stage, setStage] = useState(0);
  // stage 0 = canvas mounting / building reveal
  // stage 1 = module icons
  // stage 2 = welcome text
  // stage 3 = zoom + fade out

  const [globalOpacity, setGlobalOpacity] = useState(1);

  // Stage timeline
  useEffect(() => {
   const timers = [
  setTimeout(() => setStage(1), 1500), // building showcase
  setTimeout(() => setStage(2), 2500), // icons appear
  setTimeout(() => setStage(3), 3500), // welcome text
  setTimeout(() => setGlobalOpacity(0), 4500), // fade
  setTimeout(() => onComplete(), 5000), // dashboard
];
    return () => timers.forEach(clearTimeout);
  }, [onComplete]);

  const welcomeLine1 = userRole === "admin" ? "Welcome, Administrator" : "Welcome Back";
  const welcomeLine2 = "Apartment Living";

  return (
    <motion.div
      className="fixed inset-0 z-[9999] overflow-hidden"
      style={{
        background: "radial-gradient(ellipse at center, #0c1220 0%, #060b14 60%, #020408 100%)",
      }}
      animate={{ opacity: globalOpacity }}
      transition={{ duration: 0.45, ease: "easeInOut" }}
    >
      {/* ── Subtle grid ── */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          backgroundImage:
            "linear-gradient(rgba(56,189,248,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(56,189,248,0.04) 1px, transparent 1px)",
          backgroundSize: "60px 60px",
          zIndex: 1,
        }}
      />

      {/* ── Three.js Canvas ── */}
      <div className="absolute inset-0" style={{ zIndex: 2 }}>
        <Canvas
          camera={{ position: [0, 0.5, 6], fov: 45 }}
          shadows
          gl={{ antialias: true, alpha: true }}
        >
          <Suspense fallback={null}>
            <LightRig />
            <Stars radius={60} depth={50} count={1800} factor={3} saturation={0.5} fade speed={0.6} />
            <ApartmentBuilding />
            <CameraAnimator stage={stage} />
          </Suspense>
        </Canvas>
      </div>

      {/* ── Scanlines ── */}
      <ScanlineOverlay />

      {/* ── Module icons (HTML overlay) ── */}
      <div className="absolute inset-0 pointer-events-none" style={{ zIndex: 10 }}>
        {MODULE_ICONS.map((def, i) => (
          <ModuleIcon key={def.id} icon={def} index={i} total={MODULE_ICONS.length} isVisible={stage >= 1} />
        ))}
      </div>

      {/* ── Welcome text ── */}
      <div
        className="absolute inset-0 flex flex-col items-center justify-end pb-16 sm:pb-20 pointer-events-none"
        style={{ zIndex: 15 }}
      >
        <AnimatePresence>
          {stage >= 2 && (
            <motion.div
              key="text"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
              transition={{ duration: 0.55, ease: [0.22, 1, 0.36, 1] }}
              className="text-center space-y-2 px-6"
            >
              <motion.p
                initial={{ opacity: 0, letterSpacing: "0.4em" }}
                animate={{ opacity: 1, letterSpacing: "0.12em" }}
                transition={{ delay: 0.1, duration: 0.7 }}
                className="text-xs sm:text-sm font-semibold uppercase tracking-[0.3em] text-sky-400"
              >
                {userRole === "admin" ? "Administrator Access" : "Resident Portal"}
              </motion.p>

              <motion.h1
                initial={{ opacity: 0, scale: 0.88 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.2, duration: 0.6, type: "spring", stiffness: 180 }}
                className="text-3xl sm:text-5xl font-bold tracking-tight"
                style={{
                  background: "linear-gradient(135deg, #f8fafc 0%, #93c5fd 45%, #a5b4fc 100%)",
                  WebkitBackgroundClip: "text",
                  WebkitTextFillColor: "transparent",
                  textShadow: "none",
                  fontFamily: "'Segoe UI', system-ui, sans-serif",
                }}
              >
                {welcomeLine1}
              </motion.h1>

              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 0.7 }}
                transition={{ delay: 0.4, duration: 0.5 }}
                className="text-base sm:text-xl font-light tracking-widest text-slate-300"
              >
                {welcomeLine2}
              </motion.p>

              {/* Loading bar */}
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
                className="mt-4 flex flex-col items-center gap-2"
              >
                <div className="w-40 sm:w-56 h-[2px] bg-slate-700 rounded-full overflow-hidden">
                  <motion.div
                    className="h-full rounded-full"
                    style={{ background: "linear-gradient(90deg, #38bdf8, #818cf8)" }}
                    initial={{ width: "0%" }}
                    animate={{ width: "100%" }}
                    transition={{ delay: 0.2, duration: 0.9, ease: "easeInOut" }}
                  />
                </div>
                <motion.span
                  animate={{ opacity: [0.4, 1, 0.4] }}
                  transition={{ duration: 1.4, repeat: Infinity }}
                  className="text-[10px] tracking-[0.25em] text-slate-500 uppercase"
                >
                  Loading Dashboard…
                </motion.span>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* ── Logo / brand top-left ── */}
      <motion.div
        initial={{ opacity: 0, x: -20 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ delay: 0.3, duration: 0.5 }}
        className="absolute top-5 left-5 sm:top-7 sm:left-7 flex items-center gap-2"
        style={{ zIndex: 20 }}
      >
        <div
          className="w-8 h-8 rounded-lg flex items-center justify-center"
          style={{ background: "linear-gradient(135deg, #38bdf8, #818cf8)", boxShadow: "0 0 16px #38bdf866" }}
        >
          <svg viewBox="0 0 24 24" fill="white" className="w-4 h-4">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
            <polyline points="9 22 9 12 15 12 15 22" fill="none" stroke="white" strokeWidth="2" />
          </svg>
        </div>
        <span className="text-xs font-semibold tracking-widest text-slate-300 uppercase hidden sm:block">
          Apartment Living
        </span>
      </motion.div>

      {/* ── Corner glow orbs ── */}
      <div
        className="absolute -top-32 -left-32 w-96 h-96 rounded-full pointer-events-none"
        style={{ background: "radial-gradient(circle, rgba(56,189,248,0.08) 0%, transparent 70%)", zIndex: 1 }}
      />
      <div
        className="absolute -bottom-32 -right-32 w-96 h-96 rounded-full pointer-events-none"
        style={{ background: "radial-gradient(circle, rgba(129,140,248,0.08) 0%, transparent 70%)", zIndex: 1 }}
      />
    </motion.div>
  );
}