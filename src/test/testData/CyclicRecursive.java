class CyclicRecursive {
    void recursiveA() {
        recursiveA();
        recursiveB();
        recursiveA();
    }
    void recursiveB() {
        recursiveA();
    }

    void base() {
        recursiveA();
        recursiveB();
    }
}