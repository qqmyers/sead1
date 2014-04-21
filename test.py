#!/usr/bin/python

from appscript import *
import argparse

def ChangeBackground():
  parser = argparse.ArgumentParser(description='Set background')
  parser.add_argument('file', type=file, help='File to use as background')
  args = parser.parse_args()
  f = args.file
  se = app('System Events')
  desktops = se.desktops.display_name.get()
  for d in desktops:
    desk = se.desktops[its.display_name == d]
    print desk
    print f.name
    desk.picture.set(mactypes.File(f.name))

ChangeBackground()
