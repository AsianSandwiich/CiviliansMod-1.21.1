"""
A script to scan through all valid mod jars in build-artifacts.zip/$version/build/libs,
and generate an artifact summary table for that to GitHub action step summary
"""
__author__ = 'Fallen_Breath'
#updated from suerion for civilians mod

import functools
import glob
import hashlib
import os


def read_prop(file_name: str, key: str) -> str:
	with open(file_name) as prop:
		return next(filter(
			lambda l: l.split('=', 1)[0].strip() == key,
			prop.readlines()
		)).split('=', 1)[1].lstrip()


def get_sha256_hash(file_path: str) -> str:
	sha256_hash = hashlib.sha256()

	with open(file_path, 'rb') as f:
		for buf in iter(functools.partial(f.read, 4096), b''):
			sha256_hash.update(buf)

	return sha256_hash.hexdigest()


def main():
	target_subproject_env = os.environ.get('TARGET_SUBPROJECT', '')
    target_subprojects = list(filter(None, target_subproject_env.split(',')))
    print('target_subprojects: {}'.format(target_subprojects))

    if not target_subprojects:
        current_branch = os.environ.get('GITHUB_REF_NAME', 'main')
        subprojects = [current_branch]
    else:
        subprojects = target_subprojects

    # GitHub Step Summary schreiben
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'w') as f:
        f.write('## Build Artifacts Summary\n\n')
        f.write('| Subproject | for Minecraft | Files | SHA-256 |\n')
        f.write('| --- | --- | --- | --- |\n')

        warnings = []
        for subproject in subprojects:
            gradle_file = f'gradle.properties'
            if not os.path.exists(gradle_file):
                game_version = '*N/A*'
            else:
                game_version = read_prop(gradle_file, 'minecraft_version')

            file_paths = glob.glob(f'build-artifacts/{subproject}/build/libs/*.jar')
            # Filter sources/dev jars
            file_paths = [fp for fp in file_paths if not fp.endswith('-sources.jar') and not fp.endswith('-dev.jar')]

            if len(file_paths) == 0:
                file_name = '*not found*'
                sha256 = '*N/A*'
            else:
                file_name = '`{}`'.format(os.path.basename(file_paths[0]))
                sha256 = '`{}`'.format(get_sha256_hash(file_paths[0]))
                if len(file_paths) > 1:
                    warnings.append(f'Found too many build files in subproject {subproject}: {", ".join(file_paths)}')

            f.write(f'| {subproject} | {game_version} | {file_name} | {sha256} |\n')

        if warnings:
            f.write('\n### Warnings\n\n')
            for warning in warnings:
                f.write(f'- {warning}\n')


if __name__ == '__main__':
    main()