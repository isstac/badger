import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;

import edu.cmu.sv.kelinci.Kelinci;

public class GovernMental {
    // memory[0] is lastTimeOfNewCredit
    // memory[1] is lastCreditorPayedOut (assumed <= 1000)
    // memory[2] is profitFromCrash
    // memory[3] is round
    // memory[4] is length of creditorAddresses (assumed <= 1000)
    // memory[5] is length of creditorAmounts (assumed <= 1000)
    // memory[6] is the start of creditorAddresses
    // memory [1006] is the start of creditorAmounts
    // buddy is assumed to be greater than 5000
    // sender probably doesn't overlap with any of the previous addresses
    static int lendGovernmentMoney(int buddy, int timestamp, int callvalue, int sender, int balance, int[] memory) {
        int gas = 0;
        if (memory[0] < timestamp) {
            memory[1] = 0;
            memory[0] = timestamp;
            memory[2] = 0;
            gas = gas + 30;
//            for (int i = 6; i < memory[4]; i++) {
//                memory[i] = 0;
//                gas = gas + 50;
//            }
            // YN: ->
            if (memory.length > 6) {
                for (int i = 6; i < Math.min(memory.length, memory[4]); i++) {
                    memory[i] = 0;
                    gas = gas + 50;
                }
            }
            // <-

//            for (int i = 1006; i < memory[5]; i++) {
//                memory[i] = 0;
//                gas = gas + 50;
//            }
            // YN: ->
            if (memory.length > 1006) {
                for (int i = 1006; i <Math.min(memory.length, memory[5]); i++) {
                    memory[i] = 0;
                    gas = gas + 50;
                }
            }
            // <-
            memory[3] = memory[3] + 1;
            gas = gas + 10;
            //Debug.printSymbolicRef(gas, "#1 gas=");
            Kelinci.addCost(gas);
            return 0;
        } else {
            if (callvalue >= 100000000) {
                memory[0] = timestamp;
                gas = gas + 2;
                if (memory[2] < 10000 * 100000000) {
                    gas = gas + 5;
                    memory[2] = memory[2] + callvalue * 5 / 100;
                }
                if (memory[buddy] >= callvalue) {
                    gas = gas + callvalue / 100;
                }
                memory[sender] = memory[sender] + callvalue * 110 / 100;
                gas = gas + 100;

//                if (memory[1006 + memory[1]] <= balance - memory[2]) {
//                    memory[memory[6 + memory[1]]] = memory[memory[6 + memory[1]]] - memory[1006 + memory[1]];
//                    memory[1] = memory[1] + 1;
//                    gas = gas + 3 * memory[memory[6 + memory[1]]];
//                }
                // YN: ->
                if ((1006 + memory[1]) < memory.length) {
                    if (memory[1006 + memory[1]] <= balance - memory[2]) {
                        memory[memory[6 + memory[1]]] = memory[memory[6 + memory[1]]] - memory[1006 + memory[1]];
                        memory[1] = memory[1] + 1;
                        gas = gas + 3 * memory[memory[6 + memory[1]]];
                    }
                }
                // <-

                //Debug.printSymbolicRef(gas, "#2 gas=");
                Kelinci.addCost(gas);
                return 0;
            } else {
                gas = gas + 3;
                //Debug.printSymbolicRef(gas, "#3 gas=");
                Kelinci.addCost(gas);
                return 1;
            }
        }
    }

    // public static void main(String[] args) {
    // int n = (int)Math.pow(2,24);
    // int[] memory = new int[n];
    // lendGovernmentMoney(0, 0, 0, 0, 0, memory);
    // }

    public static void main(String[] args) {
        int N = 10;
        int memory[] = new int[N];
        int buddy = 0, timestamp = 0, callvalue = 0, sender = 0, balance = 0;

        if (args.length == 1) {

            String fileName = args[0];

            try (FileInputStream fis = new FileInputStream(fileName)) {
                byte[] bytes = new byte[Integer.BYTES];
                int i = 0;
                while (((fis.read(bytes)) != -1) && i < N+5) {
                    int value = ByteBuffer.wrap(bytes).getInt();

                    switch (i) {
                    case 0:  // Read the first five variables
                        buddy = value;
                        break;
                    case 1:
                        timestamp = value;
                        break;
                    case 2:
                        callvalue = value;
                        break;
                    case 3:
                        sender = value;
                        break;
                    case 4:
                        balance = value;
                        break;
                    default: // Read the rest in the memory array.
                        memory[i-5] = value;
                    }

                    i++;
                }
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

            System.out.println("buddy="+buddy);
            System.out.println("timestamp="+timestamp);
            System.out.println("callvalue="+callvalue);
            System.out.println("sender="+sender);
            System.out.println("balance="+balance);
            for (int i = 0; i < N; i++) {
                System.out.println("memory[" + i + "]=" + memory[i]);
            }

        } else {
            throw new RuntimeException("no input provided!");
        }

        lendGovernmentMoney(buddy, timestamp, callvalue, sender, balance, memory);
    }
}
