"""Fallow's title screen and loading screen, for FancyMenu and Drippy Loading
Screen: the Fallow logo (stone letters overgrown with moss and grass, in the
pack's mist and moss) in place of Minecraft's, and a moss loading screen with
the logo and a bar in lantern light in place of Mojang's red one.

Writes into pack/config: fancymenu/assets/fallow_logo.png, the two layouts,
the list of customised screens and FancyMenu's options (modpack mode: players
never see the editor). The title screen keeps the vanilla buttons, the
panorama drawn by build.py and the texts the game requires.

Called by build.py; can run alone: python3 scripts/interface/menus.py
"""
import hashlib
import random
from pathlib import Path

from PIL import Image

LETTERS = {
    "F": ["#####", "#....", "#....", "####.", "#....", "#....", "#...."],
    "A": [".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#"],
    "L": ["#....", "#....", "#....", "#....", "#....", "#....", "#####"],
    "O": [".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###."],
    "W": ["#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#."],
}
OUTLINE = (26, 31, 28)
BONE = (232, 229, 208)
MIST = (203, 202, 182)
SPECK = (188, 188, 167)
SHADE = (166, 170, 150)
DEPTH = [(106, 117, 104), (82, 92, 82), (66, 76, 66), (52, 60, 53)]
GREEN = [(78, 98, 72), (106, 130, 90), (140, 160, 112)]
# the loading screen
GROUND = "#1F2521"
BAR = "#E2A64EFF"
BAR_BACK = "#34403AFF"

LOGO_SCALE = 4


def logo(word="FALLOW", block=6, seed=3):
    """The logo at one pixel per GUI pixel: letters of 5x7 blocks, lit from the
    top left, pushed back in moss, with grass on some tops and vines below."""
    rnd = random.Random(seed)
    blocks = set()
    for i, ch in enumerate(word):
        for r, row in enumerate(LETTERS[ch]):
            blocks |= {(i * 6 + c, r) for c, v in enumerate(row) if v == "#"}
    depth, top = len(DEPTH), 8
    im = Image.new("RGBA", (len(word) * 6 * block + depth + 4, 7 * block + depth + top + 12))
    px = im.load()
    face = {(2 + bx * block + x, top + by * block + y): (bx, by, x, y)
            for bx, by in blocks for y in range(block) for x in range(block)}
    for d in range(depth, 0, -1):
        for x, y in face:
            px[x + d, y + d] = DEPTH[d - 1] + (255,)
    for (x, y), (bx, by, i, j) in face.items():
        c = SPECK if rnd.random() < 0.12 else MIST
        if (j == block - 1 and (bx, by + 1) not in blocks) or (i == block - 1 and (bx + 1, by) not in blocks):
            c = SHADE
        if (j == 0 and (bx, by - 1) not in blocks) or (i == 0 and (bx - 1, by) not in blocks):
            c = BONE
        px[x, y] = c + (255,)
    solid = {(x, y) for y in range(im.height) for x in range(im.width) if px[x, y][3]}
    for x, y in solid:
        for n in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if n not in solid and 0 <= n[0] < im.width and 0 <= n[1] < im.height:
                px[n] = OUTLINE + (255,)
    # moss and grass on some tops
    for bx, by in sorted(blocks):
        if (bx, by - 1) in blocks or rnd.random() >= 0.45:
            continue
        x0, y = 2 + bx * block, top + by * block
        for i in range(block):
            if rnd.random() < 0.7:
                px[x0 + i, y] = GREEN[rnd.choice((1, 2))] + (255,)
        for _ in range(rnd.randint(2, 4)):
            x, h = x0 + rnd.randint(0, block - 1), rnd.randint(1, 3)
            for k in range(h):
                px[x, y - 1 - k] = GREEN[1 if k < h - 1 else 2] + (255,)
            if not px[x - 1, y - 1][3]:
                px[x - 1, y - 1] = OUTLINE + (255,)
    # vines under some blocks
    for bx, by in sorted(blocks):
        if (bx, by + 1) in blocks or by == 6 or rnd.random() >= 0.3:
            continue
        x, y = 2 + bx * block + rnd.randint(1, block - 2), top + (by + 1) * block
        for k in range(rnd.randint(3, 8)):
            xx = x + (k % 4 == 2)
            if px[xx, y + k][3] and px[xx, y + k][:3] not in DEPTH + [OUTLINE]:
                break
            px[xx, y + k] = GREEN[0] + (255,)
            if k % 3 == 1:
                px[xx + 1, y + k] = GREEN[1] + (255,)
    return im.crop(im.getbbox())


def ident(name):
    """A stable id in FancyMenu's form, so that rebuilding changes nothing."""
    h = hashlib.sha1(name.encode()).hexdigest()
    return f"{h[:8]}-{h[8:12]}-{h[12:16]}-{h[16:20]}-{h[20:32]}-1790000000000"


