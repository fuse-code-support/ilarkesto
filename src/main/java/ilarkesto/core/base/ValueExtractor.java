package ilarkesto.core.base;

public interface ValueExtractor<V, O> {

	V getValue(O o);

}