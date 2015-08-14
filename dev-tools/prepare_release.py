# Licensed to Elasticsearch under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance  with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on
# an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied. See the License for the specific
# language governing permissions and limitations under the License.

# Prepare a release
#
# This script prepares a new release by creating two commits
#
# First commit: Update the Version.java to remove the snapshot bit
# First commit: Remove the -SNAPSHOT suffix in all pom.xml files
# Second commit: Update Documentation flags
#
# USAGE:
#
# python3 ./dev-tools/prepare-release.py
#
# Note: Ensure the script is run from the root directory
#

import fnmatch
import subprocess
import tempfile
import re
import os
import shutil

VERSION_FILE = 'core/src/main/java/org/elasticsearch/Version.java'
POM_FILE = 'pom.xml'

def run(command):
  if os.system('%s' % (command)):
    raise RuntimeError('    FAILED: %s' % (command))

def ensure_checkout_is_clean():
  # Make sure no local mods:
  s = subprocess.check_output('git diff --shortstat', shell=True)
  if len(s) > 0:
    raise RuntimeError('git diff --shortstat is non-empty: got:\n%s' % s)

  # Make sure no untracked files:
  s = subprocess.check_output('git status', shell=True).decode('utf-8', errors='replace')
  if 'Untracked files:' in s:
    raise RuntimeError('git status shows untracked files: got:\n%s' % s)

  # Make sure we have all changes from origin:
  if 'is behind' in s:
    raise RuntimeError('git status shows not all changes pulled from origin; try running "git pull origin" in this branch: got:\n%s' % (s))

  # Make sure we no local unpushed changes (this is supposed to be a clean area):
  if 'is ahead' in s:
    raise RuntimeError('git status shows local commits; try running "git fetch origin", "git checkout ", "git reset --hard origin/" in this branch: got:\n%s' % (s))

# Reads the given file and applies the
# callback to it. If the callback changed
# a line the given file is replaced with
# the modified input.
def process_file(file_path, line_callback):
  fh, abs_path = tempfile.mkstemp()
  modified = False
  with open(abs_path,'w', encoding='utf-8') as new_file:
    with open(file_path, encoding='utf-8') as old_file:
      for line in old_file:
        new_line = line_callback(line)
        modified = modified or (new_line != line)
        new_file.write(new_line)
  os.close(fh)
  if modified:
    #Remove original file
    os.remove(file_path)
    #Move new file
    shutil.move(abs_path, file_path)
    return True
  else:
    # nothing to do - just remove the tmp file
    os.remove(abs_path)
    return False

# Moves the pom.xml file from a snapshot to a release
def remove_maven_snapshot(poms, release):
  for pom in poms:
    if pom:
      #print('Replacing SNAPSHOT version in file %s' % (pom))
      pattern = '<version>%s-SNAPSHOT</version>' % (release)
      replacement = '<version>%s</version>' % (release)
      def callback(line):
        return line.replace(pattern, replacement)
      process_file(pom, callback)

# Moves the Version.java file from a snapshot to a release
def remove_version_snapshot(version_file, release):
  # 1.0.0.Beta1 -> 1_0_0_Beta1
  release = release.replace('.', '_')
  release = release.replace('-', '_')
  pattern = 'new Version(V_%s_ID, true' % (release)
  replacement = 'new Version(V_%s_ID, false' % (release)
  def callback(line):
    return line.replace(pattern, replacement)
  processed = process_file(version_file, callback)
  if not processed:
    raise RuntimeError('failed to remove snapshot version for %s' % (release))

# finds all the pom files that do have a -SNAPSHOT version
def find_pom_files_with_snapshots():
  files = subprocess.check_output('find . -name pom.xml -exec grep -l "<version>.*-SNAPSHOT</version>" {} ";"', shell=True)
  return files.decode('utf-8').split('\n')

# Checks the pom.xml for the release version.
# This method fails if the pom file has no SNAPSHOT version set ie.
# if the version is already on a release version we fail.
# Returns the next version string ie. 0.90.7
def find_release_version():
  with open('pom.xml', encoding='utf-8') as file:
    for line in file:
      match = re.search(r'<version>(.+)-SNAPSHOT</version>', line)
      if match:
        return match.group(1)
    raise RuntimeError('Could not find release version in branch')

# Stages the given files for the next git commit
def add_pending_files(*files):
  for file in files:
    if file:
      # print("Adding file: %s" % (file))
      run('git add %s' % (file))

# Executes a git commit with 'release [version]' as the commit message
def commit_release(release):
  run('git commit -m "Release: Change version from %s-SNAPSHOT to %s"' % (release, release))

def commit_feature_flags(release):
    run('git commit -m "Update Documentation Feature Flags [%s]"' % release)

# Walks the given directory path (defaults to 'docs')
# and replaces all 'coming[$version]' tags with
# 'added[$version]'. This method only accesses asciidoc files.
def update_reference_docs(release_version, path='docs'):
  pattern = 'coming[%s' % (release_version)
  replacement = 'added[%s' % (release_version)
  pending_files = []
  def callback(line):
    return line.replace(pattern, replacement)
  for root, _, file_names in os.walk(path):
    for file_name in fnmatch.filter(file_names, '*.asciidoc'):
      full_path = os.path.join(root, file_name)
      if process_file(full_path, callback):
        pending_files.append(os.path.join(root, file_name))
  return pending_files

if __name__ == "__main__":
  release_version = find_release_version()

  print('*** Preparing release version: [%s]' % release_version)

  ensure_checkout_is_clean()
  pom_files = find_pom_files_with_snapshots()

  remove_maven_snapshot(pom_files, release_version)
  remove_version_snapshot(VERSION_FILE, release_version)

  pending_files = pom_files
  pending_files.append(VERSION_FILE)
  add_pending_files(*pending_files) # expects var args use * to expand
  commit_release(release_version)

  pending_files = update_reference_docs(release_version)
  # split commits for docs and version to enable easy cherry-picking
  if pending_files:
    add_pending_files(*pending_files) # expects var args use * to expand
    commit_feature_flags(release_version)
  else:
    print('WARNING: no documentation references updates for release %s' % (release_version))

  print('*** Done removing snapshot version. Run git push manually.')

