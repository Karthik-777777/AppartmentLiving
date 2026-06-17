import Sidebar from "../components/Sidebar";

export default function MainLayout({ children }) {
  return (
    <div style={styles.container}>
      <Sidebar />
      <div style={styles.content}>{children}</div>
    </div>
  );
}

const styles = {
  container: {
    display: "flex",
    height: "100vh",
    background: "#f5f7fb", // overall background
  },

  content: {
    flex: 1,
    padding: "20px",
    overflowY: "auto",
  },
};