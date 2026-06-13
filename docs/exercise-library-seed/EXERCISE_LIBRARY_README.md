# Exercise Library Seed

Generated for FitLog / MyAndroidApp.

Files:
- `EXERCISE_LIBRARY_SEED.xlsx`: main import workbook.
- `EXERCISE_LIBRARY_SEED.csv`: UTF-8-SIG CSV version for direct parsing.

Rows: 82

Recommended import approach:
1. Map `训练部位` to the existing `TrainingTarget` values.
2. Map `肌肉图主区域key` and `肌肉图次区域key` to SVG region IDs.
3. Use `建议休息秒数` as the default exercise rest time if later adding per-exercise timers.
4. Use `默认组数` and `默认次数/时长` to prefill action slots.
5. Treat `资料依据` as provenance notes, not as UI text.

Sources consulted:
- ACE Fitness Exercise Library for common action coverage and body-part/equipment style: https://www.acefitness.org/resources/everyone/exercise-library/
- wger open-source exercise/wiki/API model for exercise/muscle/equipment data structure: https://github.com/wger-project/wger and https://wger.de/en/software/api
- ExerciseDB-style public metadata for target/secondary muscle/equipment patterns: https://oss.exercisedb.dev/docs
- NASM rest interval guidance for default rest seconds: https://blog.nasm.org/uncategorized/determining-best-rest-periods-resistance-exercise-training-goals

Important:
- Text is normalized and AI-summarized; it is not medical advice.
- Images/GIFs are not included. Add assets separately and record them in `docs/ASSET_SOURCES.md`.
