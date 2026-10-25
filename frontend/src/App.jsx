import {
  useEffect,
  useMemo,
  useState
} from "react";

import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";

import MetricCard from "./components/MetricCard.jsx";
import NetworkTopology from "./components/NetworkTopology.jsx";

import {
  loadCsv
} from "./lib/csv.js";

import "./styles.css";


function toNumber(value) {
  const result = Number(value);

  return Number.isFinite(result)
      ? result
      : 0;
}


function mean(values) {

  if (values.length === 0) {
    return 0;
  }

  return values.reduce(
      (sum, value) => sum + value,
      0
  ) / values.length;
}


function median(values) {

  if (values.length === 0) {
    return 0;
  }

  const sorted = [
    ...values
  ].sort(
      (a, b) => a - b
  );

  const middle =
      Math.floor(
          sorted.length / 2
      );

  if (
      sorted.length % 2 === 0
  ) {

    return (
        sorted[middle - 1]
        + sorted[middle]
    ) / 2;
  }

  return sorted[middle];
}


export default function App() {

  const [
    lookupRows,
    setLookupRows
  ] = useState([]);

  const [
    tunnelRows,
    setTunnelRows
  ] = useState([]);

  const [
    loading,
    setLoading
  ] = useState(true);

  const [
    error,
    setError
  ] = useState(null);


  useEffect(
      () => {

        async function loadData() {

          try {

            const [
              lookup,
              tunnel
            ] = await Promise.all([
              loadCsv(
                  "/data/lookup-results.csv"
              ),

              loadCsv(
                  "/data/tunnel-results.csv"
              )
            ]);

            setLookupRows(
                lookup
            );

            setTunnelRows(
                tunnel
            );

          } catch (e) {

            console.error(e);

            setError(
                e.message
            );

          } finally {

            setLoading(
                false
            );
          }
        }

        loadData();

      },
      []
  );


  const lookupStats =
      useMemo(
          () => {

            const total =
                lookupRows.length;

            const successful =
                lookupRows.filter(
                    row =>
                        row.success === "true"
                ).length;

            const latencies =
                lookupRows.map(
                    row =>
                        toNumber(
                            row.latency_ms
                        )
                );

            const rpcs =
                lookupRows.map(
                    row =>
                        toNumber(
                            row.rpc_count
                        )
                );

            return {
              total,
              successful,

              successRate:
                  total === 0
                      ? 0
                      : successful
                      / total
                      * 100,

              latency:
                  mean(
                      latencies
                  ),

              rpc:
                  mean(
                      rpcs
                  )
            };
          },
          [lookupRows]
      );


  const tunnelStats =
      useMemo(
          () => {

            const total =
                tunnelRows.length;

            const delivered =
                tunnelRows.filter(
                    row =>
                        row.delivered === "true"
                ).length;

            const normal =
                tunnelRows.filter(
                    row =>
                        row.scenario === "normal"
                );

            const failures =
                tunnelRows.filter(
                    row =>
                        row.scenario
                        === "relay-failure"
                );

            return {
              total,
              delivered,

              build:
                  mean(
                      tunnelRows.map(
                          row =>
                              toNumber(
                                  row.build_time_ms
                              )
                      )
                  ),

              latency:
                  mean(
                      normal.map(
                          row =>
                              toNumber(
                                  row.end_to_end_latency_ms
                              )
                      )
                  ),

              recovery:
                  mean(
                      failures.map(
                          row =>
                              toNumber(
                                  row.recovery_time_ms
                              )
                      )
                  )
            };
          },
          [tunnelRows]
      );


  const lookupChart =
      useMemo(
          () => {

            const groups =
                new Map();

            lookupRows.forEach(
                row => {

                  const key =
                      `${row.topology} / ${row.scenario}`;

                  if (
                      !groups.has(
                          key
                      )
                  ) {

                    groups.set(
                        key,
                        []
                    );
                  }

                  groups
                      .get(key)
                      .push(
                          toNumber(
                              row.latency_ms
                          )
                      );
                }
            );

            return Array
                .from(
                    groups.entries()
                )
                .map(
                    ([name, values]) => ({
                      name,

                      mean:
                          Number(
                              mean(values)
                                  .toFixed(2)
                          ),

                      median:
                          Number(
                              median(values)
                                  .toFixed(2)
                          )
                    })
                );
          },
          [lookupRows]
      );


  const tunnelChart =
      useMemo(
          () => {

            return tunnelRows
                .filter(
                    row =>
                        row.scenario === "normal"
                )
                .map(
                    (row, index) => ({
                      run:
                          index + 1,

                      latency:
                          toNumber(
                              row.end_to_end_latency_ms
                          ),

                      build:
                          toNumber(
                              row.build_time_ms
                          )
                    })
                );
          },
          [tunnelRows]
      );


  const recoveryChart =
      useMemo(
          () => {

            return tunnelRows
                .filter(
                    row =>
                        row.scenario
                        === "relay-failure"
                )
                .map(
                    (row, index) => ({
                      name:
                          `Failure ${index + 1}`,

                      recovery:
                          toNumber(
                              row.recovery_time_ms
                          )
                    })
                );
          },
          [tunnelRows]
      );


  if (loading) {

    return (
        <div className="center-message">
          Loading Secure Overlay metrics...
        </div>
    );
  }


  if (error) {

    return (
        <div className="center-message error">

          Failed to load dashboard:

          <br/>

          {error}

        </div>
    );
  }


  return (

      <div className="app">

        <header className="header">

          <div>

            <div className="eyebrow">
              DISTRIBUTED SYSTEMS LAB
            </div>

            <h1>
              Secure Overlay
            </h1>

            <p>
              Kademlia DHT · TLS 1.3 ·
              Multi-hop encrypted tunnels
            </p>

          </div>


          <div className="header-status">

            <div className="pulse"/>

            <div>

              <strong>
                Acceptance passed
              </strong>

              <span>
                            Experimental dashboard
                        </span>

            </div>

          </div>

        </header>


        <main>

          <section className="metrics-grid">

            <MetricCard
                title="Lookup success"
                value={
                  `${lookupStats.successRate.toFixed(0)}%`
                }
                subtitle={
                  `${lookupStats.successful}/${lookupStats.total} successful`
                }
            />


            <MetricCard
                title="Lookup latency"
                value={
                  `${lookupStats.latency.toFixed(2)} ms`
                }
                subtitle={
                  `Average RPC: ${lookupStats.rpc.toFixed(2)}`
                }
            />


            <MetricCard
                title="Tunnel delivery"
                value={
                  `${tunnelStats.delivered}/${tunnelStats.total}`
                }
                subtitle={
                  `Build: ${tunnelStats.build.toFixed(2)} ms`
                }
            />


            <MetricCard
                title="Tunnel recovery"
                value={
                  `${tunnelStats.recovery.toFixed(2)} ms`
                }
                subtitle={
                  `E2E: ${tunnelStats.latency.toFixed(2)} ms`
                }
            />

          </section>


          <section className="dashboard-grid">

            <NetworkTopology/>


            <div className="panel">

              <div className="panel-header">

                <div>

                  <h2>
                    DHT lookup latency
                  </h2>

                  <p>
                    Mean and median
                    by topology/scenario
                  </p>

                </div>

              </div>


              <div className="chart-container">

                <ResponsiveContainer
                    width="100%"
                    height="100%"
                >

                  <BarChart
                      data={
                        lookupChart
                      }
                  >

                    <CartesianGrid
                        strokeDasharray="3 3"
                        vertical={false}
                    />

                    <XAxis
                        dataKey="name"
                    />

                    <YAxis/>

                    <Tooltip/>

                    <Legend/>


                    <Bar
                        dataKey="mean"
                        name="Mean latency"
                        fill="#60a5fa"
                        radius={[
                          6,
                          6,
                          0,
                          0
                        ]}
                    />


                    <Bar
                        dataKey="median"
                        name="Median latency"
                        fill="#34d399"
                        radius={[
                          6,
                          6,
                          0,
                          0
                        ]}
                    />

                  </BarChart>

                </ResponsiveContainer>

              </div>

            </div>

          </section>


          <section className="dashboard-grid">

            <div className="panel">

              <div className="panel-header">

                <div>

                  <h2>
                    Tunnel performance
                  </h2>

                  <p>
                    Build and E2E latency
                  </p>

                </div>

              </div>


              <div className="chart-container">

                <ResponsiveContainer
                    width="100%"
                    height="100%"
                >

                  <LineChart
                      data={
                        tunnelChart
                      }
                  >

                    <CartesianGrid
                        strokeDasharray="3 3"
                    />

                    <XAxis
                        dataKey="run"
                    />

                    <YAxis/>

                    <Tooltip/>

                    <Legend/>


                    <Line
                        type="monotone"
                        dataKey="latency"
                        name="E2E latency"
                        stroke="#818cf8"
                        strokeWidth={3}
                        dot={false}
                    />


                    <Line
                        type="monotone"
                        dataKey="build"
                        name="Build time"
                        stroke="#f59e0b"
                        strokeWidth={2}
                        dot={false}
                    />

                  </LineChart>

                </ResponsiveContainer>

              </div>

            </div>


            <div className="panel">

              <div className="panel-header">

                <div>

                  <h2>
                    Relay recovery
                  </h2>

                  <p>
                    Automatic tunnel rebuild
                  </p>

                </div>

              </div>


              <div className="chart-container">

                <ResponsiveContainer
                    width="100%"
                    height="100%"
                >

                  <BarChart
                      data={
                        recoveryChart
                      }
                  >

                    <CartesianGrid
                        strokeDasharray="3 3"
                        vertical={false}
                    />

                    <XAxis
                        dataKey="name"
                    />

                    <YAxis/>

                    <Tooltip/>


                    <Bar
                        dataKey="recovery"
                        name="Recovery time"
                        fill="#f472b6"
                        radius={[
                          6,
                          6,
                          0,
                          0
                        ]}
                    />

                  </BarChart>

                </ResponsiveContainer>

              </div>

            </div>

          </section>


          <section className="security-panel">

            <div>

              <div className="eyebrow">
                SECURITY STACK
              </div>

              <h2>
                Protected overlay transport
              </h2>

            </div>


            <div className="security-items">

                        <span>
                            TLS 1.3
                        </span>

              <span>
                            Ed25519
                        </span>

              <span>
                            X25519
                        </span>

              <span>
                            AES-256-GCM
                        </span>

              <span>
                            Replay protection
                        </span>

            </div>

          </section>

        </main>

      </div>
  );
}