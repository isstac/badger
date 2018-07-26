package com.stac.mathematics;

import com.stac.image.utilities.*;

import gov.nasa.jpf.symbc.Debug;

public class Mathematics {
    private static final double Z255_inv = 0.00392156862745098;
    private static final double Z255_e_revert = 93.80925749833975;

    /*
     * YN: change array access to switch statement to overcome problem of AFL with
     * array branching.
     */
    // private static final int[] accuracy;
    // static {
    // accuracy = new int[255];
    // for (int n = 0; n <= 254; ++n) {
    // double v = 1.0 / Math.tan(Math.abs(30 - n) * 0.00392156862745098 + 0.001);
    // Mathematics.accuracy[n] = 4 + (int) v;
    // }
    // }

    private static double lgamma(final double x) {
        final double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
        final double ser = 1.0 + 76.18009173 / x - 86.50532033 / (x + 1.0) + 24.014092822 / (x + 2.0)
                - 1.231739516 / (x + 3.0) + 0.00120858003 / (x + 4.0) - 5.36382E-6 / (x + 5.0);
        return tmp + Math.log(ser * Math.sqrt(6.283185307179586));
    }

    private static double factorial(final double x) {
        return Math.exp(lgamma(x + 1.0));
    }

    public static double exp(final int x, final int n) {
        double exp_n = 0.0;
        for (int i = 0; i < n; ++i) {
            final double aDouble = Math.pow(x * 0.00392156862745098, n);
            final double aDouble2 = 1.0 / factorial(n);
            exp_n += aDouble * aDouble2;

        }
        return exp_n;
    }

    public static double exp2(final int x, final int n) {
        double exp_n = 0.0;
        for (int i = 0; i < n; ++i) {
            // abstracted away, because we know this method is linear w.r.t. n
        }
        return exp_n;
    }

    public static int intensify(final int a, final int r, final int g, final int b) {
        final int acc = getAccuracyValue((r + g + b) % 255);
        return ARGB.toARGB(
                a, 
                (int)(Z255_e_revert * exp2(r, acc)), 
                (int)(Z255_e_revert * exp2(g, acc)), 
                (int)(Z255_e_revert * exp2(b, acc))
            );
    }

