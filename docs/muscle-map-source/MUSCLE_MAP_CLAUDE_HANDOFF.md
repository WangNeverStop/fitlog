# 1.0 Muscle Map SVG Source And Claude Handoff

Date: 2026-06-08

## Selected Source

- Source repo: https://github.com/soroojshehryar/react-muscle-highlighter
- Local clone: `D:\MyAndroidApp\docs\muscle-map-source\react-muscle-highlighter`
- Package name: `react-muscle-highlighter`
- License: MIT
- Why selected: it provides front/back human body SVG path data, stable muscle slugs, male/female variants, and existing highlight logic. It is suitable as a local static vector source for our Android muscle response map.

Important: do not add React, WebView, npm runtime, or online image loading to the Android app. Use this repository only as a static SVG/path reference.

## Source Files To Inspect

- `assets/bodyFront.ts`: male front muscle path data.
- `assets/bodyBack.ts`: male back muscle path data.
- `assets/bodyFemaleFront.ts`: female front muscle path data.
- `assets/bodyFemaleBack.ts`: female back muscle path data.
- `components/SvgMaleWrapper.tsx`: SVG wrapper and `viewBox`.
- `components/SvgFemaleWrapper.tsx`: female SVG wrapper and `viewBox`.
- `index.tsx`: supported `Slug` values and highlight data model.
- `LICENSE`: MIT license text that must be preserved in app open-source notices.

For 1.0, include both male and female front/back sources. Choose the body model from the current user's saved sex/gender field. Do not add extra body-type customization in 1.0.

## Current Source Shape

The source does not store standalone `.svg` files. It stores path arrays in TypeScript:

```ts
{
  slug: "chest",
  path: {
    left: ["M272.91 ..."],
    right: ["M416.04 ..."]
  }
}
```

The wrapper uses these viewBoxes:

- Male front: `0 0 724 1448`
- Male back: `724 0 724 1448`
- Female front: `-50 -40 734 1538`
- Female back: `756 0 774 1448`
- Display ratio in original component: width 200, height 400

Important for Android conversion: do not assume the female paths use the same origin or x-offset as the male paths. Store per-model/per-side viewBox metadata and transform each side with its own viewBox.

## Supported Source Slugs

Front source includes:

`chest`, `obliques`, `abs`, `biceps`, `triceps`, `neck`, `trapezius`, `deltoids`, `adductors`, `quadriceps`, `knees`, `tibialis`, `calves`, `forearm`, `hands`, `ankles`, `feet`, `head`, `hair`

Back source includes:

`neck`, `trapezius`, `deltoids`, `upper-back`, `triceps`, `lower-back`, `forearm`, `gluteal`, `adductors`, `hamstring`, `calves`, `ankles`, `feet`, `hands`, `head`, `hair`

## 1.0 App-Level Muscle Keys

Use these app keys in our exercise data and UI:

- `chest`
- `deltoids`
- `biceps`
- `triceps`
- `forearm`
- `abs`
- `obliques`
- `trapezius`
- `upper_back`
- `lower_back`
- `glutes`
- `adductors`
- `quadriceps`
- `hamstrings`
- `calves`
- `tibialis`

Ignore or hide these source slugs for 1.0 because they are not training muscles in our app display:

`head`, `hair`, `neck`, `hands`, `feet`, `ankles`, `knees`

## Source Slug Mapping

| App key | Source slug | Notes |
| --- | --- | --- |
| `chest` | `chest` | Front only |
| `deltoids` | `deltoids` | Front and back |
| `biceps` | `biceps` | Front only |
| `triceps` | `triceps` | Front and back |
| `forearm` | `forearm` | Front and back |
| `abs` | `abs` | Front only |
| `obliques` | `obliques` | Front only |
| `trapezius` | `trapezius` | Front and back |
| `upper_back` | `upper-back` | Back only |
| `lower_back` | `lower-back` | Back only |
| `glutes` | `gluteal` | Back only |
| `adductors` | `adductors` | Front and back |
| `quadriceps` | `quadriceps` | Front only |
| `hamstrings` | `hamstring` | Back only |
| `calves` | `calves` | Front and back |
| `tibialis` | `tibialis` | Front only |

## Recommended Android Implementation

Create a local Compose component:

```kotlin
@Composable
fun MuscleMap(
    primary: Set<MuscleRegion>,
    secondary: Set<MuscleRegion>,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
)
```

Implementation options:

1. Preferred for 1.0: convert the TS path arrays into Kotlin path data constants and draw with Compose `Canvas`/`PathParser`.
2. Acceptable: convert front/back into Android VectorDrawable XML layers and tint each layer by region.
3. Avoid for 1.0: WebView, bundled React, remote SVG links, runtime network calls, or adding a large vector rendering dependency.

## Visual Rules For Our App

Use the same colors everywhere the muscle map appears:

- Default body/muscle fill: very pale gray-blue, e.g. `#EEF4F8`
- Outline: soft blue-gray, e.g. `#C8D8E3`
- Primary trained region: deep blue, e.g. `#2F80ED`
- Secondary/compensation region: light blue, e.g. `#8EC5FF`
- Background card: warm white / app card color, not pure hospital white

The body map should feel clean and light. The source paths are detailed, but the first version should behave like a simple response diagram, not a medical anatomy tool.

## Where This Map Is Used