def common(name, anchor, x, y, w, h):
    """The keys every FancyMenu element carries, as its editor writes them."""
    req = ident(name + ":requirements")
    return f"""  instance_identifier = {name}
  appearance_delay = no_delay
  appearance_delay_seconds = 1.0
  fade_in_v2 = no_fading
  fade_in_speed = 1.0
  fade_out = no_fading
  fade_out_speed = 1.0
  base_opacity = 1.0
  auto_sizing = false
  auto_sizing_base_screen_width = 0
  auto_sizing_base_screen_height = 0
  sticky_anchor = false
  anchor_point = {anchor}
  x = {x}
  y = {y}
  width = {w}
  height = {h}
  stretch_x = false
  stretch_y = false
  stay_on_screen = true
  element_loading_requirement_container_identifier = {req}
  [loading_requirement_container_meta:{req}] = [groups:][instances:]
  enable_parallax = false
  parallax_intensity_v2 = 0.5
  invert_parallax = false
  animated_offset_x = 0
  animated_offset_y = 0
  load_once_per_session = false
  in_editor_color = #FFC800FF
  layer_hidden_in_editor = false
"""


def hidden(name):
    """A vanilla widget, hidden."""
    block, state = ident(name + ":block"), ident(name + ":state")
    return f"""vanilla_button {{
  button_element_executable_block_identifier = {block}
  [executable_block:{block}][type:generic] = [executables:]
  restartbackgroundanimations = true
  nine_slice_custom_background = false
  nine_slice_border_x = 5
  nine_slice_border_y = 5
  navigatable = true
  widget_active_state_requirement_container_identifier = {state}
  [loading_requirement_container_meta:{state}] = [groups:][instances:]
  is_template = false
  template_apply_width = false
  template_apply_height = false
  template_apply_posx = false
  template_apply_posy = false
  template_apply_opacity = false
  template_apply_visibility = false
  template_apply_label = false
  template_share_with = buttons
  nine_slice_slider_handle = false
  nine_slice_slider_handle_border_x = 5
  nine_slice_slider_handle_border_y = 5
  element_type = vanilla_button
{common(name, "vanilla", 0, 0, 0, 0)}  is_hidden = true
  automated_button_clicks = 0
}}
"""


def image(name, source, anchor, x, y, w, h):
    return f"""element {{
  source = {source}
  repeat_texture = false
  nine_slice_texture = false
  nine_slice_texture_border_x = 5
  nine_slice_texture_border_y = 5
  image_tint = #FFFFFF
  restart_animated_on_menu_load = false
  element_type = image
{common(ident(name), anchor, x, y, w, h)}}}
"""


def bar(name, anchor, x, y, w, h):
    return f"""element {{
  bar_color = {BAR}
  background_color = {BAR_BACK}
  direction = right
  progress_for_element_anchor = false
  progress_source = {{"placeholder":"game_loading_progress"}}
  value_mode = percentage
  smooth_filling_animation = true
  element_type = progress_bar
{common(ident(name), anchor, x, y, w, h)}}}
"""


def layout(identifier, background, elements):
    meta = ident(identifier + ":meta")
    return f"""type = fancymenu_layout

layout-meta {{
  identifier = {identifier}
  render_custom_elements_behind_vanilla = false
  last_edited_time = 1790000000000
  is_enabled = true
  randommode = false
  randomgroup = 1
  randomonlyfirsttime = false
  layout_index = 0
  [loading_requirement_container_meta:{meta}] = [groups:][instances:]
}}
{background}
layout_action_executable_blocks {{
}}

""" + "\n".join(elements)


def draw(config):
    fm = config / "fancymenu"
    (fm / "assets").mkdir(parents=True, exist_ok=True)
    (fm / "customization").mkdir(parents=True, exist_ok=True)
    im = logo()
    w, h = im.size
    im.resize((w * LOGO_SCALE, h * LOGO_SCALE), Image.NEAREST).save(fm / "assets" / "fallow_logo.png", optimize=True)
    source = "[source:local]/config/fancymenu/assets/fallow_logo.png"

    # title screen: the logo where Minecraft's stood, the splash text gone
    title = layout("title_screen", "", [
        image("fallow:title:logo", source, "top-centered", -(w // 2), 30 - (h - 44) // 2 - 4, w, h),
        hidden("minecraft_logo_widget"),
        hidden("minecraft_splash_widget"),
    ])
    (fm / "customization" / "fallow_title_screen.txt").write_text(title)

    # loading screen: moss ground, the logo, a bar in lantern light
    ground = f"""
menu_background {{
  instance_identifier = {ident("fallow:loading:background")}
  background_type = color_fancymenu
  show_background = true
  color = {GROUND}
}}
"""
    loading = layout("drippy_loading_overlay", ground, [
        image("fallow:loading:logo", source, "mid-centered", -(w // 2), -h // 2 - 12, w, h),
        bar("fallow:loading:bar", "mid-centered", -80, h // 2 + 6, 160, 4),
        hidden("mojang_logo"),
        hidden("progress_bar"),
    ])
    (fm / "customization" / "fallow_loading_screen.txt").write_text(loading)

    (fm / "customizablemenus.txt").write_text(
        "type = customizablemenus\n\n"
        "net.minecraft.client.gui.screens.TitleScreen {\n}\n\n"
        "de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen {\n}\n")
    (fm / "options.txt").write_text(
        "##[customization]\n\n"
        "B:modpack_mode = 'true';\n"
        "B:show_customization_overlay = 'false';\n\n\n"
        "##[tutorial]\n\n"
        "B:show_welcome_screen = 'false';\n")


if __name__ == "__main__":
    draw(Path(__file__).resolve().parents[2] / "pack" / "config")
