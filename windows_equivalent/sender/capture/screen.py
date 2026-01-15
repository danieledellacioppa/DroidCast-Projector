from dataclasses import dataclass

from mss import mss
from PIL import Image


@dataclass
class CapturedFrame:
    image: Image.Image
    orientation_flag: int


class ScreenCapturer:
    def __init__(self) -> None:
        self._mss = mss()

    def capture(self) -> CapturedFrame:
        monitor = self._mss.monitors[0]
        raw = self._mss.grab(monitor)
        width = raw.width
        height = raw.height
        orientation_flag = 1 if width >= height else 0
        image = Image.frombytes("RGB", raw.size, raw.bgra, "raw", "BGRX")
        return CapturedFrame(image=image, orientation_flag=orientation_flag)
