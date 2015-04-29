f = open("clean.csv")
max = 0
for line in f:
    first_ind = line.find(';')+1
    sec_ind = line.find(';', first_ind)
    num = int(line[first_ind:sec_ind])
    max = num if num > max else max

print(max)
f.close()