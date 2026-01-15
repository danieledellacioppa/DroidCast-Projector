import socket
from typing import Callable


class TcpSender:
    def __init__(self, receiver_ip: str, port: int, logger: Callable[[str], None]) -> None:
        self._receiver_ip = receiver_ip
        self._port = port
        self._logger = logger

    def send(self, packet: bytes) -> None:
        try:
            with socket.create_connection((self._receiver_ip, self._port)) as sock:
                sock.sendall(packet)
        except OSError as exc:
            self._logger(f"Socket error: {exc}")
