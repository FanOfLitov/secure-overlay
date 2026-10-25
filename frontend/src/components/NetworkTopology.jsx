const nodes = Array.from(
    { length: 12 },
    (_, index) => index
);


function position(index) {

    const centerX = 250;
    const centerY = 190;
    const radius = 145;

    const angle =
        (
            Math.PI * 2 * index
            / nodes.length
        )
        - Math.PI / 2;

    return {

        x:
            centerX
            + Math.cos(angle)
            * radius,

        y:
            centerY
            + Math.sin(angle)
            * radius
    };
}


export default function NetworkTopology() {

    return (
        <div className="panel">

            <div className="panel-header">

                <div>
                    <h2>Overlay topology</h2>

                    <p>
                        12-node experimental ring
                    </p>
                </div>

                <span className="status-badge">
                    ONLINE
                </span>

            </div>


            <div className="topology-wrapper">

                <svg
                    viewBox="0 0 500 380"
                    className="topology"
                >

                    {nodes.map(index => {

                        const from =
                            position(index);

                        const to =
                            position(
                                (index + 1)
                                % nodes.length
                            );

                        return (
                            <line
                                key={`edge-${index}`}
                                x1={from.x}
                                y1={from.y}
                                x2={to.x}
                                y2={to.y}
                                className="topology-edge"
                            />
                        );
                    })}


                    {nodes.map(index => {

                        const from =
                            position(index);

                        const to =
                            position(
                                (
                                    index
                                    + nodes.length / 2
                                )
                                % nodes.length
                            );

                        return (
                            <line
                                key={`chord-${index}`}
                                x1={from.x}
                                y1={from.y}
                                x2={to.x}
                                y2={to.y}
                                className="topology-chord"
                            />
                        );
                    })}


                    {nodes.map(index => {

                        const p =
                            position(index);

                        return (

                            <g
                                key={index}
                                className="node-group"
                            >

                                <circle
                                    cx={p.x}
                                    cy={p.y}
                                    r="18"
                                    className="node-circle"
                                />

                                <text
                                    x={p.x}
                                    y={p.y + 4}
                                    textAnchor="middle"
                                    className="node-label"
                                >
                                    {index}
                                </text>

                            </g>
                        );
                    })}

                </svg>

            </div>

        </div>
    );
}