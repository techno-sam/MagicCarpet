#!/usr/bin/python3
import pygame
import os

wool_names = """black_wool.png
blue_wool.png
brown_wool.png
cyan_wool.png
gray_wool.png
green_wool.png
light_blue_wool.png
light_gray_wool.png
lime_wool.png
magenta_wool.png
orange_wool.png
pink_wool.png
purple_wool.png
red_wool.png
white_wool.png
yellow_wool.png""".split("\n")

wool_dir = "wool"
carpets_dir = "carpets"
patterns_dir = "../art"

def do_pattern(im_name, dir_name):
    for wool_name in wool_names:
        wool = pygame.image.load(os.path.join(wool_dir, wool_name))
        template = pygame.image.load(os.path.join(patterns_dir, im_name))
        out = pygame.Surface((32, 48), pygame.SRCALPHA)
        for x in range(out.get_width()):
            for y in range(out.get_height()):
                mask = template.get_at((x, y))
                if (mask[0] == 0):
                    out.set_at((x,y), (0, 0, 0, 0))
                else:
                    col = wool.get_at((x%16,y%16))
                    out.set_at((x, y), (col[0], col[1], col[2], 255))
        os.makedirs(os.path.join(carpets_dir, dir_name), exist_ok=True)
        pygame.image.save(out, os.path.join(carpets_dir,
                                            dir_name,
                                            wool_name.replace("_wool", "")))

do_pattern("border.png", "border")
do_pattern("center.png", "center")
do_pattern("decoration.png", "decoration")
