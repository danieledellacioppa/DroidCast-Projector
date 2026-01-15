# Windows Equivalent Sender

This folder contains a Python implementation of the Windows sender that matches the Android sender protocol exactly (one TCP connection per frame, PNG payload, and the same field ordering).

## Requirements

- Windows
- Python 3.9+

Install dependencies:

```bash
pip install -r requirements.txt
```

## Usage

```bash
python -m sender.main --receiver-ip 192.168.0.10 --quality 50
```

### Parameters

- `--receiver-ip` (required): IP address of the Android receiver.
- `--quality` (optional): 1–100, default 50. This is accepted to match the Android sender (PNG encoding is lossless; the quality parameter is retained for protocol parity).

## Behavior

- Captures the full screen continuously.
- Encodes each frame as PNG.
- For every frame, opens a new TCP connection to port `12345` and sends:
  1. `orientationFlag` (int32, big-endian)
  2. `imageSize` (int32, big-endian)
  3. `imageBytes` (PNG payload)
- Logs the destination IP, PNG size, orientation flag, and socket errors.
