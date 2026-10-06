'use strict';
const path = require('path');
const fs = require('fs');
const { pathToFileURL } = require('url');
async function main() {
  const entry = path.join(__dirname, 'src', 'main.mjs');
  await import(pathToFileURL(entry).href);
}
main().catch((err) => {
  const bridge = path.join(process.cwd(), 'bridge');
  fs.mkdirSync(bridge, { recursive: true });
  fs.writeFileSync(
    path.join(bridge, 'status.json'),
    JSON.stringify({ ok: false, connection: 'error', lastError: String(err) }, null, 2),
  );
  console.error(err);
  process.exitCode = 1;
});