    /*
     * YN: change array access to switch statement to overcome problem of AFL with
     * array branching.
     */
    private static int getAccuracyValue(int index) {
        int value;
        switch (index) {
        case 0:
            value = 12;
            break;
        case 1:
            value = 12;
            break;
        case 2:
            value = 12;
            break;
        case 3:
            value = 13;
            break;
        case 4:
            value = 13;
            break;
        case 5:
            value = 14;
            break;
        case 6:
            value = 14;
            break;
        case 7:
            value = 14;
            break;
        case 8:
            value = 15;
            break;
        case 9:
            value = 15;
            break;
        case 10:
            value = 16;
            break;
        case 11:
            value = 17;
            break;
        case 12:
            value = 17;
            break;
        case 13:
            value = 18;
            break;
        case 14:
            value = 19;
            break;
        case 15:
            value = 20;
            break;
        case 16:
            value = 21;
            break;
        case 17:
            value = 23;
            break;
        case 18:
            value = 24;
            break;
        case 19:
            value = 26;
            break;
        case 20:
            value = 28;
            break;
        case 21:
            value = 31;
            break;
        case 22:
            value = 34;
            break;
        case 23:
            value = 39;
            break;
        case 24:
            value = 44;
            break;
        case 25:
            value = 52;
            break;
        case 26:
            value = 63;
            break;
        case 27:
            value = 82;
            break;
        case 28:
            value = 117;
            break;
        case 29:
            value = 207;
            break;
        case 30:
            value = 1003;
            break;
        case 31:
            value = 207;
            break;
        case 32:
            value = 117;
            break;
        case 33:
            value = 82;
            break;
        case 34:
            value = 63;
            break;
        case 35:
            value = 52;
            break;
        case 36:
            value = 44;
            break;
        case 37:
            value = 39;
            break;
        case 38:
            value = 34;
            break;
        case 39:
            value = 31;
            break;
        case 40:
            value = 28;
            break;
        case 41:
            value = 26;
            break;
        case 42:
            value = 24;
            break;
        case 43:
            value = 23;
            break;
        case 44:
            value = 21;
            break;
        case 45:
            value = 20;
            break;
        case 46:
            value = 19;
            break;
        case 47:
            value = 18;
            break;
        case 48:
            value = 17;
            break;
        case 49:
            value = 17;
            break;
        case 50:
            value = 16;
            break;
        case 51:
            value = 15;
            break;
        case 52:
            value = 15;
            break;
        case 53:
            value = 14;
            break;
        case 54:
            value = 14;
            break;
        case 55:
            value = 14;
            break;
        case 56:
            value = 13;
            break;
        case 57:
            value = 13;
            break;
        case 58:
            value = 12;
            break;
        case 59:
            value = 12;
            break;
        case 60:
            value = 12;
            break;
        case 61:
            value = 12;
            break;
        case 62:
            value = 11;
            break;
        case 63:
            value = 11;
            break;
        case 64:
            value = 11;
            break;
        case 65:
            value = 11;
            break;
        case 66:
            value = 10;
            break;
        case 67:
            value = 10;
            break;
        case 68:
            value = 10;
            break;
        case 69:
            value = 10;
            break;
        case 70:
            value = 10;
            break;
        case 71:
            value = 10;
            break;
        case 72:
            value = 9;
            break;
        case 73:
            value = 9;
            break;
        case 74:
            value = 9;
            break;
        case 75:
            value = 9;
            break;
        case 76:
            value = 9;
            break;
        case 77:
            value = 9;
            break;
        case 78:
            value = 9;
            break;
        case 79:
            value = 9;
            break;
        case 80:
            value = 9;
            break;
        case 81:
            value = 8;
            break;
        case 82:
            value = 8;
            break;
        case 83:
            value = 8;
            break;
        case 84:
            value = 8;
            break;
        case 85:
            value = 8;
            break;
        case 86:
            value = 8;
            break;
        case 87:
            value = 8;
            break;
        case 88:
            value = 8;
            break;
        case 89:
            value = 8;
            break;
        case 90:
            value = 8;
            break;
        case 91:
            value = 8;
            break;
        case 92:
            value = 8;
            break;
        case 93:
            value = 7;
            break;
        case 94:
            value = 7;
            break;
        case 95:
            value = 7;
            break;
        case 96:
            value = 7;
            break;
        case 97:
            value = 7;
            break;
        case 98:
            value = 7;
            break;
        case 99:
            value = 7;
            break;
        case 100:
            value = 7;
            break;
        case 101:
            value = 7;
            break;
        case 102:
            value = 7;
            break;
        case 103:
            value = 7;
            break;
        case 104:
            value = 7;
            break;
        case 105:
            value = 7;
            break;
        case 106:
            value = 7;
            break;
        case 107:
            value = 7;
            break;
        case 108:
            value = 7;
            break;
        case 109:
            value = 7;
            break;
        case 110:
            value = 7;
            break;
        case 111:
            value = 7;
            break;
        case 112:
            value = 6;
            break;
        case 113:
            value = 6;
            break;
        case 114:
            value = 6;
            break;
        case 115:
            value = 6;
            break;
        case 116:
            value = 6;
            break;
        case 117:
            value = 6;
            break;
        case 118:
            value = 6;
            break;
        case 119:
            value = 6;
            break;
        case 120:
            value = 6;
            break;
        case 121:
            value = 6;
            break;
        case 122:
            value = 6;
            break;
        case 123:
            value = 6;
            break;
        case 124:
            value = 6;
            break;
        case 125:
            value = 6;
            break;
        case 126:
            value = 6;
            break;
        case 127:
            value = 6;
            break;
        case 128:
            value = 6;
            break;
        case 129:
            value = 6;
            break;
        case 130:
            value = 6;
            break;
        case 131:
            value = 6;
            break;
        case 132:
            value = 6;
            break;
        case 133:
            value = 6;
            break;
        case 134:
            value = 6;
            break;
        case 135:
            value = 6;
            break;
        case 136:
            value = 6;
            break;
        case 137:
            value = 6;
            break;
        case 138:
            value = 6;
            break;
        case 139:
            value = 6;
            break;
        case 140:
            value = 6;
            break;
        case 141:
            value = 6;
            break;
        case 142:
            value = 6;
            break;
        case 143:
            value = 6;
            break;
        case 144:
            value = 6;
            break;
        case 145:
            value = 6;
            break;
        case 146:
            value = 6;
            break;
        case 147:
            value = 6;
            break;
        case 148:
            value = 5;
            break;
        case 149:
            value = 5;
            break;
        case 150:
            value = 5;
            break;
        case 151:
            value = 5;
            break;
        case 152:
            value = 5;
            break;
        case 153:
            value = 5;
            break;
        case 154:
            value = 5;
            break;
        case 155:
            value = 5;
            break;
        case 156:
            value = 5;
            break;
        case 157:
            value = 5;
            break;
        case 158:
            value = 5;
            break;
        case 159:
            value = 5;
            break;
        case 160:
            value = 5;
            break;
        case 161:
            value = 5;
            break;
        case 162:
            value = 5;
            break;
        case 163:
            value = 5;
            break;
        case 164:
            value = 5;
            break;
        case 165:
            value = 5;
            break;
        case 166:
            value = 5;
            break;
        case 167:
            value = 5;
            break;
        case 168:
            value = 5;
            break;
        case 169:
            value = 5;
            break;
        case 170:
            value = 5;
            break;
        case 171:
            value = 5;
            break;
        case 172:
            value = 5;
            break;
        case 173:
            value = 5;
            break;
        case 174:
            value = 5;
            break;
        case 175:
            value = 5;
            break;
        case 176:
            value = 5;
            break;
        case 177:
            value = 5;
            break;
        case 178:
            value = 5;
            break;
        case 179:
            value = 5;
            break;
        case 180:
            value = 5;
            break;
        case 181:
            value = 5;
            break;
        case 182:
            value = 5;
            break;
        case 183:
            value = 5;
            break;
        case 184:
            value = 5;
            break;
        case 185:
            value = 5;
            break;
        case 186:
            value = 5;
            break;
        case 187:
            value = 5;
            break;
        case 188:
            value = 5;
            break;
        case 189:
            value = 5;
            break;
        case 190:
            value = 5;
            break;
        case 191:
            value = 5;
            break;
        case 192:
            value = 5;
            break;
        case 193:
            value = 5;
            break;
        case 194:
            value = 5;
            break;
        case 195:
            value = 5;
            break;
        case 196:
            value = 5;
            break;
        case 197:
            value = 5;
            break;
        case 198:
            value = 5;
            break;
        case 199:
            value = 5;
            break;
        case 200:
            value = 5;
            break;
        case 201:
            value = 5;
            break;
        case 202:
            value = 5;
            break;
        case 203:
            value = 5;
            break;
        case 204:
            value = 5;
            break;
        case 205:
            value = 5;
            break;
        case 206:
            value = 5;
            break;
        case 207:
            value = 5;
            break;
        case 208:
            value = 5;
            break;
        case 209:
            value = 5;
            break;
        case 210:
            value = 5;
            break;
        case 211:
            value = 5;
            break;
        case 212:
            value = 5;
            break;
        case 213:
            value = 5;
            break;
        case 214:
            value = 5;
            break;
        case 215:
            value = 5;
            break;
        case 216:
            value = 5;
            break;
        case 217:
            value = 5;
            break;
        case 218:
            value = 5;
            break;
        case 219:
            value = 5;
            break;
        case 220:
            value = 5;
            break;
        case 221:
            value = 5;
            break;
        case 222:
            value = 5;
            break;
        case 223:
            value = 5;
            break;
        case 224:
            value = 5;
            break;
        case 225:
            value = 5;
            break;
        case 226:
            value = 5;
            break;
        case 227:
            value = 5;
            break;
        case 228:
            value = 5;
            break;
        case 229:
            value = 5;
            break;
        case 230:
            value = 5;
            break;
        case 231:
            value = 4;
            break;
        case 232:
            value = 4;
            break;
        case 233:
            value = 4;
            break;
        case 234:
            value = 4;
            break;
        case 235:
            value = 4;
            break;
        case 236:
            value = 4;
            break;
        case 237:
            value = 4;
            break;
        case 238:
            value = 4;
            break;
        case 239:
            value = 4;
            break;
        case 240:
            value = 4;
            break;
        case 241:
            value = 4;
            break;
        case 242:
            value = 4;
            break;
        case 243:
            value = 4;
            break;
        case 244:
            value = 4;
            break;
        case 245:
            value = 4;
            break;
        case 246:
            value = 4;
            break;
        case 247:
            value = 4;
            break;
        case 248:
            value = 4;
            break;
        case 249:
            value = 4;
            break;
        case 250:
            value = 4;
            break;
        case 251:
            value = 4;
            break;
        case 252:
            value = 4;
            break;
        case 253:
            value = 4;
            break;
        case 254:
            value = 4;
            break;
        default:
            // since the actual application uses a bound-method to assure that there is no
            // out of bounds exception, we just set a really bad value for this. I don't
            // want to modify the SPF bytecode handling since this could lead to other
            // problems. This cannot occur in the real program, since the index is modulo
            // processed in this range.
            // throw new RuntimeException("Array Index out of bounds!");
            value = 0;
        }

        return value;
    }
}
