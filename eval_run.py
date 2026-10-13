#!/usr/bin/env python3
"""Day 5 eval runner: fires the 10-question test set at POST /api/ask,
times each call, and saves raw responses for human review.

Usage:  python3 eval_run.py        (app must be running on localhost:8080)
Output: eval-results/<timestamp>/{qid}.json + summary.json
"""
import json
import os
import time
import urllib.request
from datetime import datetime

BASE = "http://localhost:8080/api/ask"

# (id, question, what a PASS looks like)
QUESTIONS = [
    ("q01-doc-return-policy",
     "What is your return policy?",
     "PASS if answer mentions 30 days and cites [return-policy.md]"),
    ("q02-doc-refund-time",
     "How long do refunds take to show up?",
     "PASS if answer mentions 5-7 business days and cites [refund-timeline.md]"),
    ("q03-doc-intl-shipping",
     "Do you ship internationally?",
     "PASS if answer mentions 40+ countries / 10-20 days and cites [international-shipping.md]"),
    ("q04-doc-promo-codes",
     "Can I use two promo codes on one order?",
     "PASS if answer says one code per order and cites [promo-codes-and-discounts.md]"),
    ("q05-doc-damaged",
     "What should I do if my package arrived damaged?",
     "PASS if answer mentions 14 days + photos and cites [damaged-or-defective-items.md]"),
    ("q06-tool-known-1",
     "Where is my order BC-1001?",
     "PASS if tool was used: SHIPPED + tracking 1Z999888777"),
    ("q07-tool-known-2",
     "What is the status of order BC-1002?",
     "PASS if tool was used: PROCESSING, tracking not assigned yet"),
    ("q08-tool-unknown",
     "Where is my order BC-9999?",
     "PASS if graceful not-found, no invented details"),
    ("q09-tool-no-id",
     "Where is my order?",
     "PASS if bot ASKS for the order ID (Day 4 fix verified). FAIL if it answers from BC-1001."),
    ("q10-refusal",
     "What is the capital of France?",
     "PASS if bot says it doesn't know and offers support. FAIL if it answers Paris."),
]


def ask(question):
    req = urllib.request.Request(
        BASE,
        data=json.dumps({"question": question}).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    start = time.perf_counter()
    with urllib.request.urlopen(req, timeout=300) as resp:
        body = json.loads(resp.read())
    return body, (time.perf_counter() - start) * 1000


def main():
    outdir = os.path.join("eval-results", datetime.now().strftime("%Y%m%d-%H%M%S"))
    os.makedirs(outdir, exist_ok=True)
    summary = []
    for qid, question, expected in QUESTIONS:
        try:
            body, ms = ask(question)
            status = "ok"
        except Exception as e:  # noqa: BLE001 - report and continue the suite
            body, ms, status = {"error": str(e)}, 0.0, "ERROR"
        with open(os.path.join(outdir, f"{qid}.json"), "w") as f:
            json.dump({"question": question, "expected": expected,
                       "latency_ms": round(ms), **body}, f, indent=2)
        summary.append({"id": qid, "latency_ms": round(ms), "status": status})
        print(f"{qid}: {round(ms)} ms [{status}]")
    with open(os.path.join(outdir, "summary.json"), "w") as f:
        json.dump(summary, f, indent=2)
    lat = sorted(s["latency_ms"] for s in summary if s["status"] == "ok")
    if lat:
        print(f"\np50 latency: {lat[len(lat) // 2]} ms over {len(lat)} calls")
    print(f"Results in {outdir}/ — open each JSON, judge PASS/FAIL against 'expected'.")


if __name__ == "__main__":
    main()
