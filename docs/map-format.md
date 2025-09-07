# Map File Format

This document describes the text format used for MilitaryMadness maps.

Versioning:
- Legacy format (no header): first two integers are width and height (columns then rows).
- Versioned format (MMAPv1): the first token is `MMAPv1`, followed by width and height.
- The loader supports both formats. Saving currently writes the legacy format to remain backward compatible with existing content and tests.

Dimensions:
- `width` (columns) and `height` (rows) must be positive and reasonably bounded (validated up to 500).

Tiles section:
- After the two dimension integers, provide `width * height` tile codes in row-major order.
- Tile codes (by type):
  - `-1` = void/out of bounds (not playable)
  - `0..4` = terrain (multiplied by 10 internally for rendering): plain, forest, mountain, water, road
  - `5` = blue base
  - `6` = red base
  - `7` = neutral factory
  - `8` = blue factory
  - `9` = red factory

Units section (optional):
- After tiles, each following line may describe one unit placed on the map:
  - `x y name team`
  - `x y` are zero-based integer coordinates within bounds.
  - `name` must match an entry in `Resources/Units.txt`.
  - `team` is `true` (blue) or `false` (red).

Example (legacy):
```
3
3
0 0 0
0 5 0
0 6 0
```

Example (versioned):
```
MMAPv1 3 3
0 0 0
0 5 0
0 6 0
```

Notes:
- Files are read as UTF-8 and written as UTF-8.
- The loader validates dimensions, tile codes, and unit coordinates.
