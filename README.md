
# Badger: Complexity Analysis with Fuzzing and Symbolic Execution

Badger means the combination of fuzzing and symbolic execution for complexity analysis. 
This approach uses the strength and scalability of fuzzing and the precision of symbolic execution.
Please have a look at our [paper](https://dl.acm.org/citation.cfm?id=3213868) for more information about our approach.
This repository contains the source code for the symbolic execution part of Badger (denoted as *SymExe*), which could be also run in separate.

## Getting Started

These instructions will show you how to setup Badger and how to run it on a simple example: Insertion Sort.

### Prerequisites

In order to run Badger you need basically two things: (1) a fuzzer, and (2) a symbolic execution engine.
As fuzzer we use in our apporach the KelinciWCA, which is provided in its own [repository](https://github.com/isstac/kelinci/tree/7155d4bf383fee21e8024a8ba4cba799aeaeb620).
The symbolic execution engine (denoted as SymExe) is delivered with this repository.
It is built on top of Java PathFinder (JPF) and Symbolic PathFinder (SPF), which makes it necessary to have the projects jpf-core and jpf-symbc ready and built.
We recommend using the latest SPF version from their GitHub [repository](https://github.com/SymbolicPathFinder/jpf-symbc/tree/b64ab6a0c8dde218b34969b46ee526ece7ddee44) and, for now, our own branch of the JPF [repository](https://github.com/nolleryc/jpf-core/tree/0f2f2901cd0ae9833145c38fee57be03da90a64f).
Please be sure to have the following ".jpf/site.properties" in your home directory:
```
# JPF site configuration
jpf-core = ${user.home}/.../path-to-jpf-core-folder/jpf-core
jpf-symbc = ${user.home}/.../path-to-jpf-core-folder/pf-symbc
badger = ${user.home}/.../badger-sources/badger
extensions=${jpf-core},${jpf-symbc},${badger}
```

### Build Sources

Badger uses the ant build mechanism similar to JPF and SPF.
In order to build the Badger sources, go to the folder "badger-sources/badger" and run the command
```
ant build
```
Note: be sure that the site.properties file is setup correctly.

### Prepare Folder Structure
Normally we use the following folder structure to analyze an applicaton:
```
.
+-- config.txt
+-- kelinciwca_analysis
|   +-- src
|   +-- bin
|   +-- bin-instrumented
|   +-- in_dir
|   +-- fuzzer-out
|   |   +-- afl/queue
|   |   +-- symexe/queue
+-- symexe_analysis
|   +-- src
|   +-- bin
```
The *src* folders include the actual application and the drivers (one for fuzzing and one for symbolic execution).
The *bin* folders include the Java class files.
The *bin-instrumented* folder includes the class files instrumented by KelinciWCA.
The *in_dir* folder includes the initial inputs provided by the user (at least one, see [AFL documentation](https://github.com/mirrorer/afl/blob/master/docs/README) for more information).
The *fuzzer-out* folder will be created automatically by AFL and SymExe, and contains the generated inputs.

## Running Badger

In general our approach would run KelinicWCA and SymExe in parallel, i.e. start the Kelinci server, then AFL, and then SymExe. 

In order to run the SymExe part of Badger please execute the following command:
```
DYLD_LIBRARY_PATH=path/to/z3/build java -cp "[..]/badger-source/badger/build/*:[..]/badger-source/badger/lib/*:[..]/jpf-core/build/*:[..]/jpf-symbc/build/*:[..]/jpf-symbc/lib/*" edu.cmu.sv.badger.app.BadgerRunner path/to/config-file
```
Depending on your application, you might want to add the option -Xmx to allow more memory usage for the symbolic execution.
We did run our experiments with "-Xmx10240m".

### Configuration File
Badger needs a configuration property file as parameter. The following table shows all possible parameters:

| Parameter            | Description | Mandatory |
| -------------------- |-------------| ----------|
| dir.initial.input    | Input directory for SymExe, usually the same input directory as for KelinciWCA. | yes |
| dir.sync.input       | Input directory for SymExe, usually the queue folder KelinciWCA. Default: dir.initial.input | no |
| dir.export           | Export directory for SymExe, usually the queue that KelinciWCA uses for synchronization. |  yes | 
| dir.tmp              | Temporary directory for Symexe, which stores all generated files. Default: "./tmp" | no |
| symexe.wait.sec      | Time (seconds) SymExe will wait before checking KelinciWCA for new inputs, if it completely explored its trie and there was no new input by KelinciWCA. | yes |
| symexe.iterations    | Number of iterations (=maximum number of generated input files) SymExe will remain in its own execution before attempting to import inputs from KelinciWCA. | yes |
| symexe.delay.sec     | Time (seconds) SymExe will wait in the beginning before starting. Default: "0"| no |
| symexe.bse.steps     | Additional steps by the Bounded Symbolic Execution phase in SymExe. Default: "0", i.e. just generate inputs for the children nodes. | no |
| jpf.classpath        | Path to the application binaries. | yes |
| jpf.target           | Qualified name of the Java class (driver) that contains the main method for the analysis. | yes |
| jpf.argument         | Values of the arguments for the driver, separated by spaces. Default: "@@". | no |
| symbolic.method      | Qualified name of the method(s), which should be contained in the symbolic analysis. Definition similar to SPF. | yes |
| symbolic.dp          | Decision Procedure for constraint solving. Default: "z3". | no |
| symbolic.max_int     | Maximum value of symbolic integers. | no |
| symbolic.min_int     | Minimum value of symbolic integers. | no |
| symbolic.max_char    | Maximum value of symbolic chars. | no |
| symbolic.min_char    | Minimum value of symbolic chars. | no |
| symbolic.max_byte    | Maximum value of symbolic bytes. | no |
| symbolic.min_byte    | Minimum value of symbolic bytes. | no |
| symbolic.max_double    | Minimum value of symbolic doubles. | no |
| symbolic.min_double    | Minimum value of symbolic doubles. | no |
| symbolic.debug    | Print SPF debug information (=on to activate). | no |
| symbolic.undefined    | SPF default for don't care values (e.g, =0). | no |
| analysis.method      | Analysis method for trie exploration: "wca" or "cov". | yes |
| analysis.heuristic   | Trie exploration heuristic. For "wca": "highest-cost-highest-node". "highest-cost-lowest-node", "lowest-cost-highest-node", "lowest-cost-highest-node". For "cov": "branch". | yes |
| analysis.wca.metric  | Cost metric: "jumps", "instructions", "userdefined" (necessary for "wca"). | no |
| io.utils             | Input Generator. Please check source package "edu.cmu.sv.badger.io" for current implementations or add your own. | yes |
| io.input.sizes       | Abstract input size(s) for input generation, separated by spaces. | yes |
| io.initial.id        | Initial id for generated input file. Default: "0" | no |
| stat.print           | Boolean value whether to write files for statistics. Default: "true". | no |
| stat.file.import     | Path to file for import statistics. Default: "import-statistic.txt" | no |
| stat.file.generation | Path to file for generation statistics. Default: "generation-statistic.txt" | no |
| stat.file.export     | Path to file for export statistics. Default: "export-statistic.txt" | no |
| stat.file.trie       | Path to file for internal trie statistics. Default: "trie-statistic.txt" | no |
| stat.print.pc        | Boolean value whether to write files for path condition mapping. Default: "false" | no |
| stat.file.pc.mapping | Path to file for pc mapping to generated files. Default: "pcMap.txt" | no |
| trie.print           | Boolean value whether to write dot files for trie graph representation. Default: "false" (High memory consumption for "true"!). | no |

### Example: Insertion Sort

The directory "badger-source/example" contains a script to build the insertion sort example.
Please first build Badger like described above and then run the "build-example.sh" script.
If you want to run Badger in total (and not only the SymExe), then you need to setup KelinciWCA in advance.
You also need to instrument the class files for Kelinci by using the commands described on the KelinciWCA page.
The directory "badger-sources/badger/src/examples" includes the drivers for the insertion sort example.

```
 public static void sort(int[] a) {
        final int N = a.length;
        for (int i = 1; i < N; i++) {
            int j = i - 1;
            int x = a[i];
            while ((j >= 0) && (a[j] > x)) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = x;
        }
    }
```

For the Insertion Sort example the following configuration file is sufficient (to run SymExe alone):

```
dir.initial.input=./kelinciwca_analysis/in_dir
dir.export=./kelinciwca_analysis/fuzzer-out/symexe/queue

jpf.classpath=./symexe_analysis/bin
jpf.target=InsertionSortSym
symbolic.method=InsertionSortSym.sort(con)
symbolic.dp=z3
symbolic.min_int=0
symbolic.max_int=255

symexe.wait.sec=60
symexe.iterations=10

analysis.method=wca
analysis.wca.metric=jumps
analysis.heuristic=highest-cost-lowest-node

io.utils=int-byte-array
io.input.sizes=64
```

If you want to run the SymExe part with KelinciWCA (i.e. Badger in total), then please add the sync directory property:
```
dir.sync.input=./kelinciwca_analysis/fuzzer-out/afl/queue
```
, and configure KelinciWCA to use this path as queue folder.
To run SymExe only, please execute the above described command to run the BadgerRunner class.
If you want to run the whole Badger: first run the Kelinci server (be sure to use the instrumented class files), start KelinciWCA, and then run the above described command to run the BadgerRunner class.
Alternatively, you also can use the provided run script.

### Run Script

In the folder "badger-sources/scripts" we provide the shell script "runBadger.sh" to run Badger/KelinciWCA/Symexe for insertion sort.
It can be used as a template for other experiments.
Please adjust the path variables in the script according to your system.
Additionally note that we use the DYLD_LIBRARY_PATH variable to set the path to the z3 constraint solver. Please change this according to your setup environment.
If you want to run Badger in total, i.e. KelinciWCA + SymExe, then please make sure that KelinciWCA is setup correctly and that the classes are instrumented.
You might need to change some folder paths in the script.

## Developers

* **Yannic Noller** (yannic.noller at acm.org)


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
