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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorOutputStream;

/**
 * An output stream that compresses into the BZip2 format into another stream.
 *
 * <p>
 * The compression requires large amounts of memory. Thus you should call the {@link #close() close()} method as soon as
 * possible, to force <tt>BZip2CompressorOutputStream</tt> to release the allocated memory.
 * </p>
 *
 * <p>
 * You can shrink the amount of allocated memory and maybe raise the compression speed by choosing a lower blocksize,
 * which in turn may cause a lower compression ratio. You can avoid unnecessary memory allocation by avoiding using a
 * blocksize which is bigger than the size of the input.
 * </p>
 *
 * <p>
 * You can compute the memory usage for compressing by the following formula:
 * </p>
 *
 * <pre>
 * &lt;code&gt;400k + (9 * blocksize)&lt;/code&gt;.
 * </pre>
 *
 * <p>
 * To get the memory required for decompression by {@link BZip2CompressorInputStream} use
 * </p>
 *
 * <pre>
 * &lt;code&gt;65k + (5 * blocksize)&lt;/code&gt;.
 * </pre>
 *
 * <table width="100%" border="1">
 * <colgroup> <col width="33%" /> <col width="33%" /> <col width="33%" /> </colgroup>
 * <tr>
 * <th colspan="3">Memory usage by blocksize</th>
 * </tr>
 * <tr>
 * <th align="right">Blocksize</th>
 * <th align="right">Compression<br>
 * memory usage</th>
 * <th align="right">Decompression<br>
 * memory usage</th>
 * </tr>
 * <tr>
 * <td align="right">100k</td>
 * <td align="right">1300k</td>
 * <td align="right">565k</td>
 * </tr>
 * <tr>
 * <td align="right">200k</td>
 * <td align="right">2200k</td>
 * <td align="right">1065k</td>
 * </tr>
 * <tr>
 * <td align="right">300k</td>
 * <td align="right">3100k</td>
 * <td align="right">1565k</td>
 * </tr>
 * <tr>
 * <td align="right">400k</td>
 * <td align="right">4000k</td>
 * <td align="right">2065k</td>
 * </tr>
 * <tr>
 * <td align="right">500k</td>
 * <td align="right">4900k</td>
 * <td align="right">2565k</td>
 * </tr>
 * <tr>
 * <td align="right">600k</td>
 * <td align="right">5800k</td>
 * <td align="right">3065k</td>
 * </tr>
 * <tr>
 * <td align="right">700k</td>
 * <td align="right">6700k</td>
 * <td align="right">3565k</td>
 * </tr>
 * <tr>
 * <td align="right">800k</td>
 * <td align="right">7600k</td>
 * <td align="right">4065k</td>
 * </tr>
 * <tr>
 * <td align="right">900k</td>
 * <td align="right">8500k</td>
 * <td align="right">4565k</td>
 * </tr>
 * </table>
 *
 * <p>
 * For decompression <tt>BZip2CompressorInputStream</tt> allocates less memory if the bzipped input is smaller than one
 * block.
 * </p>
 *
 * <p>
 * Instances of this class are not threadsafe.
 * </p>
 *
 * <p>
 * TODO: Update to BZip2 1.0.1
 * </p>
 *
 * @NotThreadSafe
 */
public class BZip2CompressorOutputStream extends CompressorOutputStream implements BZip2Constants {

    /**
     * The minimum supported blocksize <tt> == 1</tt>.
     */
    public static final int MIN_BLOCKSIZE = 1;

    /**
     * The maximum supported blocksize <tt> == 9</tt>.
     */
    public static final int MAX_BLOCKSIZE = 9;

    private static final int SETMASK = (1 << 21);
    private static final int CLEARMASK = (~SETMASK);
    private static final int GREATER_ICOST = 15;
    private static final int LESSER_ICOST = 0;
    private static final int SMALL_THRESH = 20;
    private static final int DEPTH_THRESH = 10;
    private static final int WORK_FACTOR = 30;

    /*
     * <p> If you are ever unlucky/improbable enough to get a stack overflow whilst sorting, increase the following
     * constant and try again. In practice I have never seen the stack go above 27 elems, so the following limit seems
     * very generous. </p>
     */
    private static final int QSORT_STACK_SIZE = 1000;

    /**
     * Knuth's increments seem to work better than Incerpi-Sedgewick here. Possibly because the number of elems to sort
     * is usually small, typically &lt;= 20.
     */
    private static final int[] INCS = { 1, 4, 13, 40, 121, 364, 1093, 3280, 9841, 29524, 88573, 265720, 797161,
            2391484 };

    private static void hbMakeCodeLengths(final byte[] len, final int[] freq, final Data dat, final int alphaSize,
            final int maxLen) {
        /*
         * Nodes and heap entries run from 1. Entry 0 for both the heap and nodes is a sentinel.
         */
        final int[] heap = dat.heap;
        final int[] weight = dat.weight;
        final int[] parent = dat.parent;

        for (int i = alphaSize; --i >= 0;) {
            weight[i + 1] = (freq[i] == 0 ? 1 : freq[i]) << 8;
        }

        for (boolean tooLong = true; tooLong;) {
            tooLong = false;

            int nNodes = alphaSize;
            int nHeap = 0;
            heap[0] = 0;
            weight[0] = 0;
            parent[0] = -2;

            for (int i = 1; i <= alphaSize; i++) {
                parent[i] = -1;
                nHeap++;
                heap[nHeap] = i;

                int zz = nHeap;
                int tmp = heap[zz];
                while (weight[tmp] < weight[heap[zz >> 1]]) {
                    heap[zz] = heap[zz >> 1];
                    zz >>= 1;
                }
                heap[zz] = tmp;
            }

            while (nHeap > 1) {
                int n1 = heap[1];
                heap[1] = heap[nHeap];
                nHeap--;

                int yy = 0;
                int zz = 1;
                int tmp = heap[1];

                while (true) {
                    yy = zz << 1;

                    if (yy > nHeap) {
                        break;
                    }

                    if ((yy < nHeap) && (weight[heap[yy + 1]] < weight[heap[yy]])) {
                        yy++;
                    }

                    if (weight[tmp] < weight[heap[yy]]) {
                        break;
                    }

                    heap[zz] = heap[yy];
                    zz = yy;
                }

                heap[zz] = tmp;

                int n2 = heap[1];
                heap[1] = heap[nHeap];
                nHeap--;

                yy = 0;
                zz = 1;
                tmp = heap[1];

                while (true) {
                    yy = zz << 1;

                    if (yy > nHeap) {
                        break;
                    }

                    if ((yy < nHeap) && (weight[heap[yy + 1]] < weight[heap[yy]])) {
                        yy++;
                    }

                    if (weight[tmp] < weight[heap[yy]]) {
                        break;
                    }

                    heap[zz] = heap[yy];
                    zz = yy;
                }

                heap[zz] = tmp;
                nNodes++;
                parent[n1] = parent[n2] = nNodes;

                final int weight_n1 = weight[n1];
                final int weight_n2 = weight[n2];
                weight[nNodes] = ((weight_n1 & 0xffffff00) + (weight_n2 & 0xffffff00))
                        | (1 + (((weight_n1 & 0x000000ff) > (weight_n2 & 0x000000ff)) ? (weight_n1 & 0x000000ff)
                                : (weight_n2 & 0x000000ff)));

                parent[nNodes] = -1;
                nHeap++;
                heap[nHeap] = nNodes;

                tmp = 0;
                zz = nHeap;
                tmp = heap[zz];
                final int weight_tmp = weight[tmp];
                while (weight_tmp < weight[heap[zz >> 1]]) {
                    heap[zz] = heap[zz >> 1];
                    zz >>= 1;
                }
                heap[zz] = tmp;

            }

            for (int i = 1; i <= alphaSize; i++) {
                int j = 0;
                int k = i;

                for (int parent_k; (parent_k = parent[k]) >= 0;) {
                    k = parent_k;
                    j++;
                }

                len[i - 1] = (byte) j;
                if (j > maxLen) {
                    tooLong = true;
                }
            }

            if (tooLong) {
                for (int i = 1; i < alphaSize; i++) {
                    int j = weight[i] >> 8;
                    j = 1 + (j >> 1);
                    weight[i] = j << 8;
                }
            }
        }
    }

    /**
     * Index of the last char in the block, so the block size == last + 1.
     */
    private int last;

    /**
     * Index in fmap[] of original string after sorting.
     */
    private int origPtr;

    /**
     * Always: in the range 0 .. 9. The current block size is 100000 * this number.
     */
    private final int blockSize100k;

    private boolean blockRandomised;

    private int bsBuff;
    private int bsLive;
    private final CRC crc = new CRC();

    private int nInUse;

    private int nMTF;

    /*
     * Used when sorting. If too many long comparisons happen, we stop sorting, randomise the block slightly, and try
     * again.
     */
    private int workDone;
    private int workLimit;
    private boolean firstAttempt;

    private int currentChar = -1;
    private int runLength = 0;

    private int blockCRC;
    private int combinedCRC;
    private int allowableBlockSize;

    /**
     * All memory intensive stuff.
     */
    private Data data;

    private OutputStream out;

    /**
     * Chooses a blocksize based on the given length of the data to compress.
     *
     * @return The blocksize, between {@link #MIN_BLOCKSIZE} and {@link #MAX_BLOCKSIZE} both inclusive. For a negative
     *         <tt>inputLength</tt> this method returns <tt>MAX_BLOCKSIZE</tt> always.
     *
     * @param inputLength
     *            The length of the data which will be compressed by <tt>CBZip2OutputStream</tt>.
     */
    public static int chooseBlockSize(long inputLength) {
        return (inputLength > 0) ? (int) Math.min((inputLength / 132000) + 1, 9) : MAX_BLOCKSIZE;
    }

    /**
     * Constructs a new <tt>CBZip2OutputStream</tt> with a blocksize of 900k.
     *
     * @param out
     *            the destination stream.
     *
     * @throws IOException
     *             if an I/O error occurs in the specified stream.
     * @throws NullPointerException
     *             if <code>out == null</code>.
     */
    public BZip2CompressorOutputStream(final OutputStream out) throws IOException {
        this(out, MAX_BLOCKSIZE);
    }

