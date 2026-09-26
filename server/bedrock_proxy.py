#!/usr/bin/env python3
"""Bedrock intent-resolution proxy for Simple Mode.

Why this exists: Simple Mode's "Tell Me" screen lets the viewer say what
they want in their own words instead of aiming a D-pad at a grid. Making
that call directly from the Android app would mean shipping AWS secret
keys inside the APK, which is a real security problem, not a hypothetical
one. The standard, honest pattern is a thin server-side proxy that holds
the credentials and the app talks to over HTTP. This is that proxy: pure
Python standard library, no new dependencies, calls the real Bedrock
Converse API (not a canned response) via `bedrock_client.py`, using the
AWS CLI already configured on this machine (region us-east-1).

Run it before demoing the Tell Me screen:

    python3 server/bedrock_proxy.py

It listens on 127.0.0.1:8798. The Android emulator reaches host loopback
at 10.0.2.2, which is what BedrockIntentResolver.kt is pointed at.

This is not mocked: every request here is a real call to Bedrock. What IS
mocked, and clearly marked as such, is the Android-side unit test double
in app/src/test -- it never talks to this server or to AWS.
"""

from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, HTTPServer

from bedrock_client import MODEL_PREFERENCE, call_bedrock

PORT = 8798


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/resolve-intent":
            self.send_response(404)
            self.end_headers()
            return

        length = int(self.headers.get("Content-Length", 0))
        try:
            body = json.loads(self.rfile.read(length))
            result = call_bedrock(body["utterance"], body["tiles"])
            self._respond(200, result)
        except Exception as exc:  # noqa: BLE001 -- any failure becomes a 502 for the client to fall back on
            self._respond(502, {"error": str(exc)})

    def do_GET(self):
        if self.path == "/health":
            self._respond(200, {"status": "ok", "models": MODEL_PREFERENCE})
        else:
            self.send_response(404)
            self.end_headers()

    def _respond(self, status: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):  # noqa: A002 -- quiet by default
        print(f"[bedrock_proxy] {self.address_string()} - {format % args}")


if __name__ == "__main__":
    server = HTTPServer(("127.0.0.1", PORT), Handler)
    print(f"Bedrock proxy listening on http://127.0.0.1:{PORT} (models {MODEL_PREFERENCE})")
    server.serve_forever()
