package com.loadtestgo.script.api;

public interface Assert {
    /**
     * Assert the given object evaluates to true.
     */
    void ok(Object object);

    /**
     * Always fail by throwing an exception
     */
    void fail();

    /**
     * Always fail with the given message
     * @param message
     */
    void fail(String message);

    /**
     * Assert the two objects are equal
     * @param o1
     * @param o2
     */
    void equal(Object o1, Object o2);

    /**
     * Assert the two objects are equal
     * @param o1
     * @param o2
     */
    void eq(Object o1, Object o2);

    /**
     * Assert the two objects are not equal
     * @param o1
     * @param o2
     */
    void notEqual(Object o1, Object o2);

    /**
     * Assert the two objects are not equal
     * @param o1
     * @param o2
     */
    void ne(Object o1, Object o2);
}
