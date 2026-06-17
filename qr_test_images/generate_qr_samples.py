"""生成设备建档扫码测试用 PNG（需: pip install qrcode pillow）"""
import os

import qrcode

OUT = os.path.dirname(os.path.abspath(__file__))

SAMPLES = [
    (
        "01_json_name_code_dept.png",
        '{"name":"心电监护仪","code":"ECG-2026-001","dept":"ICU"}',
    ),
    (
        "02_json_aliases.png",
        '{"deviceName":"输液泵","deviceCode":"PUMP-2026-002","department":"外科"}',
    ),
    (
        "03_pipe_three_fields.png",
        "便携式超声|US-2026-003|影像科",
    ),
    (
        "04_code_only.png",
        "ONLY-CODE-2026-999",
    ),
    (
        "05_json_min_keys.png",
        '{"n":"陪护床","id":"BED-01","d":"急诊"}',
    ),
]


def main() -> None:
    os.makedirs(OUT, exist_ok=True)
    for filename, data in SAMPLES:
        path = os.path.join(OUT, filename)
        img = qrcode.make(data)
        img.save(path)
        print("Wrote", path)
    print("Payloads:")
    for _, data in SAMPLES:
        print(" -", data)


if __name__ == "__main__":
    main()
