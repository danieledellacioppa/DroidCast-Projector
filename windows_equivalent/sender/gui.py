import queue
import tkinter as tk
from typing import Union
from tkinter import messagebox, ttk

from sender.app_controller import AppController
from sender.config import DEFAULT_QUALITY, MAX_QUALITY, MIN_QUALITY
from sender.user_settings import UserSettings, load_settings, save_settings


class SenderGUI:
    def __init__(self, root: tk.Tk) -> None:
        self._root = root
        self._root.title("DroidCast Windows Sender")
        self._root.geometry("520x420")
        self._log_queue: queue.Queue[str] = queue.Queue()
        self._controller = AppController(self._enqueue_log)
        self._settings = load_settings()

        self._build_ui()
        self._apply_settings()
        self._schedule_log_pump()
        self._root.protocol("WM_DELETE_WINDOW", self._on_close)

    def _build_ui(self) -> None:
        main_frame = ttk.Frame(self._root, padding=12)
        main_frame.pack(fill=tk.BOTH, expand=True)

        form_frame = ttk.Frame(main_frame)
        form_frame.pack(fill=tk.X)

        ttk.Label(form_frame, text="Receiver IP:").grid(row=0, column=0, sticky=tk.W)
        self._ip_var = tk.StringVar()
        self._ip_entry = ttk.Entry(form_frame, textvariable=self._ip_var, width=30)
        self._ip_entry.grid(row=0, column=1, sticky=tk.W, padx=8)

        ttk.Label(form_frame, text="Quality (1-100):").grid(row=1, column=0, sticky=tk.W, pady=(8, 0))
        self._quality_var = tk.StringVar(value=str(DEFAULT_QUALITY))
        self._quality_entry = ttk.Entry(form_frame, textvariable=self._quality_var, width=10)
        self._quality_entry.grid(row=1, column=1, sticky=tk.W, padx=8, pady=(8, 0))

        button_frame = ttk.Frame(main_frame)
        button_frame.pack(fill=tk.X, pady=10)

        self._start_button = ttk.Button(button_frame, text="Start Casting", command=self._on_start)
        self._start_button.grid(row=0, column=0, padx=(0, 8))

        self._stop_button = ttk.Button(button_frame, text="Stop Casting", command=self._on_stop)
        self._stop_button.grid(row=0, column=1, padx=(0, 8))

        self._test_button = ttk.Button(button_frame, text="Test Connection", command=self._on_test)
        self._test_button.grid(row=0, column=2)

        status_frame = ttk.Frame(main_frame)
        status_frame.pack(fill=tk.X, pady=(0, 10))
        ttk.Label(status_frame, text="Status:").pack(side=tk.LEFT)
        self._status_var = tk.StringVar(value="Stopped")
        self._status_label = ttk.Label(status_frame, textvariable=self._status_var, foreground="red")
        self._status_label.pack(side=tk.LEFT, padx=6)

        log_frame = ttk.LabelFrame(main_frame, text="Log")
        log_frame.pack(fill=tk.BOTH, expand=True)

        self._log_text = tk.Text(log_frame, height=10, state=tk.DISABLED, wrap=tk.WORD)
        self._log_text.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar = ttk.Scrollbar(log_frame, command=self._log_text.yview)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        self._log_text["yscrollcommand"] = scrollbar.set

        self._set_running(False)

    def _apply_settings(self) -> None:
        if self._settings.receiver_ip:
            self._ip_var.set(self._settings.receiver_ip)
        self._quality_var.set(str(self._settings.quality))

    def _schedule_log_pump(self) -> None:
        self._root.after(100, self._pump_logs)

    def _enqueue_log(self, message: str) -> None:
        self._log_queue.put(message)

    def _pump_logs(self) -> None:
        while not self._log_queue.empty():
            message = self._log_queue.get_nowait()
            self._append_log(message)
        self._schedule_log_pump()

    def _append_log(self, message: str) -> None:
        self._log_text.configure(state=tk.NORMAL)
        self._log_text.insert(tk.END, message + "\n")
        self._log_text.configure(state=tk.DISABLED)
        self._log_text.see(tk.END)

    def _set_running(self, running: bool) -> None:
        if running:
            self._status_var.set("Running")
            self._status_label.configure(foreground="green")
        else:
            self._status_var.set("Stopped")
            self._status_label.configure(foreground="red")
        state = tk.DISABLED if running else tk.NORMAL
        self._ip_entry.configure(state=state)
        self._quality_entry.configure(state=state)
        self._start_button.configure(state=tk.DISABLED if running else tk.NORMAL)
        self._stop_button.configure(state=tk.NORMAL if running else tk.DISABLED)

    def _on_start(self) -> None:
        receiver_ip = self._ip_var.get().strip()
        if not receiver_ip:
            messagebox.showerror("Validation", "Receiver IP is required.")
            return
        try:
            quality = int(self._quality_var.get().strip())
        except ValueError:
            messagebox.showerror("Validation", "Quality must be a number between 1 and 100.")
            return
        if quality < MIN_QUALITY or quality > MAX_QUALITY:
            messagebox.showerror("Validation", "Quality must be between 1 and 100.")
            return
        try:
            self._controller.start(receiver_ip, quality)
        except ValueError as exc:
            messagebox.showerror("Validation", str(exc))
            return
        self._set_running(True)
        self._save_settings(receiver_ip, quality)

    def _on_stop(self) -> None:
        self._controller.stop()
        self._set_running(False)

    def _on_test(self) -> None:
        receiver_ip = self._ip_var.get().strip()
        if not receiver_ip:
            messagebox.showerror("Validation", "Receiver IP is required.")
            return
        self._controller.test_connection(receiver_ip)

    def _on_close(self) -> None:
        if self._controller.is_running():
            self._controller.stop()
        self._save_settings(self._ip_var.get().strip(), self._quality_var.get().strip())
        self._root.destroy()

    def _save_settings(self, receiver_ip: str, quality_value: Union[str, int]) -> None:
        try:
            quality = int(quality_value)
        except (TypeError, ValueError):
            quality = DEFAULT_QUALITY
        settings = UserSettings(receiver_ip=receiver_ip, quality=quality)
        save_settings(settings)


def main() -> None:
    root = tk.Tk()
    SenderGUI(root)
    root.mainloop()


if __name__ == "__main__":
    main()
