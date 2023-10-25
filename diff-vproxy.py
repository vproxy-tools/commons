#!/usr/bin/env python3

import sys
import os
import subprocess

if len(sys.argv) < 2:
    print ('the script accept an argument: root directory of the vproxy project')
    sys.exit(1)

VPROXY_DIR = sys.argv[1]
if not VPROXY_DIR.endswith('/'):
    VPROXY_DIR = VPROXY_DIR + '/'

BASE_DIR = 'src/main/'

def diff(a, b, n):
    p = subprocess.run(['diff', a, b], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    stdout = p.stdout.strip()
    lines = p.stdout.strip().split('\n')
    if len(stdout) == 0 and p.returncode == 0:
        print ('##### files are the same: ' + n + ' #####')
    else:
        print ('!!!!! diff ' + n + ' !!!!!')
        for line in lines:
            print ('!!!!! ' + line)
        print ('!!!!! diff return code ' + str(p.returncode) + ' !!!!!')
        print ()

def diff_file(base, n):
    a = base + n
    b = VPROXY_DIR + 'base/' + BASE_DIR + n
    if not os.path.exists(b):
        print ('!!!!! vproxy missing: ' + n + ' !!!!!')
    else:
        diff(a, b, n)

def handle(base, name):
    ls = os.listdir(base + name)
    for n in ls:
        if n == 'generated':
            continue
        if os.path.isdir(base + name + '/' + n):
            handle(base, name + '/' + n)
        elif n.endswith('.java') or n.endswith('.kt'):
            diff_file(base, name + '/' + n)

handle('./' + BASE_DIR, '')

def handle_vproxy(base, name):
    ls = os.listdir(VPROXY_DIR + 'base/' + BASE_DIR + name)
    for n in ls:
        if n == 'generated':
            continue
        if os.path.isdir(VPROXY_DIR + 'base/' + BASE_DIR + name + '/' + n):
            handle_vproxy(base, name + '/' + n)
            continue
        if not n.endswith('.java') and not n.endswith('.kt'):
            continue
        nn = base + name + '/' + n
        if not os.path.exists(nn):
            print ('##### commons missing: ' + name + '/' + n + ' #####')

handle_vproxy('./' + BASE_DIR, '')
