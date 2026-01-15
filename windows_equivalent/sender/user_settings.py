import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict

from sender.config import DEFAULT_QUALITY, MAX_QUALITY, MIN_QUALITY


CONFIG_FILENAME = ".droidcast_sender_config.json"


@dataclass
class UserSettings:
    receiver_ip: str = ""
    quality: int = DEFAULT_QUALITY


def settings_path() -> Path:
    return Path.home() / CONFIG_FILENAME


def load_settings() -> UserSettings:
    path = settings_path()
    if not path.exists():
        return UserSettings()
    try:
        payload: Dict[str, Any] = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return UserSettings()

    receiver_ip = str(payload.get("receiver_ip", ""))
    quality = payload.get("quality", DEFAULT_QUALITY)
    if not isinstance(quality, int):
        try:
            quality = int(quality)
        except (TypeError, ValueError):
            quality = DEFAULT_QUALITY
    if quality < MIN_QUALITY or quality > MAX_QUALITY:
        quality = DEFAULT_QUALITY
    return UserSettings(receiver_ip=receiver_ip, quality=quality)


def save_settings(settings: UserSettings) -> None:
    path = settings_path()
    data = {
        "receiver_ip": settings.receiver_ip,
        "quality": settings.quality,
    }
    try:
        path.write_text(json.dumps(data, indent=2), encoding="utf-8")
    except OSError:
        return
