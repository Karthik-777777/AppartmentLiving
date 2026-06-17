// ApartmentModel.jsx
// 3D Apartment Building built with React Three Fiber primitives (no external 3D assets needed)

import React, { useRef, useMemo } from "react";
import { useFrame } from "@react-three/fiber";
import { MeshDistortMaterial, Sphere, Box } from "@react-three/drei";
import * as THREE from "three";

// ─── Single floor of the building ────────────────────────────────────────────
function Floor({ y, width = 1.6, depth = 1.2, color = "#1e293b", windowColor = "#7dd3fc" }) {
  const floorRef = useRef();

  // Generate window positions for this floor
  const windows = useMemo(() => {
    const wins = [];
    const cols = 4;
    const spacing = width / (cols + 1);
    for (let i = 1; i <= cols; i++) {
      wins.push({ x: -width / 2 + spacing * i, z: depth / 2 + 0.01 });
      wins.push({ x: -width / 2 + spacing * i, z: -depth / 2 - 0.01 });
    }
    return wins;
  }, [width, depth]);

  return (
    <group ref={floorRef} position={[0, y, 0]}>
      {/* Main floor slab */}
      <mesh castShadow receiveShadow>
        <boxGeometry args={[width, 0.45, depth]} />
        <meshStandardMaterial color={color} metalness={0.3} roughness={0.6} />
      </mesh>

      {/* Floor edge trim */}
      <mesh position={[0, 0.23, 0]}>
        <boxGeometry args={[width + 0.05, 0.04, depth + 0.05]} />
        <meshStandardMaterial color="#334155" metalness={0.5} roughness={0.3} />
      </mesh>

      {/* Windows front & back */}
      {windows.map((w, i) => (
        <mesh key={i} position={[w.x, 0, w.z]}>
          <boxGeometry args={[0.22, 0.28, 0.02]} />
          <meshStandardMaterial
            color={windowColor}
            emissive={windowColor}
            emissiveIntensity={0.6}
            transparent
            opacity={0.85}
          />
        </mesh>
      ))}
    </group>
  );
}

// ─── Roof / penthouse cap ─────────────────────────────────────────────────────
function Roof({ y }) {
  return (
    <group position={[0, y, 0]}>
      <mesh castShadow>
        <boxGeometry args={[1.4, 0.3, 1.0]} />
        <meshStandardMaterial color="#0f172a" metalness={0.6} roughness={0.3} />
      </mesh>
      {/* Antenna */}
      <mesh position={[0, 0.45, 0]}>
        <cylinderGeometry args={[0.015, 0.015, 0.6, 8]} />
        <meshStandardMaterial color="#94a3b8" metalness={0.9} />
      </mesh>
      {/* Antenna light */}
      <mesh position={[0, 0.76, 0]}>
        <sphereGeometry args={[0.04, 8, 8]} />
        <meshStandardMaterial color="#f87171" emissive="#f87171" emissiveIntensity={2} />
      </mesh>
    </group>
  );
}

// ─── Floating particle cloud ──────────────────────────────────────────────────
function Particles({ count = 120 }) {
  const meshRef = useRef();

  const [positions, colors] = useMemo(() => {
    const pos = new Float32Array(count * 3);
    const col = new Float32Array(count * 3);
    const palette = [
      new THREE.Color("#38bdf8"),
      new THREE.Color("#818cf8"),
      new THREE.Color("#a78bfa"),
      new THREE.Color("#7dd3fc"),
    ];
    for (let i = 0; i < count; i++) {
      const r = 2.5 + Math.random() * 2.5;
      const theta = Math.random() * Math.PI * 2;
      const phi = (Math.random() - 0.5) * Math.PI;
      pos[i * 3] = r * Math.cos(theta) * Math.cos(phi);
      pos[i * 3 + 1] = r * Math.sin(phi) * 1.2;
      pos[i * 3 + 2] = r * Math.sin(theta) * Math.cos(phi);
      const c = palette[Math.floor(Math.random() * palette.length)];
      col[i * 3] = c.r;
      col[i * 3 + 1] = c.g;
      col[i * 3 + 2] = c.b;
    }
    return [pos, col];
  }, [count]);

  useFrame((state) => {
    if (!meshRef.current) return;
    meshRef.current.rotation.y = state.clock.elapsedTime * 0.08;
    meshRef.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.05) * 0.05;
  });

  return (
    <points ref={meshRef}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[positions, 3]} />
        <bufferAttribute attach="attributes-color" args={[colors, 3]} />
      </bufferGeometry>
      <pointsMaterial size={0.045} vertexColors transparent opacity={0.85} sizeAttenuation />
    </points>
  );
}

// ─── Glowing base ring ────────────────────────────────────────────────────────
function BaseRing() {
  const ringRef = useRef();
  useFrame((state) => {
    if (!ringRef.current) return;
    ringRef.current.rotation.y = state.clock.elapsedTime * 0.4;
  });
  return (
    <group ref={ringRef} position={[0, -1.85, 0]}>
      <mesh>
        <torusGeometry args={[1.4, 0.025, 16, 100]} />
        <meshStandardMaterial color="#38bdf8" emissive="#38bdf8" emissiveIntensity={1.5} transparent opacity={0.7} />
      </mesh>
      <mesh rotation={[0, Math.PI / 4, 0]}>
        <torusGeometry args={[1.7, 0.012, 16, 100]} />
        <meshStandardMaterial color="#818cf8" emissive="#818cf8" emissiveIntensity={1.2} transparent opacity={0.5} />
      </mesh>
    </group>
  );
}

// ─── Full apartment building ──────────────────────────────────────────────────
export default function ApartmentBuilding({ rotationProgress = 0 }) {
  const groupRef = useRef();

  const floorCount = 7;
  const floorHeight = 0.52;
  const baseY = -1.6;

  // Alternating floor colors for realism
  const floorColors = ["#1e293b", "#172033", "#1e293b", "#172033", "#1e293b", "#172033", "#1e293b"];

  useFrame((state) => {
    if (!groupRef.current) return;
    // Gentle auto-rotation + bob
    groupRef.current.rotation.y = state.clock.elapsedTime * 0.35 + rotationProgress * Math.PI * 2;
    groupRef.current.position.y = Math.sin(state.clock.elapsedTime * 0.8) * 0.06;
  });

  return (
    <group ref={groupRef}>
      {/* Base podium */}
      <mesh position={[0, baseY + 0.12, 0]} receiveShadow>
        <boxGeometry args={[2.0, 0.24, 1.5]} />
        <meshStandardMaterial color="#0f172a" metalness={0.4} roughness={0.5} />
      </mesh>

      {/* Floors */}
      {Array.from({ length: floorCount }).map((_, i) => (
        <Floor
          key={i}
          y={baseY + 0.24 + i * floorHeight + floorHeight / 2}
          color={floorColors[i]}
          windowColor={i % 2 === 0 ? "#7dd3fc" : "#a5b4fc"}
        />
      ))}

      {/* Roof */}
      <Roof y={baseY + 0.24 + floorCount * floorHeight + 0.15} />

      {/* Floating particles */}
      <Particles count={150} />

      {/* Base ring */}
      <BaseRing />

      {/* Ambient glow sphere (very large, low opacity) */}
      <mesh>
        <sphereGeometry args={[3.5, 32, 32]} />
        <meshStandardMaterial color="#1e40af" transparent opacity={0.04} side={THREE.BackSide} />
      </mesh>
    </group>
  );
}