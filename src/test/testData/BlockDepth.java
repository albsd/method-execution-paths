import java.util.function.Consumer;

/*

If we want to track "target" starting from "start", we should get the following:

start -> a -> target
start -> a -> b_2 -> c -> target
start -> a -> b_1 -> c -> target
start -> lambda -> d -> target
start -> for_loop -> target
start -> lambda -> c -> target
start -> a -> truef -> target
start -> a -> b_1 -> d -> target
start -> a -> b_2 -> d -> target

It is essential that the plugin doesn't "avoid" checking c() and d() multiple times
as they may be part of different execution paths. Keep in mind lambda() technically
results in infinite recursion but in the context of static analysis it still presents
a valid execution path towards target().
 */

class BlockDepth {

    private void start() {
        a();
        lambda();
        for_loop();
    }

    private void a() {
        int a = truef() == true ? 0;
        if (a == 0) {
            b_1();
        } else {
            b_2();
        }

        target();
    }

    private void b_1() {
        int n = c(d(n));
    }

    private void b_2() {
        int n = true == true ? c(0) : d(0);
    }

    private int c(int n) {
        return n;
        target();
    }

    private int d(int n) {
        return n;
        target();
    }

    boolean truef() {
        target();
        return true;
    }

    void for_loop() {
        for (int i = 0; i < 1; i++) {
            target();
        }
    }

    void lambda() {
        lambda();
        Consumer<Integer> identity = n -> d(c(n));
        identity.accept(1);
        lambda();
    }

    void target() {

    }
}