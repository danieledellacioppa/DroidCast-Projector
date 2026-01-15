from io import BytesIO

from PIL import Image


def encode_png(image: Image.Image, quality: int) -> bytes:
    _ = quality
    buffer = BytesIO()
    image.save(buffer, format="PNG")
    return buffer.getvalue()
