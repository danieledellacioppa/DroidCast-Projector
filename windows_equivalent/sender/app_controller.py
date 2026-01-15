import socket
import threading
from typing import Callable, Optional

from sender.capture.screen import ScreenCapturer
from sender.config import DEFAULT_PORT, MAX_QUALITY, MIN_QUALITY
from sender.encode.png import encode_png
from sender.protocol.frame_packet import build_frame_packet
from sender.transport.tcp_sender import TcpSender


class AppController:
    def __init__(self, logger: Callable[[str], None], fps: float = 10.0) -> None:
        self._logger = logger
        self._delay = 1.0 / fps if fps > 0 else 0.1
        self._stop_event = threading.Event()
        self._thread: Optional[threading.Thread] = None

    def is_running(self) -> bool:
        return self._thread is not None and self._thread.is_alive()

    def start(self, receiver_ip: str, quality: int) -> None:
        if self.is_running():
            self._logger("Worker already running.")
            return
        if quality < MIN_QUALITY or quality > MAX_QUALITY:
            raise ValueError("Quality must be between 1 and 100")
        self._stop_event = threading.Event()
        self._thread = threading.Thread(
            target=self._worker,
            args=(receiver_ip, quality, self._stop_event),
            daemon=True,
        )
        self._thread.start()

    def stop(self, timeout: float = 2.0) -> None:
        if not self.is_running():
            return
        self._stop_event.set()
        if self._thread:
            self._thread.join(timeout=timeout)
        if self._thread and self._thread.is_alive():
            self._logger("Worker did not stop within timeout.")

    def test_connection(self, receiver_ip: str, timeout: float = 1.5) -> None:
        try:
            with socket.create_connection((receiver_ip, DEFAULT_PORT), timeout=timeout):
                self._logger("Connection test: success.")
        except OSError as exc:
            self._logger(f"Connection test failed: {exc}")

    def _worker(self, receiver_ip: str, quality: int, stop_event: threading.Event) -> None:
        capturer = ScreenCapturer()
        sender = TcpSender(receiver_ip, DEFAULT_PORT, self._logger)
        while not stop_event.is_set():
            try:
                frame = capturer.capture()
            except Exception as exc:  # pragma: no cover - defensive
                self._logger(f"Capture error: {exc}")
                if stop_event.wait(self._delay):
                    break
                continue

            try:
                png_bytes = encode_png(frame.image, quality)
            except Exception as exc:  # pragma: no cover - defensive
                self._logger(f"Encode error: {exc}")
                if stop_event.wait(self._delay):
                    break
                continue

            self._logger(f"Receiver IP: {receiver_ip}")
            self._logger(f"PNG size: {len(png_bytes)} bytes")
            self._logger(f"orientationFlag: {frame.orientation_flag}")
            packet = build_frame_packet(frame.orientation_flag, png_bytes)
            sender.send(packet)
            if stop_event.wait(self._delay):
                break
        self._logger("Worker stopped.")
