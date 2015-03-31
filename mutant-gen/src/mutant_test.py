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

def env_setup(mutdirs, template, target):
    basedir = 'testenvs'
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

    os.chdir(cwd) # unnecessary?
    resfile = '{0}_'.format(path.split()[1])
    if ret == 0:
        resfile += 'ALIVE'
    else:
        resfile += 'KILLED'
    print resfile
    with open(resfile, 'a'):
        os.utime(path, None)
    return ret

def test_mutants(muts):
    cwd = os.getcwd()
    pool = Pool(4)
    results = pool.map(test_single_mutant, muts)
    pool.close()
    pool.join()
        
#    for path in muts:
#        os.chdir(path)
#        try:
#            cmd = ['sbt', 'test']
##            output = subprocess.check_output(cmd)
#            with open(os.devnull, 'w') as null:
#                output = subprocess.check_call(cmd, stdout=os.devnull, stderr=os.devnull)
#        except subprocess.CalledProcessError, e:
##            output = e.output
##            print e.output
##            print 'NOTE: {1} Returned {0}'.format(e.returncode, e.cmd)
#            output = e.returncode
#        print '{0} returned {1}'.format(path, output)
#        os.chdir(cwd)

def main(args):
    mutdirs = enumerate_mutants('mutants', args.target)
    muts = env_setup(mutdirs, args.template, args.target)
    test_mutants(muts)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Evaluate mutants.')
    parser.add_argument('target')
    parser.add_argument('template')
    args = parser.parse_args()
    try:
        main(args)
    except KeyboardInterrupt, e:
        exit()
    except:
        raise
