package org.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.TimeUnit;

// Benchmark            Mode  Cnt    Score   Error   Units
// Runner.constructor  thrpt    3  149.822 ± 5.536  ops/us
// Runner.map          thrpt    3   28.372 ± 3.109  ops/us
// Runner.reflection   thrpt    3   86.319 ± 3.179  ops/us


@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class Runner {

    static KnobsRef<Config> ref = new KnobsRef<>(Config.class);

    @Benchmark
    public static void reflection(Blackhole bh) throws Throwable {
        bh.consume(ref.construct(new Knob[] {new BooleanKnob(false), new BooleanKnob(false)}));
    }

    @Benchmark
    public static void map(Blackhole bh) throws Throwable {
        bh.consume(Map.of("toggle1", new BooleanKnob(false), "toggle2", new BooleanKnob(false)));
    }

    @Benchmark
    public static void constructor(Blackhole bh) throws Throwable {
        bh.consume(new Config(new Knob[]{new BooleanKnob(true), new BooleanKnob(false)}));
    }

}