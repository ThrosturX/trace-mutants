import argparse
import os
import shutil
import subprocess
import sys
from multiprocessing.dummy import Pool

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
#    os.chdir(path)

    logfile = os.path.join(path, 'log.txt')
    print path

    cmd = ['test_mutant.sh', path]
    try:
        with open(logfile, 'w') as log:
            ret = subprocess.check_call(cmd, stdout=log, stderr=log)
    except subprocess.CalledProcessError, e:
        ret = e.returncode

#    os.chdir(cwd)
    resfile = os.path.join('testenvs', 'results', '{0}_'.format(os.path.split(path)[1]))
    if 'original' in resfile:
        resfile = resfile.replace('/results','')
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
    pool = Pool()
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

def test_original(template, target):
    odir = os.path.join('testenvs', 'original')
    try:
        if os.path.exists(odir):
            raw_input('remove {0}?'.format(odir))
            try:
                if os.path.isfile(odir):
                    os.unlink(odir)
                elif os.path.isdir(odir):
                    shutil.rmtree(odir)
            except Exception, e:
                print e
        shutil.copytree(template, odir)
        shutil.copyfile(target, os.path.join(odir, target))
    except:
        print 'Unexpected error: ', sys.exc_info()[0]
        raise
    return test_single_mutant(odir)

def main(args):
    basedir = 'testenvs'
    mutdirs = enumerate_mutants('mutants', args.target)
    muts = env_setup(mutdirs, args.template, args.target, basedir)
    test_mutants(muts, basedir)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Evaluate mutants with sbt and the file system.')
    parser.add_argument('target', nargs='?', default='JBus.java')
    parser.add_argument('template', nargs='?', default='sbt')
    parser.add_argument('--count_only', action='store_true', help='only count result (don\'t do testing)')
    parser.add_argument('--original', action='store_true', help='only test the original (not the mutants)')
    args = parser.parse_args()
    try:
        if args.count_only:
            count_results(os.path.join('testenvs', 'results'))
            exit(0)
        if args.original:
            res = test_original(args.template, args.target)
            exit(res)
        main(args)

    except KeyboardInterrupt, e:
        exit()
    except:
        raise
