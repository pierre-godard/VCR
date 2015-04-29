#!/usr/bin/env python

import time
import datetime

# Number of seconds (according to Microsoft DateTime)
# From 01/01/0001 00:00:00 UTC to 01/01/1970 00:00:00 UTC
EPOCH_ORIGIN_IN_DATETIME_SECONDS = 62135596800

# At the time of the script generation, data was encoded
# With UTC+2 (Daylight Saving Time of France)
UTC_PLUS_WHAT = 2

# Number of ticks in a second (a tick is a 100 nano-second long)
NB_TICKS_IN_SECOND = 10000000

NB_SEC_IN_HOUR = 3600

def dateTimeToTimestamp(dateTime, utcPlusWhat):
    # Converting ticks in seconds
    dateTimeSec = dateTime / NB_TICKS_IN_SECOND

    # Changing DateTime origin to Unix Epoch origin
    timestamp = dateTimeSec - EPOCH_ORIGIN_IN_DATETIME_SECONDS

    timestamp -= utcPlusWhat * NB_SEC_IN_HOUR
    return timestamp

def printTimestamp(timestamp):
    return datetime.datetime.fromtimestamp(timestamp).strftime("%A %H:%M:%S %d-%m-%Y")

def cleanFile(inputFile, outputFile):
    f = open(inputFile)
    clean = open(outputFile, "w+")
    for line in f:
        first_ind = line.find(';')+1
        sec_ind = line.find(';', first_ind)
        cleanline_begin = line[0:first_ind]
        cleanline_end = line[sec_ind:]
        stamp = line[first_ind:sec_ind]
        cleanstamp = str(dateTimeToTimestamp(int(stamp), UTC_PLUS_WHAT))
        clean.write(cleanline_begin + cleanstamp + cleanline_end)
    f.close()
    clean.close()

cleanFile("./VeloV.csv", "clean.csv")