    /**
     * Constructs a new <tt>CBZip2OutputStream</tt> with specified blocksize.
     *
     * @param out
     *            the destination stream.
     * @param blockSize
     *            the blockSize as 100k units.
     *
     * @throws IOException
     *             if an I/O error occurs in the specified stream.
     * @throws IllegalArgumentException
     *             if <code>(blockSize < 1) || (blockSize > 9)</code>.
     * @throws NullPointerException
     *             if <code>out == null</code>.
     *
     * @see #MIN_BLOCKSIZE
     * @see #MAX_BLOCKSIZE
     */
    public BZip2CompressorOutputStream(final OutputStream out, final int blockSize) throws IOException {
        super();

        if (blockSize < 1) {
            throw new IllegalArgumentException("blockSize(" + blockSize + ") < 1");
        }
        if (blockSize > 9) {
            throw new IllegalArgumentException("blockSize(" + blockSize + ") > 9");
        }

        this.blockSize100k = blockSize;
        this.out = out;
        init();
    }

    /** {@inheritDoc} */
    @Override
    public void write(final int b) throws IOException {
        if (this.out != null) {
            write0(b);
        } else {
            throw new IOException("closed");
        }
    }

    private void writeRun() throws IOException {
        final int lastShadow = this.last;

        if (lastShadow < this.allowableBlockSize) {
            final int currentCharShadow = this.currentChar;
            final Data dataShadow = this.data;
            // dataShadow.inUse[currentCharShadow] = true;
            setInUseFlag(currentCharShadow);
            final byte ch = (byte) currentCharShadow;

            int runLengthShadow = this.runLength;
            this.crc.updateCRC(currentCharShadow, runLengthShadow);

            switch (runLengthShadow) {
            case 1:
                dataShadow.block[lastShadow + 2] = ch;
                this.last = lastShadow + 1;
                break;

            case 2:
                dataShadow.block[lastShadow + 2] = ch;
                dataShadow.block[lastShadow + 3] = ch;
                this.last = lastShadow + 2;
                break;

            case 3: {
                final byte[] block = dataShadow.block;
                block[lastShadow + 2] = ch;
                block[lastShadow + 3] = ch;
                block[lastShadow + 4] = ch;
                this.last = lastShadow + 3;
            }
                break;

            default: {
                runLengthShadow -= 4;
                dataShadow.inUse[runLengthShadow] = true;
                final byte[] block = dataShadow.block;
                block[lastShadow + 2] = ch;
                block[lastShadow + 3] = ch;
                block[lastShadow + 4] = ch;
                block[lastShadow + 5] = ch;
                block[lastShadow + 6] = (byte) runLengthShadow;
                this.last = lastShadow + 5;
            }
                break;

            }
        } else {
            endBlock();
            initBlock();
            writeRun();
        }
    }

    /**
     * Overriden to close the stream.
     */
    @Override
    protected void finalize() throws Throwable {
        finish();
        super.finalize();
    }

