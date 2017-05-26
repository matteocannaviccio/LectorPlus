package it.uniroma3.extractor.util;
/**
 * 
 * @author matteo
 *
 * @param <A>
 * @param <B>
 */
public final class Pair<A, B> {
    public final A key;
    public final B value;

    private final int hashcode;

    /**
     * 
     * @param a
     * @param b
     */
    public Pair(A a, B b) { 
	this.key = a; 
	this.value = b; 
	hashcode = (a != null ? a.hashCode() : 0) + 31 * (b != null ? b.hashCode() : 0);
    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static <A, B> Pair<A, B> make(A a, B b) { 
	return new Pair<A, B>(a, b); 
    }

    /**
     * 
     */
    public int hashCode() {
	return hashcode;
    }

    /**
     * 
     */
    public boolean equals(Object o) {
	if (o == null || o.getClass() != this.getClass()) { return false; }
	@SuppressWarnings("rawtypes")
	Pair that = (Pair) o;
	return (key == null ? that.key == null : key.equals(that.key))
		&& (value == null ? that.value == null : value.equals(that.value));
    }

    /**
     * 
     */
    public String toString(){
	return "("+key.toString()+" ; "+value.toString()+")";
    }

    /**
     * 
     * @return
     */
    public String toString4Map(){
	return key.toString()+" ; "+value.toString();
    }

}