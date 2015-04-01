import argparse
import os
import shutil
import subprocess
from multiprocessing import Pool

def find_file(name, path):
    for root, dirs, files in os.walk(path):
        if name in files:
            return os.path.join(root, name)

def enumerate_mutants(path, target):
    founds = []
    for (root, dirs, files) in os.walk(path):
        if target in files:
            founds.append(root)
    return founds

def env_setup(mutdirs, template, target, basedir):
    if os.path.exists(basedir):
        for thing in os.listdir(basedir):
            actual = os.path.join(basedir, thing)
            raw_input('remove {0}?'.format(actual))
            try:
                if os.path.isfile(actual):
                    os.unlink(actual)
                elif os.path.isdir(actual):
                    shutil.rmtree(actual)
            except Exception, e:
                print e
    else:
        os.makedirs(basedir)

    ret = []

    for mdir in mutdirs:
        edir = os.path.join(basedir, mdir)
        print 'setting up {0}' .format(edir)
        shutil.copytree(template, edir)
        tloc = find_file(target, edir)
        shutil.copyfile(os.path.join(mdir, target), tloc)
        if find_file('build.sbt', edir):
            ret.append(edir)

    return ret

def test_single_mutant(path):
    cwd = os.getcwd()
    os.chdir(path)

    logfile = 'log.txt'

    cmd = ['sbt', 'test']
    try:
        with open(logfile, 'w') as log:
            ret = subprocess.check_call(cmd, stdout=log, stderr=log)
    except subprocess.CalledProcessError, e:
        ret = e.returncode

    os.chdir(cwd)
    resfile = os.path.join('testenvs', 'results', '{0}_'.format(os.path.split(path)[1]))
    if ret == 0:
        resfile += 'ALIVE'
    else:
        resfile += 'KILLED'
    print resfile
    open(resfile, 'a').close()
    os.utime(resfile, None)
    return ret

def test_mutants(muts, basedir):
    cwd = os.getcwd()
    if not os.path.exists(os.path.join(basedir, 'results')):
        os.makedirs(os.path.join(basedir, 'results')) # todo fix
    pool = Pool(8)
    results = pool.map(test_single_mutant, muts)
    pool.close()
    pool.join()
    result = count_results(os.path.join(basedir, 'results'))
    return result;

def count_results(path):
    files = os.listdir(path)
    killed = 0
    alive = 0
    for name in files:
        if 'KILLED' in name:
            killed += 1
        else:
            alive += 1

    print 'KILLED {0}/{1} ({2} still alive)'.format(killed, killed + alive, alive)
    return (killed, alive)

def main(args):
    basedir = 'testenvs'
    mutdirs = enumerate_mutants('mutants', args.target)
    muts = env_setup(mutdirs, args.template, args.target, basedir)
    test_mutants(muts, basedir)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Evaluate mutants.')
    parser.add_argument('target')
    parser.add_argument('template')
    parser.add_argument('--count_only', action='store_true', help='only count result (don\'t do testing)')
    args = parser.parse_args()
    try:
        if args.count_only:
            count_results(os.path.join('testenvs', 'results'))
            exit(0)
        main(args)

    except KeyboardInterrupt, e:
        exit()
    except:
        raise
