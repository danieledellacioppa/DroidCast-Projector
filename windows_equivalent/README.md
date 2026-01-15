# Windows Equivalent Sender

This folder contains a Python implementation of the Windows sender that matches the Android sender protocol exactly (one TCP connection per frame, PNG payload, and the same field ordering).

## Requirements

- Windows
- Python 3.9+

Install dependencies:

```bash
pip install -r requirements.txt
```

## Usage (CLI)

```bash
python -m sender.main --receiver-ip 192.168.0.10 --quality 50
```

### Parameters

- `--receiver-ip` (required): IP address of the Android receiver.
- `--quality` (optional): 1–100, default 50. This is accepted to match the Android sender (PNG encoding is lossless; the quality parameter is retained for protocol parity).

## Usage (GUI)

```bash
python -m sender.gui
```

### GUI Features

- Enter Receiver IP and Quality (1–100).
- Start/Stop casting controls.
- Test connection button.
- Scrollable log area with the same messages as the CLI.
- Running/Stopped status indicator.
- Saves the last used IP/quality to `~/.droidcast_sender_config.json`.

## Behavior

- Captures the full screen continuously.
- Encodes each frame as PNG.
- For every frame, opens a new TCP connection to port `12345` and sends:
  1. `orientationFlag` (int32, big-endian)
  2. `imageSize` (int32, big-endian)
  3. `imageBytes` (PNG payload)
- Logs the destination IP, PNG size, orientation flag, and socket errors.

## Build (cx_Freeze)

1. Install build dependency:

   ```bash
   pip install cx_Freeze
   ```

2. Build the windowed executable:

   ```bash
   python setup_cxfreeze.py build
   ```

   The output goes to `build/exe.win-amd64-<python_version>/DroidCastSender.exe`.

## Installer (Inno Setup)

1. Build the exe with cx_Freeze.
2. Open `installer/inno_setup.iss` in Inno Setup Compiler.
3. Compile to generate the installer executable in `installer/output/`.

The script creates Start Menu and Desktop shortcuts and can run the app after install.
