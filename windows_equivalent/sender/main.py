import argparse

from sender.capture.screen import ScreenCapturer
from sender.config import DEFAULT_PORT, DEFAULT_QUALITY, MAX_QUALITY, MIN_QUALITY
from sender.encode.png import encode_png
from sender.protocol.frame_packet import build_frame_packet
from sender.transport.tcp_sender import TcpSender


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Windows equivalent screen sender")
    parser.add_argument("--receiver-ip", required=True, help="Receiver IP address")
    parser.add_argument(
        "--quality",
        type=int,
        default=DEFAULT_QUALITY,
        help="PNG quality (1-100, default 50)",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.quality < MIN_QUALITY or args.quality > MAX_QUALITY:
        raise ValueError("Quality must be between 1 and 100")

    def log(message: str) -> None:
        print(message, flush=True)

    capturer = ScreenCapturer()
    sender = TcpSender(args.receiver_ip, DEFAULT_PORT, log)

    while True:
        frame = capturer.capture()
        png_bytes = encode_png(frame.image, args.quality)
        log(f"Receiver IP: {args.receiver_ip}")
        log(f"PNG size: {len(png_bytes)} bytes")
        log(f"orientationFlag: {frame.orientation_flag}")
        packet = build_frame_packet(frame.orientation_flag, png_bytes)
        sender.send(packet)


if __name__ == "__main__":
    main()
