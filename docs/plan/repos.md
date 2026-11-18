# Rest by the hearth

Idea 3 of the study of features to add (2026-11-17), chosen by the team on 2026-11-18. A home is worth building: sitting or standing still by a lit fire, under a roof, the player gets the **Rested** effect (« Reposé »), which heals faster and makes hunger slower. The more varied the home around them, the longer it lasts. No number on screen: only the effect's icon and time.

Status: checked on a dedicated server of the full pack with one player on 2026-11-18 (`fallow` 0.33.0), not played.

## The rule, for a player

- **By the hearth**: a lit fire within 5 blocks (2 up or down): campfire or soul campfire, fire, furnace, smoker, blast furnace, the vigil fire of the clearings, Farmer's Delight's stove, Supplementaries' fire pit, Cold Sweat's hearth and boiler (block tag `fallow:foyers`). A block with a `lit` state counts only when lit.
- **Under a roof**: the sky is not visible above the player's head.
- **At rest**: moving less than 0.6 blocks every 2 seconds, or seated (on a chair, a bench, a horse).
- After **20 seconds** of rest, the action bar says « Vous vous reposez au coin du feu. » (or « Vous vous reposez, bien au chaud chez vous. » in a comfortable home) and the effect starts. While the player keeps resting, it stays full; when they leave, it runs down.
- **Duration**: 2 minutes, plus 1.5 minutes for each family of comfort found within 8 blocks (4 up or down): up to 15.5 minutes with all nine.
- **Effect**: half a heart every 6 seconds as long as food is at 6 or more (the game's own regeneration needs 18), and a quarter less hunger from every effort (walking, mining, fighting, healing).

## The nine families of comfort

Counting families rather than blocks rewards a varied home, not a warehouse of chairs. Each family is a block tag `fallow:confort/<family>`, in the `fallow` mod's data; the pack can extend them.

| Family | Blocks |
|---|---|
| `couchage` (bed) | beds, Handcrafted's fancy beds, Comforts' sleeping bags and hammocks |
| `siege` (seat) | Handcrafted's chairs, benches, couches, dining benches, cushions |
| `table` | Handcrafted's tables, desks, side tables, nightstands, counters |
| `tapis` (rug) | wool carpets, moss carpet |
| `rangement` (storage) | chests, barrels, Handcrafted's cupboards, drawers and shelves, Farmer's Delight's cabinets, Supplementaries' item shelves and sacks |
| `livres` (books) | bookshelves, chiselled bookshelf, lectern, Supplementaries' globes |
| `lumiere` (light) | candles, lanterns, Supplementaries' sconces and candle holders |
| `decoration` | flower pots, banners, decorated pots, Handcrafted's trophies and pots, Supplementaries' flags and buntings |
| `cuisine` (kitchen) | smoker, Farmer's Delight's cooking pot, cutting board, stove and skillet, Handcrafted's crockery, plates, cups and bowls |

The counter (`repos.Confort`) is written to serve the planned `vie` module too, which will count the variety of life in a clearing.

## How it works

See [mod-fallow.md](mod-fallow.md#repos-module). Config (`fallow-common.toml`, `[repos]`): `enabled` (true), `restSeconds` (20), `baseMinutes` (2), `minutesPerComfort` (1.5).

## In-game tests

| # | Test | Expected |
|---|---|---|
| RE1 | Light a campfire in the open and stand by it for a minute | Nothing: no roof |
| RE2 | Build four walls and a roof over it, stand still inside for 20 s | « Vous vous reposez au coin du feu. », the « Reposé » icon with about 2 minutes |
| RE3 | Add a bed, a chair, a table, a carpet, a chest, a bookshelf, a candle, a flower pot and a cooking pot, rest again | « … bien au chaud chez vous. », about 15 minutes |
| RE4 | Put the campfire out with a shovel | No more rest; the effect runs down |
| RE5 | Hurt, with half a food bar, rested | Health comes back half a heart every 6 seconds |
| RE6 | Sit on a Handcrafted chair by the fire, under the roof | Rest counts while seated; walking around the room stops it |
| RE7 | Effect name and description in EMI or the inventory | « Reposé », in French |
