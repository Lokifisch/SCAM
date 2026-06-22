# SCAM — Scale Character Attribute Mod

SCAM — Scale Character Attribute Mod. Resize yourself in-game with `/setheight`; just type a number and SCAM figures out whether it's metres, centimetres, or feet automatically.

---

## Usage

```
/setheight <height>
```
Alias: `/sh`

### Auto-detection (no suffix needed)

| You type | Detected as | Result |
|----------|-------------|--------|
| `1.8`    | metres      | 1.80 m |
| `180`    | centimetres | 1.80 m |
| `6ft`    | feet        | 1.83 m |

**Rule:** numbers ≤ 2 → metres · numbers > 2 → centimetres  
Append `m`, `cm`, or `ft` to override detection at any time.

---

## Limits

|         | Metric              | Imperial    |
|---------|---------------------|-------------|
| **Min** | 1/30 m (~3.3 cm)    | ~0.109 ft   |
| **Max** | 2 m (200 cm)        | ~6.56 ft    |

> Minecraft's internal floor for `generic.scale` is `0.0625` (~11 cm), so heights below that are rejected by the game itself.

---

## Requirements

- **Paper 1.20.5+** — requires the `generic.scale` attribute added in that release
- **Java 21+**

## Permissions

| Node             | Default        | Description        |
|------------------|----------------|--------------------|
| `scam.setheight` | `true` (everyone) | Use `/setheight` |
