### Define here local variables #####
#####################################
# chmod +x run_experiments.sh
# ./run_experiments.sh
#
# Besure to already build the source files in the directories:
# /kelinci_analysis/bin, /kelinci_analysis/bin-instr, /spf_analysis/bin
#
# and create the folder:
# /kelinci_analysis/fuzzer-out/spf/queue
#

# time bound for each run (in hours):
time_bound=100 #3600 #3600 = 1 hour

# number of iterations
number_of_runs=5 #5

# folder with sources
src=/vol/home-vol2/se/nollerya/fuzzing/07-GovernMental

# folder for runtime
target=/vol/home-vol2/se/nollerya/fuzzing/experiments/07-governmental-spf

# kelinci=1, kelinci-spf=2
execution_mode=3

# server parameter
server_param="GovernMental @@"

#####################################

echo "Running experiments for:"
echo "execution_mode=$execution_mode"
echo "time_bound=$time_bound"
echo "number_of_runs=$number_of_runs"
echo "src=$src"
echo "target=$target"
echo "server_param=$server_param"
echo ""

for i in `seq 1 $number_of_runs`
do
  current_target="$target-$i"
  echo "Initializing experiment environment for run=$i"

  mkdir "$current_target"
  cp -r "$src/kelinci_analysis" "$current_target/"

  cp -r "$src/spf_analysis" "$current_target/"
  mkdir "$current_target/tmp"
  cp "/vol/home-vol2/se/nollerya/fuzzing/badger-source/badger/build/BadgerRunnerJar.jar" "$current_target"
  cp "$src/config-server-berlin-spf" "$current_target"

  cd "$current_target"
  echo "Starting SPF.."
  export PATH=$PATH:/vol/home-vol2/se/nollerya/fuzzing/z3/build
  export LD_LIBRARY_PATH=/vol/home-vol2/se/nollerya/fuzzing/z3/build
  screen -S spf -d -m java -Xmx10240m -jar BadgerRunnerJar.jar config-server-berlin-spf

  echo "Waiting for $time_bound seconds.."
  sleep $time_bound

  echo "Stopping SPF.."
  screen -r spf -X kill

  echo "Finished run=$i"
  echo ""

done

echo "Done."
