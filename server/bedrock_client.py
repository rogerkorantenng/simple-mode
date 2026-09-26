"""The actual Bedrock call, split out of bedrock_proxy.py so each file
stays small and single-purpose: this one only knows how to ask Bedrock a
question and parse the answer, nothing about HTTP.
"""

from __future__ import annotations

import json
import re
import subprocess

# Newest first. `anthropic.claude-sonnet-5` and `anthropic.claude-opus-5`
# are listed by ListFoundationModels but return AccessDeniedException on an
# actual InvokeModel/Converse call on this account -- listed is not
# callable. Both entries below were verified by a real invocation, not by
# the catalogue. Trying the newest first and falling back down the list
# means this build upgrades itself the day broader access lands, without a
# code change.
MODEL_PREFERENCE = [
    "us.anthropic.claude-sonnet-4-6",
    "us.anthropic.claude-sonnet-4-5-20250929-v1:0",
]
REGION = "us-east-1"
BEDROCK_TIMEOUT_SECONDS = 12

SYSTEM_PROMPT = (
    "You are the intent router inside Simple Mode, a Fire TV app built for "
    "someone who cannot work a television unaided: low vision, low "
    "dexterity, or simply unfamiliar with a remote. A caregiver set the "
    "device up; the person using it just said, in their own words, what "
    "they want. Match it to exactly one of the given tile ids, or none if "
    "nothing fits. Reply with ONLY a JSON object, no other text: "
    '{"tileId": "<id or null>", "reply": "<one short warm sentence, '
    'spoken aloud to the viewer>"}. Keep the reply under 20 words. Never '
    "invent a tile id that was not given."
)


def call_bedrock(utterance: str, tiles: list[dict]) -> dict:
    tile_lines = "\n".join(
        f"- id={t['id']}: {t['title']} -- {t.get('shortDescription', '')}" for t in tiles
    )
    user_message = f"Tiles available:\n{tile_lines}\n\nThe viewer said: \"{utterance}\""

    last_error: Exception | None = None
    for model_id in MODEL_PREFERENCE:
        try:
            return _invoke(model_id, user_message, {t["id"] for t in tiles})
        except Exception as exc:  # noqa: BLE001 -- try the next model in the chain
            last_error = exc
    raise RuntimeError(f"every model in the preference chain failed: {last_error}")


# The id set is passed in rather than read from an enclosing scope. It used
# to be the latter, and since `tiles` is not a parameter here, every call
# raised NameError on the line below -- after the AWS request had been made
# and paid for. The fallback chain caught it, tried the next model, failed
# the same way, and handed the app its keyword matcher. The app worked, so
# nobody noticed the primary was dead for the whole build.
def _invoke(model_id: str, user_message: str, known_ids: set[str]) -> dict:
    payload = {
        "modelId": model_id,
        "messages": [{"role": "user", "content": [{"text": user_message}]}],
        "system": [{"text": SYSTEM_PROMPT}],
        "inferenceConfig": {"maxTokens": 200, "temperature": 0.2},
    }

    proc = subprocess.run(
        [
            "aws", "bedrock-runtime", "converse",
            "--region", REGION,
            "--cli-input-json", json.dumps(payload),
        ],
        capture_output=True,
        text=True,
        timeout=BEDROCK_TIMEOUT_SECONDS,
    )
    if proc.returncode != 0:
        raise RuntimeError(f"bedrock-runtime converse failed for {model_id}: {proc.stderr.strip()}")

    response = json.loads(proc.stdout)
    text = response["output"]["message"]["content"][0]["text"]
    return _parse_model_reply(text, known_ids)


def _parse_model_reply(text: str, known_ids: set[str] | None = None) -> dict:
    # The model is asked for bare JSON but may still wrap it in a code
    # fence; strip that defensively rather than trusting instruction
    # following alone.
    cleaned = re.sub(r"^```(json)?|```$", "", text.strip(), flags=re.MULTILINE).strip()
    parsed = json.loads(cleaned)
    tile_id = parsed.get("tileId")
    # The prompt tells the model never to invent a tile id. That is an
    # instruction, not a guarantee, and this is an app for someone who
    # cannot work a television: if she is sent somewhere she did not ask
    # for, she has no way to work out what happened or how to get back.
    # So the id is checked against the list that was actually sent, and a
    # miss is reported as "no match" rather than acted on. The Android side
    # checks again -- see BedrockIntentResolver -- because this proxy is
    # not the only thing that could be wrong.
    if known_ids is not None and tile_id is not None and tile_id not in known_ids:
        return {
            "tileId": None,
            "reply": parsed.get("reply", ""),
            "rejectedTileId": tile_id,
        }
    return {"tileId": tile_id, "reply": parsed.get("reply", "")}
