import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import gov.nasa.jpf.symbc.Debug;

import java.io.FileInputStream;
import java.io.IOException;

public class HashTable implements Map<String, String> {
    private Entry[] mTable;
    public int collisions;

    public HashTable(final int n) {
        this.mTable = new Entry[n];
        this.collisions = 0;
    }

    // -> YN: switch statement for array access with N=64

    private void setEntryInMTable(int index, Entry entry) {
        switch (index) {
        case 0:
            this.mTable[0] = entry;
            break;
        case 1:
            this.mTable[1] = entry;
            break;
        case 2:
            this.mTable[2] = entry;
            break;
        case 3:
            this.mTable[3] = entry;
            break;
        case 4:
            this.mTable[4] = entry;
            break;
        case 5:
            this.mTable[5] = entry;
            break;
        case 6:
            this.mTable[6] = entry;
            break;
        case 7:
            this.mTable[7] = entry;
            break;
        case 8:
            this.mTable[8] = entry;
            break;
        case 9:
            this.mTable[9] = entry;
            break;
        case 10:
            this.mTable[10] = entry;
            break;
        case 11:
            this.mTable[11] = entry;
            break;
        case 12:
            this.mTable[12] = entry;
            break;
        case 13:
            this.mTable[13] = entry;
            break;
        case 14:
            this.mTable[14] = entry;
            break;
        case 15:
            this.mTable[15] = entry;
            break;
        case 16:
            this.mTable[16] = entry;
            break;
        case 17:
            this.mTable[17] = entry;
            break;
        case 18:
            this.mTable[18] = entry;
            break;
        case 19:
            this.mTable[19] = entry;
            break;
        case 20:
            this.mTable[20] = entry;
            break;
        case 21:
            this.mTable[21] = entry;
            break;
        case 22:
            this.mTable[22] = entry;
            break;
        case 23:
            this.mTable[23] = entry;
            break;
        case 24:
            this.mTable[24] = entry;
            break;
        case 25:
            this.mTable[25] = entry;
            break;
        case 26:
            this.mTable[26] = entry;
            break;
        case 27:
            this.mTable[27] = entry;
            break;
        case 28:
            this.mTable[28] = entry;
            break;
        case 29:
            this.mTable[29] = entry;
            break;
        case 30:
            this.mTable[30] = entry;
            break;
        case 31:
            this.mTable[31] = entry;
            break;
        case 32:
            this.mTable[32] = entry;
            break;
        case 33:
            this.mTable[33] = entry;
            break;
        case 34:
            this.mTable[34] = entry;
            break;
        case 35:
            this.mTable[35] = entry;
            break;
        case 36:
            this.mTable[36] = entry;
            break;
        case 37:
            this.mTable[37] = entry;
            break;
        case 38:
            this.mTable[38] = entry;
            break;
        case 39:
            this.mTable[39] = entry;
            break;
        case 40:
            this.mTable[40] = entry;
            break;
        case 41:
            this.mTable[41] = entry;
            break;
        case 42:
            this.mTable[42] = entry;
            break;
        case 43:
            this.mTable[43] = entry;
            break;
        case 44:
            this.mTable[44] = entry;
            break;
        case 45:
            this.mTable[45] = entry;
            break;
        case 46:
            this.mTable[46] = entry;
            break;
        case 47:
            this.mTable[47] = entry;
            break;
        case 48:
            this.mTable[48] = entry;
            break;
        case 49:
            this.mTable[49] = entry;
            break;
        case 50:
            this.mTable[50] = entry;
            break;
        case 51:
            this.mTable[51] = entry;
            break;
        case 52:
            this.mTable[52] = entry;
            break;
        case 53:
            this.mTable[53] = entry;
            break;
        case 54:
            this.mTable[54] = entry;
            break;
        case 55:
            this.mTable[55] = entry;
            break;
        case 56:
            this.mTable[56] = entry;
            break;
        case 57:
            this.mTable[57] = entry;
            break;
        case 58:
            this.mTable[58] = entry;
            break;
        case 59:
            this.mTable[59] = entry;
            break;
        case 60:
            this.mTable[60] = entry;
            break;
        case 61:
            this.mTable[61] = entry;
            break;
        case 62:
            this.mTable[62] = entry;
            break;
        case 63:
            this.mTable[63] = entry;
            break;
        default:
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    private Entry getEntryFromMTable(int index) {
        Entry entry;
        switch (index) {
        case 0:
            entry = this.mTable[0];
            break;
        case 1:
            entry = this.mTable[1];
            break;
        case 2:
            entry = this.mTable[2];
            break;
        case 3:
            entry = this.mTable[3];
            break;
        case 4:
            entry = this.mTable[4];
            break;
        case 5:
            entry = this.mTable[5];
            break;
        case 6:
            entry = this.mTable[6];
            break;
        case 7:
            entry = this.mTable[7];
            break;
        case 8:
            entry = this.mTable[8];
            break;
        case 9:
            entry = this.mTable[9];
            break;
        case 10:
            entry = this.mTable[10];
            break;
        case 11:
            entry = this.mTable[11];
            break;
        case 12:
            entry = this.mTable[12];
            break;
        case 13:
            entry = this.mTable[13];
            break;
        case 14:
            entry = this.mTable[14];
            break;
        case 15:
            entry = this.mTable[15];
            break;
        case 16:
            entry = this.mTable[16];
            break;
        case 17:
            entry = this.mTable[17];
            break;
        case 18:
            entry = this.mTable[18];
            break;
        case 19:
            entry = this.mTable[19];
            break;
        case 20:
            entry = this.mTable[20];
            break;
        case 21:
            entry = this.mTable[21];
            break;
        case 22:
            entry = this.mTable[22];
            break;
        case 23:
            entry = this.mTable[23];
            break;
        case 24:
            entry = this.mTable[24];
            break;
        case 25:
            entry = this.mTable[25];
            break;
        case 26:
            entry = this.mTable[26];
            break;
        case 27:
            entry = this.mTable[27];
            break;
        case 28:
            entry = this.mTable[28];
            break;
        case 29:
            entry = this.mTable[29];
            break;
        case 30:
            entry = this.mTable[30];
            break;
        case 31:
            entry = this.mTable[31];
            break;
        case 32:
            entry = this.mTable[32];
            break;
        case 33:
            entry = this.mTable[33];
            break;
        case 34:
            entry = this.mTable[34];
            break;
        case 35:
            entry = this.mTable[35];
            break;
        case 36:
            entry = this.mTable[36];
            break;
        case 37:
            entry = this.mTable[37];
            break;
        case 38:
            entry = this.mTable[38];
            break;
        case 39:
            entry = this.mTable[39];
            break;
        case 40:
            entry = this.mTable[40];
            break;
        case 41:
            entry = this.mTable[41];
            break;
        case 42:
            entry = this.mTable[42];
            break;
        case 43:
            entry = this.mTable[43];
            break;
        case 44:
            entry = this.mTable[44];
            break;
        case 45:
            entry = this.mTable[45];
            break;
        case 46:
            entry = this.mTable[46];
            break;
        case 47:
            entry = this.mTable[47];
            break;
        case 48:
            entry = this.mTable[48];
            break;
        case 49:
            entry = this.mTable[49];
            break;
        case 50:
            entry = this.mTable[50];
            break;
        case 51:
            entry = this.mTable[51];
            break;
        case 52:
            entry = this.mTable[52];
            break;
        case 53:
            entry = this.mTable[53];
            break;
        case 54:
            entry = this.mTable[54];
            break;
        case 55:
            entry = this.mTable[55];
            break;
        case 56:
            entry = this.mTable[56];
            break;
        case 57:
            entry = this.mTable[57];
            break;
        case 58:
            entry = this.mTable[58];
            break;
        case 59:
            entry = this.mTable[59];
            break;
        case 60:
            entry = this.mTable[60];
            break;
        case 61:
            entry = this.mTable[61];
            break;
        case 62:
            entry = this.mTable[62];
            break;
        case 63:
            entry = this.mTable[63];
            break;
        default:
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return entry;
    }

    // <-

    // Hash function from SLOWFUZZ paper / vulnerable PHP hash table implementation
    private long DJBX33Ahash(String s) {
        long hash = 5381;

        for (int i = 0; i < s.length(); i++) {
            hash = ((hash << 5) + hash) + s.charAt(i);
        }

        return hash;
    }

    private int getBucket(final String s) {
        return (int) (DJBX33Ahash(s) % mTable.length);
    }

    private Entry findEntry(final Object o, final boolean b) {
        return this.findEntry(o, this.getBucket((String) o), b);
    }

    private Entry findEntry(final Object o, final int n, final boolean b) {
        int i = 0;
        for (Entry next = getEntryFromMTable(n); next != null; next = next.next) {
            if (next.key.equals(o)) {
                return next;
            }
            i++;
        }

        // count collisions
        if (i > 0)
            collisions++;

        if (b) {
            Entry e = new Entry((String) o, null, null, getEntryFromMTable(n));
            setEntryInMTable(n, e);
            return e;
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < this.mTable.length; ++i) {
            this.mTable[i] = null;
        }
    }

    public boolean containsKey(final Object o) {
        return this.findEntry(o, false) != null;
    }

    public boolean containsValue(final Object o) {
        for (int i = 0; i < this.mTable.length; ++i) {
            for (Entry next = this.mTable[i]; next != null; next = next.next) {
                if (next.value.equals(o)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return (Set<Map.Entry<String, String>>) new EntrySet();
    }

    public String get(final Object o) {
        final Entry entry = this.findEntry(o, false);
        if (entry == null) {
            return null;
        }
        return entry.value;
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.mTable.length; ++i) {
            if (this.mTable[i] != null) {
                return false;
            }
        }
        return true;
    }

    public Set<String> keySet() {
        return (Set<String>) new KeySet();
    }

    public String put(final String s, final String value) {
        final Entry entry = this.findEntry(s, true);
        final String value2 = entry.value;
        entry.value = value;
        return value2;
    }

    public void putAll(final Map<? extends String, ? extends String> map) {
        for (final Map.Entry entry : map.entrySet()) {
            this.put((String) entry.getKey(), (String) entry.getValue());
        }
    }

    private void remove(final int n, final Entry entry) {
        if (entry.prev == null) {
            this.mTable[n] = entry.next;
        } else {
            entry.prev.next = entry.next;
        }
        if (entry.next != null) {
            entry.next.prev = entry.prev;
        }
    }

    public String remove(final Object o) {
        final int bucket = this.getBucket((String) o);
        final Entry entry = this.findEntry(o, bucket, false);
        if (entry == null) {
            return null;
        }
        this.remove(bucket, entry);
        return entry.value;
    }

    public int size() {
        int n = 0;
        for (int i = 0; i < this.mTable.length; ++i) {
            for (Entry next = this.mTable[i]; next != null; next = next.next) {
                ++n;
            }
        }
        return n;
    }

    public Collection<String> values() {
        return (Collection<String>) new ValuesCollection();
    }

    // Concrete Driver
    /*
     * public static void main(String[] args) { if (args.length != 1) {
     * System.out.println("Expects file name as parameter"); return; }
     * 
     * int N = 64; int KEY_LEN = 8; String keys[] = new String[N]; HashTable table = new HashTable(N);
     * 
     * // read a maximum of N keys from file try (FileInputStream fis = new FileInputStream(args[0])) {
     * 
     * int b; for (int i = 0; i < N; i++) { char[] str = new char[KEY_LEN]; int j = 0; while (((b = fis.read()) != -1)
     * && j < KEY_LEN) { str[j] = (char) b; j++; } if (j == KEY_LEN) keys[i] = new String(str); }
     * 
     * } catch (IOException e) { System.err.println("Error reading input"); e.printStackTrace(); return; }
     * 
     * // add keys to hash table for (int i = 0; i < N; i++) { if (keys[i] != null) table.put(keys[i], "value" + i); }
     * 
     * System.out.println("Collisions: " + table.collisions); }
     */

    // Symbolic Driver
    public static void main(String[] args) {
        int N = 64;
        int KEY_LEN = 8; // length of string
        String keys[] = new String[N];
        HashTable table = new HashTable(N);

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            // read a maximum of N keys from file
            try (FileInputStream fis = new FileInputStream(fileName)) {

                int b;
                for (int i = 0; i < N; i++) {
                    char[] str = new char[KEY_LEN];
                    int j = 0;
                    while (j < KEY_LEN && ((b = fis.read()) != -1)) {
                        // read actual insert char + insert symbolic variable
                        str[j] = Debug.addSymbolicChar((char) b, "sym_" + i + "_" + j);
                        // str[j] = (char) b;
                        j++;
                    }
                    if (j == KEY_LEN) {
                        keys[i] = new String(str);
                    }
                }

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

            // add keys to hash table and .
            for (int i = 0; i < N; i++) {
                if (keys[i] != null)
                    table.put(keys[i], "value" + i);
            }

        } else {
            for (int i = 0; i < N; i++) {
                char[] str = new char[KEY_LEN];
                for (int j = 0; j < KEY_LEN; j++) {
                    str[j] = Debug.makeSymbolicChar("sym_" + i + "_" + j);
                }
                String key = new String(str);
                table.put(key, "value" + i);
            }
        }

        // System.out.println("Collisions: " + table.collisions);

    }

    public class Entry implements Map.Entry<String, String> {
        String key;
        String value;
        Entry prev;
        Entry next;

        Entry(final String key, final String value, final Entry prev, final Entry next) {
            this.key = key;
            this.value = value;
            this.prev = prev;
            if (this.prev != null) {
                this.prev.next = this;
            }
            this.next = next;
            if (this.next != null) {
                this.next.prev = this;
            }
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Entry)) {
                return false;
            }
            final Entry entry = (Entry) o;
            if (this.key == null) {
                if (entry.key != null) {
                    return false;
                }
            } else if (!this.key.equals((Object) entry.key)) {
                return false;
            }
            if ((this.value != null) ? this.value.equals((Object) entry.value) : (entry.value == null)) {
                return true;
            }
            return false;
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }

        public int hashCode() {
            return ((this.key == null) ? 0 : this.key.hashCode()) ^ ((this.value == null) ? 0 : this.value.hashCode());
        }

        public String setValue(final String value) {
            final String value2 = this.value;
            this.value = value;
            return value2;
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<String, String>> {
        private int mBucket;
        private Entry mEntry;

        EntryIterator() {
            this.mBucket = -1;
            this.mEntry = null;
        }

        public boolean hasNext() {
            if (this.mEntry != null && this.mEntry.next != null) {
                return true;
            }
            for (int i = this.mBucket + 1; i < HashTable.this.mTable.length; ++i) {
                if (HashTable.this.mTable[i] != null) {
                    return true;
                }
            }
            return false;
        }

        public Map.Entry<String, String> next() throws NoSuchElementException {
            if (this.mEntry != null && this.mEntry.next != null) {
                return (Map.Entry<String, String>) (this.mEntry = this.mEntry.next);
            }
            for (int i = this.mBucket + 1; i < HashTable.this.mTable.length; ++i) {
                if (HashTable.this.mTable[i] != null) {
                    this.mBucket = i;
                    return (Map.Entry<String, String>) (this.mEntry = HashTable.this.mTable[i]);
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            HashTable.this.remove(this.mBucket, this.mEntry);
        }
    }

    private class KeyIterator implements Iterator<String> {
        private EntryIterator mEntryIterator;

        KeyIterator() {
            this.mEntryIterator = new EntryIterator();
        }

        public boolean hasNext() {
            return this.mEntryIterator.hasNext();
        }

        public String next() throws NoSuchElementException {
            return (String) this.mEntryIterator.next().getKey();
        }

        public void remove() {
            this.mEntryIterator.remove();
        }
    }

    private class ValueIterator implements Iterator<String> {
        private EntryIterator mEntryIterator;

        ValueIterator() {
            this.mEntryIterator = new EntryIterator();
        }

        public boolean hasNext() {
            return this.mEntryIterator.hasNext();
        }

        public String next() throws NoSuchElementException {
            return (String) this.mEntryIterator.next().getValue();
        }

        public void remove() {
            this.mEntryIterator.remove();
        }
    }

    private class EntrySet implements Set<Map.Entry<String, String>> {
        public boolean add(final Map.Entry<String, String> entry) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends Map.Entry<String, String>> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            HashTable.this.clear();
        }

        public boolean contains(final Object o) {
            final Map.Entry entry = (Map.Entry) o;
            return entry.equals((Object) HashTable.this.findEntry(entry.getKey(), false));
        }

        public boolean containsAll(final Collection<?> collection) {
            final Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (!this.contains(iterator.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return HashTable.this.isEmpty();
        }

        public Iterator<Map.Entry<String, String>> iterator() {
            return (Iterator<Map.Entry<String, String>>) new EntryIterator();
        }

        public boolean remove(final Object o) {
            return HashTable.this.remove(((Map.Entry) o).getKey()) != null;
        }

        public boolean removeAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (this.remove(iterator.next())) {
                    b = true;
                }
            }
            return b;
        }

        public boolean retainAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator<Map.Entry<String, String>> iterator = this.iterator();
            try {
                while (true) {
                    if (!collection.contains(iterator.next())) {
                        iterator.remove();
                        b = true;
                    }
                }
            } catch (NoSuchElementException ex) {
                return b;
            }
        }

        public int size() {
            return HashTable.this.size();
        }

        public Object[] toArray() {
            final Object[] array = new Object[this.size()];
            int n = 0;
            final Iterator<Map.Entry<String, String>> iterator = this.iterator();
            while (iterator.hasNext()) {
                array[n] = iterator.next();
                ++n;
            }
            return array;
        }

        public <T> T[] toArray(T[] copy) {
            final int size = this.size();
            if (copy.length < size) {
                copy = (T[]) Arrays.copyOf((Object[]) copy, size);
            } else if (copy.length > size) {
                copy[size] = null;
            }
            int n = 0;
            final Iterator<Map.Entry<String, String>> iterator = this.iterator();
            while (iterator.hasNext()) {
                copy[n] = (T) iterator.next();
                ++n;
            }
            return copy;
        }
    }

    private class KeySet implements Set<String> {
        public boolean add(final String s) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends String> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            HashTable.this.clear();
        }

        public boolean contains(final Object o) {
            return HashTable.this.containsKey(o);
        }

        public boolean containsAll(final Collection<?> collection) {
            final Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (!this.contains(iterator.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return HashTable.this.isEmpty();
        }

        public Iterator<String> iterator() {
            return (Iterator<String>) new KeyIterator();
        }

        public boolean remove(final Object o) {
            return HashTable.this.remove(o) != null;
        }

        public boolean removeAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (this.remove(iterator.next())) {
                    b = true;
                }
            }
            return b;
        }

        public boolean retainAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator<String> iterator = this.iterator();
            try {
                while (true) {
                    if (!collection.contains(iterator.next())) {
                        iterator.remove();
                        b = true;
                    }
                }
            } catch (NoSuchElementException ex) {
                return b;
            }
        }

        public int size() {
            return HashTable.this.size();
        }

        public Object[] toArray() {
            final Object[] array = new Object[this.size()];
            int n = 0;
            final Iterator<String> iterator = this.iterator();
            while (iterator.hasNext()) {
                array[n] = iterator.next();
                ++n;
            }
            return array;
        }

        public <T> T[] toArray(T[] copy) {
            final int size = this.size();
            if (copy.length < size) {
                copy = (T[]) Arrays.copyOf((Object[]) copy, size);
            } else if (copy.length > size) {
                copy[size] = null;
            }
            int n = 0;
            final Iterator<String> iterator = this.iterator();
            while (iterator.hasNext()) {
                copy[n] = (T) iterator.next();
                ++n;
            }
            return copy;
        }
    }

    private class ValuesCollection implements Collection<String> {
        public boolean add(final String s) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends String> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            HashTable.this.clear();
        }

        public boolean contains(final Object o) {
            return HashTable.this.containsValue(o);
        }

        public boolean containsAll(final Collection<?> collection) {
            final Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (!this.contains(iterator.next())) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return HashTable.this.isEmpty();
        }

        public Iterator<String> iterator() {
            return (Iterator<String>) new ValueIterator();
        }

        public boolean remove(final Object o) {
            final Iterator<String> iterator = this.iterator();
            try {
                while (!((String) iterator.next()).equals(o)) {
                }
                iterator.remove();
                return true;
            } catch (NoSuchElementException ex) {
                return false;
            }
        }

        public boolean removeAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator<String> iterator = this.iterator();
            try {
                while (true) {
                    if (collection.contains(iterator.next())) {
                        iterator.remove();
                        b = true;
                    }
                }
            } catch (NoSuchElementException ex) {
                return b;
            }
        }

        public boolean retainAll(final Collection<?> collection) {
            boolean b = false;
            final Iterator<String> iterator = this.iterator();
            try {
                while (true) {
                    if (!collection.contains(iterator.next())) {
                        iterator.remove();
                        b = true;
                    }
                }
            } catch (NoSuchElementException ex) {
                return b;
            }
        }

        public int size() {
            return HashTable.this.size();
        }

        public Object[] toArray() {
            final Object[] array = new Object[this.size()];
            int n = 0;
            final Iterator<String> iterator = this.iterator();
            while (iterator.hasNext()) {
                array[n] = iterator.next();
                ++n;
            }
            return array;
        }

        public <T> T[] toArray(T[] copy) {
            final int size = this.size();
            if (copy.length < size) {
                copy = (T[]) Arrays.copyOf((Object[]) copy, size);
            } else if (copy.length > size) {
                copy[size] = null;
            }
            int n = 0;
            final Iterator<String> iterator = this.iterator();
            while (iterator.hasNext()) {
                copy[n] = (T) iterator.next();
                ++n;
            }
            return copy;
        }
    }

}
