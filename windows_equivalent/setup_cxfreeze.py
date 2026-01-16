import sys
from pathlib import Path

from cx_Freeze import Executable, setup

base = "gui" if sys.platform == "win32" else None

assets_dir = Path(__file__).parent / "assets"
icon_path = assets_dir / "app.ico"

build_exe_options = {
    "packages": ["mss", "PIL"],
    "include_files": [(str(icon_path), "assets/app.ico")],
}

executables = [
    Executable(
        script="sender/gui.py",
        base=base,
        target_name="DroidCastSender.exe",
        icon=str(icon_path),
    )
]

setup(
    name="DroidCast Windows Sender",
    version="0.1.0",
    description="DroidCast Windows GUI Sender",
    options={"build_exe": build_exe_options},
    executables=executables,
)
