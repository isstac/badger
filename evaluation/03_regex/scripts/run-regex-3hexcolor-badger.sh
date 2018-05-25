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
time_bound=18000 #18000 = 5 hours

# number of iterations
number_of_runs=5 #5

# folder with sources
src=/vol/home-vol2/se/nollerya/fuzzing/memoise/issta-experiments/03-regex

# folder for runtime
target=/vol/home-vol2/se/nollerya/fuzzing/experiments/03-regex-3hexcolor-badger

# kelinci=1, kelinci-spf=2
execution_mode=2

# server parameter
server_param="Regex resources-byte/popular-regexes/3hexcolor-byte @@"

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

  if [ $execution_mode -eq 2 ]
  then
    cp -r "$src/spf_analysis" "$current_target/"
    mkdir "$current_target/tmp"
    cp "/vol/home-vol2/se/nollerya/fuzzing/memoise/KelinciSPFRunner.jar" "$current_target"
    cp "$src/config-server-berlin" "$current_target"
  fi

  echo "Starting server.."
  cd "$current_target/kelinci_analysis"
  screen -S server -d -m java -cp ./bin-instr/ edu.cmu.sv.kelinci.Kelinci $server_param

  echo "Starting AFL.."
  screen -S afl -d -m bash -c 'AFL_SKIP_CPUFREQ=1 AFL_I_DONT_CARE_ABOUT_MISSING_CRASHES=1 /vol/home-vol2/se/nollerya/fuzzing/afl-2.51b/afl-fuzz -i ./in_dir -o ./fuzzer-out -c jumps -S afl -t 999999999 /vol/home-vol2/se/nollerya/fuzzing/kelinciwca/fuzzerside/interface @@'

  cd "$current_target"
  if [ $execution_mode -eq 2 ]
  then
    echo "Starting SPF.."
    export PATH=$PATH:/vol/home-vol2/se/nollerya/fuzzing/z3/build
    export LD_LIBRARY_PATH=/vol/home-vol2/se/nollerya/fuzzing/z3/build
    screen -S spf -d -m java -Xmx10240m -jar KelinciSPFRunner.jar config-server-berlin
  fi

  echo "Waiting for $time_bound seconds.."
  sleep $time_bound

  if [ $execution_mode -eq 2 ]
  then
    echo "Stopping SPF.."
    screen -r spf -X kill
  fi

  echo "Stopping AFL.."
  screen -r afl -X kill

  echo "Stopping server.."
  screen -r server -X kill

  echo "Finished run=$i"
  echo ""

done

echo "Done."
