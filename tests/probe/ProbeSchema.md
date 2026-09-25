# Probe JSON Schema (v0)

Output of `ProbeFragment` — one JSON file per run, schema:

```json
{
  "app":     { "versionName": str, "sdkInt": int },
  "build":   { "model": str, "manufacturer": str, "fingerprint": str, "securityPatch": str },
  "cameras": [
    {
      "id": str,
      "facing": int,
      "sensorOrientation": int,
      "hardwareLevel": int,
      "pixelArray": {"w": int, "h": int},
      "jpegSizes": "WxH,WxH,...",
      "apertures": [float]
    }
  ]
}
```

## Diffing protocol (from Run 4 onward)
For each hooked test: dump with hooks OFF (baseline) and hooks ON (spoofed).
The diff must show exactly the fields the persona intends to change and
NOTHING else. Any unexpected diff = regression. CI compares these files.
