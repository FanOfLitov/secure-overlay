export function parseCsv(text) {
    const lines = text
        .trim()
        .split(/\r?\n/);

    if (lines.length < 2) {
        return [];
    }

    const headers =
        lines[0]
            .split(",")
            .map(value => value.trim());

    return lines
        .slice(1)
        .filter(line => line.trim().length > 0)
        .map(line => {

            const values =
                line
                    .split(",")
                    .map(value => value.trim());

            const row = {};

            headers.forEach(
                (header, index) => {

                    row[header] =
                        values[index] ?? "";
                }
            );

            return row;
        });
}


export async function loadCsv(path) {

    const response =
        await fetch(path);

    if (!response.ok) {

        throw new Error(
            `Cannot load ${path}: ${response.status}`
        );
    }

    const text =
        await response.text();

    return parseCsv(text);
}