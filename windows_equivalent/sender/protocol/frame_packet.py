import struct


def build_frame_packet(orientation_flag: int, image_bytes: bytes) -> bytes:
    header = struct.pack(">ii", orientation_flag, len(image_bytes))
    return header + image_bytes
