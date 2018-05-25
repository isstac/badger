"""
    Script to aggregate the results from spf experiments

    Input: source folder path, e.g.
    /vol/home-vol2/se/nollerya/fuzzing/experiments/01-insertionsort-kelinci-<id>

    !!!! needs server running in background

"""
import sys
import csv
import os
from subprocess import call

NUMBER_OF_EXPERIMENTS = 1 #5
EXPERIMENT_TIMEOUT=18000 #seconds = 5 hours
STEP_SIZE = 60 # seconds

UNIX_TIME_COLUMN_ID = 0
FILE_COLUMN_ID = 2
HIGHSCORE_COLUMN_ID = 4 # or 5 depends

JUMPS_COLUMN_ID=3
USERDEFINED_COLUMN_ID=4

#interfaceCmd="/Users/yannic/Downloads/interface_cost_log"
#interfaceCmd="/Users/yannic/Downloads/kelinciwca/fuzzerside/interface_cost_log"
interfaceCmd="/Users/yannic/Downloads/kelinciwca/fuzzerside/interface_cost_log_latest"
#interfaceCmd="/vol/home-vol2/se/nollerya/fuzzing/kelinciwca/fuzzerside/interface_cost_log"

if __name__ == '__main__':

    if len(sys.argv) != 2:
        raise Exception("usage: source-folder")

    srcDir = sys.argv[1]

    collected_data = []
    for i in range(1, NUMBER_OF_EXPERIMENTS+1):

        # Get start time.
        statFilePath = srcDir + str(i) + "/import-statistics.txt"
        with open(statFilePath, 'r') as statFile:
            statFile.readline()
            secondLine = statFile.readline()
            secondLine = secondLine.split(",")
            startTime = int(secondLine[0].strip()) # startTime

        # Generate Cost File.
        costFilePath = os.getcwd() + "/cost-" + str(i) + ".csv"
        print(costFilePath)
        try:
            os.remove(costFilePath)
        except OSError:
            pass
        with open(costFilePath, 'a') as results_file:
            results_file.write('')

        # Generate Cost Info
        collected_times = []
        exportFile = srcDir + str(i) + "/export-statistics.txt"
        with open(exportFile,'r') as csvfile:
            csvreader = csv.reader(csvfile)
            timeBucket = STEP_SIZE
            next(csvreader) # skip first row
            previousValue = 0
            j = 0
            for row in csvreader:
                currentTime = int(row[UNIX_TIME_COLUMN_ID]) - startTime
                currentFile = row[FILE_COLUMN_ID]
                currentFilePath = srcDir + str(i) + currentFile[1:]
                call([interfaceCmd, currentFilePath, costFilePath])
                collected_times.append(currentTime)

        data = {}
        with open(costFilePath,'r') as csvfile:
            csvreader = csv.reader(csvfile, delimiter=";")
            timeBucket = STEP_SIZE
            previousValue = 0
            counter = 0
            for row in csvreader:
                currentTime = collected_times[counter]
                counter += 1
                #currentValue = int(row[JUMPS_COLUMN_ID].strip())
                currentValue = int(row[USERDEFINED_COLUMN_ID].strip())

                while (currentTime > timeBucket):
                    data[timeBucket] = previousValue
                    timeBucket += STEP_SIZE

                if currentValue > previousValue:
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
    for i in range(1, NUMBER_OF_EXPERIMENTS+1):
        headers.append('highscore#' + str(i))
    with open("./collected-data.csv", "w") as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=headers)
        writer.writeheader()
        for timeBucket in range(STEP_SIZE, EXPERIMENT_TIMEOUT+1, STEP_SIZE):
            values = {'minutes' : int(timeBucket/60)}
            for i in range(1, NUMBER_OF_EXPERIMENTS+1):
                values['highscore#' + str(i)] = collected_data[i-1][timeBucket]
            writer.writerow(values)

print("Done.")
