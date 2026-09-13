# 0010. Adopt the FINMORI indigo identity

**Status:** Accepted
**Date:** 2026-09-13

## Context

FinFlow's visual identity was a deep "money" green (`#1B6B4A`) with an amber secondary, asserted
in two places: `Color.kt`'s header comment and `Theme.kt`'s KDoc, which turned dynamic colour off
on the grounds that "FinFlow's green identity is part of the product".

The product owner supplied eleven reference screens in `docs/screenshot/` — home, analytics,
goals, transaction history, profile, add-transaction — and asked for the app theme to be set from
them. Those screens are not green. They are an indigo-and-gray-blue system in which green appears
only as the income colour.

The sampled values are literal Untitled UI gray-blue and brand ramp entries (`#101323`, `#363F72`,
`#293056`, `#D5D9EB`, `#F8F9FC`, `#1234D1`, `#079455`, `#D92D20`), which is why they land on such
round numbers. The palette was extracted by sampling flat regions across all eleven frames rather
than by eye.

Typography is also two families rather than one: a geometric sans for all UI, and a high-contrast
serif reserved for hero numerals and proper names — a balance, a goal title, a person's name.
`Type.kt` had used `FontFamily.Default` on the stated grounds that the platform font "keeps the
APK small and renders consistently in Compose previews".

## Decision

Replace the green identity with the indigo one, in `Color.kt`, and bundle both font families in
`ui/src/main/res/font/`.

Green is retained, narrowed to its semantic role: `FinFlowExtendedColors.income` and `.success`.

Two tokens were added to `FinFlowExtendedColors` for roles the reference screens use and Material
3 has no slot for: `emphasis`/`onEmphasis`, the solid navy surface behind the primary CTA, the
bottom bar and chart tooltips; and `progress`/`progressTrack`, the goal progress bar, which is a
different hue from income because progress is not money.

## Consequences

Every screen in the app changes colour at once, because every component reads these tokens. That
is the intent, but it means this commit cannot be reviewed screen by screen — the tokens are the
review surface.

The APK grows by roughly 500 KB for six static font files. They are static weight instances rather
than the variable originals because `minSdk` is 24 and a variable font's weight axis only responds
from API 26; on 24–25 every weight would silently collapse to a single instance. Both families are
SIL OFL.

The font identification is a best-match read from raster screenshots, not confirmed against Figma
text properties — the Figma file was not machine-readable (`api.figma.com` returns `403` without a
token, and the design canvas is painted by authenticated JS, so a public link carries no design
content). The sans has a double-storey `a` and a straight-descender `y`, which rules out Poppins
and Inter; Plus Jakarta Sans is the closest fit. Playfair Display is the serif. Both are read
through `Type.kt` alone, so swapping either is a one-file change with no call-site churn.

The dark scheme is derived, not sampled — all eleven reference frames are light. Two values are
deliberately not the light ones darkened: `primary` lifts to `#8FA6FF` and the CTA surface to
`#4361EE`, because `#1234D1` and `#293056` both fall below 3:1 against the `#101323` ground and
would read as unlit shapes. It should be re-checked against real dark reference frames when they
exist.

Two values sit in AA-large rather than AA, and are kept because they are the design's own:
`income` `#079455` is 3.91:1 on white, and is only ever used for amounts set at 16sp SemiBold or
larger; `progress` `#66C61C` is 1.55:1 against its own track, which is acceptable for a filled bar
whose boundary is also a shape edge, but would not be for text.

## Alternatives considered

**Keep the green and take only the layout.** Rejected: the request was explicitly to set the theme
from the screenshots, and the palette is the most visible half of what those screens are.

**Keep `FontFamily.Default`.** Rejected. The serif numerals are the signature of this design —
without them the screens read as a generic Material app wearing indigo. The stated reasons for the
platform font were APK size and preview consistency; 500 KB is a fair price, and bundled static
fonts render identically in previews, which downloadable fonts would not.

**Use the variable font files.** Rejected on `minSdk 24`, as above.

**Generate both schemes from a seed colour.** Rejected for the same reason the original palette was
written out explicitly: a generated ramp does not reproduce the sampled values, and the whole point
of extracting exact hexes was to match the reference.