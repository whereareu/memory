#!/usr/bin/env python3
from xml.etree import ElementTree
import subprocess
import os

def create_svg_from_vector(xml_file, svg_file):
    ElementTree.register_namespace('android', 'http://schemas.android.com/apk/res/android')
    
    tree = ElementTree.parse(xml_file)
    root = tree.getroot()

    viewport_width = root.get('{http://schemas.android.com/apk/res/android}viewportWidth', '108')
    viewport_height = root.get('{http://schemas.android.com/apk/res/android}viewportHeight', '108')

    svg_lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{viewport_width}" height="{viewport_height}" viewBox="0 0 {viewport_width} {viewport_height}">'
    ]

    for path in root.iter():
        if path.tag.endswith('path'):
            path_data = path.get('{http://schemas.android.com/apk/res/android}pathData')
            fill = path.get('{http://schemas.android.com/apk/res/android}fillColor')
            stroke = path.get('{http://schemas.android.com/apk/res/android}strokeColor')
            stroke_width = path.get('{http://schemas.android.com/apk/res/android}strokeWidth', '1')
            fill_alpha = path.get('{http://schemas.android.com/apk/res/android}fillAlpha', '1')
            stroke_alpha = path.get('{http://schemas.android.com/apk/res/android}strokeAlpha', '1')

            if not path_data:
                continue

            attrs = [f'd="{path_data}"']

            if fill and fill != '#00000000':
                attrs.append(f'fill="{fill}"')
                if fill_alpha != '1':
                    try:
                        alpha = float(fill_alpha)
                        attrs.append(f'fill-opacity="{alpha}"')
                    except: pass
            else:
                attrs.append('fill="none"')

            if stroke and stroke != '#00000000':
                attrs.append(f'stroke="{stroke}"')
                attrs.append(f'stroke-width="{stroke_width}"')
                if stroke_alpha != '1':
                    try:
                        alpha = float(stroke_alpha)
                        attrs.append(f'stroke-opacity="{alpha}"')
                    except: pass

            svg_lines.append('  <path ' + ' '.join(attrs) + ' />')

    svg_lines.append('</svg>')

    with open(svg_file, 'w') as f:
        f.write('\n'.join(svg_lines))

def main():
    xml_file = 'app/src/main/res/drawable/ic_launcher_foreground.xml'
    svg_file = 'logo_export/icon.svg'
    os.makedirs('logo_export', exist_ok=True)

    print("Converting vector drawable to SVG...")
    create_svg_from_vector(xml_file, svg_file)

    # 验证viewport
    tree = ElementTree.parse(xml_file)
    root = tree.getroot()
    viewport_width = root.get('{http://schemas.android.com/apk/res/android}viewportWidth')
    viewport_height = root.get('{http://schemas.android.com/apk/res/android}viewportHeight')
    print(f"✓ Vector viewport: {viewport_width}×{viewport_height}")

    densities = [('mdpi.png', 48), ('hdpi.png', 72), ('xhdpi.png', 96), ('xxhdpi.png', 144), ('xxxhdpi.png', 192)]

    for name, size in densities:
        output = f'logo_export/{name}'
        print(f"Generating {name} ({size}x{size})...")

        # 使用-density确保SVG正确渲染，然后resize到目标尺寸
        cmd = ['/opt/homebrew/bin/magick', '-density', '300', svg_file,
               '-background', 'none', '-resize', f'{size}x{size}!', output]
        result = subprocess.run(cmd, capture_output=True, text=True)

        if result.returncode != 0:
            print(f"  Error: {result.stderr}")

    print("\n✓ Done! Files in logo_export/")
    print(f"  Original vector size: 70x70 viewport")

if __name__ == '__main__':
    main()
