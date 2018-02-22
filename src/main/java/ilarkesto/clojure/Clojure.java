/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.clojure;

import java.util.Arrays;

import clojure.lang.IFn;

public class Clojure {

	public static void main(String[] args) {
		System.out.println(eval("(+ 2 3)"));
		System.out.println(eval("(defn doit [] (println \"hello from clojure\") (+ 3 4))\n" + "(doit)"));

		IFn myFunction = evalExpectFn("(ns my.stuff) *ns* (defn html [input] (str input))");
		System.out.println(myFunction.invoke(Arrays.asList(1, 2, 3, 5, 8, 13)));
	}

	private static IFn evalExpectFn(String clojureCode) {
		return (IFn) eval(clojureCode);
	}

	public static final IFn loadString = clojure.java.api.Clojure.var("clojure.core", "load-string");

	public static Object read(String s) {
		return clojure.java.api.Clojure.read(s);
	}

	public static Object eval(String clojureCode) {
		return loadString.invoke(clojureCode);
	}

}
