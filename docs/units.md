# Units and Stats

Units are defined in `Resources/Units.txt` and loaded at runtime by `LocationManager` when placing units from map files.

File format (excerpt):
- The file is a whitespace-separated list with entries per unit type. The loader seeks by unit name and reads fields in order.
- Fields (in order):
  - `name` (string)
  - `type` (string, e.g., Infantry, Tank)
  - `isRanged` (boolean)
  - `isAir` (boolean)
  - `landAttack` (int)
  - `airAttack` (int)
  - `range` (int)
  - `defense` (int)
  - `shift` (int)

Notes:
- Team is provided by the map file (true for blue, false for red).
- At runtime, the engine currently stores team as a boolean but adapters to the Team enum are available on Unit to support future migration.
- Images are resolved by `ModelManager` / `Model` using the unit `name` mapped to `Resources/<name>.gif`.

Future improvement:
- Consider externalizing to a structured format like JSON or YAML for readability and validation.
- A converter can be added to produce JSON from the existing Units.txt and maintain backward compatibility.
