/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.compressors.bzip2;

/**
 * A simple class the hold and calculate the CRC for sanity checking of the data.
 *
 * @NotThreadSafe
 */
class CRC {
    private static final int crc32Table[] = { 0x00000000, 0x04c11db7, 0x09823b6e, 0x0d4326d9, 0x130476dc, 0x17c56b6b,
            0x1a864db2, 0x1e475005, 0x2608edb8, 0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61, 0x350c9b64, 0x31cd86d3, 0x3c8ea00a,
            0x384fbdbd, 0x4c11db70, 0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac, 0x5bd4b01b, 0x569796c2, 0x52568b75,
            0x6a1936c8, 0x6ed82b7f, 0x639b0da6, 0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd, 0x9823b6e0,
            0x9ce2ab57, 0x91a18d8e, 0x95609039, 0x8b27c03c, 0x8fe6dd8b, 0x82a5fb52, 0x8664e6e5, 0xbe2b5b58, 0xbaea46ef,
            0xb7a96036, 0xb3687d81, 0xad2f2d84, 0xa9ee3033, 0xa4ad16ea, 0xa06c0b5d, 0xd4326d90, 0xd0f37027, 0xddb056fe,
            0xd9714b49, 0xc7361b4c, 0xc3f706fb, 0xceb42022, 0xca753d95, 0xf23a8028, 0xf6fb9d9f, 0xfbb8bb46, 0xff79a6f1,
            0xe13ef6f4, 0xe5ffeb43, 0xe8bccd9a, 0xec7dd02d, 0x34867077, 0x30476dc0, 0x3d044b19, 0x39c556ae, 0x278206ab,
            0x23431b1c, 0x2e003dc5, 0x2ac12072, 0x128e9dcf, 0x164f8078, 0x1b0ca6a1, 0x1fcdbb16, 0x018aeb13, 0x054bf6a4,
            0x0808d07d, 0x0cc9cdca, 0x7897ab07, 0x7c56b6b0, 0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5,
            0x66d0fb02, 0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066, 0x4d9b3063, 0x495a2dd4, 0x44190b0d, 0x40d816ba,
            0xaca5c697, 0xa864db20, 0xa527fdf9, 0xa1e6e04e, 0xbfa1b04b, 0xbb60adfc, 0xb6238b25, 0xb2e29692, 0x8aad2b2f,
            0x8e6c3698, 0x832f1041, 0x87ee0df6, 0x99a95df3, 0x9d684044, 0x902b669d, 0x94ea7b2a, 0xe0b41de7, 0xe4750050,
            0xe9362689, 0xedf73b3e, 0xf3b06b3b, 0xf771768c, 0xfa325055, 0xfef34de2, 0xc6bcf05f, 0xc27dede8, 0xcf3ecb31,
            0xcbffd686, 0xd5b88683, 0xd1799b34, 0xdc3abded, 0xd8fba05a, 0x690ce0ee, 0x6dcdfd59, 0x608edb80, 0x644fc637,
            0x7a089632, 0x7ec98b85, 0x738aad5c, 0x774bb0eb, 0x4f040d56, 0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a,
            0x58c1663d, 0x558240e4, 0x51435d53, 0x251d3b9e, 0x21dc2629, 0x2c9f00f0, 0x285e1d47, 0x36194d42, 0x32d850f5,
            0x3f9b762c, 0x3b5a6b9b, 0x0315d626, 0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa, 0x14d0bd4d, 0x19939b94,
            0x1d528623, 0xf12f560e, 0xf5ee4bb9, 0xf8ad6d60, 0xfc6c70d7, 0xe22b20d2, 0xe6ea3d65, 0xeba91bbc, 0xef68060b,
            0xd727bbb6, 0xd3e6a601, 0xdea580d8, 0xda649d6f, 0xc423cd6a, 0xc0e2d0dd, 0xcda1f604, 0xc960ebb3, 0xbd3e8d7e,
            0xb9ff90c9, 0xb4bcb610, 0xb07daba7, 0xae3afba2, 0xaafbe615, 0xa7b8c0cc, 0xa379dd7b, 0x9b3660c6, 0x9ff77d71,
            0x92b45ba8, 0x9675461f, 0x8832161a, 0x8cf30bad, 0x81b02d74, 0x857130c3, 0x5d8a9099, 0x594b8d2e, 0x5408abf7,
            0x50c9b640, 0x4e8ee645, 0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c, 0x7b827d21, 0x7f436096, 0x7200464f, 0x76c15bf8,
            0x68860bfd, 0x6c47164a, 0x61043093, 0x65c52d24, 0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30, 0x029f3d35,
            0x065e2082, 0x0b1d065b, 0x0fdc1bec, 0x3793a651, 0x3352bbe6, 0x3e119d3f, 0x3ad08088, 0x2497d08d, 0x2056cd3a,
            0x2d15ebe3, 0x29d4f654, 0xc5a92679, 0xc1683bce, 0xcc2b1d17, 0xc8ea00a0, 0xd6ad50a5, 0xd26c4d12, 0xdf2f6bcb,
            0xdbee767c, 0xe3a1cbc1, 0xe760d676, 0xea23f0af, 0xeee2ed18, 0xf0a5bd1d, 0xf464a0aa, 0xf9278673, 0xfde69bc4,
            0x89b8fd09, 0x8d79e0be, 0x803ac667, 0x84fbdbd0, 0x9abc8bd5, 0x9e7d9662, 0x933eb0bb, 0x97ffad0c, 0xafb010b1,
            0xab710d06, 0xa6322bdf, 0xa2f33668, 0xbcb4666d, 0xb8757bda, 0xb5365d03, 0xb1f740b4 };

