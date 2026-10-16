const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const TARGET_HOST = 'ecg.test.upc.ua';

const server = http.createServer((req, res) => {
    // Встановлюємо загальні CORS-заголовки
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Accept, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    if (req.url.startsWith('/go/')) {
        console.log(`[PROXY] Forwarding ${req.method} ${req.url} to https://${TARGET_HOST}${req.url}`);
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            const proxyReq = https.request({
                hostname: TARGET_HOST,
                port: 443,
                path: req.url,
                method: req.method,
                headers: {
                    'Content-Type': 'application/json',
                    'Content-Length': Buffer.byteLength(body)
                }
            }, proxyRes => {
                const responseHeaders = {
                    ...proxyRes.headers,
                    'access-control-allow-origin': '*',
                    'access-control-allow-methods': 'GET, POST, PUT, DELETE, OPTIONS',
                    'access-control-allow-headers': 'Content-Type, Accept, Authorization'
                };
                res.writeHead(proxyRes.statusCode, responseHeaders);
                proxyRes.pipe(res);
                console.log(`[PROXY] Received response from https://${TARGET_HOST}${req.url} -> Status: ${proxyRes.statusCode}`);
            });

            proxyReq.on('error', err => {
                console.error(`[PROXY ERROR] Failed to forward to ${TARGET_HOST}: ${err.message}`);
                res.writeHead(502, { 
                    'Content-Type': 'application/json',
                    'access-control-allow-origin': '*'
                });
                res.end(JSON.stringify({ tranCode: 'ERR', comment: `Proxy error: ${err.message}` }));
            });

            proxyReq.write(body);
            proxyReq.end();
        });
    } else {
        // Роздаємо статичні файли демо (index.html, app.js тощо)
        const cleanUrl = req.url.split('?')[0];
        let filePath = path.join(__dirname, cleanUrl === '/' ? 'index.html' : cleanUrl);
        if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
            const ext = path.extname(filePath).toLowerCase();
            const contentTypes = {
                '.html': 'text/html; charset=utf-8',
                '.js': 'application/javascript; charset=utf-8',
                '.css': 'text/css; charset=utf-8',
                '.json': 'application/json; charset=utf-8'
            };
            res.writeHead(200, { 'Content-Type': contentTypes[ext] || 'text/plain' });
            fs.createReadStream(filePath).pipe(res);
        } else {
            res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
            res.end('Not Found');
        }
    }
});

server.listen(PORT, () => {
    console.log(`====================================================`);
    console.log(`UPC Token Service SDK - Local Proxy & Web Demo`);
    console.log(`Web UI: http://localhost:${PORT}`);
    console.log(`Target: https://${TARGET_HOST}`);
    console.log(`====================================================`);
});