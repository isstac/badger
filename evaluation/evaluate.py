"""
    Script to aggregate the results from an experiment.

    Input: source folder path, e.g.
    /vol/home-vol2/se/nollerya/fuzzing/experiments/01-insertionsort-kelinci-<id>

"""
import sys
import csv
import statistics
import math

# Adjust these parameters
NUMBER_OF_EXPERIMENTS = 10
EXPERIMENT_TIMEOUT=18000 #seconds = 5 hours
STEP_SIZE = 60 # seconds


# do not change this parameters
START_INDEX = 1
UNIX_TIME_COLUMN_ID = 0
HIGHSCORE_COLUMN_ID = 12

if __name__ == '__main__':

    if len(sys.argv) != 3:
        raise Exception("usage: source-folder fuzzer-out-dir-in-src-folder")

    srcDir = sys.argv[1]
    fuzzerOutDir = sys.argv[2]

    times_greater_0 = []
    times_greater_64 = []
    global_max = 0

    # Read data
    collected_data = []
    for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
        data = {}
        statFilePath = srcDir + str(i) + fuzzerOutDir + "/afl/fuzzer_stats"
        with open(statFilePath, 'r') as statFile:
            firstLine = statFile.readline()
            firstLine = firstLine.split(":")
            startTime = int(firstLine[1].strip())

        dataFile = srcDir + str(i) + fuzzerOutDir + "/afl/plot_data"
        with open(dataFile,'r') as csvfile:
            csvreader = csv.reader(csvfile)
            timeBucket = STEP_SIZE
            next(csvreader) # skip first row
            previousValue = 0
            for row in csvreader:
                currentTime = int(row[UNIX_TIME_COLUMN_ID]) - startTime
                currentValue = int(row[HIGHSCORE_COLUMN_ID])

                if (previousValue <= 0 and currentValue > 0):
                    times_greater_0.append(currentTime)
                if (previousValue <= 64 and currentValue > 64):
                    times_greater_64.append(currentTime)
                if (currentValue > global_max):
                    global_max = currentValue

                while (currentTime > timeBucket):
                    data[timeBucket] = previousValue
                    timeBucket += STEP_SIZE

                previousValue = currentValue

                if timeBucket > EXPERIMENT_TIMEOUT:
                    break

            # fill data with last known value if not enough information
            while timeBucket <= EXPERIMENT_TIMEOUT:
                data[timeBucket] = previousValue
                timeBucket += STEP_SIZE

        collected_data.append(data)

    # Write collected data
    headers = ['minutes']
    #headers = ['seconds']
    for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
        headers.append('highscore#' + str(i))
    with open("./collected-data.csv", "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'minutes' : int(timeBucket/60)}
            #values = {'seconds' : timeBucket}
            for i in range(START_INDEX, NUMBER_OF_EXPERIMENTS+1):
                values['highscore#' + str(i)] = collected_data[i-START_INDEX][timeBucket]
            writer.writerow(values)

print("Done.")
