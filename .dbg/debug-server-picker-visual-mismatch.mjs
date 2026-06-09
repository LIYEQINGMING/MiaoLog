import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';

const sessionId = 'picker-visual-mismatch';
const port = 7778;
const host = '10.70.13.135';
const outDir = path.resolve('.dbg');
const logFile = path.join(outDir, `trae-debug-log-${sessionId}.ndjson`);

fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, `${sessionId}.env`), `DEBUG_SERVER_URL=http://${host}:${port}/event\nDEBUG_SESSION_ID=${sessionId}\n`, 'utf8');
fs.writeFileSync(logFile, '', 'utf8');

const server = http.createServer((req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,DELETE,OPTIONS');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  if (req.method === 'POST' && req.url === '/event') {
    const chunks = [];
    req.on('data', chunk => chunks.push(chunk));
    req.on('end', () => {
      try {
        const payload = JSON.parse(Buffer.concat(chunks).toString('utf8') || '{}');
        payload.ts = payload.ts || Date.now();
        fs.appendFileSync(logFile, `${JSON.stringify(payload)}\n`, 'utf8');
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: true }));
      } catch (error) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ ok: false, error: String(error) }));
      }
    });
    return;
  }

  if (req.method === 'GET' && req.url?.startsWith('/logs')) {
    const text = fs.existsSync(logFile) ? fs.readFileSync(logFile, 'utf8') : '';
    res.writeHead(200, { 'Content-Type': 'text/plain; charset=utf-8' });
    res.end(text);
    return;
  }

  if (req.method === 'DELETE' && req.url === '/logs') {
    fs.writeFileSync(logFile, '', 'utf8');
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ ok: true }));
    return;
  }

  if (req.method === 'GET' && req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ ok: true, sessionId, port, host }));
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ ok: false, error: 'not_found' }));
});

server.listen(port, '0.0.0.0', () => {
  process.stdout.write(`debug-server ${sessionId} http://${host}:${port}/event\n`);
});
