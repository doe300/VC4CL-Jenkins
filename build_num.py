#!/usr/bin/env python
# -*- coding: utf-8 -*-

import json
import sys

# Usage: <script> <JSON file> [<params>]

def main():
  assert (len (sys.argv) >= 2)
  job = 'cross'
  branch = 'master'
  i = 2
  while i < len(sys.argv):
    if sys.argv[i] == '--job':
      job = sys.argv[i+1]
    if sys.argv[i] == '--branch':
      branch = sys.argv[i+1]
    i = i + 2

  items = json.load (open (sys.argv[1], 'r'))
  for item in items:
    job_name = item['workflows']['job_name']
    build_status = item['status']
    build_branch = item['branch']
    if job_name == job and build_status == 'success' and build_branch == branch:
      print (item['build_num'])
      exit(0)
  exit (1)

main()
