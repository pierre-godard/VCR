f = open("clean.csv")
outp = open("future.csv", "w+")

for line in f:
    first_ind = line.find(';')+1
    sec_ind = line.find(';', first_ind)
    num = int(line[first_ind:sec_ind])
    if num > 1430202436:
        outp.write(line)

f.close()
outp.close()
