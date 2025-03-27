class ExampleA {
    public static void a() {
        ExampleB.a();
    }
}

class ExampleB {
    public static void a() {
        ExampleC.a();
    }
}

class ExampleC {
    public static void a() {
        target();
    }

    public static void target() {

    }
}