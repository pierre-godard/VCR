import sys

# 2 args :
# 1. Path to file
# 2. Max number of results (si 0 => tous)
# 3. Id of the station

fpath = sys.argv[1]
maxreq = int(sys.argv[2])
station = int(sys.argv[3])

f = open(fpath)
requested = open("requested.csv", "w+")

req = 0

for line in f:
	if maxreq != 0 and req >= maxreq:
		break
	if int(line.split(';')[0]) == station:
		req += 1
		requested.write(line)

f.close()
requested.close()