    private static int getCrc32TableValue(int index) {
        switch (index) {
        case 0:
            return crc32Table[0];
        case 1:
            return crc32Table[1];
        case 2:
            return crc32Table[2];
        case 3:
            return crc32Table[3];
        case 4:
            return crc32Table[4];
        case 5:
            return crc32Table[5];
        case 6:
            return crc32Table[6];
        case 7:
            return crc32Table[7];
        case 8:
            return crc32Table[8];
        case 9:
            return crc32Table[9];
        case 10:
            return crc32Table[10];
        case 11:
            return crc32Table[11];
        case 12:
            return crc32Table[12];
        case 13:
            return crc32Table[13];
        case 14:
            return crc32Table[14];
        case 15:
            return crc32Table[15];
        case 16:
            return crc32Table[16];
        case 17:
            return crc32Table[17];
        case 18:
            return crc32Table[18];
        case 19:
            return crc32Table[19];
        case 20:
            return crc32Table[20];
        case 21:
            return crc32Table[21];
        case 22:
            return crc32Table[22];
        case 23:
            return crc32Table[23];
        case 24:
            return crc32Table[24];
        case 25:
            return crc32Table[25];
        case 26:
            return crc32Table[26];
        case 27:
            return crc32Table[27];
        case 28:
            return crc32Table[28];
        case 29:
            return crc32Table[29];
        case 30:
            return crc32Table[30];
        case 31:
            return crc32Table[31];
        case 32:
            return crc32Table[32];
        case 33:
            return crc32Table[33];
        case 34:
            return crc32Table[34];
        case 35:
            return crc32Table[35];
        case 36:
            return crc32Table[36];
        case 37:
            return crc32Table[37];
        case 38:
            return crc32Table[38];
        case 39:
            return crc32Table[39];
        case 40:
            return crc32Table[40];
        case 41:
            return crc32Table[41];
        case 42:
            return crc32Table[42];
        case 43:
            return crc32Table[43];
        case 44:
            return crc32Table[44];
        case 45:
            return crc32Table[45];
        case 46:
            return crc32Table[46];
        case 47:
            return crc32Table[47];
        case 48:
            return crc32Table[48];
        case 49:
            return crc32Table[49];
        case 50:
            return crc32Table[50];
        case 51:
            return crc32Table[51];
        case 52:
            return crc32Table[52];
        case 53:
            return crc32Table[53];
        case 54:
            return crc32Table[54];
        case 55:
            return crc32Table[55];
        case 56:
            return crc32Table[56];
        case 57:
            return crc32Table[57];
        case 58:
            return crc32Table[58];
        case 59:
            return crc32Table[59];
        case 60:
            return crc32Table[60];
        case 61:
            return crc32Table[61];
        case 62:
            return crc32Table[62];
        case 63:
            return crc32Table[63];
        case 64:
            return crc32Table[64];
        case 65:
            return crc32Table[65];
        case 66:
            return crc32Table[66];
        case 67:
            return crc32Table[67];
        case 68:
            return crc32Table[68];
        case 69:
            return crc32Table[69];
        case 70:
            return crc32Table[70];
        case 71:
            return crc32Table[71];
        case 72:
            return crc32Table[72];
        case 73:
            return crc32Table[73];
        case 74:
            return crc32Table[74];
        case 75:
            return crc32Table[75];
        case 76:
            return crc32Table[76];
        case 77:
            return crc32Table[77];
        case 78:
            return crc32Table[78];
        case 79:
            return crc32Table[79];
        case 80:
            return crc32Table[80];
        case 81:
            return crc32Table[81];
        case 82:
            return crc32Table[82];
        case 83:
            return crc32Table[83];
        case 84:
            return crc32Table[84];
        case 85:
            return crc32Table[85];
        case 86:
            return crc32Table[86];
        case 87:
            return crc32Table[87];
        case 88:
            return crc32Table[88];
        case 89:
            return crc32Table[89];
        case 90:
            return crc32Table[90];
        case 91:
            return crc32Table[91];
        case 92:
            return crc32Table[92];
        case 93:
            return crc32Table[93];
        case 94:
            return crc32Table[94];
        case 95:
            return crc32Table[95];
        case 96:
            return crc32Table[96];
        case 97:
            return crc32Table[97];
        case 98:
            return crc32Table[98];
        case 99:
            return crc32Table[99];
        case 100:
            return crc32Table[100];
        case 101:
            return crc32Table[101];
        case 102:
            return crc32Table[102];
        case 103:
            return crc32Table[103];
        case 104:
            return crc32Table[104];
        case 105:
            return crc32Table[105];
        case 106:
            return crc32Table[106];
        case 107:
            return crc32Table[107];
        case 108:
            return crc32Table[108];
        case 109:
            return crc32Table[109];
        case 110:
            return crc32Table[110];
        case 111:
            return crc32Table[111];
        case 112:
            return crc32Table[112];
        case 113:
            return crc32Table[113];
        case 114:
            return crc32Table[114];
        case 115:
            return crc32Table[115];
        case 116:
            return crc32Table[116];
        case 117:
            return crc32Table[117];
        case 118:
            return crc32Table[118];
        case 119:
            return crc32Table[119];
        case 120:
            return crc32Table[120];
        case 121:
            return crc32Table[121];
        case 122:
            return crc32Table[122];
        case 123:
            return crc32Table[123];
        case 124:
            return crc32Table[124];
        case 125:
            return crc32Table[125];
        case 126:
            return crc32Table[126];
        case 127:
            return crc32Table[127];
        case 128:
            return crc32Table[128];
        case 129:
            return crc32Table[129];
        case 130:
            return crc32Table[130];
        case 131:
            return crc32Table[131];
        case 132:
            return crc32Table[132];
        case 133:
            return crc32Table[133];
        case 134:
            return crc32Table[134];
        case 135:
            return crc32Table[135];
        case 136:
            return crc32Table[136];
        case 137:
            return crc32Table[137];
        case 138:
            return crc32Table[138];
        case 139:
            return crc32Table[139];
        case 140:
            return crc32Table[140];
        case 141:
            return crc32Table[141];
        case 142:
            return crc32Table[142];
        case 143:
            return crc32Table[143];
        case 144:
            return crc32Table[144];
        case 145:
            return crc32Table[145];
        case 146:
            return crc32Table[146];
        case 147:
            return crc32Table[147];
        case 148:
            return crc32Table[148];
        case 149:
            return crc32Table[149];
        case 150:
            return crc32Table[150];
        case 151:
            return crc32Table[151];
        case 152:
            return crc32Table[152];
        case 153:
            return crc32Table[153];
        case 154:
            return crc32Table[154];
        case 155:
            return crc32Table[155];
        case 156:
            return crc32Table[156];
        case 157:
            return crc32Table[157];
        case 158:
            return crc32Table[158];
        case 159:
            return crc32Table[159];
        case 160:
            return crc32Table[160];
        case 161:
            return crc32Table[161];
        case 162:
            return crc32Table[162];
        case 163:
            return crc32Table[163];
        case 164:
            return crc32Table[164];
        case 165:
            return crc32Table[165];
        case 166:
            return crc32Table[166];
        case 167:
            return crc32Table[167];
        case 168:
            return crc32Table[168];
        case 169:
            return crc32Table[169];
        case 170:
            return crc32Table[170];
        case 171:
            return crc32Table[171];
        case 172:
            return crc32Table[172];
        case 173:
            return crc32Table[173];
        case 174:
            return crc32Table[174];
        case 175:
            return crc32Table[175];
        case 176:
            return crc32Table[176];
        case 177:
            return crc32Table[177];
        case 178:
            return crc32Table[178];
        case 179:
            return crc32Table[179];
        case 180:
            return crc32Table[180];
        case 181:
            return crc32Table[181];
        case 182:
            return crc32Table[182];
        case 183:
            return crc32Table[183];
        case 184:
            return crc32Table[184];
        case 185:
            return crc32Table[185];
        case 186:
            return crc32Table[186];
        case 187:
            return crc32Table[187];
        case 188:
            return crc32Table[188];
        case 189:
            return crc32Table[189];
        case 190:
            return crc32Table[190];
        case 191:
            return crc32Table[191];
        case 192:
            return crc32Table[192];
        case 193:
            return crc32Table[193];
        case 194:
            return crc32Table[194];
        case 195:
            return crc32Table[195];
        case 196:
            return crc32Table[196];
        case 197:
            return crc32Table[197];
        case 198:
            return crc32Table[198];
        case 199:
            return crc32Table[199];
        case 200:
            return crc32Table[200];
        case 201:
            return crc32Table[201];
        case 202:
            return crc32Table[202];
        case 203:
            return crc32Table[203];
        case 204:
            return crc32Table[204];
        case 205:
            return crc32Table[205];
        case 206:
            return crc32Table[206];
        case 207:
            return crc32Table[207];
        case 208:
            return crc32Table[208];
        case 209:
            return crc32Table[209];
        case 210:
            return crc32Table[210];
        case 211:
            return crc32Table[211];
        case 212:
            return crc32Table[212];
        case 213:
            return crc32Table[213];
        case 214:
            return crc32Table[214];
        case 215:
            return crc32Table[215];
        case 216:
            return crc32Table[216];
        case 217:
            return crc32Table[217];
        case 218:
            return crc32Table[218];
        case 219:
            return crc32Table[219];
        case 220:
            return crc32Table[220];
        case 221:
            return crc32Table[221];
        case 222:
            return crc32Table[222];
        case 223:
            return crc32Table[223];
        case 224:
            return crc32Table[224];
        case 225:
            return crc32Table[225];
        case 226:
            return crc32Table[226];
        case 227:
            return crc32Table[227];
        case 228:
            return crc32Table[228];
        case 229:
            return crc32Table[229];
        case 230:
            return crc32Table[230];
        case 231:
            return crc32Table[231];
        case 232:
            return crc32Table[232];
        case 233:
            return crc32Table[233];
        case 234:
            return crc32Table[234];
        case 235:
            return crc32Table[235];
        case 236:
            return crc32Table[236];
        case 237:
            return crc32Table[237];
        case 238:
            return crc32Table[238];
        case 239:
            return crc32Table[239];
        case 240:
            return crc32Table[240];
        case 241:
            return crc32Table[241];
        case 242:
            return crc32Table[242];
        case 243:
            return crc32Table[243];
        case 244:
            return crc32Table[244];
        case 245:
            return crc32Table[245];
        case 246:
            return crc32Table[246];
        case 247:
            return crc32Table[247];
        case 248:
            return crc32Table[248];
        case 249:
            return crc32Table[249];
        case 250:
            return crc32Table[250];
        case 251:
            return crc32Table[251];
        case 252:
            return crc32Table[252];
        case 253:
            return crc32Table[253];
        case 254:
            return crc32Table[254];
        case 255:
            return crc32Table[255];
        default:
            throw new RuntimeException("ArrayIndexOutOfBounds, should not happen!");
        }
    }

    CRC() {
        initialiseCRC();
    }

    void initialiseCRC() {
        globalCrc = 0xffffffff;
    }

    int getFinalCRC() {
        return ~globalCrc;
    }

    int getGlobalCRC() {
        return globalCrc;
    }

    void setGlobalCRC(int newCrc) {
        globalCrc = newCrc;
    }

    void updateCRC(int inCh) {
        int temp = (globalCrc >> 24) ^ inCh;
        if (temp < 0) {
            temp = 256 + temp;
        }
        globalCrc = (globalCrc << 8) ^ getCrc32TableValue(temp);
    }

    void updateCRC(int inCh, int repeat) {
        int globalCrcShadow = this.globalCrc;
        while (repeat-- > 0) {
            int temp = (globalCrcShadow >> 24) ^ inCh;
            globalCrcShadow = (globalCrcShadow << 8) ^ getCrc32TableValue((temp >= 0) ? temp : (temp + 256));
        }
        this.globalCrc = globalCrcShadow;
    }

    private int globalCrc;
}