- Action selection page lower half: selected actions drive primary/secondary highlights.
- Exercise detail page: one action drives primary/secondary highlights.
- Training summary/details: show main trained muscle groups if space allows.
- Calendar selected-day detail: can show compact map for that day's main training area later.

Homepage weekly dot still uses the original selected training target, not the union of all selected action muscles.

## Data Contract

The exercise library should keep stable muscle keys:

- `primaryMuscleKeys: List<String>`
- `secondaryMuscleKeys: List<String>`

For current seed files, map `muscle_map_primary_keys` and `muscle_map_secondary_keys` into these lists. The UI should not parse Chinese muscle names to infer highlights.

## CSV Key Normalization

Claude feedback on 2026-06-08: the current implementation highlights by broad training target first. To support per-exercise precision, normalize CSV muscle-map keys before passing them to `MuscleMap`.

Do not rewrite the CSV just to fit the SVG source. Keep the detailed CSV keys, then add a Kotlin normalization layer:

```kotlin
fun normalizeMuscleKey(raw: String): MuscleRegion?
```

Suggested mapping from current CSV keys to 1.0 `MuscleRegion`:

| CSV key | 1.0 app region |
| --- | --- |
| `chest`, `upper_chest`, `lower_chest` | `chest` |
| `front_delts`, `side_delts`, `rear_delts`, `shoulders`, `rotator_cuff`, `shoulder_stabilizers` | `deltoids` |
| `biceps`, `brachialis` | `biceps` |
| `triceps`, `triceps_long_head` | `triceps` |
| `forearms`, `brachioradialis` | `forearm` |
| `abs`, `abs_lower`, `core`, `hip_flexors` | `abs` |
| `obliques` | `obliques` |
| `traps`, `upper_traps`, `mid_traps`, `lower_traps`, `levator_scapulae` | `trapezius` |
| `upper_back`, `lats`, `rhomboids`, `teres_major`, `back` | `upper_back` |
| `erectors` | `lower_back` |
| `glutes`, `glute_med` | `glutes` |
| `adductors` | `adductors` |
| `quads`, `legs` | `quadriceps` |
| `hamstrings` | `hamstrings` |
| `calves`, `calves_gastrocnemius`, `soleus` | `calves` |
| `cardio`, `arms` | `null` or broad fallback only |

Rules:

- Primary regions should win over secondary regions if the same normalized region appears in both sets.
- Drop `null` keys silently for the map. For example, `cardio` should not light up the whole body in 1.0.
- Keep the original detailed keys in model/data when possible, because they may be useful for 2.0 detail labels.
- If an imported action has no usable normalized key, fall back to `TrainingTarget.toMuscleRegions()` rather than showing an empty broken state.

Recommended next implementation step:

1. Parse `肌肉图主区域key` and `肌肉图次区域key` from seed/imported exercises.
2. Normalize each comma-separated key into `MuscleRegion`.
3. Drive action selection and exercise detail maps from normalized per-action keys.
4. Keep the current training-target mapping only as fallback.

## Sex-Based Body Model Scope

User decision on 2026-06-08: female map should be included in 1.0. The app already stores simple user body data including sex/gender, so the muscle map should use that value to choose male or female paths.

Use a simple model enum:

```kotlin
enum class MuscleMapBodyModel { MALE, FEMALE }
```

Recommended API:

```kotlin
@Composable
fun MuscleMap(
    primary: Set<MuscleRegion>,
    secondary: Set<MuscleRegion>,
    bodyModel: MuscleMapBodyModel,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
)
```

Rules:

- If current user sex/gender is female, use female front/back paths.
- If current user sex/gender is male, use male front/back paths.
- If sex/gender is unset, unknown, or not loaded yet, default to `MALE` or a neutral default consistently; do not crash or show a blank map.
- This is only a display model switch in 1.0. Do not add extra body shape, height/weight scaling, or anatomy customization.
- Male and female should share the same `MuscleRegion` enum and CSV key normalization rules.
- Add previews for both male and female maps.

## Tap-To-Show Muscle Name Scope

Tap-to-show muscle names is useful, but it is not required for the first replacement of the placeholder map.

For 1.0, the map can be display-only. If interaction is easy after path conversion, support a simple callback:

```kotlin
onRegionClick: ((MuscleRegion) -> Unit)? = null
```

When enabled, show a small bottom sheet or tooltip with the Chinese muscle/group name, for example `胸部`, `三角肌`, `肱三头肌`. Avoid complex hit testing if it delays the basic muscle response map.

## License Requirements

- Keep the cloned source under `docs/muscle-map-source/react-muscle-highlighter`.
- Preserve `LICENSE` from the source repo.
- Add this source to app open-source/about notices before release.
- If paths are modified/simplified, note that they are adapted from `react-muscle-highlighter`.

## Suggested First Claude Task

1. Add `MuscleRegion` enum/sealed model using the app keys above.
2. Add mapping from source slugs to app keys.
3. Convert male and female front/back path files into local Kotlin constants or VectorDrawable layers:
   - male: `bodyFront.ts`, `bodyBack.ts`
   - female: `bodyFemaleFront.ts`, `bodyFemaleBack.ts`
4. Implement `MuscleMap(primary, secondary, bodyModel)` preview with at least these states:
   - empty state
   - chest primary + triceps secondary
   - back primary + biceps/forearm secondary
   - female chest/triceps state
5. Replace existing placeholder body map in action selection and exercise detail pages.
6. Read the current user's saved sex/gender field and pass the corresponding `MuscleMapBodyModel` into the map.
