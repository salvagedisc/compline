# The Hosts

Six masked peoples live in Fallow's wild places. Each one guards one of Archie's Dinosaurs and wears its armour. Each people sends one recurring emissary, who comes out of the mist, speaks with the player and accepts an offering.

Status: pilot, `fallow` 0.37.0 (`hotes` module). The emissaries appear only through a command for now: they do not spawn on their own yet, and nothing in the world depends on them. Checked on the bench client on 2026-11-21, not played.

## The six peoples

Each people is named by the way it guards its beast. Names and lines are in French in game (the people's name is a gesture, the emissary's a made-up name).

| Id | Beast (Archie's Dinosaurs) | Emissary – people (fr) | English | Mask | Habitat behind the portrait | Offerings |
|---|---|---|---|---|---|---|
| `ecorce` | Lushsaur | Ounèle – les Discrets | the Unseen | `fallow:masque_de_l_ecorce` | lush cave, bioshrooms | glow berries, honey bottle, bone meal |
| `braise` | Obsidiceratops | Orrek – les Attiseurs | the Stokers | `fallow:masque_de_la_braise` | redstone caves | charcoal, magma block, cooked mutton |
| `givre` | Chorusoatl | Cilvi – les Chantres | the Cantors | `fallow:masque_du_givre` | prismachasm (Regions Unexplored) | amethyst shard, chorus fruit, feather |
| `fonds` | Coralosaur | Loubé – les Souffleurs | the Breathgivers | `fallow:masque_des_fonds` | warm reef, light shafts from the surface | cod, kelp, sea pickle |
| `sables` | Therimoss | Kéta – les Guetteurs | the Watchers | `fallow:masque_des_sables` | jungle | melon slice, bamboo, cocoa beans |
| `silence` | Sculksaur | Sifé – les Taiseux | the Hushed | `fallow:masque_du_silence` | deep dark, sculk sensors, one grey candle | grey wool, echo shard, grey candle |

## How it works

- `/fallow hotes appeler <peuple>` (operators): the emissary appears 14 blocks ahead of the player and walks up.
- Right-clicking the emissary opens the dialogue screen: a live portrait of the emissary turned 25°, framed the same way for every people, over the people's habitat. The title reads « Nom – les Peuple ». The answers are a menu ([0042](../decisions/0042-interaction-language.md)): a click finishes the text, Space or a click continues, Escape leaves, and the camera turns in 150 ms.
- Most lines have variants (`<key>.2`, `<key>.3` in the lang files), picked at random each time.
- The emissaries speak to the player as « tu »: it is in-world speech, not one of Fallow's own hints, which say « vous » ([0042](../decisions/0042-interaction-language.md)).
- An emissary ignores damage that has no attacker (suffocation, cramming, a fall), unless the damage bypasses invulnerability (`/kill`, the void), so it cannot vanish on its own.

## Masks and regalia

- A mask is a real helmet (`ArmorItem`). Its shape is described once, in `assets/fallow/hotes/masques/<peuple>.json`. `MasqueModel` reads it for the worn model, and the item icon is drawn from the same shape. Pieces marked `porte_seulement` are only on the worn mask: they are left out of the item and of its icon.
- Every mask shares one outline: the plate covers x -4..3 and y -9..0, and the ornaments stay inside x -6..5 and y -12..0. Icons are anchored on the same pixel (8, 13), so the six masks line up in the inventory. Item models are rotated by 45° at most.
- The regalia (`assets/fallow/hotes/parures/<peuple>.json`, `ParureLayer`) are the rest of the people's dress; the emissaries keep Archie's boots.
- Flat planes have a back face that mirrors the front face exactly, otherwise they flicker.

## Next steps

- Let the emissaries come on their own, near their beast's habitat, and give their offerings a lasting effect.
- Play the dialogue with each people in a real world.
