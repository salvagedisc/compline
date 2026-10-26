"""Fallow's title screen and loading screen, for FancyMenu and Drippy Loading
Screen: the Fallow logo (drawn by logo.py) in place of Minecraft's over a moss
veil that darkens the panorama behind the buttons, and a moss loading screen
with the logo and a bar in lantern light in place of Mojang's red one.

Writes into pack/config/fancymenu the logo, the two layouts, the list of
customised screens and FancyMenu's options (modpack mode: players never see
the editor). The title screen keeps the vanilla buttons, the panorama drawn
by build.py and the texts the game requires.

Called by build.py; can run alone: python3 scripts/interface/menus.py
"""
import hashlib
from pathlib import Path

from PIL import Image

import logo

# the title screen: a moss veil darkening the panorama towards the bottom and
# the sides, so that the buttons read over the forest
VEIL = (20, 26, 22)
# the loading screen
GROUND = "#1F2521"
BAR = "#E2A64EFF"
BAR_BACK = "#34403AFF"


def ident(name):
    """A stable id in FancyMenu's form, so that rebuilding changes nothing."""
    h = hashlib.sha1(name.encode()).hexdigest()
    return f"{h[:8]}-{h[8:12]}-{h[12:16]}-{h[16:20]}-{h[20:32]}-1790000000000"


def common(name, anchor, x, y, w, h, stretch=False):
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
  stretch_x = {str(stretch).lower()}
  stretch_y = {str(stretch).lower()}
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


def image(name, source, anchor, x, y, w, h, stretch=False):
    return f"""element {{
  source = {source}
  repeat_texture = false
  nine_slice_texture = false
  nine_slice_texture_border_x = 5
  nine_slice_texture_border_y = 5
  image_tint = #FFFFFF
  restart_animated_on_menu_load = false
  element_type = image
{common(ident(name), anchor, x, y, w, h, stretch)}}}
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


def layout(identifier, background, elements, behind=False):
    meta = ident(identifier + ":meta")
    return f"""type = fancymenu_layout

layout-meta {{
  identifier = {identifier}
  render_custom_elements_behind_vanilla = {str(behind).lower()}
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


def veil(w=320, h=180):
    """The title screen's veil: clear around the logo, a moss shade rising from the
    bottom and creeping in from the sides."""
    im = Image.new("RGBA", (w, h))
    px = im.load()
    for y in range(h):
        for x in range(w):
            u, v = (x + 0.5) / w * 2 - 1, (y + 0.5) / h
            shade = 0.55 * max(0.0, v - 0.3) / 0.7 + 0.35 * abs(u) ** 3
            px[x, y] = VEIL + (round(255 * min(0.7, shade)),)
    return im


def draw(config):
    fm = config / "fancymenu"
    (fm / "assets").mkdir(parents=True, exist_ok=True)
    (fm / "customization").mkdir(parents=True, exist_ok=True)
    im = logo.draw()
    im.save(fm / "assets" / "fallow_logo.png", optimize=True)
    w, h = im.width // logo.SCALE, im.height // logo.SCALE
    source = "[source:local]/config/fancymenu/assets/fallow_logo.png"

    # title screen: the logo where Minecraft's stood, the splash text gone, a
    # veil over the panorama behind the buttons
    veil().save(fm / "assets" / "fallow_veil.png", optimize=True)
    title = layout("title_screen", "", [
        image("fallow:title:veil", "[source:local]/config/fancymenu/assets/fallow_veil.png", "top-left",
              0, 0, 320, 180, stretch=True),
        image("fallow:title:logo", source, "top-centered", -(w // 2), 12, w, h),
        hidden("minecraft_logo_widget"),
        hidden("minecraft_splash_widget"),
    ], behind=True)
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
