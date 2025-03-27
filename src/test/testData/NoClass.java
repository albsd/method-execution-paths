class NoClass{
    //
}

int a() {
   return 0;
}

int b() {
    return a() + 1;
}

int c() {
    return a() + b();
}

