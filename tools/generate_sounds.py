"""Sinh 4 file âm thanh .ogg ngắn (sóng sine) cho game, ghi vào app/src/main/res/raw/.

Chạy (cần Python 3 + các gói soundfile, numpy; chỉ dùng để SINH file, app không phụ thuộc):

    python -m venv .venv-snd
    .venv-snd\\Scripts\\pip install soundfile numpy
    .venv-snd\\Scripts\\python tools\\generate_sounds.py

File đã sinh được commit sẵn trong res/raw/ nên không cần chạy lại trừ khi muốn đổi âm.
"""
from pathlib import Path

import numpy as np
import soundfile as sf

RATE = 44100
OUT = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "raw"


def tone(freq, dur, amp=0.5, harmonic=0.0, decay=6.0, glide_to=None):
    """Một nốt sine, tắt dần theo hàm mũ. glide_to: trượt tần số từ freq tới glide_to."""
    t = np.linspace(0, dur, int(RATE * dur), endpoint=False)
    if glide_to is None:
        phase = 2 * np.pi * freq * t
    else:
        f = np.linspace(freq, glide_to, t.size)
        phase = 2 * np.pi * np.cumsum(f) / RATE
    wave = np.sin(phase) + harmonic * np.sin(2 * phase)
    envelope = np.exp(-decay * t / dur)
    # fade-in/out 5 ms để không bị tiếng "tách"
    fade = int(RATE * 0.005)
    envelope[:fade] *= np.linspace(0, 1, fade)
    envelope[-fade:] *= np.linspace(1, 0, fade)
    return amp * wave * envelope


def sequence(notes):
    """Ghép các nốt (tần số, thời lượng) nối tiếp nhau."""
    return np.concatenate([tone(f, d, amp=0.5, harmonic=0.25, decay=4.0) for f, d in notes])


def save(name, data):
    data = data / max(1e-9, np.abs(data).max()) * 0.8  # chuẩn hóa, chừa headroom
    path = OUT / f"{name}.ogg"
    sf.write(path, data.astype(np.float32), RATE, format="OGG", subtype="VORBIS")
    print(f"{path.name}: {path.stat().st_size} byte, {data.size / RATE:.2f} s")


OUT.mkdir(parents=True, exist_ok=True)

# Trượt: tiếng "vút" thấp, rất ngắn
save("sfx_slide", tone(220, 0.09, amp=0.5, decay=5.0, glide_to=130))

# Gộp: nốt Đô cao (C5). Cao độ được tăng khi phát (SoundPool rate) theo giá trị ô gộp
save("sfx_merge", tone(523.25, 0.18, amp=0.6, harmonic=0.3, decay=5.0))

# Thắng: arpeggio đi lên Đô - Mi - Sol - Đô cao
save("sfx_win", sequence([(523.25, 0.13), (659.25, 0.13), (783.99, 0.13), (1046.5, 0.5)]))

# Thua: ba nốt đi xuống Sol - Mi - Đô
save("sfx_lose", sequence([(392.0, 0.2), (329.63, 0.2), (261.63, 0.45)]))
