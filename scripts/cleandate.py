import time
import datetime

def toTimestamp(dt):
    dt = dt / 10000000
    t = dt - 62135596800
    t += 2 * time.timezone
    return t

def printTimestamp(timestamp):
    return datetime.datetime.fromtimestamp(timestamp).strftime("%A %H:%M:%S %d-%m-%Y")

def cleanFile(inp,outp):
    f = open(inp)
    clean = open(outp, "w+")
    for line in f:
        first_ind = line.find(';')+1
        sec_ind = line.find(';', first_ind)
        cleanline_begin = line[0:first_ind]
        cleanline_end = line[sec_ind:]
        stamp = line[first_ind:sec_ind]
        cleanstamp = str(int(toTimestamp(int(stamp))))
        clean.write(cleanline_begin + cleanstamp + cleanline_end)
    f.close()
    clean.close()

cleanFile("./VeloV/VeloV.csv", "clean.csv")

