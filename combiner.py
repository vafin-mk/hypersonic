import os

folderPath = 'src/hypersonic'
# mainFile is the file containing the main class.
mainFile = 'src/Player.java'
# exclude these files from the project.
excluded = ['State.java', 'Frame.java', 'Label.java']

def printSafe(s):
    s = str(s).encode('ascii', 'ignore')
    s = s.decode('ascii')
    print(s)

def getName(file):
    return file[file.rfind('/')+1:]

def isMain(file):
    global mainFile
    return getName(file) == mainFile

def notExcluded(file):
    global excluded
    return (file not in excluded)

def getFirstClassInterface(s):
    firstClass = s.find('class')
    firstInterface = s.find('interface')
    if firstClass == -1:
        index = firstInterface
    elif firstInterface == -1:
        index = firstClass
    else:
        index = min(firstClass, firstInterface)
    return index


def getImports(file):
    f = open(file)
    s = f.read()
    f.close()

    s = s[:getFirstClassInterface(s)]
    lines = s.split('\n')
    impts = []
    for line in lines:
        if line.find('import java') != -1:
            impts.append(line)
    return impts

def processFile(file, isPublic):
    f = open(file)
    s = f.read()
    f.close()

    startIndex = getFirstClassInterface(s)
    classCommentIndex = s.find('/*')
    commentEndIndex = s.find('*/', classCommentIndex)
    if classCommentIndex < startIndex < commentEndIndex:
        startIndex = commentEndIndex + getFirstClassInterface(s[commentEndIndex:])

    publicString = ''
    if isPublic:
        publicString = 'public '

    if classCommentIndex > startIndex or True:
        # print('Missing class comment for ' + file)
        return publicString + s[startIndex:]
    else:
        return s[classCommentIndex:commentEndIndex] + '*/\n' + publicString + s[startIndex:]


if __name__ == '__main__':
    # ds = os.listdir(folderPath)
    # allFiles = []
    # for d in ds:
    #     files = os.listdir(folderPath + d)
    #     files = filter(lambda s : s[-5:] == '.java', files)
    #     files = filter(notExcluded, files)
    #     files = map(lambda s : folderPath + d + '/' + s, files)
    #     allFiles += files
    allFiles = []
    files = os.listdir(folderPath)
    files = filter(lambda s : s[-5:] == '.java', files)
    files = filter(notExcluded, files)
    files = map(lambda s : folderPath + '/' + s, files)
    allFiles += files


    jImports = set()
    for file in allFiles:
        jImports = jImports.union(getImports(file))

    s = []
    for imp in jImports:
        s.append(imp + '\n')

    s.append('\n')
    # for file in allFiles:
    #     if isMain(file):
    #         s.append(processFile(file, True))
    s.append('class Player { public static void main(String[] args) { new Engine().start();}}')

    s.append('\n')
    for file in allFiles:
        if not isMain(file):
            pro = processFile(file, False)
            s.append(pro)
            print('- combine ' + getName(file))
            s.append('\n')

    s = ''.join(s)

    f = open(mainFile, 'w+')
    f.write(s)
    f.close()
    #printSafe(s)
    print ('Combination complete')