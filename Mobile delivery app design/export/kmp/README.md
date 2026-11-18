# Shendo — home screen (layout 1c) in Compose Multiplatform

Transcription of the `1c` mock from *Shendo Home.dc.html* into Kotlin.
Written against Compose Multiplatform 1.7+ / Kotlin 2.0+. It has **not** been
compiled here — treat it as a faithful starting point, not a built artifact.

## Files

| File | What it holds |
| --- | --- |
| `ui/theme/OrganicTheme.kt` | Every Organic token — colour roles, the three 100–900 ramps, spacing, radii, type scale, `ShendoTheme` |
| `ui/theme/OrganicTheme.android.kt` / `.ios.kt` | `expect/actual` font families (Caprasimo + Figtree) |
| `data/Place.kt` | `Place` model, `Category` enum with its dot colour, sample data, `money()` |
| `ui/home/HomeScreen.kt` | The screen: profile bar, search pill, craving circles, place cards, bottom nav |
| `App.kt` | Themed entry point |

## Wiring it up

1. Copy `composeApp/src/` into your KMP module (package `com.shendo.app`).
2. Add the fonts: `caprasimo_regular.ttf` and `figtree_variable.ttf` in
   `res/font/` (Android) and `composeResources/font/` (shared), then fill in
   the iOS `actual`.
3. Replace `samplePlaces` with your repository/ViewModel. `HomeScreen` keeps
   its state in `remember` — lift it into a `ViewModel` before shipping.
4. Swap `PlaceThumb` and the avatar `Box` for real images (Coil 3 or Kamel).

## Known gaps

- Photography is placeholder tint blocks, as in the mock.
- Search is a static pill, not a working field.
- The cart bar was removed from 1c, so no cart state is modelled here.
- `Icons.Outlined.*` is Material's set; the system asks for Lucide at
  stroke-width 2.75 — import the Lucide vectors for a faithful match.
