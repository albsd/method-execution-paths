class SimpleRecursive {
    void recursive() {
        recursive();
    }
    void firstBase() {
        recursive();
        secondBase();
    }

    void secondBase() {
        recursive();
    }
}