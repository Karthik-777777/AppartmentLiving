import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Legend,
} from "recharts";




const COLORS = ["#ef4444", "#f59e0b", "#22c55e"];

export default function ChartsSection({
  complaints,
  payments,
}) {  


const complaintData = [

  {
    name: "Open",
value: (complaints || []).filter(
        (item) =>
        item.status === "Pending"
    ).length,
  },

  {
    name: "In Progress",
value: (complaints || []).filter(
        (item) =>
        item.status === "In Progress"
    ).length,
  },

  {
    name: "Resolved",
value: (complaints || []).filter(
        (item) =>
        item.status === "Resolved"
    ).length,
  },

];

const monthlyData = {};

(payments || []).forEach((item) => {

  const month =
    new Date(item.createdAt)
      .toLocaleString("default", {
        month: "short",
      });

  if(!monthlyData[month]) {
    monthlyData[month] = 0;
  }

  monthlyData[month] +=
    Number(item.amount || 0);

});

const revenueData =
  Object.keys(monthlyData).map(
    (month) => ({
      month,
      revenue: monthlyData[month],
    })
  );


  return (
    <div className="grid grid-cols-2 gap-6 mb-8">

      {/* PIE CHART */}
      <div className="bg-white/90 backdrop-blur-md rounded-2xl p-6 shadow-sm border border-white/40">

        <div className="flex items-center justify-between mb-5">
          <h2 className="text-[18px] font-semibold text-slate-800">
            Complaint Analytics
          </h2>
        </div>

        <div className="h-[300px]">

          <ResponsiveContainer width="100%" height="100%">

            <PieChart>

              <Pie
                data={complaintData}
                cx="50%"
                cy="50%"
                outerRadius={100}
                dataKey="value"
                label
                isAnimationActive={true}
animationDuration={1200}
              >

                {complaintData.map((entry, index) => (
                  <Cell
                    key={index}
                    fill={COLORS[index % COLORS.length]}
                  />
                ))}

              </Pie>

              <Tooltip />

            </PieChart>

          </ResponsiveContainer>

        </div>
      </div>

      {/* BAR CHART */}
      <div className="bg-white/90 backdrop-blur-md rounded-2xl p-6 shadow-sm border border-white/40">

        <div className="flex items-center justify-between mb-5">
          <h2 className="text-[18px] font-semibold text-slate-800">
            Monthly Revenue
          </h2>
        </div>

        <div className="h-[300px]">

          <ResponsiveContainer width="100%" height="100%">

            <BarChart data={revenueData}>

              <CartesianGrid strokeDasharray="3 3" />

              <XAxis dataKey="month" />

              <YAxis />

              <Tooltip />

              <Legend />

              <Bar
                dataKey="revenue"
                animationDuration={1200}
                fill="#2563eb"
                radius={[10, 10, 0, 0]}
              />

            </BarChart>

          </ResponsiveContainer>

        </div>
      </div>

    </div>
  );
}