    public void finish() throws IOException {
        if (out != null) {
            try {
                if (this.runLength > 0) {
                    writeRun();
                }
                this.currentChar = -1;
                endBlock();
                endCompression();
            } finally {
                this.out = null;
                this.data = null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            OutputStream outShadow = this.out;
            finish();
            outShadow.close();
        }
    }

    @Override
    public void flush() throws IOException {
        OutputStream outShadow = this.out;
        if (outShadow != null) {
            outShadow.flush();
        }
    }

    /**
     * Writes magic bytes like BZ on the first position of the stream and bytes indiciating the file-format, which is
     * huffmanised, followed by a digit indicating blockSize100k.
     *
     * @throws IOException
     *             if the magic bytes could not been written
     */
    private void init() throws IOException {
        bsPutUByte('B');
        bsPutUByte('Z');

        this.data = new Data(this.blockSize100k);

        // huffmanised magic bytes
        bsPutUByte('h');
        bsPutUByte('0' + this.blockSize100k);

        this.combinedCRC = 0;
        initBlock();
    }

    private void initBlock() {
        // blockNo++;
        this.crc.initialiseCRC();
        this.last = -1;
        // ch = 0;

        boolean[] inUse = this.data.inUse;
        for (int i = 256; --i >= 0;) {
            inUse[i] = false;
        }

        /* 20 is just a paranoia constant */
        this.allowableBlockSize = (this.blockSize100k * BZip2Constants.BASEBLOCKSIZE) - 20;
    }

    private void endBlock() throws IOException {
        this.blockCRC = this.crc.getFinalCRC();
        this.combinedCRC = (this.combinedCRC << 1) | (this.combinedCRC >>> 31);
        this.combinedCRC ^= this.blockCRC;

        // empty block at end of file
        if (this.last == -1) {
            return;
        }

        /* sort the block and establish posn of original string */
        blockSort();

        /*
         * A 6-byte block header, the value chosen arbitrarily as 0x314159265359 :-). A 32 bit value does not really
         * give a strong enough guarantee that the value will not appear by chance in the compressed datastream.
         * Worst-case probability of this event, for a 900k block, is about 2.0e-3 for 32 bits, 1.0e-5 for 40 bits and
         * 4.0e-8 for 48 bits. For a compressed file of size 100Gb -- about 100000 blocks -- only a 48-bit marker will
         * do. NB: normal compression/ decompression donot rely on these statistical properties. They are only important
         * when trying to recover blocks from damaged files.
         */
        bsPutUByte(0x31);
        bsPutUByte(0x41);
        bsPutUByte(0x59);
        bsPutUByte(0x26);
        bsPutUByte(0x53);
        bsPutUByte(0x59);

        /* Now the block's CRC, so it is in a known place. */
        bsPutInt(this.blockCRC);

        /* Now a single bit indicating randomisation. */
        if (this.blockRandomised) {
            bsW(1, 1);
        } else {
            bsW(1, 0);
        }

        /* Finally, block's contents proper. */
        moveToFrontCodeAndSend();
    }

    private void endCompression() throws IOException {
        /*
         * Now another magic 48-bit number, 0x177245385090, to indicate the end of the last block. (sqrt(pi), if you
         * want to know. I did want to use e, but it contains too much repetition -- 27 18 28 18 28 46 -- for me to feel
         * statistically comfortable. Call me paranoid.)
         */
        bsPutUByte(0x17);
        bsPutUByte(0x72);
        bsPutUByte(0x45);
        bsPutUByte(0x38);
        bsPutUByte(0x50);
        bsPutUByte(0x90);

        bsPutInt(this.combinedCRC);
        bsFinishedWithStream();
    }

    /**
     * Returns the blocksize parameter specified at construction time.
     */
    public final int getBlockSize() {
        return this.blockSize100k;
    }

    @Override
    public void write(final byte[] buf, int offs, final int len) throws IOException {
        if (offs < 0) {
            throw new IndexOutOfBoundsException("offs(" + offs + ") < 0.");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("len(" + len + ") < 0.");
        }
        if (offs + len > buf.length) {
            throw new IndexOutOfBoundsException(
                    "offs(" + offs + ") + len(" + len + ") > buf.length(" + buf.length + ").");
        }
        if (this.out == null) {
            throw new IOException("stream closed");
        }

        for (int hi = offs + len; offs < hi;) {
            write0(buf[offs++]);
        }
    }

    private void write0(int b) throws IOException {
        if (this.currentChar != -1) {
            b &= 0xff;
            if (this.currentChar == b) {
                if (++this.runLength > 254) {
                    writeRun();
                    this.currentChar = -1;
                    this.runLength = 0;
                }
                // else nothing to do
            } else {
                writeRun();
                this.runLength = 1;
                this.currentChar = b;
            }
        } else {
            this.currentChar = b & 0xff;
            this.runLength++;
        }
    }

    private static void hbAssignCodes(final int[] code, final byte[] length, final int minLen, final int maxLen,
            final int alphaSize) {
        int vec = 0;
        for (int n = minLen; n <= maxLen; n++) {
            for (int i = 0; i < alphaSize; i++) {
                if ((length[i] & 0xff) == n) {
                    code[i] = vec;
                    vec++;
                }
            }
            vec <<= 1;
        }
    }

    private void bsFinishedWithStream() throws IOException {
        while (this.bsLive > 0) {
            int ch = this.bsBuff >> 24;
            this.out.write(ch); // write 8-bit
            this.bsBuff <<= 8;
            this.bsLive -= 8;
        }
    }

    private void bsW(final int n, final int v) throws IOException {
        final OutputStream outShadow = this.out;
        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        while (bsLiveShadow >= 8) {
            outShadow.write(bsBuffShadow >> 24); // write 8-bit
            bsBuffShadow <<= 8;
            bsLiveShadow -= 8;
        }

        this.bsBuff = bsBuffShadow | (v << (32 - bsLiveShadow - n));
        this.bsLive = bsLiveShadow + n;
    }

    private void bsPutUByte(final int c) throws IOException {
        bsW(8, c);
    }

    private void bsPutInt(final int u) throws IOException {
        bsW(8, (u >> 24) & 0xff);
        bsW(8, (u >> 16) & 0xff);
        bsW(8, (u >> 8) & 0xff);
        bsW(8, u & 0xff);
    }

    private void sendMTFValues() throws IOException {
        final byte[][] len = this.data.sendMTFValues_len;
        final int alphaSize = this.nInUse + 2;

        for (int t = N_GROUPS; --t >= 0;) {
            byte[] len_t = len[t];
            for (int v = alphaSize; --v >= 0;) {
                len_t[v] = GREATER_ICOST;
            }
        }

        /* Decide how many coding tables to use */
        // assert (this.nMTF > 0) : this.nMTF;
        final int nGroups = (this.nMTF < 200) ? 2
                : (this.nMTF < 600) ? 3 : (this.nMTF < 1200) ? 4 : (this.nMTF < 2400) ? 5 : 6;

        /* Generate an initial set of coding tables */
        sendMTFValues0(nGroups, alphaSize);

        /*
         * Iterate up to N_ITERS times to improve the tables.
         */
        final int nSelectors = sendMTFValues1(nGroups, alphaSize);

        /* Compute MTF values for the selectors. */
        sendMTFValues2(nGroups, nSelectors);

        /* Assign actual codes for the tables. */
        sendMTFValues3(nGroups, alphaSize);

        /* Transmit the mapping table. */
        sendMTFValues4();

        /* Now the selectors. */
        sendMTFValues5(nGroups, nSelectors);

        /* Now the coding tables. */
        sendMTFValues6(nGroups, alphaSize);

        /* And finally, the block data proper */
        sendMTFValues7();
    }

    private void sendMTFValues0(final int nGroups, final int alphaSize) {
        final byte[][] len = this.data.sendMTFValues_len;
        final int[] mtfFreq = this.data.mtfFreq;

        int remF = this.nMTF;
        int gs = 0;

        for (int nPart = nGroups; nPart > 0; nPart--) {
            final int tFreq = remF / nPart;
            int ge = gs - 1;
            int aFreq = 0;

            for (final int a = alphaSize - 1; (aFreq < tFreq) && (ge < a);) {
                aFreq += mtfFreq[++ge];
            }

            if ((ge > gs) && (nPart != nGroups) && (nPart != 1) && (((nGroups - nPart) & 1) != 0)) {
                aFreq -= mtfFreq[ge--];
            }

            final byte[] len_np = len[nPart - 1];
            for (int v = alphaSize; --v >= 0;) {
                if ((v >= gs) && (v <= ge)) {
                    len_np[v] = LESSER_ICOST;
                } else {
                    len_np[v] = GREATER_ICOST;
                }
            }

            gs = ge + 1;
            remF -= aFreq;
        }
    }

    private int sendMTFValues1(final int nGroups, final int alphaSize) {
        final Data dataShadow = this.data;
        final int[][] rfreq = dataShadow.sendMTFValues_rfreq;
        final int[] fave = dataShadow.sendMTFValues_fave;
        final short[] cost = dataShadow.sendMTFValues_cost;
        final char[] sfmap = dataShadow.sfmap;
        final byte[] selector = dataShadow.selector;
        final byte[][] len = dataShadow.sendMTFValues_len;
        final byte[] len_0 = len[0];
        final byte[] len_1 = len[1];
        final byte[] len_2 = len[2];
        final byte[] len_3 = len[3];
        final byte[] len_4 = len[4];
        final byte[] len_5 = len[5];
        final int nMTFShadow = this.nMTF;

        int nSelectors = 0;

        for (int iter = 0; iter < N_ITERS; iter++) {
            for (int t = nGroups; --t >= 0;) {
                fave[t] = 0;
                int[] rfreqt = rfreq[t];
                for (int i = alphaSize; --i >= 0;) {
                    rfreqt[i] = 0;
                }
            }

            nSelectors = 0;

            for (int gs = 0; gs < this.nMTF;) {
                /* Set group start & end marks. */

                /*
                 * Calculate the cost of this group as coded by each of the coding tables.
                 */

                final int ge = Math.min(gs + G_SIZE - 1, nMTFShadow - 1);

                if (nGroups == N_GROUPS) {
                    // unrolled version of the else-block

                    short cost0 = 0;
                    short cost1 = 0;
                    short cost2 = 0;
                    short cost3 = 0;
                    short cost4 = 0;
                    short cost5 = 0;

                    for (int i = gs; i <= ge; i++) {
                        final int icv = sfmap[i];
                        cost0 += len_0[icv] & 0xff;
                        cost1 += len_1[icv] & 0xff;
                        cost2 += len_2[icv] & 0xff;
                        cost3 += len_3[icv] & 0xff;
                        cost4 += len_4[icv] & 0xff;
                        cost5 += len_5[icv] & 0xff;
                    }

                    cost[0] = cost0;
                    cost[1] = cost1;
                    cost[2] = cost2;
                    cost[3] = cost3;
                    cost[4] = cost4;
                    cost[5] = cost5;

                } else {
                    for (int t = nGroups; --t >= 0;) {
                        cost[t] = 0;
                    }

                    for (int i = gs; i <= ge; i++) {
                        final int icv = sfmap[i];
                        for (int t = nGroups; --t >= 0;) {
                            cost[t] += len[t][icv] & 0xff;
                        }
                    }
                }

                /*
                 * Find the coding table which is best for this group, and record its identity in the selector table.
                 */
                int bt = -1;
                for (int t = nGroups, bc = 999999999; --t >= 0;) {
                    final int cost_t = cost[t];
                    if (cost_t < bc) {
                        bc = cost_t;
                        bt = t;
                    }
                }

                fave[bt]++;
                selector[nSelectors] = (byte) bt;
                nSelectors++;

                /*
                 * Increment the symbol frequencies for the selected table.
                 */
                final int[] rfreq_bt = rfreq[bt];
                for (int i = gs; i <= ge; i++) {
                    rfreq_bt[sfmap[i]]++;
                }

                gs = ge + 1;
            }

            /*
             * Recompute the tables based on the accumulated frequencies.
             */
            for (int t = 0; t < nGroups; t++) {
                hbMakeCodeLengths(len[t], rfreq[t], this.data, alphaSize, 20);
            }
        }

        return nSelectors;
    }

    private void sendMTFValues2(final int nGroups, final int nSelectors) {
        // assert (nGroups < 8) : nGroups;

        final Data dataShadow = this.data;
        byte[] pos = dataShadow.sendMTFValues2_pos;

        for (int i = nGroups; --i >= 0;) {
            pos[i] = (byte) i;
        }

        for (int i = 0; i < nSelectors; i++) {
            final byte ll_i = dataShadow.selector[i];
            byte tmp = pos[0];
            int j = 0;

            while (ll_i != tmp) {
                j++;
                byte tmp2 = tmp;
                tmp = pos[j];
                pos[j] = tmp2;
            }

            pos[0] = tmp;
            dataShadow.selectorMtf[i] = (byte) j;
        }
    }

    private void sendMTFValues3(final int nGroups, final int alphaSize) {
        int[][] code = this.data.sendMTFValues_code;
        byte[][] len = this.data.sendMTFValues_len;

        for (int t = 0; t < nGroups; t++) {
            int minLen = 32;
            int maxLen = 0;
            final byte[] len_t = len[t];
            for (int i = alphaSize; --i >= 0;) {
                final int l = len_t[i] & 0xff;
                if (l > maxLen) {
                    maxLen = l;
                }
                if (l < minLen) {
                    minLen = l;
                }
            }

            // assert (maxLen <= 20) : maxLen;
            // assert (minLen >= 1) : minLen;

            hbAssignCodes(code[t], len[t], minLen, maxLen, alphaSize);
        }
    }

    private void sendMTFValues4() throws IOException {
        final boolean[] inUse = this.data.inUse;
        final boolean[] inUse16 = this.data.sentMTFValues4_inUse16;

        for (int i = 16; --i >= 0;) {
            inUse16[i] = false;
            final int i16 = i * 16;
            for (int j = 16; --j >= 0;) {
                if (inUse[i16 + j]) {
                    inUse16[i] = true;
                }
            }
        }

        for (int i = 0; i < 16; i++) {
            bsW(1, inUse16[i] ? 1 : 0);
        }

        final OutputStream outShadow = this.out;
        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int i = 0; i < 16; i++) {
            if (inUse16[i]) {
                final int i16 = i * 16;
                for (int j = 0; j < 16; j++) {
                    // inlined: bsW(1, inUse[i16 + j] ? 1 : 0);
                    while (bsLiveShadow >= 8) {
                        outShadow.write(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    if (inUse[i16 + j]) {
                        bsBuffShadow |= 1 << (32 - bsLiveShadow - 1);
                    }
                    bsLiveShadow++;
                }
            }
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues5(final int nGroups, final int nSelectors) throws IOException {
        bsW(3, nGroups);
        bsW(15, nSelectors);

        final OutputStream outShadow = this.out;
        final byte[] selectorMtf = this.data.selectorMtf;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int i = 0; i < nSelectors; i++) {
            for (int j = 0, hj = selectorMtf[i] & 0xff; j < hj; j++) {
                // inlined: bsW(1, 1);
                while (bsLiveShadow >= 8) {
                    outShadow.write(bsBuffShadow >> 24);
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                bsBuffShadow |= 1 << (32 - bsLiveShadow - 1);
                bsLiveShadow++;
            }

            // inlined: bsW(1, 0);
            while (bsLiveShadow >= 8) {
                outShadow.write(bsBuffShadow >> 24);
                bsBuffShadow <<= 8;
                bsLiveShadow -= 8;
            }
            // bsBuffShadow |= 0 << (32 - bsLiveShadow - 1);
            bsLiveShadow++;
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues6(final int nGroups, final int alphaSize) throws IOException {
        final byte[][] len = this.data.sendMTFValues_len;
        final OutputStream outShadow = this.out;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int t = 0; t < nGroups; t++) {
            byte[] len_t = len[t];
            int curr = len_t[0] & 0xff;

            // inlined: bsW(5, curr);
            while (bsLiveShadow >= 8) {
                outShadow.write(bsBuffShadow >> 24); // write 8-bit
                bsBuffShadow <<= 8;
                bsLiveShadow -= 8;
            }
            bsBuffShadow |= curr << (32 - bsLiveShadow - 5);
            bsLiveShadow += 5;

            for (int i = 0; i < alphaSize; i++) {
                int lti = len_t[i] & 0xff;
                while (curr < lti) {
                    // inlined: bsW(2, 2);
                    while (bsLiveShadow >= 8) {
                        outShadow.write(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    bsBuffShadow |= 2 << (32 - bsLiveShadow - 2);
                    bsLiveShadow += 2;

                    curr++; /* 10 */
                }

                while (curr > lti) {
                    // inlined: bsW(2, 3);
                    while (bsLiveShadow >= 8) {
                        outShadow.write(bsBuffShadow >> 24); // write 8-bit
                        bsBuffShadow <<= 8;
                        bsLiveShadow -= 8;
                    }
                    bsBuffShadow |= 3 << (32 - bsLiveShadow - 2);
                    bsLiveShadow += 2;

                    curr--; /* 11 */
                }

                // inlined: bsW(1, 0);
                while (bsLiveShadow >= 8) {
                    outShadow.write(bsBuffShadow >> 24); // write 8-bit
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                // bsBuffShadow |= 0 << (32 - bsLiveShadow - 1);
                bsLiveShadow++;
            }
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void sendMTFValues7() throws IOException {
        final Data dataShadow = this.data;
        final byte[][] len = dataShadow.sendMTFValues_len;
        final int[][] code = dataShadow.sendMTFValues_code;
        final OutputStream outShadow = this.out;
        final byte[] selector = dataShadow.selector;
        final char[] sfmap = dataShadow.sfmap;
        final int nMTFShadow = this.nMTF;

        int selCtr = 0;

        int bsLiveShadow = this.bsLive;
        int bsBuffShadow = this.bsBuff;

        for (int gs = 0; gs < nMTFShadow;) {
            final int ge = Math.min(gs + G_SIZE - 1, nMTFShadow - 1);
            final int selector_selCtr = selector[selCtr] & 0xff;
            final int[] code_selCtr = code[selector_selCtr];
            final byte[] len_selCtr = len[selector_selCtr];

            while (gs <= ge) {
                final int sfmap_i = sfmap[gs];

                //
                // inlined: bsW(len_selCtr[sfmap_i] & 0xff,
                // code_selCtr[sfmap_i]);
                //
                while (bsLiveShadow >= 8) {
                    outShadow.write(bsBuffShadow >> 24);
                    bsBuffShadow <<= 8;
                    bsLiveShadow -= 8;
                }
                final int n = len_selCtr[sfmap_i] & 0xFF;
                bsBuffShadow |= code_selCtr[sfmap_i] << (32 - bsLiveShadow - n);
                bsLiveShadow += n;

                gs++;
            }

            gs = ge + 1;
            selCtr++;
        }

        this.bsBuff = bsBuffShadow;
        this.bsLive = bsLiveShadow;
    }

    private void moveToFrontCodeAndSend() throws IOException {
        bsW(24, this.origPtr);
        generateMTFValues();
        sendMTFValues();
    }

    /**
     * This is the most hammered method of this class.
     *
     * <p>
     * This is the version using unrolled loops. Normally I never use such ones in Java code. The unrolling has shown a
     * noticable performance improvement on JRE 1.4.2 (Linux i586 / HotSpot Client). Of course it depends on the JIT
     * compiler of the vm.
     * </p>
     */
    private boolean mainSimpleSort(final Data dataShadow, final int lo, final int hi, final int d) {
        final int bigN = hi - lo + 1;
        if (bigN < 2) {
            return this.firstAttempt && (this.workDone > this.workLimit);
        }

        int hp = 0;
        while (INCS[hp] < bigN) {
            hp++;
        }

        final int[] fmap = dataShadow.fmap;
        final char[] quadrant = dataShadow.quadrant;
        final byte[] block = dataShadow.block;
        final int lastShadow = this.last;
        final int lastPlus1 = lastShadow + 1;
        final boolean firstAttemptShadow = this.firstAttempt;
        final int workLimitShadow = this.workLimit;
        int workDoneShadow = this.workDone;

        // Following block contains unrolled code which could be shortened by
        // coding it in additional loops.

        HP: while (--hp >= 0) {
            final int h = INCS[hp];
            final int mj = lo + h - 1;

            for (int i = lo + h; i <= hi;) {
                // copy
                for (int k = 3; (i <= hi) && (--k >= 0); i++) {
                    final int v = fmap[i];
                    final int vd = v + d;
                    int j = i;

                    // for (int a;
                    // (j > mj) && mainGtU((a = fmap[j - h]) + d, vd,
                    // block, quadrant, lastShadow);
                    // j -= h) {
                    // fmap[j] = a;
                    // }
                    //
                    // unrolled version:

                    // start inline mainGTU
                    boolean onceRunned = false;
                    int a = 0;

                    HAMMER: while (true) {
                        if (onceRunned) {
                            fmap[j] = a;
                            if ((j -= h) <= mj) {
                                break HAMMER;
                            }
                        } else {
                            onceRunned = true;
                        }

                        a = fmap[j - h];
                        int i1 = a + d;
                        int i2 = vd;

                        // following could be done in a loop, but
                        // unrolled it for performance:
                        if (block[i1 + 1] == block[i2 + 1]) {
                            if (block[i1 + 2] == block[i2 + 2]) {
                                if (block[i1 + 3] == block[i2 + 3]) {
                                    if (block[i1 + 4] == block[i2 + 4]) {
                                        if (block[i1 + 5] == block[i2 + 5]) {
                                            if (block[(i1 += 6)] == block[(i2 += 6)]) {
                                                int x = lastShadow;
                                                X: while (x > 0) {
                                                    x -= 4;

                                                    if (block[i1 + 1] == block[i2 + 1]) {
                                                        if (quadrant[i1] == quadrant[i2]) {
                                                            if (block[i1 + 2] == block[i2 + 2]) {
                                                                if (quadrant[i1 + 1] == quadrant[i2 + 1]) {
                                                                    if (block[i1 + 3] == block[i2 + 3]) {
                                                                        if (quadrant[i1 + 2] == quadrant[i2 + 2]) {
                                                                            if (block[i1 + 4] == block[i2 + 4]) {
                                                                                if (quadrant[i1 + 3] == quadrant[i2
                                                                                        + 3]) {
                                                                                    if ((i1 += 4) >= lastPlus1) {
                                                                                        i1 -= lastPlus1;
                                                                                    }
                                                                                    if ((i2 += 4) >= lastPlus1) {
                                                                                        i2 -= lastPlus1;
                                                                                    }
                                                                                    workDoneShadow++;
                                                                                    continue X;
                                                                                } else if ((quadrant[i1
                                                                                        + 3] > quadrant[i2 + 3])) {
                                                                                    continue HAMMER;
                                                                                } else {
                                                                                    break HAMMER;
                                                                                }
                                                                            } else if ((block[i1 + 4]
                                                                                    & 0xff) > (block[i2 + 4] & 0xff)) {
                                                                                continue HAMMER;
                                                                            } else {
                                                                                break HAMMER;
                                                                            }
                                                                        } else if ((quadrant[i1 + 2] > quadrant[i2
                                                                                + 2])) {
                                                                            continue HAMMER;
                                                                        } else {
                                                                            break HAMMER;
                                                                        }
                                                                    } else if ((block[i1 + 3] & 0xff) > (block[i2 + 3]
                                                                            & 0xff)) {
                                                                        continue HAMMER;
                                                                    } else {
                                                                        break HAMMER;
                                                                    }
                                                                } else if ((quadrant[i1 + 1] > quadrant[i2 + 1])) {
                                                                    continue HAMMER;
                                                                } else {
                                                                    break HAMMER;
                                                                }
                                                            } else if ((block[i1 + 2] & 0xff) > (block[i2 + 2]
                                                                    & 0xff)) {
                                                                continue HAMMER;
                                                            } else {
                                                                break HAMMER;
                                                            }
                                                        } else if ((quadrant[i1] > quadrant[i2])) {
                                                            continue HAMMER;
                                                        } else {
                                                            break HAMMER;
                                                        }
                                                    } else if ((block[i1 + 1] & 0xff) > (block[i2 + 1] & 0xff)) {
                                                        continue HAMMER;
                                                    } else {
                                                        break HAMMER;
                                                    }

                                                }
                                                break HAMMER;
                                            } // while x > 0
                                            else {
                                                if ((block[i1] & 0xff) > (block[i2] & 0xff)) {
                                                    continue HAMMER;
                                                } else {
                                                    break HAMMER;
                                                }
                                            }
                                        } else if ((block[i1 + 5] & 0xff) > (block[i2 + 5] & 0xff)) {
                                            continue HAMMER;
                                        } else {
                                            break HAMMER;
                                        }
                                    } else if ((block[i1 + 4] & 0xff) > (block[i2 + 4] & 0xff)) {
                                        continue HAMMER;
                                    } else {
                                        break HAMMER;
                                    }
                                } else if ((block[i1 + 3] & 0xff) > (block[i2 + 3] & 0xff)) {
                                    continue HAMMER;
                                } else {
                                    break HAMMER;
                                }
                            } else if ((block[i1 + 2] & 0xff) > (block[i2 + 2] & 0xff)) {
                                continue HAMMER;
                            } else {
                                break HAMMER;
                            }
                        } else if ((block[i1 + 1] & 0xff) > (block[i2 + 1] & 0xff)) {
                            continue HAMMER;
                        } else {
                            break HAMMER;
                        }

                    } // HAMMER
                      // end inline mainGTU

                    fmap[j] = v;
                }

                if (firstAttemptShadow && (i <= hi) && (workDoneShadow > workLimitShadow)) {
                    break HP;
                }
            }
        }

        this.workDone = workDoneShadow;
        return firstAttemptShadow && (workDoneShadow > workLimitShadow);
    }

    private static void vswap(int[] fmap, int p1, int p2, int n) {
        n += p1;
        while (p1 < n) {
            int t = fmap[p1];
            fmap[p1++] = fmap[p2];
            fmap[p2++] = t;
        }
    }

    private static byte med3(byte a, byte b, byte c) {
        return (a < b) ? (b < c ? b : a < c ? c : a) : (b > c ? b : a > c ? c : a);
    }

    private void blockSort() {
        this.workLimit = WORK_FACTOR * this.last;
        this.workDone = 0;
        this.blockRandomised = false;
        this.firstAttempt = true;
        mainSort();

        if (this.firstAttempt && (this.workDone > this.workLimit)) {
            randomiseBlock();
            this.workLimit = this.workDone = 0;
            this.firstAttempt = false;
            mainSort();
        }

        int[] fmap = this.data.fmap;
        this.origPtr = -1;
        for (int i = 0, lastShadow = this.last; i <= lastShadow; i++) {
            if (fmap[i] == 0) {
                this.origPtr = i;
                break;
            }
        }

        // assert (this.origPtr != -1) : this.origPtr;
    }

    /**
     * Method "mainQSort3", file "blocksort.c", BZip2 1.0.2
     */
    private void mainQSort3(final Data dataShadow, final int loSt, final int hiSt, final int dSt) {
        final int[] stack_ll = dataShadow.stack_ll;
        final int[] stack_hh = dataShadow.stack_hh;
        final int[] stack_dd = dataShadow.stack_dd;
        final int[] fmap = dataShadow.fmap;
        final byte[] block = dataShadow.block;

        stack_ll[0] = loSt;
        stack_hh[0] = hiSt;
        stack_dd[0] = dSt;

        for (int sp = 1; --sp >= 0;) {
            final int lo = stack_ll[sp];
            final int hi = stack_hh[sp];
            final int d = stack_dd[sp];

            if ((hi - lo < SMALL_THRESH) || (d > DEPTH_THRESH)) {
                if (mainSimpleSort(dataShadow, lo, hi, d)) {
                    return;
                }
            } else {
                final int d1 = d + 1;
                final int med = med3(block[fmap[lo] + d1], block[fmap[hi] + d1], block[fmap[(lo + hi) >>> 1] + d1])
                        & 0xff;

                int unLo = lo;
                int unHi = hi;
                int ltLo = lo;
                int gtHi = hi;

                while (true) {
                    while (unLo <= unHi) {
                        final int n = (block[fmap[unLo] + d1] & 0xff) - med;
                        if (n == 0) {
                            final int temp = fmap[unLo];
                            fmap[unLo++] = fmap[ltLo];
                            fmap[ltLo++] = temp;
                        } else if (n < 0) {
                            unLo++;
                        } else {
                            break;
                        }
                    }

                    while (unLo <= unHi) {
                        final int n = (block[fmap[unHi] + d1] & 0xff) - med;
                        if (n == 0) {
                            final int temp = fmap[unHi];
                            fmap[unHi--] = fmap[gtHi];
                            fmap[gtHi--] = temp;
                        } else if (n > 0) {
                            unHi--;
                        } else {
                            break;
                        }
                    }

                    if (unLo <= unHi) {
                        final int temp = fmap[unLo];
                        fmap[unLo++] = fmap[unHi];
                        fmap[unHi--] = temp;
                    } else {
                        break;
                    }
                }

                if (gtHi < ltLo) {
                    stack_ll[sp] = lo;
                    stack_hh[sp] = hi;
                    stack_dd[sp] = d1;
                    sp++;
                } else {
                    int n = ((ltLo - lo) < (unLo - ltLo)) ? (ltLo - lo) : (unLo - ltLo);
                    vswap(fmap, lo, unLo - n, n);
                    int m = ((hi - gtHi) < (gtHi - unHi)) ? (hi - gtHi) : (gtHi - unHi);
                    vswap(fmap, unLo, hi - m + 1, m);

                    n = lo + unLo - ltLo - 1;
                    m = hi - (gtHi - unHi) + 1;

                    stack_ll[sp] = lo;
                    stack_hh[sp] = n;
                    stack_dd[sp] = d;
                    sp++;

                    stack_ll[sp] = n + 1;
                    stack_hh[sp] = m - 1;
                    stack_dd[sp] = d1;
                    sp++;

                    stack_ll[sp] = m;
                    stack_hh[sp] = hi;
                    stack_dd[sp] = d;
                    sp++;
                }
            }
        }
    }

    private void mainSort() {
        final Data dataShadow = this.data;
        final int[] runningOrder = dataShadow.mainSort_runningOrder;
        final int[] copy = dataShadow.mainSort_copy;
        final boolean[] bigDone = dataShadow.mainSort_bigDone;
        final int[] ftab = dataShadow.ftab;
        final byte[] block = dataShadow.block;
        final int[] fmap = dataShadow.fmap;
        final char[] quadrant = dataShadow.quadrant;
        final int lastShadow = this.last;
        final int workLimitShadow = this.workLimit;
        final boolean firstAttemptShadow = this.firstAttempt;

        // Set up the 2-byte frequency table
        for (int i = 65537; --i >= 0;) {
            ftab[i] = 0;
        }

        /*
         * In the various block-sized structures, live data runs from 0 to last+NUM_OVERSHOOT_BYTES inclusive. First,
         * set up the overshoot area for block.
         */
        for (int i = 0; i < NUM_OVERSHOOT_BYTES; i++) {
            block[lastShadow + i + 2] = block[(i % (lastShadow + 1)) + 1];
        }
        for (int i = lastShadow + NUM_OVERSHOOT_BYTES + 1; --i >= 0;) {
            quadrant[i] = 0;
        }
        block[0] = block[lastShadow + 1];

        // Complete the initial radix sort:

        int c1 = block[0] & 0xff;
        for (int i = 0; i <= lastShadow; i++) {
            final int c2 = block[i + 1] & 0xff;
            ftab[(c1 << 8) + c2]++;
            c1 = c2;
        }

        for (int i = 1; i <= 65536; i++) {
            ftab[i] += ftab[i - 1];
        }

        c1 = block[1] & 0xff;
        for (int i = 0; i < lastShadow; i++) {
            final int c2 = block[i + 2] & 0xff;
            fmap[--ftab[(c1 << 8) + c2]] = i;
            c1 = c2;
        }

        fmap[--ftab[((block[lastShadow + 1] & 0xff) << 8) + (block[1] & 0xff)]] = lastShadow;

        /*
         * Now ftab contains the first loc of every small bucket. Calculate the running order, from smallest to largest
         * big bucket.
         */
        for (int i = 256; --i >= 0;) {
            bigDone[i] = false;
            runningOrder[i] = i;
        }

        for (int h = 364; h != 1;) {
            h /= 3;
            for (int i = h; i <= 255; i++) {
                final int vv = runningOrder[i];
                final int a = ftab[(vv + 1) << 8] - ftab[vv << 8];
                final int b = h - 1;
                int j = i;
                for (int ro = runningOrder[j - h]; (ftab[(ro + 1) << 8] - ftab[ro << 8]) > a; ro = runningOrder[j
                        - h]) {
                    runningOrder[j] = ro;
                    j -= h;
                    if (j <= b) {
                        break;
                    }
                }
                runningOrder[j] = vv;
            }
        }

        /*
         * The main sorting loop.
         */
        for (int i = 0; i <= 255; i++) {
            /*
             * Process big buckets, starting with the least full.
             */
            final int ss = runningOrder[i];

            // Step 1:
            /*
             * Complete the big bucket [ss] by quicksorting any unsorted small buckets [ss, j]. Hopefully previous
             * pointer-scanning phases have already completed many of the small buckets [ss, j], so we don't have to
             * sort them at all.
             */
            for (int j = 0; j <= 255; j++) {
                final int sb = (ss << 8) + j;
                final int ftab_sb = ftab[sb];
                if ((ftab_sb & SETMASK) != SETMASK) {
                    final int lo = ftab_sb & CLEARMASK;
                    final int hi = (ftab[sb + 1] & CLEARMASK) - 1;
                    if (hi > lo) {
                        mainQSort3(dataShadow, lo, hi, 2);
                        if (firstAttemptShadow && (this.workDone > workLimitShadow)) {
                            return;
                        }
                    }
                    ftab[sb] = ftab_sb | SETMASK;
                }
            }

            // Step 2:
            // Now scan this big bucket so as to synthesise the
            // sorted order for small buckets [t, ss] for all t != ss.

            for (int j = 0; j <= 255; j++) {
                copy[j] = ftab[(j << 8) + ss] & CLEARMASK;
            }

            for (int j = ftab[ss << 8] & CLEARMASK, hj = (ftab[(ss + 1) << 8] & CLEARMASK); j < hj; j++) {
                final int fmap_j = fmap[j];
                c1 = block[fmap_j] & 0xff;
                if (!bigDone[c1]) {
                    fmap[copy[c1]] = (fmap_j == 0) ? lastShadow : (fmap_j - 1);
                    copy[c1]++;
                }
            }

            for (int j = 256; --j >= 0;) {
                ftab[(j << 8) + ss] |= SETMASK;
            }

            // Step 3:
            /*
             * The ss big bucket is now done. Record this fact, and update the quadrant descriptors. Remember to update
             * quadrants in the overshoot area too, if necessary. The "if (i < 255)" test merely skips this updating for
             * the last bucket processed, since updating for the last bucket is pointless.
             */
            bigDone[ss] = true;

            if (i < 255) {
                final int bbStart = ftab[ss << 8] & CLEARMASK;
                final int bbSize = (ftab[(ss + 1) << 8] & CLEARMASK) - bbStart;
                int shifts = 0;

                while ((bbSize >> shifts) > 65534) {
                    shifts++;
                }

                for (int j = 0; j < bbSize; j++) {
                    final int a2update = fmap[bbStart + j];
                    final char qVal = (char) (j >> shifts);
                    quadrant[a2update] = qVal;
                    if (a2update < NUM_OVERSHOOT_BYTES) {
                        quadrant[a2update + lastShadow + 1] = qVal;
                    }
                }
            }

        }
    }

    private void randomiseBlock() {
        final boolean[] inUse = this.data.inUse;
        final byte[] block = this.data.block;
        final int lastShadow = this.last;

        for (int i = 256; --i >= 0;) {
            inUse[i] = false;
        }

        int rNToGo = 0;
        int rTPos = 0;
        for (int i = 0, j = 1; i <= lastShadow; i = j, j++) {
            if (rNToGo == 0) {
                rNToGo = (char) Rand.rNums(rTPos);
                if (++rTPos == 512) {
                    rTPos = 0;
                }
            }

            rNToGo--;
            block[j] ^= ((rNToGo == 1) ? 1 : 0);

            // handle 16 bit signed numbers
            inUse[block[j] & 0xff] = true;
        }

        this.blockRandomised = true;
    }

    private void generateMTFValues() {
        final int lastShadow = this.last;
        final Data dataShadow = this.data;
        final boolean[] inUse = dataShadow.inUse;
        final byte[] block = dataShadow.block;
        final int[] fmap = dataShadow.fmap;
        final char[] sfmap = dataShadow.sfmap;
        final int[] mtfFreq = dataShadow.mtfFreq;
        final byte[] unseqToSeq = dataShadow.unseqToSeq;
        final byte[] yy = dataShadow.generateMTFValues_yy;

        // make maps
        int nInUseShadow = 0;
        for (int i = 0; i < 256; i++) {
            if (getInUse(i)) {
                unseqToSeq[i] = (byte) nInUseShadow;
                nInUseShadow++;
            }
        }
        this.nInUse = nInUseShadow;

        final int eob = nInUseShadow + 1;

        for (int i = eob; i >= 0; i--) {
            mtfFreq[i] = 0;
        }

        for (int i = nInUseShadow; --i >= 0;) {
            yy[i] = (byte) i;
        }

        int wr = 0;
        int zPend = 0;

        for (int i = 0; i <= lastShadow; i++) {
            final byte ll_i = unseqToSeq[block[fmap[i]] & 0xff];
            byte tmp = yy[0];
            int j = 0;

            while (ll_i != tmp) {
                j++;
                byte tmp2 = tmp;
                tmp = yy[j];
                yy[j] = tmp2;
            }
            yy[0] = tmp;

            if (j == 0) {
                zPend++;
            } else {
                if (zPend > 0) {
                    zPend--;
                    while (true) {
                        if ((zPend & 1) == 0) {
                            sfmap[wr] = RUNA;
                            wr++;
                            mtfFreq[RUNA]++;
                        } else {
                            sfmap[wr] = RUNB;
                            wr++;
                            mtfFreq[RUNB]++;
                        }

                        if (zPend >= 2) {
                            zPend = (zPend - 2) >> 1;
                        } else {
                            break;
                        }
                    }
                    zPend = 0;
                }
                sfmap[wr] = (char) (j + 1);
                wr++;
                mtfFreq[j + 1]++;
            }
        }

        if (zPend > 0) {
            zPend--;
            while (true) {
                if ((zPend & 1) == 0) {
                    sfmap[wr] = RUNA;
                    wr++;
                    mtfFreq[RUNA]++;
                } else {
                    sfmap[wr] = RUNB;
                    wr++;
                    mtfFreq[RUNB]++;
                }

                if (zPend >= 2) {
                    zPend = (zPend - 2) >> 1;
                } else {
                    break;
                }
            }
        }

        sfmap[wr] = (char) eob;
        mtfFreq[eob]++;
        this.nMTF = wr + 1;
    }

    private boolean getInUse(int index) { // TODO
        switch (index) {
        case 0:
            return this.data.inUse[0];
        case 1:
            return this.data.inUse[1];
        case 2:
            return this.data.inUse[2];
        case 3:
            return this.data.inUse[3];
        case 4:
            return this.data.inUse[4];
        case 5:
            return this.data.inUse[5];
        case 6:
            return this.data.inUse[6];
        case 7:
            return this.data.inUse[7];
        case 8:
            return this.data.inUse[8];
        case 9:
            return this.data.inUse[9];
        case 10:
            return this.data.inUse[10];
        case 11:
            return this.data.inUse[11];
        case 12:
            return this.data.inUse[12];
        case 13:
            return this.data.inUse[13];
        case 14:
            return this.data.inUse[14];
        case 15:
            return this.data.inUse[15];
        case 16:
            return this.data.inUse[16];
        case 17:
            return this.data.inUse[17];
        case 18:
            return this.data.inUse[18];
        case 19:
            return this.data.inUse[19];
        case 20:
            return this.data.inUse[20];
        case 21:
            return this.data.inUse[21];
        case 22:
            return this.data.inUse[22];
        case 23:
            return this.data.inUse[23];
        case 24:
            return this.data.inUse[24];
        case 25:
            return this.data.inUse[25];
        case 26:
            return this.data.inUse[26];
        case 27:
            return this.data.inUse[27];
        case 28:
            return this.data.inUse[28];
        case 29:
            return this.data.inUse[29];
        case 30:
            return this.data.inUse[30];
        case 31:
            return this.data.inUse[31];
        case 32:
            return this.data.inUse[32];
        case 33:
            return this.data.inUse[33];
        case 34:
            return this.data.inUse[34];
        case 35:
            return this.data.inUse[35];
        case 36:
            return this.data.inUse[36];
        case 37:
            return this.data.inUse[37];
        case 38:
            return this.data.inUse[38];
        case 39:
            return this.data.inUse[39];
        case 40:
            return this.data.inUse[40];
        case 41:
            return this.data.inUse[41];
        case 42:
            return this.data.inUse[42];
        case 43:
            return this.data.inUse[43];
        case 44:
            return this.data.inUse[44];
        case 45:
            return this.data.inUse[45];
        case 46:
            return this.data.inUse[46];
        case 47:
            return this.data.inUse[47];
        case 48:
            return this.data.inUse[48];
        case 49:
            return this.data.inUse[49];
        case 50:
            return this.data.inUse[50];
        case 51:
            return this.data.inUse[51];
        case 52:
            return this.data.inUse[52];
        case 53:
            return this.data.inUse[53];
        case 54:
            return this.data.inUse[54];
        case 55:
            return this.data.inUse[55];
        case 56:
            return this.data.inUse[56];
        case 57:
            return this.data.inUse[57];
        case 58:
            return this.data.inUse[58];
        case 59:
            return this.data.inUse[59];
        case 60:
            return this.data.inUse[60];
        case 61:
            return this.data.inUse[61];
        case 62:
            return this.data.inUse[62];
        case 63:
            return this.data.inUse[63];
        case 64:
            return this.data.inUse[64];
        case 65:
            return this.data.inUse[65];
        case 66:
            return this.data.inUse[66];
        case 67:
            return this.data.inUse[67];
        case 68:
            return this.data.inUse[68];
        case 69:
            return this.data.inUse[69];
        case 70:
            return this.data.inUse[70];
        case 71:
            return this.data.inUse[71];
        case 72:
            return this.data.inUse[72];
        case 73:
            return this.data.inUse[73];
        case 74:
            return this.data.inUse[74];
        case 75:
            return this.data.inUse[75];
        case 76:
            return this.data.inUse[76];
        case 77:
            return this.data.inUse[77];
        case 78:
            return this.data.inUse[78];
        case 79:
            return this.data.inUse[79];
        case 80:
            return this.data.inUse[80];
        case 81:
            return this.data.inUse[81];
        case 82:
            return this.data.inUse[82];
        case 83:
            return this.data.inUse[83];
        case 84:
            return this.data.inUse[84];
        case 85:
            return this.data.inUse[85];
        case 86:
            return this.data.inUse[86];
        case 87:
            return this.data.inUse[87];
        case 88:
            return this.data.inUse[88];
        case 89:
            return this.data.inUse[89];
        case 90:
            return this.data.inUse[90];
        case 91:
            return this.data.inUse[91];
        case 92:
            return this.data.inUse[92];
        case 93:
            return this.data.inUse[93];
        case 94:
            return this.data.inUse[94];
        case 95:
            return this.data.inUse[95];
        case 96:
            return this.data.inUse[96];
        case 97:
            return this.data.inUse[97];
        case 98:
            return this.data.inUse[98];
        case 99:
            return this.data.inUse[99];
        case 100:
            return this.data.inUse[100];
        case 101:
            return this.data.inUse[101];
        case 102:
            return this.data.inUse[102];
        case 103:
            return this.data.inUse[103];
        case 104:
            return this.data.inUse[104];
        case 105:
            return this.data.inUse[105];
        case 106:
            return this.data.inUse[106];
        case 107:
            return this.data.inUse[107];
        case 108:
            return this.data.inUse[108];
        case 109:
            return this.data.inUse[109];
        case 110:
            return this.data.inUse[110];
        case 111:
            return this.data.inUse[111];
        case 112:
            return this.data.inUse[112];
        case 113:
            return this.data.inUse[113];
        case 114:
            return this.data.inUse[114];
        case 115:
            return this.data.inUse[115];
        case 116:
            return this.data.inUse[116];
        case 117:
            return this.data.inUse[117];
        case 118:
            return this.data.inUse[118];
        case 119:
            return this.data.inUse[119];
        case 120:
            return this.data.inUse[120];
        case 121:
            return this.data.inUse[121];
        case 122:
            return this.data.inUse[122];
        case 123:
            return this.data.inUse[123];
        case 124:
            return this.data.inUse[124];
        case 125:
            return this.data.inUse[125];
        case 126:
            return this.data.inUse[126];
        case 127:
            return this.data.inUse[127];
        case 128:
            return this.data.inUse[128];
        case 129:
            return this.data.inUse[129];
        case 130:
            return this.data.inUse[130];
        case 131:
            return this.data.inUse[131];
        case 132:
            return this.data.inUse[132];
        case 133:
            return this.data.inUse[133];
        case 134:
            return this.data.inUse[134];
        case 135:
            return this.data.inUse[135];
        case 136:
            return this.data.inUse[136];
        case 137:
            return this.data.inUse[137];
        case 138:
            return this.data.inUse[138];
        case 139:
            return this.data.inUse[139];
        case 140:
            return this.data.inUse[140];
        case 141:
            return this.data.inUse[141];
        case 142:
            return this.data.inUse[142];
        case 143:
            return this.data.inUse[143];
        case 144:
            return this.data.inUse[144];
        case 145:
            return this.data.inUse[145];
        case 146:
            return this.data.inUse[146];
        case 147:
            return this.data.inUse[147];
        case 148:
            return this.data.inUse[148];
        case 149:
            return this.data.inUse[149];
        case 150:
            return this.data.inUse[150];
        case 151:
            return this.data.inUse[151];
        case 152:
            return this.data.inUse[152];
        case 153:
            return this.data.inUse[153];
        case 154:
            return this.data.inUse[154];
        case 155:
            return this.data.inUse[155];
        case 156:
            return this.data.inUse[156];
        case 157:
            return this.data.inUse[157];
        case 158:
            return this.data.inUse[158];
        case 159:
            return this.data.inUse[159];
        case 160:
            return this.data.inUse[160];
        case 161:
            return this.data.inUse[161];
        case 162:
            return this.data.inUse[162];
        case 163:
            return this.data.inUse[163];
        case 164:
            return this.data.inUse[164];
        case 165:
            return this.data.inUse[165];
        case 166:
            return this.data.inUse[166];
        case 167:
            return this.data.inUse[167];
        case 168:
            return this.data.inUse[168];
        case 169:
            return this.data.inUse[169];
        case 170:
            return this.data.inUse[170];
        case 171:
            return this.data.inUse[171];
        case 172:
            return this.data.inUse[172];
        case 173:
            return this.data.inUse[173];
        case 174:
            return this.data.inUse[174];
        case 175:
            return this.data.inUse[175];
        case 176:
            return this.data.inUse[176];
        case 177:
            return this.data.inUse[177];
        case 178:
            return this.data.inUse[178];
        case 179:
            return this.data.inUse[179];
        case 180:
            return this.data.inUse[180];
        case 181:
            return this.data.inUse[181];
        case 182:
            return this.data.inUse[182];
        case 183:
            return this.data.inUse[183];
        case 184:
            return this.data.inUse[184];
        case 185:
            return this.data.inUse[185];
        case 186:
            return this.data.inUse[186];
        case 187:
            return this.data.inUse[187];
        case 188:
            return this.data.inUse[188];
        case 189:
            return this.data.inUse[189];
        case 190:
            return this.data.inUse[190];
        case 191:
            return this.data.inUse[191];
        case 192:
            return this.data.inUse[192];
        case 193:
            return this.data.inUse[193];
        case 194:
            return this.data.inUse[194];
        case 195:
            return this.data.inUse[195];
        case 196:
            return this.data.inUse[196];
        case 197:
            return this.data.inUse[197];
        case 198:
            return this.data.inUse[198];
        case 199:
            return this.data.inUse[199];
        case 200:
            return this.data.inUse[200];
        case 201:
            return this.data.inUse[201];
        case 202:
            return this.data.inUse[202];
        case 203:
            return this.data.inUse[203];
        case 204:
            return this.data.inUse[204];
        case 205:
            return this.data.inUse[205];
        case 206:
            return this.data.inUse[206];
        case 207:
            return this.data.inUse[207];
        case 208:
            return this.data.inUse[208];
        case 209:
            return this.data.inUse[209];
        case 210:
            return this.data.inUse[210];
        case 211:
            return this.data.inUse[211];
        case 212:
            return this.data.inUse[212];
        case 213:
            return this.data.inUse[213];
        case 214:
            return this.data.inUse[214];
        case 215:
            return this.data.inUse[215];
        case 216:
            return this.data.inUse[216];
        case 217:
            return this.data.inUse[217];
        case 218:
            return this.data.inUse[218];
        case 219:
            return this.data.inUse[219];
        case 220:
            return this.data.inUse[220];
        case 221:
            return this.data.inUse[221];
        case 222:
            return this.data.inUse[222];
        case 223:
            return this.data.inUse[223];
        case 224:
            return this.data.inUse[224];
        case 225:
            return this.data.inUse[225];
        case 226:
            return this.data.inUse[226];
        case 227:
            return this.data.inUse[227];
        case 228:
            return this.data.inUse[228];
        case 229:
            return this.data.inUse[229];
        case 230:
            return this.data.inUse[230];
        case 231:
            return this.data.inUse[231];
        case 232:
            return this.data.inUse[232];
        case 233:
            return this.data.inUse[233];
        case 234:
            return this.data.inUse[234];
        case 235:
            return this.data.inUse[235];
        case 236:
            return this.data.inUse[236];
        case 237:
            return this.data.inUse[237];
        case 238:
            return this.data.inUse[238];
        case 239:
            return this.data.inUse[239];
        case 240:
            return this.data.inUse[240];
        case 241:
            return this.data.inUse[241];
        case 242:
            return this.data.inUse[242];
        case 243:
            return this.data.inUse[243];
        case 244:
            return this.data.inUse[244];
        case 245:
            return this.data.inUse[245];
        case 246:
            return this.data.inUse[246];
        case 247:
            return this.data.inUse[247];
        case 248:
            return this.data.inUse[248];
        case 249:
            return this.data.inUse[249];
        case 250:
            return this.data.inUse[250];
        case 251:
            return this.data.inUse[251];
        case 252:
            return this.data.inUse[252];
        case 253:
            return this.data.inUse[253];
        case 254:
            return this.data.inUse[254];
        case 255:
            return this.data.inUse[255];
        default:
            throw new RuntimeException("ArrayIndexOutOfBounds, should not happen!");
        }

    }

    private void setInUseFlag(int index) {
        switch (index) {
        case 0:
            this.data.inUse[0] = true;
            break;
        case 1:
            this.data.inUse[1] = true;
            break;
        case 2:
            this.data.inUse[2] = true;
            break;
        case 3:
            this.data.inUse[3] = true;
            break;
        case 4:
            this.data.inUse[4] = true;
            break;
        case 5:
            this.data.inUse[5] = true;
            break;
        case 6:
            this.data.inUse[6] = true;
            break;
        case 7:
            this.data.inUse[7] = true;
            break;
        case 8:
            this.data.inUse[8] = true;
            break;
        case 9:
            this.data.inUse[9] = true;
            break;
        case 10:
            this.data.inUse[10] = true;
            break;
        case 11:
            this.data.inUse[11] = true;
            break;
        case 12:
            this.data.inUse[12] = true;
            break;
        case 13:
            this.data.inUse[13] = true;
            break;
        case 14:
            this.data.inUse[14] = true;
            break;
        case 15:
            this.data.inUse[15] = true;
            break;
        case 16:
            this.data.inUse[16] = true;
            break;
        case 17:
            this.data.inUse[17] = true;
            break;
        case 18:
            this.data.inUse[18] = true;
            break;
        case 19:
            this.data.inUse[19] = true;
            break;
        case 20:
            this.data.inUse[20] = true;
            break;
        case 21:
            this.data.inUse[21] = true;
            break;
        case 22:
            this.data.inUse[22] = true;
            break;
        case 23:
            this.data.inUse[23] = true;
            break;
        case 24:
            this.data.inUse[24] = true;
            break;
        case 25:
            this.data.inUse[25] = true;
            break;
        case 26:
            this.data.inUse[26] = true;
            break;
        case 27:
            this.data.inUse[27] = true;
            break;
        case 28:
            this.data.inUse[28] = true;
            break;
        case 29:
            this.data.inUse[29] = true;
            break;
        case 30:
            this.data.inUse[30] = true;
            break;
        case 31:
            this.data.inUse[31] = true;
            break;
        case 32:
            this.data.inUse[32] = true;
            break;
        case 33:
            this.data.inUse[33] = true;
            break;
        case 34:
            this.data.inUse[34] = true;
            break;
        case 35:
            this.data.inUse[35] = true;
            break;
        case 36:
            this.data.inUse[36] = true;
            break;
        case 37:
            this.data.inUse[37] = true;
            break;
        case 38:
            this.data.inUse[38] = true;
            break;
        case 39:
            this.data.inUse[39] = true;
            break;
        case 40:
            this.data.inUse[40] = true;
            break;
        case 41:
            this.data.inUse[41] = true;
            break;
        case 42:
            this.data.inUse[42] = true;
            break;
        case 43:
            this.data.inUse[43] = true;
            break;
        case 44:
            this.data.inUse[44] = true;
            break;
        case 45:
            this.data.inUse[45] = true;
            break;
        case 46:
            this.data.inUse[46] = true;
            break;
        case 47:
            this.data.inUse[47] = true;
            break;
        case 48:
            this.data.inUse[48] = true;
            break;
        case 49:
            this.data.inUse[49] = true;
            break;
        case 50:
            this.data.inUse[50] = true;
            break;
        case 51:
            this.data.inUse[51] = true;
            break;
        case 52:
            this.data.inUse[52] = true;
            break;
        case 53:
            this.data.inUse[53] = true;
            break;
        case 54:
            this.data.inUse[54] = true;
            break;
        case 55:
            this.data.inUse[55] = true;
            break;
        case 56:
            this.data.inUse[56] = true;
            break;
        case 57:
            this.data.inUse[57] = true;
            break;
        case 58:
            this.data.inUse[58] = true;
            break;
        case 59:
            this.data.inUse[59] = true;
            break;
        case 60:
            this.data.inUse[60] = true;
            break;
        case 61:
            this.data.inUse[61] = true;
            break;
        case 62:
            this.data.inUse[62] = true;
            break;
        case 63:
            this.data.inUse[63] = true;
            break;
        case 64:
            this.data.inUse[64] = true;
            break;
        case 65:
            this.data.inUse[65] = true;
            break;
        case 66:
            this.data.inUse[66] = true;
            break;
        case 67:
            this.data.inUse[67] = true;
            break;
        case 68:
            this.data.inUse[68] = true;
            break;
        case 69:
            this.data.inUse[69] = true;
            break;
        case 70:
            this.data.inUse[70] = true;
            break;
        case 71:
            this.data.inUse[71] = true;
            break;
        case 72:
            this.data.inUse[72] = true;
            break;
        case 73:
            this.data.inUse[73] = true;
            break;
        case 74:
            this.data.inUse[74] = true;
            break;
        case 75:
            this.data.inUse[75] = true;
            break;
        case 76:
            this.data.inUse[76] = true;
            break;
        case 77:
            this.data.inUse[77] = true;
            break;
        case 78:
            this.data.inUse[78] = true;
            break;
        case 79:
            this.data.inUse[79] = true;
            break;
        case 80:
            this.data.inUse[80] = true;
            break;
        case 81:
            this.data.inUse[81] = true;
            break;
        case 82:
            this.data.inUse[82] = true;
            break;
        case 83:
            this.data.inUse[83] = true;
            break;
        case 84:
            this.data.inUse[84] = true;
            break;
        case 85:
            this.data.inUse[85] = true;
            break;
        case 86:
            this.data.inUse[86] = true;
            break;
        case 87:
            this.data.inUse[87] = true;
            break;
        case 88:
            this.data.inUse[88] = true;
            break;
        case 89:
            this.data.inUse[89] = true;
            break;
        case 90:
            this.data.inUse[90] = true;
            break;
        case 91:
            this.data.inUse[91] = true;
            break;
        case 92:
            this.data.inUse[92] = true;
            break;
        case 93:
            this.data.inUse[93] = true;
            break;
        case 94:
            this.data.inUse[94] = true;
            break;
        case 95:
            this.data.inUse[95] = true;
            break;
        case 96:
            this.data.inUse[96] = true;
            break;
        case 97:
            this.data.inUse[97] = true;
            break;
        case 98:
            this.data.inUse[98] = true;
            break;
        case 99:
            this.data.inUse[99] = true;
            break;
        case 100:
            this.data.inUse[100] = true;
            break;
        case 101:
            this.data.inUse[101] = true;
            break;
        case 102:
            this.data.inUse[102] = true;
            break;
        case 103:
            this.data.inUse[103] = true;
            break;
        case 104:
            this.data.inUse[104] = true;
            break;
        case 105:
            this.data.inUse[105] = true;
            break;
        case 106:
            this.data.inUse[106] = true;
            break;
        case 107:
            this.data.inUse[107] = true;
            break;
        case 108:
            this.data.inUse[108] = true;
            break;
        case 109:
            this.data.inUse[109] = true;
            break;
        case 110:
            this.data.inUse[110] = true;
            break;
        case 111:
            this.data.inUse[111] = true;
            break;
        case 112:
            this.data.inUse[112] = true;
            break;
        case 113:
            this.data.inUse[113] = true;
            break;
        case 114:
            this.data.inUse[114] = true;
            break;
        case 115:
            this.data.inUse[115] = true;
            break;
        case 116:
            this.data.inUse[116] = true;
            break;
        case 117:
            this.data.inUse[117] = true;
            break;
        case 118:
            this.data.inUse[118] = true;
            break;
        case 119:
            this.data.inUse[119] = true;
            break;
        case 120:
            this.data.inUse[120] = true;
            break;
        case 121:
            this.data.inUse[121] = true;
            break;
        case 122:
            this.data.inUse[122] = true;
            break;
        case 123:
            this.data.inUse[123] = true;
            break;
        case 124:
            this.data.inUse[124] = true;
            break;
        case 125:
            this.data.inUse[125] = true;
            break;
        case 126:
            this.data.inUse[126] = true;
            break;
        case 127:
            this.data.inUse[127] = true;
            break;
        case 128:
            this.data.inUse[128] = true;
            break;
        case 129:
            this.data.inUse[129] = true;
            break;
        case 130:
            this.data.inUse[130] = true;
            break;
        case 131:
            this.data.inUse[131] = true;
            break;
        case 132:
            this.data.inUse[132] = true;
            break;
        case 133:
            this.data.inUse[133] = true;
            break;
        case 134:
            this.data.inUse[134] = true;
            break;
        case 135:
            this.data.inUse[135] = true;
            break;
        case 136:
            this.data.inUse[136] = true;
            break;
        case 137:
            this.data.inUse[137] = true;
            break;
        case 138:
            this.data.inUse[138] = true;
            break;
        case 139:
            this.data.inUse[139] = true;
            break;
        case 140:
            this.data.inUse[140] = true;
            break;
        case 141:
            this.data.inUse[141] = true;
            break;
        case 142:
            this.data.inUse[142] = true;
            break;
        case 143:
            this.data.inUse[143] = true;
            break;
        case 144:
            this.data.inUse[144] = true;
            break;
        case 145:
            this.data.inUse[145] = true;
            break;
        case 146:
            this.data.inUse[146] = true;
            break;
        case 147:
            this.data.inUse[147] = true;
            break;
        case 148:
            this.data.inUse[148] = true;
            break;
        case 149:
            this.data.inUse[149] = true;
            break;
        case 150:
            this.data.inUse[150] = true;
            break;
        case 151:
            this.data.inUse[151] = true;
            break;
        case 152:
            this.data.inUse[152] = true;
            break;
        case 153:
            this.data.inUse[153] = true;
            break;
        case 154:
            this.data.inUse[154] = true;
            break;
        case 155:
            this.data.inUse[155] = true;
            break;
        case 156:
            this.data.inUse[156] = true;
            break;
        case 157:
            this.data.inUse[157] = true;
            break;
        case 158:
            this.data.inUse[158] = true;
            break;
        case 159:
            this.data.inUse[159] = true;
            break;
        case 160:
            this.data.inUse[160] = true;
            break;
        case 161:
            this.data.inUse[161] = true;
            break;
        case 162:
            this.data.inUse[162] = true;
            break;
        case 163:
            this.data.inUse[163] = true;
            break;
        case 164:
            this.data.inUse[164] = true;
            break;
        case 165:
            this.data.inUse[165] = true;
            break;
        case 166:
            this.data.inUse[166] = true;
            break;
        case 167:
            this.data.inUse[167] = true;
            break;
        case 168:
            this.data.inUse[168] = true;
            break;
        case 169:
            this.data.inUse[169] = true;
            break;
        case 170:
            this.data.inUse[170] = true;
            break;
        case 171:
            this.data.inUse[171] = true;
            break;
        case 172:
            this.data.inUse[172] = true;
            break;
        case 173:
            this.data.inUse[173] = true;
            break;
        case 174:
            this.data.inUse[174] = true;
            break;
        case 175:
            this.data.inUse[175] = true;
            break;
        case 176:
            this.data.inUse[176] = true;
            break;
        case 177:
            this.data.inUse[177] = true;
            break;
        case 178:
            this.data.inUse[178] = true;
            break;
        case 179:
            this.data.inUse[179] = true;
            break;
        case 180:
            this.data.inUse[180] = true;
            break;
        case 181:
            this.data.inUse[181] = true;
            break;
        case 182:
            this.data.inUse[182] = true;
            break;
        case 183:
            this.data.inUse[183] = true;
            break;
        case 184:
            this.data.inUse[184] = true;
            break;
        case 185:
            this.data.inUse[185] = true;
            break;
        case 186:
            this.data.inUse[186] = true;
            break;
        case 187:
            this.data.inUse[187] = true;
            break;
        case 188:
            this.data.inUse[188] = true;
            break;
        case 189:
            this.data.inUse[189] = true;
            break;
        case 190:
            this.data.inUse[190] = true;
            break;
        case 191:
            this.data.inUse[191] = true;
            break;
        case 192:
            this.data.inUse[192] = true;
            break;
        case 193:
            this.data.inUse[193] = true;
            break;
        case 194:
            this.data.inUse[194] = true;
            break;
        case 195:
            this.data.inUse[195] = true;
            break;
        case 196:
            this.data.inUse[196] = true;
            break;
        case 197:
            this.data.inUse[197] = true;
            break;
        case 198:
            this.data.inUse[198] = true;
            break;
        case 199:
            this.data.inUse[199] = true;
            break;
        case 200:
            this.data.inUse[200] = true;
            break;
        case 201:
            this.data.inUse[201] = true;
            break;
        case 202:
            this.data.inUse[202] = true;
            break;
        case 203:
            this.data.inUse[203] = true;
            break;
        case 204:
            this.data.inUse[204] = true;
            break;
        case 205:
            this.data.inUse[205] = true;
            break;
        case 206:
            this.data.inUse[206] = true;
            break;
        case 207:
            this.data.inUse[207] = true;
            break;
        case 208:
            this.data.inUse[208] = true;
            break;
        case 209:
            this.data.inUse[209] = true;
            break;
        case 210:
            this.data.inUse[210] = true;
            break;
        case 211:
            this.data.inUse[211] = true;
            break;
        case 212:
            this.data.inUse[212] = true;
            break;
        case 213:
            this.data.inUse[213] = true;
            break;
        case 214:
            this.data.inUse[214] = true;
            break;
        case 215:
            this.data.inUse[215] = true;
            break;
        case 216:
            this.data.inUse[216] = true;
            break;
        case 217:
            this.data.inUse[217] = true;
            break;
        case 218:
            this.data.inUse[218] = true;
            break;
        case 219:
            this.data.inUse[219] = true;
            break;
        case 220:
            this.data.inUse[220] = true;
            break;
        case 221:
            this.data.inUse[221] = true;
            break;
        case 222:
            this.data.inUse[222] = true;
            break;
        case 223:
            this.data.inUse[223] = true;
            break;
        case 224:
            this.data.inUse[224] = true;
            break;
        case 225:
            this.data.inUse[225] = true;
            break;
        case 226:
            this.data.inUse[226] = true;
            break;
        case 227:
            this.data.inUse[227] = true;
            break;
        case 228:
            this.data.inUse[228] = true;
            break;
        case 229:
            this.data.inUse[229] = true;
            break;
        case 230:
            this.data.inUse[230] = true;
            break;
        case 231:
            this.data.inUse[231] = true;
            break;
        case 232:
            this.data.inUse[232] = true;
            break;
        case 233:
            this.data.inUse[233] = true;
            break;
        case 234:
            this.data.inUse[234] = true;
            break;
        case 235:
            this.data.inUse[235] = true;
            break;
        case 236:
            this.data.inUse[236] = true;
            break;
        case 237:
            this.data.inUse[237] = true;
            break;
        case 238:
            this.data.inUse[238] = true;
            break;
        case 239:
            this.data.inUse[239] = true;
            break;
        case 240:
            this.data.inUse[240] = true;
            break;
        case 241:
            this.data.inUse[241] = true;
            break;
        case 242:
            this.data.inUse[242] = true;
            break;
        case 243:
            this.data.inUse[243] = true;
            break;
        case 244:
            this.data.inUse[244] = true;
            break;
        case 245:
            this.data.inUse[245] = true;
            break;
        case 246:
            this.data.inUse[246] = true;
            break;
        case 247:
            this.data.inUse[247] = true;
            break;
        case 248:
            this.data.inUse[248] = true;
            break;
        case 249:
            this.data.inUse[249] = true;
            break;
        case 250:
            this.data.inUse[250] = true;
            break;
        case 251:
            this.data.inUse[251] = true;
            break;
        case 252:
            this.data.inUse[252] = true;
            break;
        case 253:
            this.data.inUse[253] = true;
            break;
        case 254:
            this.data.inUse[254] = true;
            break;
        case 255:
            this.data.inUse[255] = true;
            break;
        default:
            throw new RuntimeException("ArrayIndexOutOfBounds, should not happen!");
        }
    }

    private static final class Data extends Object {

        // with blockSize 900k
        final boolean[] inUse = new boolean[256]; // 256 byte
        final byte[] unseqToSeq = new byte[256]; // 256 byte
        final int[] mtfFreq = new int[MAX_ALPHA_SIZE]; // 1032 byte
        final byte[] selector = new byte[MAX_SELECTORS]; // 18002 byte
        final byte[] selectorMtf = new byte[MAX_SELECTORS]; // 18002 byte

        final byte[] generateMTFValues_yy = new byte[256]; // 256 byte
        final byte[][] sendMTFValues_len = new byte[N_GROUPS][MAX_ALPHA_SIZE]; // 1548
        // byte
        final int[][] sendMTFValues_rfreq = new int[N_GROUPS][MAX_ALPHA_SIZE]; // 6192
        // byte
        final int[] sendMTFValues_fave = new int[N_GROUPS]; // 24 byte
        final short[] sendMTFValues_cost = new short[N_GROUPS]; // 12 byte
        final int[][] sendMTFValues_code = new int[N_GROUPS][MAX_ALPHA_SIZE]; // 6192
        // byte
        final byte[] sendMTFValues2_pos = new byte[N_GROUPS]; // 6 byte
        final boolean[] sentMTFValues4_inUse16 = new boolean[16]; // 16 byte

        final int[] stack_ll = new int[QSORT_STACK_SIZE]; // 4000 byte
        final int[] stack_hh = new int[QSORT_STACK_SIZE]; // 4000 byte
        final int[] stack_dd = new int[QSORT_STACK_SIZE]; // 4000 byte

        final int[] mainSort_runningOrder = new int[256]; // 1024 byte
        final int[] mainSort_copy = new int[256]; // 1024 byte
        final boolean[] mainSort_bigDone = new boolean[256]; // 256 byte

        final int[] heap = new int[MAX_ALPHA_SIZE + 2]; // 1040 byte
        final int[] weight = new int[MAX_ALPHA_SIZE * 2]; // 2064 byte
        final int[] parent = new int[MAX_ALPHA_SIZE * 2]; // 2064 byte

        final int[] ftab = new int[65537]; // 262148 byte
        // ------------
        // 333408 byte

        final byte[] block; // 900021 byte
        final int[] fmap; // 3600000 byte
        final char[] sfmap; // 3600000 byte
        // ------------
        // 8433529 byte
        // ============

        /**
         * Array instance identical to sfmap, both are used only temporarily and indepently, so we do not need to
         * allocate additional memory.
         */
        final char[] quadrant;

        Data(int blockSize100k) {
            super();

            final int n = blockSize100k * BZip2Constants.BASEBLOCKSIZE;
            this.block = new byte[(n + 1 + NUM_OVERSHOOT_BYTES)];
            this.fmap = new int[n];
            this.sfmap = new char[2 * n];
            this.quadrant = this.sfmap;
        }

    }

}
