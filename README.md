# bp-log-android

# BP-Log (Android)

A privacy-first blood pressure logger that works completely offline.
No account, no cloud, no analytics. Your data stays on your device.

## Features
- Log blood pressure (systolic / diastolic) and pulse
- History and charts (7 days / 30 days / all time)
- Export measurements to CSV
- Export reports to PDF (including charts)
- Fully offline — no internet connection required

## CSV format
Example:
timestamp,date,time,systolic,diastolic,pulse
2026-02-18T08:30:15.123,2026-02-18,08:30:15,128,82,65

Field description:

- `timestamp` — ISO datetime including milliseconds
- `date` — YYYY-MM-DD
- `time` — HH:MM:SS (24h format)
- `systolic` — mmHg
- `diastolic` — mmHg
- `pulse` — bpm

Times are stored using the device's local time.

## Privacy
BP-Log does not collect, store, or transmit personal data.
All measurements remain on your device.

## Medical disclaimer
This app is intended for self-tracking only and does not provide medical advice, diagnosis, or treatment.
Always consult a qualified healthcare professional regarding medical concerns.

## Build
Open the project in Android Studio and run the `app` configuration.

## License
MIT — see [LICENSE](LICENSE).