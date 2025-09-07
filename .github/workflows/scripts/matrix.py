"""
A script to scan through the versions directory and collect all folder names as the subproject list,
then output a json as the github action include matrix
"""
__author__ = 'Fallen_Breath'
# edited by Skidam, https://github.com/Skidamek
# updated for civiliansmod from suerion

import json
import os
import sys

def read_minecraft_version(file_path='gradle.properties'):
    if not os.path.exists(file_path):
        print(f"gradle.properties not found at {file_path}", file=sys.stderr)
           sys.exit(1)

    with open(file_path, 'r') as f:
        for line in f:
            line = line.strip()
            if line.startswith('minecraft_version='):
                return line.split('=', 1)[1].strip()
    print("minecraft_version not found in gradle.properties", file=sys.stderr)
    sys.exit(1)

def main():
    version = read_minecraft_version()
    print(f"Detected minecraft_version: {version}")

    matrix_entries = [{
        'subproject': version,  # minecraft version
        'mod_brand': 'fabric'   # is allways fabric, if changed, need to change
    }]

    matrix = {'include': matrix_entries}

    with open(os.environ['GITHUB_OUTPUT'], 'w') as f:
        f.write('matrix={}\n'.format(json.dumps(matrix)))

    print('matrix:')
    print(json.dumps(matrix, indent=2))

if __name__ == '__main__':
    main()